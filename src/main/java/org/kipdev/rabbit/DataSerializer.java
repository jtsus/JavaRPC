package org.kipdev.rabbit;

public interface DataSerializer {
    <T> T parse(byte[] data);

    <T> byte[] write(T data);
}
