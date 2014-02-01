package pl.matisoft.soy.example;

import com.google.common.collect.Lists;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import pl.matisoft.soy.example.ecb.Cube;
import pl.matisoft.soy.example.ecb.Envelope;
import pl.matisoft.soy.example.model.CurrencyEntry;
import pl.matisoft.soy.example.model.CurrencyModel;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.functions.Action1;
import rx.util.functions.Func2;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Controller
public class CurrencyAsyncController4 {

    @RequestMapping(value="/async4")
    public DeferredResult<ModelAndView> openHomepage() throws IOException, JAXBException, InterruptedException, ExecutionException, TimeoutException {
        final CloseableHttpAsyncClient httpClient = HttpAsyncClients.createDefault();
        httpClient.start();
        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();

        final Func2<Envelope, Map<String,Map>, CurrencyModel> combine = new Func2<Envelope, Map<String,Map>, CurrencyModel>() {

            @Override
            public CurrencyModel call(final Envelope e, final Map<String,Map> entryMap) {
                final List<CurrencyEntry> currencyEntryList = Lists.newArrayList();
                if (!e.getCube().getCubes().isEmpty()) {
                    for (final Cube cube : e.getCube().getCubes().iterator().next().getCubes()) {
                        final String symbol = cube.getCurrency();
                        final Double rate = cube.getRate();
                        final String desc = (String) entryMap.get(symbol).get("name");
                        currencyEntryList.add(new CurrencyEntry(symbol, desc, rate));
                    }
                }

                return new CurrencyModel(currencyEntryList);
            }

        };

        final Observable<CurrencyModel> currencyModelObs = Observable.zip(CurrencyRates.currencyRates(httpClient), CurrencyNames.currencyNames(httpClient), combine);

        currencyModelObs.subscribe(
                new Action1<CurrencyModel>() {
                    @Override
                    public void call(CurrencyModel currencyModel) {
                        result.setResult(new ModelAndView("soy:soy.example.index", "model", currencyModel));
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        System.out.println("error:" + t.getMessage());
                        result.setErrorResult(t);
                    }
                }, Schedulers.io());

        return result;
    }

}
