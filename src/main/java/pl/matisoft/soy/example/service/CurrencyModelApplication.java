package pl.matisoft.soy.example.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.matisoft.soy.example.ecb.Cube;
import pl.matisoft.soy.example.ecb.Envelope;
import pl.matisoft.soy.example.model.CurrencyEntry;
import pl.matisoft.soy.example.model.CurrencyModel;
import rx.Observable;
import rx.util.functions.Func2;

import java.util.List;
import java.util.Map;

/**
 * Created by mati on 02/02/2014.
 */
@Service
public class CurrencyModelApplication {

    @Autowired
    private CurrencyNamesService currencyNames;

    @Autowired
    private CurrencyRatesService currencyRatesService;

    public Observable<CurrencyModel> currencyModel() {
        final Observable<Envelope> currencyRatesObs = currencyRatesService.currencyRates();
        final Observable<Map<String,Map>> currencyNamesObs = currencyNames.currencyNames();

        return Observable.zip(currencyRatesObs, currencyNamesObs, combine());
    }

    private Func2<Envelope, Map<String,Map>, CurrencyModel> combine() {
        return new Func2<Envelope, Map<String,Map>, CurrencyModel>() {

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
    }

}
