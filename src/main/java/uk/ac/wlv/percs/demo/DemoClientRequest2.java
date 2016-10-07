package uk.ac.wlv.percs.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.wlv.percs.lib.Request;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Class matching client's XML request
 *
 * @see Request
 */
public class DemoClientRequest2 extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(DemoClientRequest2.class);

    private String id;
    private String tk;

    public DemoClientRequest2(){};

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
                && xsr.getLocalName().equals("response")
                && xsr.getAttributeLocalName(0).equals("id")) {
            id = xsr.getAttributeValue(0);
            xsr.next();
            if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT
                    && xsr.getLocalName().equals("tk")) {
                xsr.next();
                if (xsr.getEventType() == XMLStreamConstants.CHARACTERS) {
                    tk = xsr.getText();
                    xsr.next();
                    if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT
                            && xsr.getLocalName().equals("tk")) {
                        xsr.next();
                        if (xsr.getEventType() == XMLStreamConstants.END_ELEMENT
                                && xsr.getLocalName().equals("response")) {
                            ans = true;
                        }
                    }
                }
            }
        }
        return ans;
    } // End of received()

    /**
     * This method returns id included with {@code id} attribute
     *
     * @return id
     */
    public final String getId() {
        return id;
    }

    /**
     * This method returns request token contained in {@code <tk/>} tag
     *
     * @return token
     */
    public final String getTk() {
        return tk;
    }

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public final String getXMLResponse() {
        return "<response id=\"" + id + "\">" + "<tk>" + tk + "</tk></response>";
    }

} // end of DemoClientRequest2:Request
