package com.owlike.genson.ext.jsr353;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import com.owlike.genson.GensonBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

import com.owlike.genson.Genson;

public class JsonValueTest {
  private final Genson genson = new GensonBuilder().withBundle(new JSR353Bundle()).create();

  @Test
  public void testSerArrayOfLiterals() {
    String json =
      genson.serialize(JSR353Bundle.factory.createArrayBuilder().addNull().add(1.22)
        .add(false).add("str").build());
    assertEquals("[null,1.22,false,\"str\"]", json);
  }

  @Test
  public void testSerObjectAndArray() {
    String json =
      genson.serialize(JSR353Bundle.factory.createObjectBuilder().add("int", 98)
        .addNull("null")
        .add("array", JSR353Bundle.factory.createArrayBuilder().build()).build());
    assertEquals("{\"int\":98,\"null\":null,\"array\":[]}", json);
  }

  @Test
  public void testDeserArrayOfLiterals() {
    JsonArray array = genson.deserialize("[1, 2.2, \"str\", true, null]", JsonArray.class);
    assertEquals(1, array.getInt(0));
    assertEquals(2.2, array.getJsonNumber(1).doubleValue(), 1e-200);
    assertEquals("str", array.getString(2));
    assertEquals(true, array.getBoolean(3));
    assertEquals(true, array.isNull(4));
  }

  @Test
  public void testDeserObject() {
    JsonObject object =
      genson.deserialize("{\"str\":\"a\", \"array\": [1]}", JsonObject.class);
    assertEquals("a", object.getString("str"));
    JsonArray array = (JsonArray) object.get("array");
    assertEquals(1, array.getInt(0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testJsonValueImmutability() {
    JsonObject object =
      genson.deserialize("{\"str\":\"a\", \"array\": [1]}", JsonObject.class);
    try {
      object.put("str", new GensonJsonString("b"));
      fail("Should've thrown UnsupportedOperationException");
    }
    catch (UnsupportedOperationException e) {
      object.getJsonArray("array").add(new GensonJsonNumber.IntJsonNumber(5));
    }
  }

  @Test
  public void testRoundTripMixBeanAndJsonStructures() {
    JsonArray array = JSR353Bundle.factory.createArrayBuilder().add(1).add(2).build();
    JsonObject object = JSR353Bundle.factory.createObjectBuilder().add("key", "value").addNull("foo").build();
    Bean bean = new Bean();
    bean.setArray(array);
    bean.setObj(object);
    bean.setStr(object.getJsonString("key"));

    String json = genson.serialize(bean);
    Bean actual = genson.deserialize(json, Bean.class);

    assertEquals(array, actual.array);
    assertEquals(object, actual.obj);
    assertEquals(object.getJsonString("key"), actual.str);
  }

  @Test
  public void classMetadataConverterShouldNotKickIn() {
    Genson genson = new GensonBuilder()
            .useClassMetadata(true)
            .withBundle(new JSR353Bundle())
            .create();

    String actual = genson.serialize(JSR353Bundle.factory.createObjectBuilder().build());

    assertEquals("{}", actual);
    // shouldn't fail
    JsonObject value = genson.deserialize("{\"@class\": \"class.that.doesnt.exist.AH!\"}", JsonObject.class);
    assertTrue(JsonObject.class.isAssignableFrom(value.getClass()));
  }

  @Test
  public void classMetadataConverterShouldKickIn() {
    Genson genson = new GensonBuilder()
            .useClassMetadata(true)
            .addAlias("bean", Bean.class)
            .withBundle(new JSR353Bundle())
            .create();

    Object v = genson.deserialize("{\"@class\": \"bean\"}", Object.class);
    assertTrue(Bean.class.isAssignableFrom(v.getClass()));
  }

  @Test public void readUnknownTypesAsJsonValueWhenMissingClassMetadata() {
    Genson genson = new GensonBuilder()
            .useClassMetadata(true)
            .withBundle(new JSR353Bundle().readUnknownTypesAsJsonValue(true))
            .create();

    assertTrue(JsonObject.class.isAssignableFrom(genson.deserialize("{}", Object.class).getClass()));
  }

  @Test
  public void readUnknownTypesAsActualTypeWhenClassMetadataPresent() {
    Genson genson = new GensonBuilder()
            .useClassMetadata(true)
            .addAlias("bean", Bean.class)
            .withBundle(new JSR353Bundle().readUnknownTypesAsJsonValue(true))
            .create();

    Object v = genson.deserialize("{\"@class\": \"bean\"}", Object.class);

    assertTrue(Bean.class.isAssignableFrom(v.getClass()));
  }


  public static class Bean {
    private JsonString str;
    private JsonObject obj;
    private JsonArray array;

    public JsonString getStr() {
      return str;
    }

    public void setStr(JsonString str) {
      this.str = str;
    }

    public JsonObject getObj() {
      return obj;
    }

    public void setObj(JsonObject obj) {
      this.obj = obj;
    }

    public JsonArray getArray() {
      return array;
    }

    public void setArray(JsonArray array) {
      this.array = array;
    }
  }
}
