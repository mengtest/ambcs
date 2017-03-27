package com.github.cezs.ambcs.lib;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* XML */

/* Standard */
import java.util.ArrayList;

/**
 * This abstract class has to be implemented by class providing translation
 * of string xml messages with a parser of choice e.g. SAX, StAX or XPath
 * (i.e., it's task is to convert a given xml string into internal
 * class representation).
 */
abstract class RequestAccumulator {

    private final Logger log = LoggerFactory.getLogger(RequestAccumulator.class);

    // Custom protocol
    Protocol protocol;
    // Received information
    String request;
    // Storing requests for sequential processing
    ArrayList<Request> requests = new ArrayList<Request>();

    /**
     * RequestAccumulator constructor.
     *
     * @param protocol a custom protocol
     */
    public RequestAccumulator(Protocol protocol) {
        this.protocol = protocol;
    }

    public ArrayList<Request> getRequests(String msg) throws Exception {
        processXMLString(msg);
        return requests;
    }

    /**
     * Process XML request
     *
     * @param msg XML data in the form of string literal received form client
     * @throws Exception cf. {@link Exception}
     */
    public abstract void processXMLString(String msg) throws Exception;
}
