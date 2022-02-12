package com.owlike.genson.ext.jaxb;

import static org.junit.Assert.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.owlike.genson.GensonBuilder;
import com.owlike.genson.annotation.JsonIgnore;
import org.junit.Before;
import org.junit.Test;

import com.owlike.genson.Genson;

public class JaxbAnnotationsSupportTest {
  private Genson genson;

  @Before
  public void setUp() {
    genson = new GensonBuilder().withBundle(new JAXBBundle()).create();
  }

  @Test
  public void testXmlAccessorTypeSerialization() {
    assertEquals("{\"a\":1}", genson.serialize(new XmlAccessorTypeBean()));
  }

  @Test
  public void testXmlAccessorTypeDeserialization() {
    XmlAccessorTypeBean bean = genson.deserialize(
      "{\"a\": 10, \"b\": 5, \"transientField\": 9}", XmlAccessorTypeBean.class);
    assertEquals(10, bean.a);
    assertEquals(0, bean.b);
    assertEquals(2, bean.transientField);
  }

  @Test
  public void testXmlAccessorTypeProperty() {
    PropertyAccessPojo bean = new PropertyAccessPojo();
    bean.setB(3);

    assertEquals("{\"b\":3}", genson.serialize(bean));
    assertEquals(3, genson.deserialize("{\"b\":3}", PropertyAccessPojo.class).getB());
  }

  @Test
  public void testXmlAccessorTypePublicMember() {
    PublicMemberAccessPojo bean = new PublicMemberAccessPojo();
    bean.setB(3);

    assertEquals("{\"b\":3}", genson.serialize(bean));
    assertEquals(3, genson.deserialize("{\"b\":3}", PropertyAccessPojo.class).getB());
  }

  @Test
  public void testXmlAttributeSerialization() {
    assertEquals("{\"a\":0,\"c\":1,\"d\":null}", genson.serialize(new XmlAttributeBean()));
  }

  @Test
  public void testXmlAttributeDeserialization() {
    XmlAttributeBean bean = genson.deserialize("{\"a\":2,\"d\":3,\"c\":4}",
      XmlAttributeBean.class);
    assertEquals(2, bean.a);
    assertEquals(3, bean.b.intValue());
    assertEquals(4, bean.c);
  }

  @Test
  public void testXmlElementSerialization() {
    assertEquals("{\"b\":0,\"bean\":{\"a\":0},\"c\":0}", genson.serialize(new XmlElementBean()));
  }

  @Test
  public void testXmlElementDeserialization() {
    XmlElementBean bean = genson.deserialize("{\"b\":1,\"bean\":{\"a\":2},\"c\":3}",
      XmlElementBean.class);
    assertEquals(1, bean.b);
    assertEquals(2, ((EmptyBean) bean.a).a);
    assertEquals(3, bean.c);
  }

  @Test
  public void testXmlEnumValue() {
    String json = genson.serialize(XmlEnumValueBean.ONE);
    assertEquals("\"1\"", json);
    assertEquals(XmlEnumValueBean.ONE, genson.deserialize(json, XmlEnumValueBean.class));

    json = genson.serialize(XmlEnumValueBean.TWO);
    assertEquals("\"TWO\"", json);
    assertEquals(XmlEnumValueBean.TWO, genson.deserialize(json, XmlEnumValueBean.class));
  }

  @Test
  public void testXmlJavaTypeAdapter() {
    assertEquals("{\"v\":\"0\"}", genson.serialize(new XmlJavaTypeAdapterBean()));
  }

  @Test
  public void testXmlJavaTypeAdapterWithXmlElement() {
    assertEquals("{\"v\":\"0\"}", genson.serialize(new XmlJavaTypeAdapterWithXmlElementBean()));
  }
  
  @Test
  public void testXmlTransientSerialization() {
    XmlTransientContainer container = new XmlTransientContainer();
    container.bean = new XmlTransientBean();
    assertEquals("{}", genson.serialize(container));
  }

  @Test
  public void testXmlTransientDeserialization() {
    XmlTransientContainer container = genson.deserialize("{\"a\":1,\"b\":2,\"bean\":{}}",
      XmlTransientContainer.class);
    assertEquals(0, container.a);
    assertEquals(0, container.b);
    assertEquals(null, container.bean);
  }

  @Test public void shouldFavorGensonAnnotationsOverJaxb() {
    PojoWithJaxbAndGensonAnnotations pojo = new PojoWithJaxbAndGensonAnnotations();
    pojo.value = 1;

    assertEquals("{}", genson.serialize(pojo));
  }

  @Test public void shouldFavorFilteringAndRenamingFromBuilderOverJaxb() {
    Genson genson = new GensonBuilder()
      .withBundle(new JAXBBundle())
      .exclude("a")
      .exclude("c")
      .rename("b", "foo")
      .create();

    assertEquals("{\"foo\":null}", genson.serialize(new XmlAttributeBean()));
  }

  @Test public void roundTripWithWrappedRootElement() {
    XmlRootElementDefaultBean expected = new XmlRootElementDefaultBean();
    expected.i = 1;

    Genson genson = new GensonBuilder().withBundle(new JAXBBundle().wrapRootValues(true)).create();
    String json = genson.serialize(expected);

    assertEquals("{\"xmlRootElementDefaultBean\":{\"i\":1}}", json);
    assertEquals(expected.i, genson.deserialize(json, XmlRootElementDefaultBean.class).i);
  }

  @Test public void roundTripWithWrappedRenamedRootElement() {
    XmlRootElementBean expected = new XmlRootElementBean();
    expected.i = 1;

    Genson genson = new GensonBuilder().withBundle(new JAXBBundle().wrapRootValues(true)).create();
    String json = genson.serialize(expected);

    assertEquals("{\"oo\":{\"i\":1}}", json);
    assertEquals(expected.i, genson.deserialize(json, XmlRootElementBean.class).i);
  }

  public static class XmlAttributeBean {
    @XmlAttribute
    private int a;
    @XmlAttribute(name = "d", required = true)
    private Integer b;
    public transient int c = 1;

    @XmlAttribute
    private int getC() {
      return c;
    }

    @XmlAttribute
    private void setC(int c) {
      this.c = c;
    }
  }

  public static class XmlElementBean {
    @XmlElement(name = "bean", type = EmptyBean.class)
    private Object a = new EmptyBean();
    @XmlElement(name = "c")
    private int c;
    private transient int b;

    @XmlElement
    private int getB() {
      return b;
    }

    @XmlElement
    private void setB(int b) {
      this.b = b;
    }
  }

  public static class EmptyBean {
    public int a;
  }

  public static class XmlJavaTypeAdapterBean {
    @XmlJavaTypeAdapter(MyXmlAdapter.class)
    public int v;
  }

  public static class XmlJavaTypeAdapterWithXmlElementBean {
    @XmlElement(type=String.class)
    @XmlJavaTypeAdapter(MyXmlAdapter.class)
    public int v;
  }
  
  public static class MyXmlAdapter extends XmlAdapter<String, Integer> {

    @Override
    public Integer unmarshal(String v) throws Exception {
      return Integer.valueOf(v);
    }

    @Override
    public String marshal(Integer v) throws Exception {
      return v.toString();
    }

  }

  @XmlEnum(Integer.class)
  public static enum XmlEnumValueBean {
    @XmlEnumValue("1")
    ONE, TWO
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class XmlElementRefBean {

    // TODO not yet implemented, does really someone use such feature in JSON world?
    @XmlElementRef
    private XmlRootElementBean a;
    @XmlElementRef
    private XmlRootElementDefaultBean b;
  }

  @XmlRootElement
  public static class XmlRootElementDefaultBean {
    public int i;
  }

  @XmlRootElement(name = "oo")
  public static class XmlRootElementBean {
    public int i;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class XmlAccessorTypeBean {
    private int a = 1;
    private transient int transientField = 2;
    private transient int b;

    public void setB(int b) {
      this.b = b;
    }

    public int getB() {
      return 3;
    }
  }

  public static class XmlTransientContainer {
    @XmlTransient
    public int a;
    public XmlTransientBean bean;
    private int b;

    public int getB() {
      return b;
    }

    @XmlTransient
    public void setB(int b) {
      this.b = b;
    }
  }

  @XmlTransient
  public static class XmlTransientBean {

  }

  public static class PojoWithJaxbAndGensonAnnotations {
    @XmlElement
    @JsonIgnore
    public int value;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class PropertyAccessPojo {
    private int a;

    public int getB() {
      return a;
    }

    public void setB(int b) {
      this.a = b;
    }
  }

  @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
  public static class PublicMemberAccessPojo {
    private int a;

    public int getB() {
      return a;
    }

    public void setB(int b) {
      this.a = b;
    }
  }
}
