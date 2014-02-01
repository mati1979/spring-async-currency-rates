package pl.matisoft.soy.example.ecb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class Cube implements Comparable<Cube> {
	
	@XmlElement(name="Cube", namespace=EcbNamespaces.ECB)
	private List<Cube> cubes;
	
	@XmlAttribute(name="currency")
	private String currency;
	
	@XmlAttribute(name="rate")
	private Double rate;
	
	@XmlAttribute(name="time")
	private String time;
	
	
	public List<Cube> getCubes() {
		return cubes;
	}
	
	public Double getRate() {
		return rate;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public String getTime() {
		return time;
	}

	@Override
	public int compareTo(Cube o) {
		if (time != null && !time.isEmpty() && o.time != null && !o.time.isEmpty()) {
			return o.time.compareTo(this.time);
		}

		return 0;
	}

	@Override
	public String toString() {
		return "Cube [cubes=" + cubes + ", currency=" + currency + ", rate="
				+ rate + ", time=" + time + "]";
	}

}
