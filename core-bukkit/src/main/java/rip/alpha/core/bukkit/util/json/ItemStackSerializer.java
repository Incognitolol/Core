package rip.alpha.core.bukkit.util.json;

import com.google.gson.*;
import lombok.Cleanup;
import net.minecraft.util.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();
            String base64 = jsonObject.get("item").getAsString();
            @Cleanup ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            @Cleanup BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            return (ItemStack) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonElement serialize(ItemStack item, Type typeOfSrc, JsonSerializationContext context) {
        try {
            JsonObject jsonObject = new JsonObject();
            @Cleanup ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            @Cleanup BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            String base64 = Base64Coder.encodeLines(outputStream.toByteArray());
            jsonObject.addProperty("item", base64);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
