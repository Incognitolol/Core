package rip.alpha.core.voter.net.protocol.decoder;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import rip.alpha.core.voter.VotifierCore;
import rip.alpha.core.voter.data.Vote;
import rip.alpha.core.voter.net.VotifierBootstrap;
import rip.alpha.core.voter.utils.QuietException;
import rip.alpha.core.voter.utils.RSA;
import rip.alpha.libraries.logging.AlphaLoggerFactory;
import rip.alpha.libraries.logging.LogLevel;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes original protocol votes.
 */
public class VotifierProtocol1Decoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (!ctx.channel().isActive()) {
            buf.skipBytes(buf.readableBytes());
            return;
        }

        if (buf.readableBytes() < 256) {
            // The client might have not sent all the data yet, so don't eject the connection.
            return;
        }

        if (buf.readableBytes() > 256) {
            // They sent too much data.
            throw new QuietException("Could not decrypt data from " + ctx.channel().remoteAddress() + " as it is too long. Attack?");
        }

        byte[] block = ByteBufUtil.getBytes(buf);
        buf.skipBytes(buf.readableBytes());

        VotifierCore plugin = ctx.channel().attr(VotifierBootstrap.KEY).get();

        try {
            block = RSA.decrypt(block, plugin.getKeyPair().getPrivate());
        } catch (Exception e) {
            if (AlphaLoggerFactory.getCurrentLogLevel() == LogLevel.VERBOSE) {
                throw new CorruptedFrameException("Could not decrypt data from " + ctx.channel().remoteAddress() + ". Make sure the public key on the list is correct.", e);
            } else {
                throw new QuietException("Could not decrypt data from " + ctx.channel().remoteAddress() + ". Make sure the public key on the list is correct.");
            }
        }

        // Parse the string we received.
        String all = new String(block, StandardCharsets.US_ASCII);
        String[] split = all.split("\n");
        if (split.length < 5) {
            throw new QuietException("Not enough fields specified in vote. This is not a Core Votifier issue. Got " + split.length + " fields, but needed 5.");
        }

        if (!split[0].equals("VOTE")) {
            throw new QuietException("The VOTE opcode was not present. This is not a Core Votifier issue, but a bug with the server list.");
        }

        // Create the vote.
        Vote vote = new Vote(split[1], split[2], split[3], split[4]);
        list.add(vote);

        // We are done, remove ourselves. Why? Sometimes, we will decode multiple vote messages.
        // Netty doesn't like this, so we must remove ourselves from the pipeline. With Protocol 1,
        // ending votes is a "fire and forget" operation, so this is safe.
        ctx.pipeline().remove(this);
    }
}