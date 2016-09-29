package uk.ac.wlv.percs.lib;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Each request in the form of XML is translated to a class implementing
 * this interface.
 */
public abstract class Request {

    /**
     * This method signalises whether processed XML data/request
     * corresponds to a class implementing this interface.
     *
     * @param xsr a stream containing XML data
     * @return true if request corresponds to an interface implementation
     * @throws XMLStreamException {@link XMLStreamException}
     */
    public abstract boolean received(XMLStreamReader xsr) throws XMLStreamException;

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public abstract String getXMLRequest();

} // end of Request{}
