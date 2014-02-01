package pl.matisoft.soy.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.util.functions.Func1;

import java.util.Map;

/**
 * Created by mati on 01/02/2014.
 */
public class CurrencyNames {

    private final static Gson gson = new GsonBuilder().create();

    public static Observable<Map<String,Map>> currencyNames(final HttpAsyncClient httpClient) {
        return ObservableHttp.createGet("http://www.localeplanet.com/api/auto/currencymap.json?name=Y", httpClient).toObservable()
                .flatMap(ObservableFuncs.responseToString())
                .flatMap(new Func1<String, Observable<Map<String, Map>>>() {

                    @Override
                    public Observable<Map<String, Map>> call(final String json) {
                        try {
                            System.out.println("names");
                            return Observable.from(parseCurrencyNames(json));
                        } catch (final Exception e) {
                            return Observable.error(e);
                        }
                    }

                });

    }

    private static Map<String,Map> parseCurrencyNames(final String json) {
        return gson.fromJson(json, Map.class);
    }

}
