package com.github.radium226.commons.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/30/17.
 */
public class InputStreamWithCloseHandler extends FilterInputStream {

    @FunctionalInterface
    public interface CloseHandler {

        void handleClose(InputStream inputStream) throws IOException;

    }

    private CloseHandler closeHandler;

    public InputStreamWithCloseHandler(InputStream inputStream, CloseHandler closeHandler) {
        super(inputStream);

        this.closeHandler = closeHandler;
    }

    @Override
    public void close() throws IOException {
        closeHandler.handleClose(in);
    }

    public int read() throws IOException {
        int i = in.read();
        if (i < 0) {
            closeHandler.handleClose(in);
        }
        return i;
    }

    public int read(byte[] bytes) throws IOException {
        int i = in.read(bytes);
        if (i < 0) {
            closeHandler.handleClose(in);
        }
        return i;
    }

    public int read(byte[] bytes, int i, int i1) throws IOException {
        int s = in.read(bytes, i, i1);
        if (s < 0) {
            closeHandler.handleClose(in);
        }
        return s;
    }

    public long skip(long l) throws IOException {
        return in.skip(l);
    }

    public int available() throws IOException {
        int s = in.available();
        if (s < 0) {
            closeHandler.handleClose(in);
        }
        return s;
    }

    public void mark(int i) {
        in.mark(i);
    }

    public void reset() throws IOException {
        in.reset();
    }

    public boolean markSupported() {
        return in.markSupported();
    }

}
