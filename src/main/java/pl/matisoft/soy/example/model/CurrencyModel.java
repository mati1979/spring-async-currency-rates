package pl.matisoft.soy.example.model;

import java.util.List;

/**
 * Created by mati on 01/02/2014.
 */
public class CurrencyModel {

    private List<CurrencyEntry> entryList;

    public CurrencyModel(List<CurrencyEntry> entryList) {
        this.entryList = entryList;
    }

    public List<CurrencyEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<CurrencyEntry> entryList) {
        this.entryList = entryList;
    }

}
