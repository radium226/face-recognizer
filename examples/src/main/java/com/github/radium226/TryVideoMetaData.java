package com.github.radium226;

import com.github.radium226.io.VideoInputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrien on 1/29/17.
 */
public class TryVideoMetaData {

    final public static Path PALMASHOW = Paths.get("/home/adrien/Personal/Projects/marvin-example/src/main/resources/palmashow.webm");

    public static void main(String[] arguments) throws IOException {
        /*byte[] bytes = new byte[1024 * 512];
        InputStream inputStream = Files.newInputStream(PALMASHOW);
        inputStream.read(bytes);
        inputStream.close();

        System.out.println(VideoMetaData.of(bytes));*/


        //try (VideoInputStream youtubeInputStream = new VideoInputStream(YouTube.rawOpen("https://www.youtube.com/watch?v=ZLlhKRL10kk"))) {try (VideoInputStream youtubeInputStream = new VideoInputStream(YouTube.rawOpen("https://www.youtube.com/watch?v=ZLlhKRL10kk"))) {
        try (VideoInputStream youtubeInputStream = new VideoInputStream(Files.newInputStream(PALMASHOW))) {
            VideoMetaData videoMetaData = youtubeInputStream.getMetaData();
            System.out.println(videoMetaData);
        }
    }

}
