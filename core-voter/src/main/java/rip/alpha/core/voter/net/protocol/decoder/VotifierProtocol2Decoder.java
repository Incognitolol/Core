package rip.alpha.core.voter.net.protocol.decoder;

import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import rip.alpha.core.voter.VotifierCore;
import rip.alpha.core.voter.data.Vote;
import rip.alpha.core.voter.net.VotifierBootstrap;
import rip.alpha.core.voter.net.VotifierSession;
import rip.alpha.libraries.json.GsonProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Decodes protocol 2 JSON votes.
 */
public class VotifierProtocol2Decoder extends MessageToMessageDecoder<String> {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void decode(ChannelHandlerContext ctx, String s, List<Object> list) throws Exception {
        JsonObject voteMessage = GsonProvider.fromJson(s, JsonObject.class);
        VotifierSession session = ctx.channel().attr(VotifierSession.KEY).get();

        // Deserialize the payload.
        String payload = voteMessage.get("payload").getAsString();
        JsonObject votePayload = GsonProvider.fromJson(payload, JsonObject.class);

        // Verify challenge.
        if (!votePayload.get("challenge").getAsString().equals(session.getChallenge())) {
            throw new CorruptedFrameException("Challenge is not valid");
        }

        // Verify that we have keys available.
        VotifierCore votifierCore = ctx.channel().attr(VotifierBootstrap.KEY).get();
        Key key = votifierCore.getToken(votePayload.get("serviceName").getAsString());

        if (key == null) {
            key = votifierCore.getToken("default");
            if (key == null) {
                throw new RuntimeException("Unknown service '" + votePayload.get("serviceName").getAsString() + "'");
            }
        }

        // Verify signature.
        String sigHash = voteMessage.get("signature").getAsString();
        byte[] sigBytes = Base64.getDecoder().decode(sigHash);

        if (!hmacEqual(sigBytes, payload.getBytes(StandardCharsets.UTF_8), key)) {
            throw new CorruptedFrameException("Signature is not valid (invalid token?)");
        }

        // Stopgap: verify the "uuid" field is valid, if provided.
        if (votePayload.has("uuid")) {
            UUID.fromString(votePayload.get("uuid").getAsString());
        }

        if (votePayload.get("username").getAsString().length() > 16) {
            throw new CorruptedFrameException("Username too long");
        }

        // Create the vote.
        Vote vote = new Vote(votePayload);
        list.add(vote);

        ctx.pipeline().remove(this);
    }

    private boolean hmacEqual(byte[] sig, byte[] message, Key key) throws NoSuchAlgorithmException, InvalidKeyException {
        // See https://www.nccgroup.trust/us/about-us/newsroom-and-events/blog/2011/february/double-hmac-verification/
        // This randomizes the byte order to make timing attacks more difficult.
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] calculatedSig = mac.doFinal(message);

        // Generate a random key for use in comparison
        byte[] randomKey = new byte[32];
        RANDOM.nextBytes(randomKey);

        // Then generate two HMACs for the different signatures found
        Mac mac2 = Mac.getInstance("HmacSHA256");
        mac2.init(new SecretKeySpec(randomKey, "HmacSHA256"));
        byte[] clientSig = mac2.doFinal(sig);
        mac2.reset();
        byte[] realSig = mac2.doFinal(calculatedSig);

        return MessageDigest.isEqual(clientSig, realSig);
    }
}