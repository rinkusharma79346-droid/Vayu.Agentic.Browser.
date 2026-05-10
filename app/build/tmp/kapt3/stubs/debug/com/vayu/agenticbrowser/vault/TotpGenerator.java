package com.vayu.agenticbrowser.vault;

import com.vayu.agenticbrowser.common.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0005\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0006H\u0002J\u000e\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\u0006J\u0006\u0010\r\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/vayu/agenticbrowser/vault/TotpGenerator;", "", "()V", "CODE_DIGITS", "", "HMAC_ALGORITHM", "", "TIME_STEP_SECONDS", "decodeBase32", "", "base32", "generate", "base32Seed", "getSecondsRemaining", "app_debug"})
public final class TotpGenerator {
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String HMAC_ALGORITHM = "HmacSHA1";
    @org.jetbrains.annotations.NotNull()
    public static final com.vayu.agenticbrowser.vault.TotpGenerator INSTANCE = null;
    
    private TotpGenerator() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generate(@org.jetbrains.annotations.NotNull()
    java.lang.String base32Seed) {
        return null;
    }
    
    public final int getSecondsRemaining() {
        return 0;
    }
    
    private final byte[] decodeBase32(java.lang.String base32) {
        return null;
    }
}