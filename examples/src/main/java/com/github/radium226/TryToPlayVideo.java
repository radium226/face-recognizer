package com.github.radium226;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TryToPlayVideo extends Application {

    final public static String PALMASHOW_URL = "https://www.youtube.com/watch?v=6Jyes8Hzwn4";

    public void start(Stage stage) throws Exception {
        int width = 500;
        int height = 300;

        WritableImage writableImage = new WritableImage(width, height);
        ImageView imageView = new ImageView(writableImage);

        Pane pane = new Pane(imageView);
        Scene scene = new Scene(pane);

        stage.setScene(scene);
        stage.setMinWidth(writableImage.getWidth());
        stage.setMinHeight(writableImage.getHeight());
        stage.show();


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (DataInputStream bgrDataInputStream = new DataInputStream(YouTube.open(PALMASHOW_URL, width, height))) {
                Tick.every(TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / 24, () -> {
                    Image image = readImage(bgrDataInputStream, width, height);
                    if (image == null) {
                        return false;
                    }

                    imageView.setImage(image);
                    return true;
                });
            }
            return null;
        });
    }

    public static int bgrPixelAsByteArrayToARGBPixelAsInt(byte[] bgrByteArray, int offset) {
        int b = bgrByteArray[offset + 0] & 255;
        int g = bgrByteArray[offset + 1] & 255;
        int r = bgrByteArray[offset + 2] & 255;
        int a = 255 & 255;
        return a << 24 | r << 16 | g << 8 | b;
    }


    public static boolean readImageAsBGRPixelsByteArray(DataInputStream dataInputStream, byte[] byteArray) throws IOException {
        int byteArraySize = byteArray.length;
        /*if (inputStream.read(byteArray) < byteArraySize) { // We stop here
            return false;
        }*/
        try {
            dataInputStream.readFully(byteArray);
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    public static void bgrPixelsAsByteArrayToARGBPixelsAsIntArray(byte[] bgrPixelsAsByteArray, int[] argbPixelsAsIntArray) {
        int channelCount = 3;
        int argbPixelsAsIntArraySize = argbPixelsAsIntArray.length;
        for (int i = 0; i < argbPixelsAsIntArraySize; i++) {
            argbPixelsAsIntArray[i] = bgrPixelAsByteArrayToARGBPixelAsInt(bgrPixelsAsByteArray, i * channelCount);
        }
    }

    public static Image readImage(DataInputStream dataInputStream, int width, int height) throws IOException {
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        int channelCount = 3;
        int bgrPixelsAsByteArraySize = width * height * channelCount;
        byte[] bgrPixelsAsByteArray = new byte[bgrPixelsAsByteArraySize];

        int argbPixelsAsIntArraySize = width * height;
        int[] argbPixelsAsIntArray = new int[argbPixelsAsIntArraySize];

        if(readImageAsBGRPixelsByteArray(dataInputStream, bgrPixelsAsByteArray)) {
            bgrPixelsAsByteArrayToARGBPixelsAsIntArray(bgrPixelsAsByteArray, argbPixelsAsIntArray);
            pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argbPixelsAsIntArray, 0, width);
        } else {
            return null;
        }
        return writableImage;
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }

}
