package rip.alpha.core.shared.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.reflect.TypeToken;
import rip.alpha.libraries.Libraries;
import rip.alpha.libraries.json.GsonProvider;

import java.io.Serializable;
import java.util.*;

public class SerializationTest {

    @BeforeAll
    public static void setup() {
        Libraries.getInstance().registerSerializers();
        GsonProvider.registerAbstractClass(AbstractData.class);
        GsonProvider.registerInterface(IData.class);
    }

    @Test
    public void simpleDataSerializationTest() {
        UUID uuid = UUID.randomUUID();
        SimpleData simpleData = new SimpleData("Peter", 20, uuid);
        String json = GsonProvider.toJson(simpleData);
        SimpleData deserialized = GsonProvider.fromJson(json, SimpleData.class);
        Assertions.assertEquals(simpleData, deserialized);
    }

    @Test
    public void immutableDataSerializationTest() {
        UUID uuid = UUID.randomUUID();
        ImmutableData immutableData = new ImmutableData("Peter", 20, uuid);
        String json = GsonProvider.toJson(immutableData);
        ImmutableData deserialized = GsonProvider.fromJson(json, ImmutableData.class);
        Assertions.assertEquals(immutableData, deserialized);
    }

    @Test
    public void recordDataSerializationTest() {
        UUID uuid = UUID.randomUUID();
        RecordData recordData = new RecordData("Peter", 20, uuid);
        String json = GsonProvider.toJson(recordData);
        RecordData deserialized = GsonProvider.fromJson(json, RecordData.class);
        Assertions.assertEquals(recordData, deserialized);
    }

    @Test
    public void nestedDataSerializationTest() {
        UUID uuid = UUID.randomUUID();
        NestedData nestedData = new NestedData(new NestedData.Nest("Peter", 20, uuid), uuid);
        String json = GsonProvider.toJson(nestedData);
        NestedData deserialized = GsonProvider.fromJson(json, NestedData.class);
        Assertions.assertEquals(nestedData, deserialized);
    }

    @Test
    public void simpleListTest() {
        List<SimpleData> list = new ArrayList<>();
        UUID uuidA = UUID.randomUUID();
        list.add(new SimpleData("Peter", 20, uuidA));

        UUID uuidB = UUID.randomUUID();
        list.add(new SimpleData("Violet", 27, uuidB));

        UUID uuidC = UUID.randomUUID();
        list.add(new SimpleData("El Salvador", 25, uuidC));

        String json = GsonProvider.toJson(list);

        TypeToken<List<SimpleData>> token = new TypeToken<>() {
        };

        List<SimpleData> deserialized = GsonProvider.fromJson(json, token.getType());
        Assertions.assertEquals(list, deserialized);
    }

    @Test
    public void nestedListTest() {
        List<NestedData> list = new ArrayList<>();
        UUID uuidA = UUID.randomUUID();
        list.add(new NestedData(new NestedData.Nest("Peter", 20, uuidA), uuidA));

        UUID uuidB = UUID.randomUUID();
        list.add(new NestedData(new NestedData.Nest("Violet", 27, uuidB), uuidB));

        UUID uuidC = UUID.randomUUID();
        list.add(new NestedData(new NestedData.Nest("El Salvador", 25, uuidC), uuidC));

        String json = GsonProvider.toJson(list);

        TypeToken<List<NestedData>> token = new TypeToken<>() {
        };

        List<NestedData> deserialized = GsonProvider.fromJson(json, token.getType());
        Assertions.assertEquals(list, deserialized);
    }

    @Test
    public void nestedMapNestedDataTest() {
        Map<String, Map<UUID, NestedData>> nestedMap = new HashMap<>();
        String name = "Peter0123";
        UUID uuidA = UUID.randomUUID();
        UUID uuidB = UUID.randomUUID();
        UUID uuidC = UUID.randomUUID();
        NestedData dataA = new NestedData(new NestedData.Nest(name, 30, uuidA), uuidA);
        NestedData dataB = new NestedData(new NestedData.Nest(name, 40, uuidB), uuidB);
        NestedData dataC = new NestedData(new NestedData.Nest(name, 56, uuidC), uuidC);
        nestedMap.computeIfAbsent(name, key -> new HashMap<>()).put(uuidA, dataA);
        nestedMap.computeIfAbsent(name, key -> new HashMap<>()).put(uuidB, dataB);
        nestedMap.computeIfAbsent(name, key -> new HashMap<>()).put(uuidC, dataC);

        String json = GsonProvider.toJson(nestedMap);

        TypeToken<Map<String, Map<UUID, NestedData>>> token = new TypeToken<>() {
        };
        Map<String, Map<UUID, NestedData>> deserializedMap = GsonProvider.fromJson(json, token.getType());

        Assertions.assertEquals(nestedMap, deserializedMap);
    }

    @Test
    public void classDataTest() {
        ClassData classData = new ClassData(UUID.class, Number.class, Serializable.class);
        String json = GsonProvider.toJson(classData);
        ClassData deserialized = GsonProvider.fromJson(json, ClassData.class);
        Assertions.assertEquals(classData, deserialized);
    }

    @Test
    public void abstractDataTest() {
        AbstractData dataA = new ImplDataA("Peter", UUID.randomUUID());
        AbstractData dataB = new ImplDataB("Violet", UUID.randomUUID());
        String jsonA = GsonProvider.toJson(dataA);
        String jsonB = GsonProvider.toJson(dataB);
        AbstractData deserializedA = GsonProvider.fromJson(jsonA, AbstractData.class);
        AbstractData deserializedB = GsonProvider.fromJson(jsonB, AbstractData.class);
        Assertions.assertEquals(dataA, deserializedA);
        Assertions.assertEquals(dataB, deserializedB);
    }

    @Test
    public void interfaceDataTest() {
        IData.AData dataA = new IData.AData("Peter", UUID.randomUUID());
        BData dataB = new BData("Violet", UUID.randomUUID());
        String jsonA = GsonProvider.toJson(dataA);
        String jsonB = GsonProvider.toJson(dataB);
        IData.AData deserializedA = GsonProvider.fromJson(jsonA, IData.AData.class);
        BData deserializedB = GsonProvider.fromJson(jsonB, BData.class);
        Assertions.assertEquals(dataA, deserializedA);
        Assertions.assertEquals(dataB, deserializedB);
    }

}
