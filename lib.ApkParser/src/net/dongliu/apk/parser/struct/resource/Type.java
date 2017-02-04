package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.StringPool;
import net.dongliu.apk.parser.utils.Buffers;
import net.dongliu.apk.parser.utils.ParseUtils;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author dongliu
 */
public class Type {

    private String name;
    private short id;

    private Locale locale;

    private StringPool keyStringPool;
    private ByteBuffer buffer;
    private long[] offsets;
    private StringPool stringPool;

    public Type(TypeHeader header) {
        this.id = header.getId();
        this.locale = new Locale(header.getConfig().getLanguage(), header.getConfig().getCountry());
    }

    public ResourceEntry getResourceEntry(int id) {
        if (id >= offsets.length) {
            return null;
        }

        if (offsets[id] == TypeHeader.NO_ENTRY) {
            return null;
        }

        // read Resource Entries
        buffer.position((int) offsets[id]);
        return readResourceEntry();
    }

    private ResourceEntry readResourceEntry() {
        long beginPos = buffer.position();
        ResourceEntry resourceEntry = new ResourceEntry();
        // size is always 8(simple), or 16(complex)
        resourceEntry.setSize(Buffers.readUShort(buffer));
        resourceEntry.setFlags(Buffers.readUShort(buffer));
        long keyRef = buffer.getInt();
        resourceEntry.setKey(keyStringPool.get((int) keyRef));

        if ((resourceEntry.getFlags() & ResourceEntry.FLAG_COMPLEX) != 0) {
            ResourceMapEntry resourceMapEntry = new ResourceMapEntry(resourceEntry);

            // Resource identifier of the parent mapping, or 0 if there is none.
            resourceMapEntry.setParent(Buffers.readUInt(buffer));
            resourceMapEntry.setCount(Buffers.readUInt(buffer));

            buffer.position((int) (beginPos + resourceEntry.getSize()));

            //An individual complex Resource entry comprises an entry immediately followed by one or more fields.
            ResourceTableMap[] resourceTableMaps = new ResourceTableMap[(int) resourceMapEntry.getCount()];
            for (int i = 0; i < resourceMapEntry.getCount(); i++) {
                resourceTableMaps[i] = readResourceTableMap();
            }

            resourceMapEntry.setResourceTableMaps(resourceTableMaps);
            return resourceMapEntry;
        } else {
            buffer.position((int) (beginPos + resourceEntry.getSize()));
            resourceEntry.setValue(ParseUtils.readResValue(buffer, stringPool));
            return resourceEntry;
        }
    }

    private ResourceTableMap readResourceTableMap() {
        ResourceTableMap resourceTableMap = new ResourceTableMap();
        resourceTableMap.setNameRef(Buffers.readUInt(buffer));
        resourceTableMap.setResValue(ParseUtils.readResValue(buffer, stringPool));

        if ((resourceTableMap.getNameRef() & 0x02000000) != 0) {
            //read arrays
        } else if ((resourceTableMap.getNameRef() & 0x01000000) != 0) {
            // read attrs
        } else {
        }

        return resourceTableMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public StringPool getKeyStringPool() {
        return keyStringPool;
    }

    public void setKeyStringPool(StringPool keyStringPool) {
        this.keyStringPool = keyStringPool;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public long[] getOffsets() {
        return offsets;
    }

    public void setOffsets(long[] offsets) {
        this.offsets = offsets;
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public void setStringPool(StringPool stringPool) {
        this.stringPool = stringPool;
    }

    @Override
    public String toString() {
        return "Type{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", locale=" + locale +
                '}';
    }
}
