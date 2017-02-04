package net.dongliu.apk.parser.struct.xml;

/**
 * xml node attributes
 *
 * @author dongliu
 */
public class Attributes {

    private final Attribute[] attributes;

    public Attributes(int size) {
        this.attributes = new Attribute[size];
    }

    public void set(int i, Attribute attribute) {
        attributes[i] = attribute;
    }

    public String get(String name) {
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(name)) {
                return attribute.getValue();
            }
        }
        return null;
    }

    public int size() {
        return attributes.length;
    }

    public boolean getBoolean(String name, boolean b) {
        String value = get(name);
        return value == null ? b : Boolean.parseBoolean(value);
    }

    public Integer getInt(String name) {
        String value = get(name);
        return value == null ? null : Integer.valueOf(value);
    }

    public Long getLong(String name) {
        String value = get(name);
        return value == null ? null : Long.valueOf(value);
    }

    public Attribute[] value() {
        return this.attributes;
    }
}

