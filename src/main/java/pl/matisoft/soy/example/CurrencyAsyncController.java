package pl.matisoft.soy.example;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
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

@Controller
public class CurrencyAsyncController {

    @RequestMapping(value="/async1")
    public DeferredResult<ModelAndView> openHomepage() throws IOException, JAXBException {
        final AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();

        final AsyncHttpClient.BoundRequestBuilder rates = asyncHttpClient.prepareGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
        final AsyncHttpClient.BoundRequestBuilder names = asyncHttpClient.prepareGet("https://gist.github.com/Fluidbyte/2973986/raw/9ead0f85b6ee6071d018564fa5a314a0297212cc/Common-Currency.json");

        rates.execute(new AsyncCompletionHandler<Void>() {

            @Override
            public Void onCompleted(final Response response) throws Exception {
                processRates(names, result, parseCurrencyRates(response.getResponseBody()));
                return null;
            }

            @Override
            public void onThrowable(final Throwable t) {
                result.setErrorResult(t);
            }

        });

        return result;
    }

    private void processRates(final AsyncHttpClient.BoundRequestBuilder names, final DeferredResult<ModelAndView> result, final Envelope envelope) throws IOException {
        names.execute(new AsyncCompletionHandler<Void>() {
            @Override
            public Void onCompleted(final Response response) throws Exception {
                final String json = response.getResponseBody();
                final GsonBuilder gson = new GsonBuilder();
                Map<String,Map> entryMap = gson.create().fromJson(json, Map.class);
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
                return null;
            }

            @Override
            public void onThrowable(final Throwable t) {
                result.setErrorResult(t);
            }
        });
    }

    private Envelope parseCurrencyRates(final String xml) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
        final Envelope envelope = (Envelope) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xml));

        return envelope;
    }

}
