package uk.ac.wlv.percs.lib;

/* XML */
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Each request in the form of XML is translated to a class implementing
 * this interface.
 */
public abstract class Request<ReaderType> {

    /**
     * This method signalises whether processed XML data/request
     * corresponds to a class implementing this interface.
     *
     * @param reader a stream containing XML data
     * @return true if request corresponds to an interface implementation
     * @throws Exception {eg. @link XMLStreamException}
     */
    public abstract boolean received(ReaderType reader) throws Exception;

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public abstract String getXMLResponse();

} // end of Request{}
