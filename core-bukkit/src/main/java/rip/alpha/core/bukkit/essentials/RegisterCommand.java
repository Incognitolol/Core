package rip.alpha.core.bukkit.essentials;

import com.google.gson.JsonObject;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.alpha.libraries.command.annotation.Command;
import rip.alpha.libraries.command.annotation.CommandUsage;
import rip.alpha.libraries.json.GsonProvider;
import rip.alpha.libraries.util.message.MessageBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RegisterCommand {
    private static final String authKey = "35tpl53kot54kt534tk054j0tj05tj4t5j054tj0t54j0hnsf09jsf09jsf9jsfj9f";
    private static final String registrationEndpoint = "http://localhost:81/api/register";
    private static final Map<UUID, Long> lastRequest = new HashMap<>();
    private static final long registerDelay = 60 * 1000;

    @CommandUsage("<email>")
    @Command(names = {"register", "singup", "websiteaccount"}, async = true)
    public static void registerCommand(Player player, String email) {
        if (!isValidEmail(email)) {
            player.sendMessage(ChatColor.RED + "That is an invalid email!");
            return;
        }

        long lastTime = lastRequest.computeIfAbsent(player.getUniqueId(), uuid -> -1L);

        if (lastTime != -1 && (lastTime - System.currentTimeMillis() > 0)) {
            player.sendMessage(MessageBuilder.constructError("You are currently on register cooldown."));
            return;
        }

        lastRequest.put(player.getUniqueId(), System.currentTimeMillis() + registerDelay);
        makeAccount(player, email).thenAccept(player::sendMessage);
    }

    private static CompletableFuture<String> makeAccount(Player player, String email) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, String> params = new HashMap<>();
            params.put("uuid", player.getUniqueId().toString());
            params.put("username", player.getName());
            params.put("email", email);

            try {
                JsonObject response = GsonProvider.fromJson(makeRequest(params), JsonObject.class);
                boolean success = response.get("success").getAsBoolean();

                if (success) {
                    return ChatColor.GREEN + "Success. You will receive a confirmation email shortly!";
                } else {
                    return getResponseForError(response.get("error").getAsString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return ChatColor.RED + "There was an error on our end, try again shortly or contact staff if it persists!";
            }
        });
    }

    private static String getResponseForError(String error) {
        return switch (error) {
            case "emailAlreadyInUse" -> ChatColor.RED + "It appears that email is already in use by another player!";
            case "emailDeliveryFailed" -> ChatColor.RED + "Failed to send the registration email to your email address!";
            case "userAlreadyRegistered" -> ChatColor.RED + "Your Minecraft account is already registered!";
            case "invalidEmail" -> ChatColor.RED + "The email you provided is invalid!";
            default -> ChatColor.RED + "There was an error on our end, try again shortly or contact staff if it persists!";
        };
    }

    private static boolean isValidEmail(String input) {
        return input.contains("@") && input.contains(".") && input.length() >= 5; // 5 length because x@x.x
    }

    private static String makeRequest(Map<String, String> arguments) throws Exception {
        URL url = new URL(registrationEndpoint);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> param : arguments.entrySet()) {
            if (postData.length() != 0)
                postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
        }
        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("auth_key", authKey);
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setConnectTimeout(30);
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        String line;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        bufferedReader.close();
        return stringBuilder.toString();
    }
}
