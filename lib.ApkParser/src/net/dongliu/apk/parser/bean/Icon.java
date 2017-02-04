package net.dongliu.apk.parser.bean;

import java.util.Arrays;

/**
 * The apk icon file path, and data
 *
 * @author Liu Dong
 */
public class Icon {

    private final String path;
    private final byte[] data;

    public Icon(String path, byte[] data) {
        this.path = path;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Icon{path='" + path + '\'' + ", size=" + (data == null ? 0 : data.length) + '}';
    }
}
