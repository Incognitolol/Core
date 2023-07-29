package rip.alpha.core.voter.net;

import io.netty.util.AttributeKey;
import rip.alpha.core.voter.utils.RSAKeygen;


public class VotifierSession {
    public static final AttributeKey<VotifierSession> KEY = AttributeKey.valueOf("votifier_session");
    private ProtocolVersion version = ProtocolVersion.UNKNOWN;
    private final String challenge;
    private boolean hasCompletedVote = false;

    public VotifierSession() {
        challenge = RSAKeygen.newToken();
    }

    public void setVersion(ProtocolVersion version) {
        if (this.version != ProtocolVersion.UNKNOWN)
            throw new IllegalStateException("Protocol version already switched");

        this.version = version;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public String getChallenge() {
        return challenge;
    }

    public void completeVote() {
        if (hasCompletedVote)
            throw new IllegalStateException("Protocol completed vote twice!");

        hasCompletedVote = true;
    }

    public boolean hasCompletedVote() {
        return hasCompletedVote;
    }

    public enum ProtocolVersion {
        UNKNOWN("unknown"),
        ONE("protocol v1"),
        TWO("protocol v2"),
        TEST("test");

        public final String humanReadable;
        ProtocolVersion(String hr) {
            this.humanReadable = hr;
        }
    }
}