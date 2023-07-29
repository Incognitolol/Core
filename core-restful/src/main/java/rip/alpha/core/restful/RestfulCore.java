package rip.alpha.core.restful;

import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import rip.alpha.core.shared.AlphaCore;
import rip.alpha.core.shared.data.AlphaProfile;
import rip.alpha.core.shared.data.AlphaProfileManager;
import rip.alpha.core.shared.ranks.Rank;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.json.GsonProvider;
import rip.alpha.libraries.util.data.NameCache;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RestfulCore {

    private Javalin javalin;
    private final String apiKey;
    private final JsonObject noFoundObject, noProfileFound, notAuthenticated;

    public RestfulCore(String apiKey) {
        this.apiKey = apiKey;
        this.noFoundObject = new JsonObject();
        this.noFoundObject.addProperty("error", "That username was not found");
        this.noProfileFound = new JsonObject();
        this.noProfileFound.addProperty("error", "That username had no profile");
        this.notAuthenticated = new JsonObject();
        this.notAuthenticated.addProperty("error", "You are not authenticated");
        Libraries.getInstance().enable();
        AlphaCore.enable();
        this.hook();
        this.shutdownHook();
    }

    private void hook() {
        this.javalin = Javalin.create().start(7071);
        this.javalin.get("/player/{playerName}", context -> {
            if (!this.isAuthenticated(context)) {
                context.status(401);
                context.result(this.notAuthenticated.toString());
                return;
            }

            UUID playerId = NameCache.getInstance().getIDAsync(context.pathParam("playerName")).get(5, TimeUnit.SECONDS);

            if (playerId == null) {
                context.status(404);
                context.result(this.noFoundObject.toString());
                return;
            }

            if (!AlphaProfileManager.profiles().exists(playerId)) {
                context.status(404);
                context.result(this.noProfileFound.toString());
                return;
            }

            AlphaProfile profile = AlphaProfileManager.profiles().getOrCreateRealTimeData(playerId);
            JsonObject jsonObject = GsonProvider.toJsonTree(profile).getAsJsonObject();
            jsonObject.addProperty("highestRank", profile.getHighestRank().name());
            context.result(GsonProvider.toJsonPretty(jsonObject));
            context.status(200);
        });

        this.javalin.get("/rankMembers", context -> {
            if (!this.isAuthenticated(context)) {
                context.status(401);
                context.result(this.notAuthenticated.toString());
                return;
            }

            Map<Rank, Set<AlphaProfile>> profiles = new LinkedHashMap<>();
            for (Rank value : Rank.values()) {
                profiles.put(value, new HashSet<>());
            }
            AlphaProfileManager.profiles().fetchAllKeys().forEach(uuid -> {
                AlphaProfile profile = AlphaProfileManager.profiles().getOrCreateRealTimeData(uuid);
                profiles.computeIfAbsent(profile.getHighestRank(), rank -> new HashSet<>()).add(profile);
            });
            context.result(GsonProvider.toJsonPretty(profiles));
            context.status(200);
        });

        this.javalin.get("/rankData", context -> {
            if (!this.isAuthenticated(context)) {
                context.status(401);
                context.result(this.notAuthenticated.toString());
                return;
            }

            Map<Rank, JsonObject> ranks = new LinkedHashMap<>();
            for (Rank value : Rank.values()) {
                JsonObject object = new JsonObject();
                object.addProperty("priority", value.getPriority());
                object.addProperty("prettyName", value.getName());
                object.addProperty("mcColor", value.getColor());
                object.add("rgbColor", GsonProvider.toJsonTree(value.getJavaColor()));
                ranks.put(value, object);
            }
            context.result(GsonProvider.toJsonPretty(ranks));
            context.status(200);
        });
    }

    private boolean isAuthenticated(Context context) {
        String apiKey = context.header("X-AlphaRPoint-Key");

        if (apiKey == null) {
            return false;
        }

        return this.apiKey.equals(apiKey);
    }

    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.javalin.stop();
                AlphaCore.disable();
                Libraries.getInstance().disable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
