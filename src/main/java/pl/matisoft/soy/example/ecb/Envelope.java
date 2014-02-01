package pl.matisoft.soy.example.ecb;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Envelope", namespace=EcbNamespaces.GESMES)
public class Envelope {

    @XmlElement(name="Cube", namespace=EcbNamespaces.ECB)
    private Cube cube;

    public Cube getCube() {
        return cube;
    }

}
