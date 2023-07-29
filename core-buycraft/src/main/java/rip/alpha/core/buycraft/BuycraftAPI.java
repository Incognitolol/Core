package rip.alpha.core.buycraft;

import com.google.gson.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import rip.alpha.core.buycraft.http.HttpDeleteWithBody;
import rip.alpha.core.buycraft.response.*;
import rip.alpha.libraries.json.GsonProvider;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author TewPingz
 */
public class BuycraftAPI {

    private final String key;
    private final CloseableHttpClient httpClient;

    public BuycraftAPI(String key) {
        this.key = key;
        this.httpClient = HttpClients.createDefault();
    }

    public InformationResponse fetchInformation() throws IOException {
        JsonObject responseObject = this.get("information");
        JsonObject accountObject = responseObject.getAsJsonObject("account");
        String domain = accountObject.get("domain").getAsString();
        String name = accountObject.get("name").getAsString();
        JsonObject currencyObject = accountObject.getAsJsonObject("currency");
        String currencyName = currencyObject.get("iso_4217").getAsString();
        String currencySymbol = currencyObject.get("symbol").getAsString();
        return new InformationResponse(name, domain, currencyName, currencySymbol);
    }
    public PaymentResponse fetchPayment(String transaction) throws IOException {
        JsonObject responseObject = this.get("payments/" + transaction);
        int id = responseObject.get("id").getAsInt();
        String amount = responseObject.get("amount").getAsString();
        String status = responseObject.get("status").getAsString();
        String date = responseObject.get("date").getAsString();
        JsonObject currencyObject = responseObject.get("currency").getAsJsonObject();

        Currency currency = new Currency(currencyObject.get("iso_4217").getAsString(), currencyObject.get("symbol").getAsString());
        JsonObject playerObject = responseObject.get("player").getAsJsonObject();
        PaymentResponse.Player player = new PaymentResponse.Player(
                playerObject.get("id").getAsInt(),
                playerObject.get("name").getAsString(),
                playerObject.get("uuid").getAsString()
        );
        List<PaymentResponse.Package> packages = new ArrayList<>();
        responseObject.getAsJsonArray("packages").forEach(element -> {
            JsonObject jsonObject = element.getAsJsonObject();
            int packageId = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            packages.add(new PaymentResponse.Package(packageId, name));
        });
        return new PaymentResponse(id, amount, status, date, currency, player, packages);
    }

    public PlayersDueResponse fetchDuePlayers() throws IOException {
        JsonObject responseObject = this.get("queue");
        JsonObject metaObject = responseObject.getAsJsonObject("meta");
        JsonArray jsonArray = responseObject.getAsJsonArray("players");
        int nextCheck = metaObject.get("next_check").getAsInt();
        PriorityQueue<PlayersDueResponse.QueuedPlayer> playersQueued = new PriorityQueue<>();
        jsonArray.forEach(element -> {
            JsonObject jsonObject = element.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            String uuid = jsonObject.get("uuid").getAsString();
            PlayersDueResponse.QueuedPlayer queuedPlayer = new PlayersDueResponse.QueuedPlayer(id, name, uuid);
            playersQueued.add(queuedPlayer);
        });
        return new PlayersDueResponse(nextCheck, playersQueued);
    }
    public void fetchPlayerLookup(String uuid)  throws IOException {
        JsonObject responseObject = this.get("user/" + uuid.toString());
        System.out.println(GsonProvider.toJsonPretty(responseObject));


    }

    public CommandsDueResponse fetchDueCommands() throws IOException {
        JsonObject responseObject = this.get("queue/offline-commands");
        JsonArray jsonArray = responseObject.getAsJsonArray("commands");
        PriorityQueue<CommandsDueResponse.QueuedCommand> commandsQueued = new PriorityQueue<>();
        jsonArray.forEach(element -> {
            JsonObject jsonObject = element.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            String command = jsonObject.get("command").getAsString();
            int payment = jsonObject.get("payment").getAsInt();
            int packageId = jsonObject.get("package").getAsInt();
            JsonObject playerObject = jsonObject.getAsJsonObject("player");
            int playerId = playerObject.get("id").getAsInt();
            String name = playerObject.get("name").getAsString();
            String playerUUID = playerObject.get("uuid").getAsString();
            CommandsDueResponse.Player player = new CommandsDueResponse.Player(playerId, name, playerUUID);
            CommandsDueResponse.QueuedCommand queuedCommand = new CommandsDueResponse.QueuedCommand(id, command, payment, packageId, player);
            commandsQueued.add(queuedCommand);
        });
        return new CommandsDueResponse(commandsQueued);
    }

    public void queueDelete(List<Integer> ids) throws IOException {
        HttpDeleteWithBody delete = new HttpDeleteWithBody("https://plugin.tebex.io/queue");
        delete.addHeader("User-Agent", "Core-Buycraft v:1.0");
        delete.addHeader("X-Tebex-Secret", key);
        delete.addHeader("Content-Type", "application/json");
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("ids", GsonProvider.toJsonTree(ids));
        delete.setEntity(new StringEntity(jsonObject.toString()));
        CloseableHttpResponse response = this.httpClient.execute(delete);
        response.close();
        if (response.getStatusLine().getStatusCode() != 204) {
            throw new BuycraftException(response.getStatusLine().getStatusCode(), new JsonObject());
        }
    }

    public void close() throws IOException {
        this.httpClient.close();
    }

    public JsonObject get(String path) throws IOException {
        HttpGet get = new HttpGet("https://plugin.tebex.io/" + path);
        get.addHeader("User-Agent", "Core-Buycraft v:1.0");
        get.addHeader("X-Tebex-Secret", key);
        get.addHeader("Content-Type", "application/json");

        CloseableHttpResponse response = this.httpClient.execute(get);
        JsonObject jsonObject = this.readResponse(response);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new BuycraftException(response.getStatusLine().getStatusCode(), jsonObject);
        }

        return jsonObject;
    }

    private JsonObject readResponse(CloseableHttpResponse response) throws IOException {
        JsonElement jsonObject = GsonProvider.getGson().fromJson(new InputStreamReader(response.getEntity().getContent()), JsonElement.class);
        response.close();
        return jsonObject.getAsJsonObject();
    }
}
