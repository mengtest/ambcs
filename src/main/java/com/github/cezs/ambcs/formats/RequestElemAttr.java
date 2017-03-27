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
public class RequestElemAttr extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(RequestElemAttr.class);

    private String value;
    private String attribute;
    private ArrayList<String> nodes;

    public RequestElemAttr(String parent, String attribute) {
        this.nodes = new ArrayList<String>(1);
        this.nodes.add(parent);
        this.attribute = attribute;
        this.value = null;
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
                && xsr.getAttributeLocalName(0).equals(this.attribute)) {
            value = xsr.getAttributeValue(0);
            xsr.next();
            if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT
                    && xsr.getLocalName().equals(this.nodes.get(0))) {
                ans = true;
            }
        }
        return ans;
    } // End of received()

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public String getXML() {
        return "<" + this.nodes.get(0) + "\"" + attribute + "=\"" + this.value + "\">" + "</" + "</" + this.nodes.get(0) + ">";
    }

} // end of RequestElemAttr