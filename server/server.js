/**
 * VAYU MCP Relay Server
 * =====================
 * Bridges Claude AI (SSE transport) to the VAYU Android Browser (WebSocket).
 *
 * Architecture:
 *   Claude AI ──SSE──▸ Render Server ◂──WebSocket── Android Browser
 *
 * Endpoints:
 *   GET  /sse       — SSE stream for Claude AI (MCP standard)
 *   POST /message   — HTTP message endpoint for Claude AI (MCP standard)
 *   GET  /relay     — WebSocket upgrade for Android browser relay
 *   GET  /health    — Render health check
 *   GET  /info      — Server info & connected browser status
 *   GET  /          — Landing page
 */

const express = require('express');
const http = require('http');
const { WebSocketServer, WebSocket } = require('ws');
const cors = require('cors');
const crypto = require('crypto');
const { v4: uuidv4 } = require('uuid');

// ─── Configuration ───────────────────────────────────────────────────────────

const PORT = parseInt(process.env.PORT || '10000', 10);
const PAIRING_CODE = process.env.PAIRING_CODE || 'vayu1234';
const API_KEY = process.env.API_KEY || '';              // Optional API key auth
const MAX_MESSAGE_SIZE = parseInt(process.env.MAX_MESSAGE_SIZE || '1048576', 10);  // 1 MB
const RELAY_TIMEOUT = parseInt(process.env.RELAY_TIMEOUT || '60000', 10);          // 60s
const HEARTBEAT_INTERVAL = parseInt(process.env.HEARTBEAT_INTERVAL || '30000', 10); // 30s
const LOG_LEVEL = process.env.LOG_LEVEL || 'info';      // debug | info | warn | error

// ─── Logging ─────────────────────────────────────────────────────────────────

const LEVELS = { debug: 0, info: 1, warn: 2, error: 3 };
function log(level, msg, meta = {}) {
    if (LEVELS[level] < LEVELS[LOG_LEVEL]) return;
    const ts = new Date().toISOString();
    const metaStr = Object.keys(meta).length ? ` ${JSON.stringify(meta)}` : '';
    console.log(`[${ts}] [${level.toUpperCase()}] ${msg}${metaStr}`);
}

// ─── State ───────────────────────────────────────────────────────────────────

/**
 * Connected Android browsers.
 * Map<browserId, { ws, pairingCode, connectedAt, lastHeartbeat, metadata }>
 */
const browsers = new Map();

/**
 * SSE clients (Claude AI).
 * Map<clientId, { res, channel, connectedAt }>
 */
const sseClients = new Map();

/**
 * Pending relay requests — waiting for Android browser to respond.
 * Map<requestId, { resolve, reject, timeout, clientId, browserId, timestamp }>
 */
const pendingRequests = new Map();

/**
 * Pairing code hash for auth verification.
 * Browser must send: SHA-256(pairingCode + challenge)
 */
function sha256Hex(input) {
    return crypto.createHash('sha256').update(input).digest('hex');
}

// ─── Express App ─────────────────────────────────────────────────────────────

const app = express();
app.use(cors());
app.use(express.json({ limit: MAX_MESSAGE_SIZE }));
app.use(express.raw({ type: 'application/octet-stream', limit: MAX_MESSAGE_SIZE }));

// ─── Routes ──────────────────────────────────────────────────────────────────

/**
 * Landing page — shows server info and connected browsers.
 */
app.get('/', (_req, res) => {
    const browserList = Array.from(browsers.entries()).map(([id, b]) => ({
        id,
        connectedAt: b.connectedAt,
        lastHeartbeat: b.lastHeartbeat,
        metadata: b.metadata || {},
    }));

    res.json({
        name: 'VAYU MCP Relay Server',
        version: '1.3.0',
        status: 'running',
        uptime: process.uptime(),
        browsers: browserList.length,
        sseClients: sseClients.size,
        pendingRequests: pendingRequests.size,
        endpoints: {
            sse: '/sse',
            message: '/message',
            relay: '/relay (WebSocket)',
            health: '/health',
            info: '/info',
        },
    });
});

/**
 * Health check — used by Render to detect if server is alive.
 */
app.get('/health', (_req, res) => {
    res.json({ status: 'ok', uptime: process.uptime() });
});

/**
 * Server info — detailed status.
 */
app.get('/info', (_req, res) => {
    const browserInfo = Array.from(browsers.entries()).map(([id, b]) => ({
        id,
        connectedAt: b.connectedAt,
        lastHeartbeat: b.lastHeartbeat,
        metadata: b.metadata || {},
    }));

    res.json({
        name: 'VAYU MCP Relay Server',
        version: '1.3.0',
        uptime: process.uptime(),
        pairingCodeRequired: true,
        browsers: browserInfo,
        sseClientCount: sseClients.size,
        pendingRequestCount: pendingRequests.size,
        config: {
            relayTimeout: RELAY_TIMEOUT,
            heartbeatInterval: HEARTBEAT_INTERVAL,
            maxMessageSize: MAX_MESSAGE_SIZE,
        },
    });
});

/**
 * SSE endpoint — Claude AI connects here.
 *
 * MCP SSE transport:
 *   1. Client GETs /sse — server sends "endpoint" event with message URL
 *   2. Client POSTs to /message — server processes and pushes response via SSE
 */
app.get('/sse', (req, res) => {
    // Optional API key check
    if (API_KEY && req.query.apiKey !== API_KEY && req.headers['authorization'] !== `Bearer ${API_KEY}`) {
        return res.status(401).json({ error: 'Unauthorized: invalid or missing API key' });
    }

    const clientId = uuidv4();
    log('info', `SSE client connected`, { clientId, ip: req.ip });

    // Set SSE headers
    res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache, no-transform',
        'Connection': 'keep-alive',
        'X-Accel-Buffering': 'no',  // Disable nginx buffering
        'Access-Control-Allow-Origin': '*',
    });

    // Send endpoint event (MCP protocol)
    const messageUrl = `/message?clientId=${clientId}`;
    res.write(`event: endpoint\ndata: ${messageUrl}\n\n`);

    // Store client
    const channel = {
        res,
        connectedAt: Date.now(),
    };
    sseClients.set(clientId, channel);

    // Heartbeat to keep connection alive
    const heartbeatTimer = setInterval(() => {
        try {
            res.write(':heartbeat\n\n');
        } catch (e) {
            clearInterval(heartbeatTimer);
        }
    }, HEARTBEAT_INTERVAL);

    // Cleanup on disconnect
    req.on('close', () => {
        clearInterval(heartbeatTimer);
        sseClients.delete(clientId);
        log('info', `SSE client disconnected`, { clientId });
    });
});

/**
 * HTTP message endpoint — Claude AI sends tool calls here.
 *
 * MCP SSE transport: Client POSTs JSON-RPC messages.
 * The server relays to the connected Android browser and responds via SSE.
 */
app.post('/message', async (req, res) => {
    const clientId = req.query.clientId;
    if (!clientId || !sseClients.has(clientId)) {
        return res.status(400).json({ error: 'Invalid or missing clientId' });
    }

    // Optional API key check
    if (API_KEY && req.query.apiKey !== API_KEY && req.headers['authorization'] !== `Bearer ${API_KEY}`) {
        return res.status(401).json({ error: 'Unauthorized' });
    }

    let rawBody;
    if (req.is('application/octet-stream')) {
        rawBody = req.body.toString('utf-8');
    } else {
        rawBody = JSON.stringify(req.body);
    }

    log('debug', `Message from SSE client`, { clientId, size: rawBody.length });

    // Select browser — first connected browser, or round-robin
    const browserId = selectBrowser();
    if (!browserId) {
        const errorResp = JSON.stringify({
            jsonrpc: '2.0',
            id: null,
            error: { code: -32000, message: 'No browser connected to relay' },
        });
        pushSseMessage(clientId, errorResp);
        return res.json({ status: 'queued', note: 'No browser connected, error sent via SSE' });
    }

    // Parse message to get id for correlation
    let messageId = null;
    try {
        const parsed = JSON.parse(rawBody);
        messageId = parsed.id || null;
    } catch (_) {}

    // Relay to browser via WebSocket and wait for response
    try {
        const response = await relayToBrowser(browserId, rawBody, clientId);
        pushSseMessage(clientId, response);
        res.json({ status: 'sent' });
    } catch (e) {
        const errorResp = JSON.stringify({
            jsonrpc: '2.0',
            id: messageId,
            error: { code: -32000, message: `Relay error: ${e.message}` },
        });
        pushSseMessage(clientId, errorResp);
        res.json({ status: 'error', message: e.message });
    }
});

/**
 * Push a message to an SSE client.
 */
function pushSseMessage(clientId, data) {
    const client = sseClients.get(clientId);
    if (!client) {
        log('warn', `SSE client not found for push`, { clientId });
        return;
    }
    try {
        client.res.write(`event: message\ndata: ${data}\n\n`);
        log('debug', `Pushed SSE message`, { clientId, size: data.length });
    } catch (e) {
        log('error', `Failed to push SSE message`, { clientId, error: e.message });
        sseClients.delete(clientId);
    }
}

/**
 * Select a connected browser for relay.
 * Simple strategy: first available browser.
 */
function selectBrowser() {
    for (const [id, browser] of browsers) {
        if (browser.ws && browser.ws.readyState === WebSocket.OPEN) {
            return id;
        }
    }
    return null;
}

/**
 * Relay a message to a connected browser and wait for the response.
 */
function relayToBrowser(browserId, message, clientId) {
    return new Promise((resolve, reject) => {
        const browser = browsers.get(browserId);
        if (!browser || browser.ws.readyState !== WebSocket.OPEN) {
            return reject(new Error('Browser disconnected'));
        }

        const requestId = uuidv4();
        const relayMsg = JSON.stringify({
            type: 'relay_request',
            requestId,
            clientId,
            payload: message,
            timestamp: Date.now(),
        });

        // Set timeout
        const timeout = setTimeout(() => {
            pendingRequests.delete(requestId);
            reject(new Error('Relay timeout — browser did not respond'));
        }, RELAY_TIMEOUT);

        pendingRequests.set(requestId, {
            resolve,
            reject,
            timeout,
            clientId,
            browserId,
            timestamp: Date.now(),
        });

        browser.ws.send(relayMsg);
        log('debug', `Relayed request to browser`, { requestId, browserId });
    });
}

// ─── HTTP Server & WebSocket ─────────────────────────────────────────────────

const server = http.createServer(app);

/**
 * WebSocket server for Android browser relay connections.
 * Path: /relay
 */
const wss = new WebSocketServer({ server, path: '/relay', maxPayload: MAX_MESSAGE_SIZE });

wss.on('connection', (ws, req) => {
    const browserId = uuidv4();
    const ip = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
    log('info', `Browser WebSocket connected`, { browserId, ip });

    let authenticated = false;
    let challenge = null;

    // Send auth challenge
    challenge = uuidv4();
    const authChallenge = JSON.stringify({
        type: 'auth_challenge',
        nonce: challenge,
    });
    ws.send(authChallenge);
    log('debug', `Sent auth challenge`, { browserId });

    // Message handler
    ws.on('message', (data) => {
        let parsed;
        try {
            parsed = JSON.parse(data.toString('utf-8'));
        } catch (e) {
            log('warn', `Invalid JSON from browser`, { browserId });
            ws.send(JSON.stringify({ type: 'error', message: 'Invalid JSON' }));
            return;
        }

        // ─── Auth flow ────────────────────────────────────────
        if (!authenticated) {
            if (parsed.type === 'auth_response' && parsed.hash) {
                // Verify: SHA-256(pairingCode + challenge)
                const expectedHash = sha256Hex(PAIRING_CODE + challenge);
                if (parsed.hash === expectedHash) {
                    authenticated = true;
                    browsers.set(browserId, {
                        ws,
                        connectedAt: Date.now(),
                        lastHeartbeat: Date.now(),
                        metadata: parsed.metadata || {},
                    });
                    ws.send(JSON.stringify({
                        type: 'auth_success',
                        browserId,
                        message: 'Authenticated with VAYU MCP Relay',
                    }));
                    log('info', `Browser authenticated`, { browserId, metadata: parsed.metadata });
                } else {
                    ws.send(JSON.stringify({
                        type: 'auth_failure',
                        error: 'Invalid pairing code',
                    }));
                    log('warn', `Browser auth failed — bad pairing code`, { browserId });
                    ws.close(4001, 'Auth failed');
                }
            } else {
                ws.send(JSON.stringify({
                    type: 'auth_failure',
                    error: 'Expected auth_response with hash',
                }));
                ws.close(4001, 'Auth expected');
            }
            return;
        }

        // ─── Authenticated messages ───────────────────────────

        // Heartbeat
        if (parsed.type === 'heartbeat' || parsed.type === 'ping') {
            browsers.get(browserId).lastHeartbeat = Date.now();
            ws.send(JSON.stringify({ type: 'heartbeat_ack', timestamp: Date.now() }));
            return;
        }

        // Relay response — browser responding to a pending request
        if (parsed.type === 'relay_response' && parsed.requestId) {
            const pending = pendingRequests.get(parsed.requestId);
            if (pending) {
                clearTimeout(pending.timeout);
                pendingRequests.delete(parsed.requestId);
                pending.resolve(parsed.payload || '{}');
                log('debug', `Browser relay response received`, { requestId: parsed.requestId, browserId });
            } else {
                log('warn', `Unknown relay response`, { requestId: parsed.requestId, browserId });
            }
            return;
        }

        // Tool list request — browser can send its tool list
        if (parsed.type === 'tool_list') {
            browsers.get(browserId).toolList = parsed.tools || [];
            log('info', `Browser sent tool list`, { browserId, toolCount: (parsed.tools || []).length });
            return;
        }

        // Status update
        if (parsed.type === 'status') {
            browsers.get(browserId).metadata = { ...browsers.get(browserId).metadata, ...parsed };
            log('debug', `Browser status update`, { browserId });
            return;
        }

        // Direct SSE push — browser pushes a notification to all SSE clients
        if (parsed.type === 'sse_broadcast') {
            const data = parsed.data || '{}';
            for (const [clientId, client] of sseClients) {
                pushSseMessage(clientId, data);
            }
            log('debug', `Browser broadcast to SSE clients`, { browserId, sseClientCount: sseClients.size });
            return;
        }

        // Unknown type
        log('warn', `Unknown message type from browser`, { browserId, type: parsed.type });
    });

    // ─── Disconnect ────────────────────────────────────────────────
    ws.on('close', (code, reason) => {
        browsers.delete(browserId);
        // Clean up any pending requests from this browser
        for (const [requestId, pending] of pendingRequests) {
            if (pending.browserId === browserId) {
                clearTimeout(pending.timeout);
                pendingRequests.delete(requestId);
                pending.reject(new Error('Browser disconnected'));
            }
        }
        log('info', `Browser disconnected`, { browserId, code });
    });

    ws.on('error', (err) => {
        log('error', `Browser WebSocket error`, { browserId, error: err.message });
        browsers.delete(browserId);
    });

    // ─── Heartbeat check ──────────────────────────────────────────
    const heartbeatCheck = setInterval(() => {
        const browser = browsers.get(browserId);
        if (!browser) {
            clearInterval(heartbeatCheck);
            return;
        }
        const elapsed = Date.now() - browser.lastHeartbeat;
        if (elapsed > HEARTBEAT_INTERVAL * 3) {
            log('warn', `Browser heartbeat timeout`, { browserId, elapsedMs: elapsed });
            ws.close(4002, 'Heartbeat timeout');
            browsers.delete(browserId);
            clearInterval(heartbeatCheck);
        }
    }, HEARTBEAT_INTERVAL);
});

// ─── Periodic cleanup ────────────────────────────────────────────────────────

setInterval(() => {
    // Clean stale pending requests
    const now = Date.now();
    for (const [requestId, pending] of pendingRequests) {
        if (now - pending.timestamp > RELAY_TIMEOUT * 2) {
            clearTimeout(pending.timeout);
            pendingRequests.delete(requestId);
            pending.reject(new Error('Request expired'));
            log('warn', `Cleaned stale pending request`, { requestId });
        }
    }
}, 60000);

// ─── Start ───────────────────────────────────────────────────────────────────

server.listen(PORT, '0.0.0.0', () => {
    log('info', `VAYU MCP Relay Server started`, {
        port: PORT,
        pairingCodeRequired: true,
        apiKeyRequired: !!API_KEY,
        relayTimeout: RELAY_TIMEOUT,
        heartbeatInterval: HEARTBEAT_INTERVAL,
    });
    log('info', `Endpoints:`, {
        sse: `http://0.0.0.0:${PORT}/sse`,
        message: `http://0.0.0.0:${PORT}/message`,
        relay: `ws://0.0.0.0:${PORT}/relay`,
        health: `http://0.0.0.0:${PORT}/health`,
    });
});

// ─── Graceful shutdown ───────────────────────────────────────────────────────

process.on('SIGTERM', () => {
    log('info', 'SIGTERM received, shutting down gracefully...');
    // Close all browser connections
    for (const [id, browser] of browsers) {
        browser.ws.close(1001, 'Server shutting down');
    }
    // Close all SSE clients
    for (const [id, client] of sseClients) {
        client.res.end();
    }
    server.close(() => {
        log('info', 'Server closed');
        process.exit(0);
    });
    setTimeout(() => process.exit(1), 10000);
});

process.on('SIGINT', () => {
    process.emit('SIGTERM');
});
