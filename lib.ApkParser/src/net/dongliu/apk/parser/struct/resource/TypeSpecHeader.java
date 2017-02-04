package net.dongliu.apk.parser.struct.resource;

import net.dongliu.apk.parser.struct.ChunkHeader;

/**
 * @author dongliu
 */
public class TypeSpecHeader extends ChunkHeader {

    // The type identifier this chunk is holding.  Type IDs start at 1 (corresponding to the value
    // of the type bits in a resource identifier).  0 is invalid.
    // The id also specifies the name of the Resource type. It is the string at index id - 1 in the
    // typeStrings StringPool chunk in the containing Package chunk.
    // uint8_t
    private short id;

    // Must be 0. uint8_t
    private short res0;

    // Must be 0.uint16_t
    private int res1;

    // Number of uint32_t entry configuration masks that follow.
    private long entryCount;

    public TypeSpecHeader(int chunkType, int headerSize, long chunkSize) {
        super(chunkType, headerSize, chunkSize);
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public short getRes0() {
        return res0;
    }

    public void setRes0(short res0) {
        this.res0 = res0;
    }

    public int getRes1() {
        return res1;
    }

    public void setRes1(int res1) {
        this.res1 = res1;
    }

    public long getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(long entryCount) {
        this.entryCount = entryCount;
    }
}
