package rip.alpha.core.voter;

import lombok.Getter;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.economy.EconomyManager;
import rip.alpha.core.shared.economy.TokenType;
import rip.alpha.core.voter.data.Vote;
import rip.alpha.core.voter.net.VotifierBootstrap;
import rip.alpha.core.voter.net.VotifierSession;
import rip.alpha.core.voter.utils.RSAIO;
import rip.alpha.core.voter.utils.RSAKeygen;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.logging.AlphaLogger;
import rip.alpha.libraries.logging.AlphaLoggerFactory;
import rip.alpha.libraries.logging.LogLevel;
import rip.alpha.libraries.util.data.NameCache;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Moose1301
 * @date 4/18/2022
 */
public class VotifierCore {
    public static final AlphaLogger LOGGER = AlphaLoggerFactory.createLogger("Votifier-Core");
    @Getter private static VotifierCore instance;
    @Getter private long TIMEOUT = TimeUnit.HOURS.toMillis(28);
    @Getter private VotifierBootstrap bootstrap;
    @Getter private KeyPair keyPair;

    private Map<String, Key> tokens = new HashMap<>();

    public VotifierCore(int port) {
        instance = this;
        Libraries.getInstance().enable();
        LogLevel logLevel = CoreConfig.getInstance().getLogLevel();
        AlphaLoggerFactory.setCurrentLogLevel(logLevel);
        LOGGER.log("LogLevel is set to %s".formatted(logLevel), LogLevel.BASIC);

        for (Map.Entry<String, String> entry : CoreConfig.getInstance().getTokens().entrySet()) {
            tokens.put(entry.getKey(), RSAKeygen.createKeyFrom(entry.getValue()));
        }
        File rsaDirectory = new File("config", "rsa");
        try {
            if (!rsaDirectory.exists()) {
                if (!rsaDirectory.mkdir()) {
                    throw new RuntimeException("Unable to create the RSA key folder " + rsaDirectory);
                }
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.log("Error reading configuration file or RSA tokens", LogLevel.BASIC);
            return;
        }

        this.bootstrap = new VotifierBootstrap("0.0.0.0", port);
        this.bootstrap.start();
    }


    public Key getToken(String serviceName) {
        return tokens.getOrDefault(serviceName, null);
    }

    public void onVoteReceived(Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) throws Exception {
        AlphaProfileManager.profiles().applyToData(NameCache.getInstance().getID(vote.getUsername()), new Consumer<AlphaProfile>() {
            @Override
            public void accept(AlphaProfile profile) {
                if(System.currentTimeMillis() > profile.getVoteTimeOut()) {
                    profile.setVotesToday(0);
                }
                profile.setVotesToday(profile.getVotesToday() + 1);
                profile.setVoteTimeOut(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24));
                if(profile.getVotesToday() == tokens.size() - 1) {
                    if(System.currentTimeMillis() > profile.getVoteStreakTimeOut()) {
                        profile.setVoteStreak(1);
                    } else {
                        profile.setVoteStreak(profile.getVoteStreak() + 1);
                    }
                    profile.setVoteStreakTimeOut(System.currentTimeMillis() + TIMEOUT);
                    int toGive = CoreConfig.getInstance().getStartingCoins() + (profile.getVoteStreak() * 100);
                    toGive = Math.max(toGive, CoreConfig.getInstance().getMaxCoins());
                    EconomyManager.addToFunds("Voting", profile.getMojangID(), TokenType.EARNED, toGive);
                }


            }
        });
    }

    public void onError(Throwable throwable, boolean alreadyHandledVote, String remoteAddress) {
        if (alreadyHandledVote) {
            throwable.printStackTrace();
            LOGGER.log("Vote processed, however an exception " +
                    "occurred with a vote from " + remoteAddress, LogLevel.DEBUG);
        } else {
            throwable.printStackTrace();
            LOGGER.log("Unable to process vote from " + remoteAddress, LogLevel.DEBUG);
        }
    }
}
