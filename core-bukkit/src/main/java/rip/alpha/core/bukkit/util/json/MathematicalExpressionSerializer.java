package rip.alpha.core.bukkit.util.json;


import com.google.gson.*;
import rip.alpha.core.bukkit.util.math.MathematicalExpression;

import java.lang.reflect.Type;

public class MathematicalExpressionSerializer implements JsonSerializer<MathematicalExpression>, JsonDeserializer<MathematicalExpression> {

    @Override
    public MathematicalExpression deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return new MathematicalExpression(jsonObject.get("expression").getAsString());
    }

    @Override
    public JsonElement serialize(MathematicalExpression mathematicalExpression, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("expression", mathematicalExpression.getOriginal());
        return jsonObject;
    }

}
