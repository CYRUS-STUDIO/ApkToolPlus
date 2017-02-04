package net.dongliu.apk.parser.utils;

import net.dongliu.apk.parser.bean.Locales;
import net.dongliu.apk.parser.exception.ParserException;
import net.dongliu.apk.parser.parser.StringPoolEntry;
import net.dongliu.apk.parser.struct.*;
import net.dongliu.apk.parser.struct.resource.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

/**
 * @author dongliu
 */
public class ParseUtils {

    public static Charset charsetUTF8 = Charset.forName("UTF-8");

    /**
     * read string from input buffer. if get EOF before read enough data, throw IOException.
     */
    public static String readString(ByteBuffer buffer, boolean utf8) {
        if (utf8) {
            //  The lengths are encoded in the same way as for the 16-bit format
            // but using 8-bit rather than 16-bit integers.
            int strLen = readLen(buffer);
            int bytesLen = readLen(buffer);
            byte[] bytes = Buffers.readBytes(buffer, bytesLen);
            String str = new String(bytes, charsetUTF8);
            // zero
            int trailling = Buffers.readUByte(buffer);
            return str;
        } else {
            // The length is encoded as either one or two 16-bit integers as per the commentRef...
            int strLen = readLen16(buffer);
            String str = Buffers.readString(buffer, strLen);
            // zero
            int trailling = Buffers.readUShort(buffer);
            return str;
        }
    }

    /**
     * read utf-16 encoding str, use zero char to end str.
     */
    public static String readStringUTF16(ByteBuffer buffer, int strLen) {
        String str = Buffers.readString(buffer, strLen);
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == 0) {
                return str.substring(0, i);
            }
        }
        return str;
    }

    /**
     * read encoding len.
     * see StringPool.cpp ENCODE_LENGTH
     */
    private static int readLen(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUByte(buffer);
        if ((i & 0x80) != 0) {
            //read one more byte.
            len |= (i & 0x7f) << 7;
            len += Buffers.readUByte(buffer);
        } else {
            len = i;
        }
        return len;
    }

    /**
     * read encoding len.
     * see Stringpool.cpp ENCODE_LENGTH
     */
    private static int readLen16(ByteBuffer buffer) {
        int len = 0;
        int i = Buffers.readUShort(buffer);
        if ((i & 0x8000) != 0) {
            len |= (i & 0x7fff) << 15;
            len += Buffers.readUShort(buffer);
        } else {
            len = i;
        }
        return len;
    }


    /**
     * read String pool, for apk binary xml file and resource table.
     */
    public static StringPool readStringPool(ByteBuffer buffer, StringPoolHeader stringPoolHeader) {

        long beginPos = buffer.position();
        long[] offsets = new long[(int) stringPoolHeader.getStringCount()];
        // read strings offset
        if (stringPoolHeader.getStringCount() > 0) {
            for (int idx = 0; idx < stringPoolHeader.getStringCount(); idx++) {
                offsets[idx] = Buffers.readUInt(buffer);
            }
        }
        // read flag
        // the string index is sorted by the string values if true
        boolean sorted = (stringPoolHeader.getFlags() & StringPoolHeader.SORTED_FLAG) != 0;
        // string use utf-8 format if true, otherwise utf-16
        boolean utf8 = (stringPoolHeader.getFlags() & StringPoolHeader.UTF8_FLAG) != 0;

        // read strings. the head and metas have 28 bytes
        long stringPos = beginPos + stringPoolHeader.getStringsStart() - stringPoolHeader.getHeaderSize();
        buffer.position((int) stringPos);

        StringPoolEntry[] entries = new StringPoolEntry[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            entries[i] = new StringPoolEntry(i, stringPos + offsets[i]);
        }

        String lastStr = null;
        long lastOffset = -1;
        StringPool stringPool = new StringPool((int) stringPoolHeader.getStringCount());
        for (StringPoolEntry entry : entries) {
            if (entry.getOffset() == lastOffset) {
                stringPool.set(entry.getIdx(), lastStr);
                continue;
            }

            buffer.position((int) entry.getOffset());
            lastOffset = entry.getOffset();
            String str = ParseUtils.readString(buffer, utf8);
            lastStr = str;
            stringPool.set(entry.getIdx(), str);
        }

        // read styles
        if (stringPoolHeader.getStyleCount() > 0) {
            // now we just skip it
        }

        buffer.position((int) (beginPos + stringPoolHeader.getBodySize()));

        return stringPool;
    }

    /**
     * method to read resource value RGB/ARGB type.
     */
    public static String readRGBs(ByteBuffer buffer, int strLen) {
        long l = Buffers.readUInt(buffer);
        StringBuilder sb = new StringBuilder();
        for (int i = strLen / 2 - 1; i >= 0; i--) {
            sb.append(Integer.toHexString((int) ((l >> i * 8) & 0xff)));
        }
        return sb.toString();
    }

    /**
     * read res value, convert from different types to string.
     */
    public static ResourceEntity readResValue(ByteBuffer buffer, StringPool stringPool) {
        ResValue resValue = new ResValue();
        resValue.setSize(Buffers.readUShort(buffer));
        resValue.setRes0(Buffers.readUByte(buffer));
        resValue.setDataType(Buffers.readUByte(buffer));

        switch (resValue.getDataType()) {
            case ResValue.ResType.INT_DEC:
            case ResValue.ResType.INT_HEX:
                resValue.setData(new ResourceEntity(buffer.getInt()));
                break;
            case ResValue.ResType.STRING:
                int strRef = buffer.getInt();
                if (strRef >= 0) {
                    resValue.setData(new ResourceEntity(stringPool.get(strRef)));
                }
                break;
            case ResValue.ResType.REFERENCE:
                long resourceId = Buffers.readUInt(buffer);
                resValue.setData(new ResourceEntity(resourceId));
                break;
            case ResValue.ResType.INT_BOOLEAN:
                resValue.setData(new ResourceEntity(buffer.getInt() != 0));
                break;
            case ResValue.ResType.NULL:
                resValue.setData(new ResourceEntity(""));
                break;
            case ResValue.ResType.INT_COLOR_RGB8:
            case ResValue.ResType.INT_COLOR_RGB4:
                resValue.setData(new ResourceEntity(readRGBs(buffer, 6)));
                break;
            case ResValue.ResType.INT_COLOR_ARGB8:
            case ResValue.ResType.INT_COLOR_ARGB4:
                resValue.setData(new ResourceEntity(readRGBs(buffer, 8)));
                break;
            case ResValue.ResType.DIMENSION:
                resValue.setData(new ResourceEntity(getDimension(buffer)));
                break;
            case ResValue.ResType.FRACTION:
                resValue.setData(new ResourceEntity(getFraction(buffer)));
                break;
            default:
                resValue.setData(new ResourceEntity("{" + resValue.getDataType() + ":"
                        + Buffers.readUInt(buffer) + "}"));
        }
        return resValue.getData();
    }

    private static String getDimension(ByteBuffer buffer) {
        long l = Buffers.readUInt(buffer);
        short unit = (short) (l & 0xff);
        String unitStr;
        switch (unit) {
            case ResValue.ResDataCOMPLEX.UNIT_MM:
                unitStr = "mm";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_PX:
                unitStr = "px";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_DIP:
                unitStr = "dp";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_SP:
                unitStr = "sp";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_PT:
                unitStr = "pt";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_IN:
                unitStr = "in";
                break;
            default:
                unitStr = "unknown unit:0x" + Integer.toHexString(unit);
        }
        return (l >> 8) + unitStr;
    }

    private static String getFraction(ByteBuffer buffer) {
        long l = Buffers.readUInt(buffer);
        // The low-order 4 bits of the data value specify the type of the fraction
        short type = (short) (l & 0xf);
        String pstr;
        switch (type) {
            case ResValue.ResDataCOMPLEX.UNIT_FRACTION:
                pstr = "%";
                break;
            case ResValue.ResDataCOMPLEX.UNIT_FRACTION_PARENT:
                pstr = "%p";
                break;
            default:
                pstr = "unknown type:0x" + Integer.toHexString(type);
        }
        float value = Float.intBitsToFloat((int) (l >> 4));
        return value + pstr;
    }

    public static void checkChunkType(int expected, int real) {
        if (expected != real) {
            throw new ParserException("Expect chunk type:" + Integer.toHexString(expected)
                    + ", but got:" + Integer.toHexString(real));
        }
    }

    /**
     * get resource value by string-format via resourceId.
     */
    public static String getResourceById(long resourceId, ResourceTable resourceTable, Locale locale) {
//        An Android Resource id is a 32-bit integer. It comprises
//        an 8-bit Package id [bits 24-31]
//        an 8-bit Type id [bits 16-23]
//        a 16-bit Entry index [bits 0-15]

        // android system styles.
        if (resourceId > AndroidConstants.SYS_STYLE_ID_START && resourceId < AndroidConstants.SYS_STYLE_ID_END) {
            return "@android:style/" + ResourceTable.sysStyle.get((int) resourceId);
        }

        String str = "resourceId:0x" + Long.toHexString(resourceId);
        if (resourceTable == null) {
            return str;
        }

        short packageId = (short) (resourceId >> 24 & 0xff);
        short typeId = (short) ((resourceId >> 16) & 0xff);
        int entryIndex = (int) (resourceId & 0xffff);
        ResourcePackage resourcePackage = resourceTable.getPackage(packageId);
        if (resourcePackage == null) {
            return str;
        }
        TypeSpec typeSpec = resourcePackage.getTypeSpec(typeId);
        List<Type> types = resourcePackage.getTypes(typeId);
        if (typeSpec == null || types == null) {
            return str;
        }
        if (!typeSpec.exists(entryIndex)) {
            return str;
        }

        // read from type resource
        ResourceEntry resource = null;
        String ref = null;
        int currentLevel = -1;
        for (Type type : types) {
            ResourceEntry curResource = type.getResourceEntry(entryIndex);
            if (curResource == null) {
                continue;
            }
            // not ResourceMapEntry and resource == 0. some resource entry do not have  effective resource id
            if (curResource.getValue() != null && curResource.getValue().getResourceId() == 0
                    && curResource.getValue().getValue() == null) {
                continue;
            }
            ref = curResource.getKey();
            int level = Locales.match(locale, type.getLocale());
            if (level == 2) {
                resource = curResource;
                break;
            } else if (level > currentLevel) {
                resource = curResource;
                currentLevel = level;
            }
        }
        String result;
        if (locale == null || resource == null) {
            result = "@" + typeSpec.getName() + "/" + ref;
        } else {
            result = resource.toStringValue(resourceTable, locale);
        }
        return result;
    }

}
