package uk.ac.wlv.percs.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.wlv.percs.lib.Request;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Class matching client's XML request
 *
 * @see Request
 */
public class DemoInvalidRequest extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(DemoInvalidRequest.class);

    public DemoInvalidRequest(){};

    /**
     * This method signalises whether processed XML data corresponds to a request
     * implemented with this class.
     *
     * @param xsr a stream containing XML data
     * @return true if XML string corresponds to the expected request
     * @throws XMLStreamException {@link XMLStreamException}
     */
    public final boolean received(XMLStreamReader xsr) throws XMLStreamException {
        return true;
    }


    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public String getXMLResponse() {
        return "<<poor-request/>/>";
    }

} // end of DemoInvalidRequest:Request
