package com.github.radium226;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by adrien on 1/26/17.
 */
public class DetectFaces implements Observable.Operator<Mat, Mat> {

    private CascadeClassifier cascadeClassifier;

    public DetectFaces() {
        super();

        this.cascadeClassifier = new CascadeClassifier("/usr/share/opencv/haarcascades/haarcascade_frontalface_alt.xml");
    }

    @Override
    public Subscriber<? super Mat> call(Subscriber<? super Mat> subscriber) {
        return new Subscriber<Mat>() {

            private int counter = 0;
            private MatOfRect facesAsMatOfRect = new MatOfRect();

            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(Mat mat) {
                //if (counter % 4 == 0) {
                    cascadeClassifier.detectMultiScale(mat, facesAsMatOfRect);
                    Rect[] facesAsRectArray = facesAsMatOfRect.toArray();
                    for (Rect faceAsRect : facesAsRectArray) {
                        Imgproc.rectangle(mat, faceAsRect.tl(), faceAsRect.br(), new Scalar(255, 255, 255));
                    }
                //}
                subscriber.onNext(mat);
                counter++;
            }
        };
    }
}
