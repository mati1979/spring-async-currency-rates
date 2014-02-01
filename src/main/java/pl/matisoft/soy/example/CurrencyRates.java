package pl.matisoft.soy.example;

import org.apache.http.nio.client.HttpAsyncClient;
import pl.matisoft.soy.example.ecb.Envelope;
import rx.Observable;
import rx.apache.http.ObservableHttp;
import rx.util.functions.Func1;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

/**
 * Created by mati on 01/02/2014.
 */
public class CurrencyRates {

    public static Observable<Envelope> currencyRates(final HttpAsyncClient httpClient) {
        return ObservableHttp.createGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", httpClient).toObservable()
                .flatMap(ObservableFuncs.responseToString())
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
    }

    private static Envelope parseCurrencyRates(final String xml) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
        final Envelope envelope = (Envelope) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xml));

        return envelope;
    }

}
