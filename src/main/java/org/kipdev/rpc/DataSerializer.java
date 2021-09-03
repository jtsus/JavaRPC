package org.kipdev.rpc;

public interface DataSerializer {
    <T> T parse(byte[] data);

    <T> byte[] write(T data);
}
