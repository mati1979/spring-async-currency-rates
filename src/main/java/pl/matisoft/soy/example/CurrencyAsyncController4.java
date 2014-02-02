package pl.matisoft.soy.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.ModelAndView;
import pl.matisoft.soy.example.model.CurrencyModel;
import pl.matisoft.soy.example.service.CurrencyModelApplication;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.functions.Action1;

@Controller
public class CurrencyAsyncController4 {

    @Autowired
    private CurrencyModelApplication currencyModelApplication;

    @RequestMapping(value="/async4")
    public DeferredResult<ModelAndView> openHomepage() {
        final DeferredResult<ModelAndView> result = new DeferredResult<ModelAndView>();
        final ModelAndView mav = new ModelAndView("soy:soy.example.index");

        final Observable<CurrencyModel> currencyModelObs = currencyModelApplication.currencyModel();

        currencyModelObs.subscribe(
                new Action1<CurrencyModel>() {
                    @Override
                    public void call(final CurrencyModel currencyModel) {
                        mav.addObject("model", currencyModel);
                        result.setResult(mav);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(final Throwable t) {
                        result.setErrorResult(t);
                    }
                }, Schedulers.io());

        return result;
    }

}
