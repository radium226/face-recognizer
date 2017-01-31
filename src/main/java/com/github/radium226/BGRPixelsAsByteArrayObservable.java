package com.github.radium226;

import com.github.davidmoten.rx.Bytes;
import rx.Observable;
import rx.Subscriber;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/26/17.
 */
public class BGRPixelsAsByteArrayObservable {

    public static Observable<byte[]> from(DataInputStream dataInputStream, int width, int height) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {

            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                subscriber.onStart();
                while (true) {
                    int bgrPixelsAsByteArraySize = width * height * 3;
                    byte[] bgrPixelsAsByteArray = new byte[bgrPixelsAsByteArraySize];
                    try {
                        dataInputStream.readFully(bgrPixelsAsByteArray);
                        subscriber.onNext(bgrPixelsAsByteArray);
                    } catch (EOFException e) {
                        subscriber.onCompleted();
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
            }

        });
    }


}
