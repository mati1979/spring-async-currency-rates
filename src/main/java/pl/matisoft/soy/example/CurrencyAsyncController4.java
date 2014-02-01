package pl.matisoft.soy.example;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import pl.matisoft.soy.example.ecb.Cube;
import pl.matisoft.soy.example.ecb.Envelope;
import pl.matisoft.soy.example.model.CurrencyEntry;
import pl.matisoft.soy.example.model.CurrencyModel;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.apache.http.ObservableHttpResponse;
import rx.schedulers.Schedulers;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class CurrencyAsyncController4 {

    private final static Gson gson = new GsonBuilder().create();

    @RequestMapping(value="/async4")
    public DeferredResult<ModelAndView> openHomepage() throws IOException, JAXBException, InterruptedException, ExecutionException, TimeoutException {
        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();

        final Observable<Envelope> ratesObs = ObservableHttp.createGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", httpClient).toObservable()
                .flatMap(new Func1<ObservableHttpResponse, Observable<String>>() {

                    @Override
                    public Observable<String> call(final ObservableHttpResponse response) {
                        return response.getContent().map(new Func1<byte[], String>() {
                            @Override
                            public String call(final byte[] bytes) {
                                return new String(bytes);
                            }
                        });
                    }
                })
                .flatMap(new Func1<String, Observable<Envelope>>() {

                    @Override
                    public Observable<Envelope> call(final String xml) {
                        try {
                            System.out.println("rates");
                            return Observable.from(parseCurrencyRates(xml));
                        } catch (final Exception e) {
                            return Observable.error(e);
                        }
                    }

                });

        final Observable<Map<String,Map>> namesObs = ObservableHttp.createGet("http://www.localeplanet.com/api/auto/currencymap.json?name=Y", httpClient).toObservable()
                .flatMap(new Func1<ObservableHttpResponse, Observable<String>>() {

                    @Override
                    public Observable<String> call(final ObservableHttpResponse response) {
                        return response.getContent().map(new Func1<byte[], String>() {
                            @Override
                            public String call(final byte[] bytes) {
                                return new String(bytes);
                            }
                        });
                    }
                })
                .flatMap(new Func1<String, Observable<Map<String,Map>>>() {

                    @Override
                    public Observable<Map<String,Map>> call(final String json) {
                        try {
                            System.out.println("names");
                            return Observable.from(parseCurrencyNames(json));
                        } catch (final Exception e) {
                            return Observable.error(e);
                        }
                    }

                });

        final Func2<Envelope, Map<String,Map>, CurrencyModel> combine = new Func2<Envelope, Map<String,Map>, CurrencyModel>() {

            @Override
            public CurrencyModel call(final Envelope e, final Map<String,Map> entryMap) {
                try {
                    return processRates(e, entryMap);
                } catch (IOException e1) {
                    return null;
                } catch (JAXBException e1) {
                    return null;
                }
            }

        };

        final rx.Observer<CurrencyModel> observer = new rx.Observer<CurrencyModel>() {

            @Override
            public void onCompleted() {
                System.out.println("On completed");
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("error");
                result.setErrorResult(t);
            }

            @Override
            public void onNext(final CurrencyModel currencyModel) {
                System.out.println("CurrencyModel:" + currencyModel);
                if (currencyModel == null) {
                    result.setErrorResult("unknown error");
                } else {
                    result.setResult(new ModelAndView("soy:soy.example.index", "model", currencyModel));
                }
            }

        };

        final Observable<CurrencyModel> os = Observable.zip(ratesObs, namesObs, combine);
        os.subscribe(observer, Schedulers.io());

        return result;
    }

    private CurrencyModel processRates(final Envelope envelope, final Map<String,Map> entryMap) throws IOException, JAXBException {
        final List<CurrencyEntry> currencyEntryList = Lists.newArrayList();
        if (!envelope.getCube().getCubes().isEmpty()) {
            for (final Cube cube : envelope.getCube().getCubes().iterator().next().getCubes()) {
                final String symbol = cube.getCurrency();
                final Double rate = cube.getRate();
                final String desc = (String) entryMap.get(symbol).get("name");
                currencyEntryList.add(new CurrencyEntry(symbol, desc, rate));
            }
        }

        return new CurrencyModel(currencyEntryList);
    }

    private Envelope parseCurrencyRates(final String xml) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
        final Envelope envelope = (Envelope) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xml));

        return envelope;
    }

    private Map<String,Map> parseCurrencyNames(final String json) {
        return gson.fromJson(json, Map.class);
    }

}
