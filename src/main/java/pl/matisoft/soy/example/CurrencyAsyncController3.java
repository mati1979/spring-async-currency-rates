//package pl.matisoft.soy.example;
//
//import com.google.common.collect.Lists;
//import com.google.common.util.concurrent.Futures;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.ning.http.client.AsyncCompletionHandler;
//import com.ning.http.client.AsyncHttpClient;
//import com.ning.http.client.ListenableFuture;
//import com.ning.http.client.Response;
//import com.spotify.trickle.Func0;
//import com.spotify.trickle.Func1;
//import com.spotify.trickle.Func2;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.context.request.async.DeferredResult;
//import org.springframework.web.servlet.ModelAndView;
//import pl.matisoft.soy.example.ecb.Cube;
//import pl.matisoft.soy.example.ecb.Envelope;
//import pl.matisoft.soy.example.model.CurrencyEntry;
//import pl.matisoft.soy.example.model.CurrencyModel;
//
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.JAXBException;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//
//@Controller
//public class CurrencyAsyncController3 {
//
//    private final static Gson gson = new GsonBuilder().create();
//
//    private ExecutorService executorService = new ForkJoinPool();
//
//    @RequestMapping(value="/async3")
//    public DeferredResult<ModelAndView> openHomepage() throws IOException, JAXBException, InterruptedException, ExecutionException, TimeoutException {
//        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();
//
//        final Func0<Envelope> findCurrencyRates = new Func0<Envelope>() {
//
//            @Override
//            public com.google.common.util.concurrent.ListenableFuture<Envelope> run() {
//                try {
//                    asyncHttpClient.prepareGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml").execute(new AsyncCompletionHandler<Envelope>() {
//
//                        @Override
//                        public Envelope onCompleted(final Response response) throws Exception {
//                            return parseCurrencyRates(response.getResponseBody());
//                        }
//
//                        @Override
//                        public void onThrowable(final Throwable t) {
//                            result.setErrorResult(t);
//                            Futures.immediateFailedFuture(t);
//                        }
//
//                    });
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        };
//
//        final Func1<Void, Map<String, Map>> findCurrencyNames = new Func1<Void, Map<String, Map>>() {
//
//            @Override
//            public com.google.common.util.concurrent.ListenableFuture< Map<String, Map>> run(Void aVoid) {
//                return null;
//            }
//
//        };
//
//        final Func2<Envelope, Map<String, Map>, List<CurrencyEntry>> combined = new Func2<Envelope, Map<String, Map>, List<CurrencyEntry>>() {
//
//            @Override
//            public com.google.common.util.concurrent.ListenableFuture<List<CurrencyEntry>> run(final Envelope envelope, final Map<String, Map> entryMap) {
//                final List<CurrencyEntry> currencyEntryList = Lists.newArrayList();
//                if (!envelope.getCube().getCubes().isEmpty()) {
//                    for (final Cube cube : envelope.getCube().getCubes().iterator().next().getCubes()) {
//                        final String symbol = cube.getCurrency();
//                        final Double rate = cube.getRate();
//                        final String desc = (String) entryMap.get(symbol).get("name");
//                        currencyEntryList.add(new CurrencyEntry(symbol, desc, rate));
//                    }
//                }
//
//                return Futures.immediateFuture(currencyEntryList);
//            }
//        };
//
//        return result;
//    }
//
//    private void processRates(final Envelope envelope, Map<String,Map> entryMap, final DeferredResult<ModelAndView> result) throws IOException, JAXBException {
//        final List<CurrencyEntry> currencyEntryList = Lists.newArrayList();
//        if (!envelope.getCube().getCubes().isEmpty()) {
//            for (final Cube cube : envelope.getCube().getCubes().iterator().next().getCubes()) {
//                final String symbol = cube.getCurrency();
//                final Double rate = cube.getRate();
//                final String desc = (String) entryMap.get(symbol).get("name");
//                currencyEntryList.add(new CurrencyEntry(symbol, desc, rate));
//            }
//        }
//
//        result.setResult(new ModelAndView("soy:soy.example.index", "model", new CurrencyModel(currencyEntryList)));
//    }
//
//    private Envelope parseCurrencyRates(final String xml) throws Exception {
//        final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
//        final Envelope envelope = (Envelope) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xml));
//
//        return envelope;
//    }
//
//    private Map<String,Map> parseCurrencyNames(final String json) {
//        return gson.fromJson(json, Map.class);
//    }
//
//}
