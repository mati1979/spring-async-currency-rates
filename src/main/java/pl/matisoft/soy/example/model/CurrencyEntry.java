package pl.matisoft.soy.example.model;

/**
 * Created by mati on 01/02/2014.
 */
public class CurrencyEntry {

    private String symbol;
    private String desc;
    private Double rate;

    public CurrencyEntry(String symbol, String desc, Double rate) {
        this.symbol = symbol;
        this.desc = desc;
        this.rate = rate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

}
