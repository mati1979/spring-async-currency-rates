package pl.matisoft.soy.example;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import pl.matisoft.soy.example.ecb.Cube;
import pl.matisoft.soy.example.ecb.Envelope;
import pl.matisoft.soy.example.model.CurrencyEntry;
import pl.matisoft.soy.example.model.CurrencyModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Controller
public class CurrencyAsyncController2 {

    private final static Gson gson = new GsonBuilder().create();

    private ExecutorService executorService = new ForkJoinPool();

    @RequestMapping(value="/async2")
    public DeferredResult<ModelAndView> openHomepage() throws IOException, JAXBException, InterruptedException, ExecutionException, TimeoutException {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();

        final long time1 = System.currentTimeMillis();

        final CountDownLatch countDownLatch = new CountDownLatch(2);

        final ListenableFuture<Envelope> rates = asyncHttpClient.prepareGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").execute(new AsyncCompletionHandler<Envelope>() {

            @Override
            public Envelope onCompleted(final Response response) throws Exception {
                System.out.println("rates in " + (System.currentTimeMillis() - time1) + " ms");
                countDownLatch.countDown();
                return parseCurrencyRates(response.getResponseBody());
            }

            @Override
            public void onThrowable(final Throwable t) {
                countDownLatch.countDown();
                result.setErrorResult(t);
            }

        });

        final ListenableFuture<Map<String,Map>> names = asyncHttpClient.prepareGet("http://www.localeplanet.com/api/auto/currencymap.json?name=Y").execute(new AsyncCompletionHandler<Map<String,Map>>() {

            @Override
            public Map<String,Map> onCompleted(final Response response) throws Exception {
                System.out.println("names in " + (System.currentTimeMillis() - time1) + " ms");
                return parseCurrencyNames(response.getResponseBody());
            }

            @Override
            public void onThrowable(final Throwable t) {
                countDownLatch.countDown();
                result.setErrorResult(t);
            }

        });

        rates.addListener(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        }, executorService);

        names.addListener(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        }, executorService);

        countDownLatch.await(15, TimeUnit.SECONDS);
        System.out.println("all:" + (System.currentTimeMillis() - time1) + " ms");
        if (!result.hasResult()) {
            processRates(rates.get(1, TimeUnit.SECONDS), names.get(1, TimeUnit.SECONDS), result);
        }

        return result;
    }

    private void processRates(final Envelope envelope, Map<String,Map> entryMap, final DeferredResult<ModelAndView> result) throws IOException, JAXBException {
        final List<CurrencyEntry> currencyEntryList = Lists.newArrayList();
        if (!envelope.getCube().getCubes().isEmpty()) {
            for (final Cube cube : envelope.getCube().getCubes().iterator().next().getCubes()) {
                final String symbol = cube.getCurrency();
                final Double rate = cube.getRate();
                final String desc = (String) entryMap.get(symbol).get("name");
                currencyEntryList.add(new CurrencyEntry(symbol, desc, rate));
            }
        }

        result.setResult(new ModelAndView("soy:soy.example.index", "model", new CurrencyModel(currencyEntryList)));
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
