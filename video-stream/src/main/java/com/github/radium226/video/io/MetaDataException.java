package com.github.radium226.video.io;

import java.io.IOException;

public class MetaDataException extends IOException {

    public MetaDataException(String message) {
        super(message);
    }

    public MetaDataException(Throwable cause) {
        super(cause);
    }

    public MetaDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
