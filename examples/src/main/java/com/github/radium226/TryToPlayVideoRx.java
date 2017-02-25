package com.github.radium226;

import com.google.common.io.Closeables;
import com.google.common.util.concurrent.RateLimiter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.opencv.core.*;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.io.DataInputStream;

public class TryToPlayVideoRx extends Application {

    final public static String PALMASHOW_URL = "https://www.youtube.com/watch?v=6Jyes8Hzwn4";

    public void start(Stage stage) throws Exception {
        System.load("/usr/share/opencv/java/libopencv_java310.so");

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

        RateLimiter rateLimiter = RateLimiter.create(24 * 3);

        DataInputStream dataInputStream = new DataInputStream(YouTube.open(PALMASHOW_URL, width, height));
        BGRPixelsAsByteArrayObservable.from(dataInputStream, width, height)
                .doOnCompleted(() -> Closeables.closeQuietly(dataInputStream))
                .map((bgrPixelsAsByteArray) -> bgrPixelsAsByteArrayToMat(bgrPixelsAsByteArray, height, width))
                /*.map((mat) -> {
                    Imgproc.rectangle(mat, new Point(0, 0), new Point(200, 100), new Scalar(255, 255, 255));
                    return mat;
                })*/
                .lift(new DetectFaces())
                .map((mat) -> {
                    byte[] byteArray = new byte[width * height * 3];
                    mat.get(0, 0, byteArray);
                    return byteArray;
                })
                .map((bgrPixelsAsByteArray) -> bgrPixelsAsByteArrayToARGBPixelsAsIntArray(bgrPixelsAsByteArray))
                .map((argbPixelsAsIntArray) -> argbPixelsAsIntArrayToImage(argbPixelsAsIntArray, width, height))
                .subscribeOn(Schedulers.io())
            .forEach(limitRate(rateLimiter, (image) -> imageView.setImage(image)));
            //.forEach(());
    }

    public static int bgrPixelAsByteArrayToARGBPixelAsInt(byte[] bgrByteArray, int offset) {
        int b = bgrByteArray[offset + 0] & 255;
        int g = bgrByteArray[offset + 1] & 255;
        int r = bgrByteArray[offset + 2] & 255;
        int a = 255 & 255;
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static Image argbPixelsAsIntArrayToImage(int[] argbPixelsAsIntArray, int width, int height) {
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, width, height, PixelFormat.getIntArgbInstance(), argbPixelsAsIntArray, 0, width);
        return writableImage;
    }

    public static int[] bgrPixelsAsByteArrayToARGBPixelsAsIntArray(byte[] bgrPixelsAsByteArray) {
        int[] argbPixelsAsIntArray = new int[bgrPixelsAsByteArray.length / 3];
        bgrPixelsAsByteArrayToARGBPixelsAsIntArray(bgrPixelsAsByteArray, argbPixelsAsIntArray);
        return argbPixelsAsIntArray;
    }

    public static void bgrPixelsAsByteArrayToARGBPixelsAsIntArray(byte[] bgrPixelsAsByteArray, int[] argbPixelsAsIntArray) {
        int channelCount = 3;
        int argbPixelsAsIntArraySize = argbPixelsAsIntArray.length;
        for (int i = 0; i < argbPixelsAsIntArraySize; i++) {
            argbPixelsAsIntArray[i] = bgrPixelAsByteArrayToARGBPixelAsInt(bgrPixelsAsByteArray, i * channelCount);
        }
    }

    public static <T> Action1<T> limitRate(RateLimiter rateLimiter, Action1<T> action) {
        return new Action1<T>() {

            @Override
            public void call(T t) {
                rateLimiter.acquire();
                action.call(t);
            }

        };
    }

    public static Mat bgrPixelsAsByteArrayToMat(byte[] bgrPixelsAsByteArray, int width, int height) {
        Mat mat = new Mat(width, height, CvType.CV_8UC3);
        mat.put(0, 0, bgrPixelsAsByteArray);
        return mat;
    }

    public static void main(String[] arguments) {
        launch(arguments);
    }

}
