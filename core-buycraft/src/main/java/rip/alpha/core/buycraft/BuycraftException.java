package rip.alpha.core.buycraft;

import com.google.gson.JsonElement;

public class BuycraftException extends IllegalStateException {
    public BuycraftException(int statusCode, JsonElement response) {
        super("Received status code: %s with the response %s.".formatted(statusCode, response.toString()));
    }
}
