package pl.matisoft.soy.example;

import rx.Observable;
import rx.apache.http.ObservableHttpResponse;
import rx.util.functions.Func1;

/**
 * Created by mati on 01/02/2014.
 */
public class ObservableFuncs {

    public static Func1<ObservableHttpResponse, Observable<String>> responseToString() {
        return new Func1<ObservableHttpResponse, Observable<String>>() {

            @Override
            public Observable<String> call(final ObservableHttpResponse response) {
                return response.getContent().map(new Func1<byte[], String>() {
                    @Override
                    public String call(final byte[] bytes) {
                        return new String(bytes);
                    }
                });
            }
        };
    }

}
