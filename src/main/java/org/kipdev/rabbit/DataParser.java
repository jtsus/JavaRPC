package org.kipdev.rabbit;

public interface DataParser {
    <T> T parse(byte[] data);

    <T> byte[] write(T data);
}
