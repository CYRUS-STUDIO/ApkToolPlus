package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class PackageHeader extends ChunkHeader {

    // If this is a base package, its ID.  ResourcePackage IDs start at 1 (corresponding to the value of
    // the package bits in a resource identifier).  0 means this is not a base package.
    // uint32_t
    private long id;

    // Actual name of this package, -terminated.
    // char16_t name[128]
    private String name;

    // Offset to a ResStringPool_header defining the resource type symbol table.
    //  If zero, this package is inheriting from another base package (overriding specific values in it).
    // uinit 32
    private long typeStrings;


    // Last index into typeStrings that is for public use by others.
    // uint32_t
    private long lastPublicType;

    // Offset to a ResStringPool_header defining the resource
    // key symbol table.  If zero, this package is inheriting from
    // another base package (overriding specific values in it).
    // uint32_t
    private long keyStrings;

    // Last index into keyStrings that is for public use by others.
    // uint32_t
    private long lastPublicKey;

    public PackageHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTypeStrings() {
        return typeStrings;
    }

    public void setTypeStrings(long typeStrings) {
        this.typeStrings = typeStrings;
    }

    public long getLastPublicType() {
        return lastPublicType;
    }

    public void setLastPublicType(long lastPublicType) {
        this.lastPublicType = lastPublicType;
    }

    public long getKeyStrings() {
        return keyStrings;
    }

    public void setKeyStrings(long keyStrings) {
        this.keyStrings = keyStrings;
    }

    public long getLastPublicKey() {
        return lastPublicKey;
    }

    public void setLastPublicKey(long lastPublicKey) {
        this.lastPublicKey = lastPublicKey;
    }
}
