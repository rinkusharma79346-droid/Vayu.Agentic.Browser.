# VAYU MCP Relay Server

The relay server bridges **Claude AI** (SSE transport) to the **VAYU Android Browser** (WebSocket), enabling AI agents to control the browser remotely.

## Architecture

```
Claude AI ────SSE────▸ Render Server ◂────WebSocket──── Android Browser
                        (this server)
```

1. Claude AI connects to `/sse` and sends tool calls via `/message`
2. The relay server forwards tool calls to the connected Android browser via WebSocket
3. The Android browser executes tools locally and sends responses back
4. The relay server pushes responses to Claude AI via SSE

## Deploy on Render

1. Connect your GitHub repo to Render
2. Set these settings:
   - **Root Directory**: `server`
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
3. Add environment variables (see `.env.example`)
4. Deploy!

## Local Development

```bash
cd server
npm install
npm start
```

Server runs at `http://localhost:10000`

## Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sse` | GET | SSE stream for Claude AI (MCP standard) |
| `/message` | POST | HTTP message endpoint for Claude AI |
| `/relay` | WebSocket | Android browser relay connection |
| `/health` | GET | Render health check |
| `/info` | GET | Server status & connected browsers |
| `/` | GET | Landing page |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `10000` | Server port (Render sets automatically) |
| `PAIRING_CODE` | `vayu1234` | Browser auth pairing code |
| `API_KEY` | (empty) | Optional API key for SSE auth |
| `RELAY_TIMEOUT` | `60000` | Timeout for browser responses (ms) |
| `HEARTBEAT_INTERVAL` | `30000` | WebSocket heartbeat interval (ms) |
| `LOG_LEVEL` | `info` | Logging level |

## Browser Authentication

The Android browser authenticates via SHA-256 challenge-response:

1. Server sends `auth_challenge` with a random nonce
2. Browser computes `SHA-256(pairingCode + nonce)` and sends `auth_response` with the hash
3. Server verifies and grants access

The pairing code must match `McpConfig.PAIRING_CODE` in the Android app.

## Connecting Claude AI

In Claude Desktop config:

```json
{
  "mcpServers": {
    "vayu-browser": {
      "url": "https://your-render-app.onrender.com/sse"
    }
  }
}
```
