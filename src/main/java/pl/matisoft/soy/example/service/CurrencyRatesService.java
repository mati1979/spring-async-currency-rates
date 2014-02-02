package pl.matisoft.soy.example.service;

import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.matisoft.soy.example.ObservableTransformers;
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
@Service
public class CurrencyRatesService {

    @Autowired
    private HttpAsyncClient httpAsyncClient;

    public Observable<Envelope> currencyRates() {
        return ObservableHttp.createGet("http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", httpAsyncClient).toObservable()
                .flatMap(ObservableTransformers.responseToString())
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

                }) //
                .onErrorReturn(new Func1<Throwable, Envelope>() {
                    @Override
                    public Envelope call(Throwable throwable) {
                        return new Envelope();
                    }
                });
    }

    private static Envelope parseCurrencyRates(final String xml) throws JAXBException {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Envelope.class);
        final Envelope envelope = (Envelope) jaxbContext.createUnmarshaller().unmarshal(new StringReader(xml));

        return envelope;
    }

}
