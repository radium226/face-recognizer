package com.github.radium226.commons.io.pipe;

import java.io.InputStream;
import java.io.OutputStream;

public interface PipeListener {

    static PipeListener ignore() {
        return new PipeListener() {

        };
    }

    default void outputStreamEnded(OutputStream outputStream) {

    }

    default void inputStreamEnded(InputStream inputStream) {

    }

}
