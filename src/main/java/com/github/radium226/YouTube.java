package com.github.radium226;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/26/17.
 */
public class YouTube {

    // https://www.youtube.com/watch?v=JIoDqXnc5zM
    public static InputStream open(String url, int width, int height) throws IOException {
        //ProcessBuilder downloadProcessBuilder = new ProcessBuilder("youtube-dl", url, "-o", "-");
        ProcessBuilder downloadProcessBuilder = new ProcessBuilder("cat", "/home/adrien/Personal/Projects/marvin-example/src/main/resources/palmashow.webm");
        ProcessBuilder conversionProcessBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", "-",
                "-f", "image2pipe",
                "-s", 200 + "x" + 100,
                "-pix_fmt", "bgr24",
                "-vcodec", "rawvideo",
                "-"
            );

        downloadProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        downloadProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        conversionProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process downloadProcess = downloadProcessBuilder.start();
        Process conversionProcess = conversionProcessBuilder.start();
        Processes.pipe(downloadProcess, conversionProcess);

        return conversionProcess.getInputStream();
    }


    public static InputStream rawOpen(String url) throws IOException {
        ProcessBuilder youtubeDLProcessBuilder = new ProcessBuilder("cat", "/home/adrien/Personal/Projects/marvin-example/src/main/resources/palmashow.webm");
        //ProcessBuilder youtubeDLProcessBuilder = new ProcessBuilder("youtube-dl", url, "-o", "-");

        youtubeDLProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
        youtubeDLProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process youtubeDLProcess = youtubeDLProcessBuilder.start();

        return youtubeDLProcess.getInputStream();
    }

}
