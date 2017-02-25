package com.github.radium226.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by adrien on 1/30/17.
 */
public class ByteArrayIterator implements Iterator<byte[]> {

    final public static Logger LOGGER = LoggerFactory.getLogger(ByteArrayIterator.class);

    private InputStream inputStream;
    private int byteArraySize;
    private byte[] nextByteArray = null;

    private ByteArrayIterator(InputStream inputStream, int byteArraySize) {
        super();

        this.inputStream = inputStream;
        this.byteArraySize = byteArraySize;
    }


    public static ByteArrayIterator fromInputStream(InputStream inputStream, int byteArraySize) {
        LOGGER.debug("Creating ByteArrayIterator");
        return new ByteArrayIterator(inputStream, byteArraySize);
    }

    @Override
    public boolean hasNext() {
        try {
            byte[] byteArray = new byte[byteArraySize];
            int readByteTotalCount = 0;
            while (readByteTotalCount < byteArraySize) {
                int readByteCount = inputStream.read(nextByteArray, readByteTotalCount, byteArraySize - readByteTotalCount);
                LOGGER.trace("readByteCount={}", readByteCount);
                if (readByteCount < 0) {
                    break;
                }
                readByteTotalCount += readByteCount;
            }

            if (readByteTotalCount < byteArraySize) {
                LOGGER.warn("It seems that the last byteArray is truncated");
                return false;
            }

            LOGGER.debug("A full byteArray has been filled");
            nextByteArray = byteArray;
            return true;
        } catch (IOException e) {
            LOGGER.error("Something appened while reading", e);
            return false;
        }
    }

    @Override
    public byte[] next() {
        LOGGER.info("Next has been called");
        return nextByteArray;
    }

}
