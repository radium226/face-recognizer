package com.github.radium226.commons.io;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * Created by adrien on 1/30/17.
 */
public class InputStreamWithHeader extends FilterInputStream {

    private byte[] headerByteArray;

    public InputStreamWithHeader(InputStream inputStream, byte[] headerByteArray) {
        super(new SequenceInputStream(new ByteArrayInputStream(headerByteArray), inputStream));

        this.headerByteArray = headerByteArray;
    }

    public byte[] getHeader() {
        return headerByteArray;
    }

}
