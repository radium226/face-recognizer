package com.github.radium226;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by adrien on 1/26/17.
 */
public class YouTube {

    // https://www.youtube.com/watch?v=JIoDqXnc5zM
    public static InputStream open(URL url) throws IOException {
        ProcessBuilder youtubeProcessBuilder = new ProcessBuilder("youtube-dl", url.toString(), "-o", "-");
        youtubeProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        youtubeProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process youtubeProcess = youtubeProcessBuilder.start();

        return youtubeProcess.getInputStream();
    }


}
