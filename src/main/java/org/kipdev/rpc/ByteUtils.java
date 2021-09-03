package org.kipdev.rpc;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;

public class ByteUtils {

    public static void putInt(int x, ByteArrayOutputStream out) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            out.write((byte) x);
            out.write((byte) (x >> 8));
            out.write((byte) (x >> 16));
            out.write((byte) (x >> 24));
        } else {
            out.write((byte) (x >> 24));
            out.write((byte) (x >> 16));
            out.write((byte) (x >> 8));
            out.write((byte) x);
        }
    }
}
