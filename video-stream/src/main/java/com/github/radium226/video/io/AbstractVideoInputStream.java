package com.github.radium226.video.io;

import com.github.radium226.video.VideoMetaData;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractVideoInputStream extends InputStream {

    public abstract VideoMetaData getMetaData() throws IOException;

}
