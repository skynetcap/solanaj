package org.p2p.solanaj.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils {

    public static Byte guardedShift(List<Byte> byteArray) {
        if (byteArray.isEmpty()) {
            throw new IllegalArgumentException("Byte array length is 0");
        }
        return byteArray.remove(0);
    }

    /**
     * @param byteList array source
     * @param start array start index
     * @param deleteCount fetch count
     * @param items
     * @return
     */
    public static byte[] guardedSplice(
            List<Byte> byteList,
            Integer start,
            Integer deleteCount,
            byte... items) {
        List<Byte> removedItems;
        if (deleteCount != null) {
            if (start + deleteCount > byteList.size()) {
                throw new Error("Reached end of bytes");
            }
            removedItems = new ArrayList<>(byteList.subList(start, start + deleteCount));
            byteList.subList(start, start + deleteCount).clear();
        } else {
            if (start > byteList.size()) {
                throw new Error("Reached end of bytes");
            }
            removedItems = Collections.emptyList();
        }
        List<Byte> itemsToAdd = ByteUtils.toByteList(items);
        byteList.addAll(start, itemsToAdd);

        if (!removedItems.isEmpty()) {
            return ByteUtils.toByteArray(removedItems);
        }
        return ByteUtils.emptyByteArray();
    }
}
