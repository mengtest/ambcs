package com.github.cezs.ambcs.formats;

import com.github.cezs.ambcs.lib.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Class matching client's XML request
 *
 * @see Request
 */
public class RequestElem extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(RequestElem.class);
    private String msg = "";

    public RequestElem(String message) {
        this.msg = message;
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
        if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT) {
            if (xsr.getLocalName().equals(this.msg)) {
                xsr.next();
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
    public final String getXML() {
        return this.msg;
    }

} // end of DemoClientRequest1:Request
