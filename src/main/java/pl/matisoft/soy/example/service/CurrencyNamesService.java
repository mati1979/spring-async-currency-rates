package pl.matisoft.soy.example.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.matisoft.soy.example.ObservableTransformers;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.util.functions.Func1;

import java.util.Map;

/**
 * Created by mati on 01/02/2014.
 */
@Service
public class CurrencyNamesService {

    private final static Gson gson = new GsonBuilder().create();

    @Autowired
    private HttpAsyncClient httpAsyncClient;

    public Observable<Map<String,Map>> currencyNames() {
        return ObservableHttp.createGet("http://www.localeplanet.com/api/auto/currencymap.json?name=Y", httpAsyncClient).toObservable()
                .flatMap(ObservableTransformers.responseToString())
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

                })
                .onErrorReturn(new Func1<Throwable, Map<String, Map>>() {
                    @Override
                    public Map<String, Map> call(Throwable throwable) {
                        return Maps.newHashMap();
                    }
                });


    }

    private static Map<String,Map> parseCurrencyNames(final String json) {
        return gson.fromJson(json, Map.class);
    }

}
