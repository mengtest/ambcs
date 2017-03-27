package com.github.cezs.ambcs.formats;

import com.github.cezs.ambcs.lib.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;

/**
 * Class matching client's XML request
 *
 * @see Request
 */
public class RequestElemAttrElem extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(RequestElemAttrElem.class);

    private ArrayList<String> values;
    private ArrayList<String> nodes;
    private String attribute;

    public RequestElemAttrElem(String parent, String attribute, String child) {
        this.nodes = new ArrayList<String>(2);
        this.nodes.add(parent);
        this.nodes.add(child);
        this.attribute = attribute;
        this.values = null;
    };

    /**
     * This method signalises whether processed XML data corresponds to a request
     * implemented with this class.
     *
     * @param xsr a stream containing XML data
     * @return true if XML string corresponds to the expected request
     * @throws XMLStreamException {@link XMLStreamException}
     */
    public final boolean received(XMLStreamReader xsr) throws XMLStreamException {
        boolean ans = false;
        if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT
                && xsr.getLocalName().equals(this.nodes.get(0))
                && xsr.getAttributeLocalName(0).equals(attribute)) {
            values.add(xsr.getAttributeValue(0));
            xsr.next();
            if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT
                    && xsr.getLocalName().equals(this.nodes.get(1))) {
                xsr.next();
                if (xsr.getEventType() == XMLStreamConstants.CHARACTERS) {
                    values.add(xsr.getText());
                    xsr.next();
                    if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT
                            && xsr.getLocalName().equals(this.nodes.get(1))) {
                        xsr.next();
                        if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT
                                && xsr.getLocalName().equals(this.nodes.get(0))) {
                            ans = true;
                        }
                    }
                }
            }
        }
        return ans;
    } // End of received()

    /**
     * This method returns first element's attribute value.
     *
     * @return value attribute's value
     */
    public final String getAttrVal() {
        return this.values.get(0);
    }

    /**
     * This method returns string surrounded by second element
     *
     * @return token string surrounded by second element
     */
    public final String getElemVal() {
        return this.values.get(1);
    }

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public final String getXML() {
        return "<" + this.nodes.get(0) + "\"" + attribute + "=\"" + values.get(0) + "\">" + "<" + this.nodes.get(1) + ">" + values.get(1) + "</" + this.nodes.get(1) + ">" + "</" + this.nodes.get(0) + ">";
    }

} // end of RequestElemAttrElem
