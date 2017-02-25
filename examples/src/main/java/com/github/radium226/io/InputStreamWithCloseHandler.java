package com.github.radium226.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/30/17.
 */
public class InputStreamWithCloseHandler extends FilterInputStream {

    @FunctionalInterface
    public static interface CloseHandler {

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

}
