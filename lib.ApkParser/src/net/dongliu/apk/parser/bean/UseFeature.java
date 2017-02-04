package net.dongliu.apk.parser.bean;

/**
 * the permission used by apk
 *
 * @author dongliu
 */
public class UseFeature {
    private String name;
    private boolean required = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
