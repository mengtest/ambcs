package uk.ac.wlv.percs.lib;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* XML */
import javax.xml.stream.*;

/* Standard */
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

class StreamRequestAccumulator extends RequestAccumulator {

    private final Logger log = LoggerFactory.getLogger(StreamRequestAccumulator.class);

    // StAX
    private XMLInputFactory xif;
    private XMLStreamReader xsr;

    public StreamRequestAccumulator(Protocol protocol) {
        super(protocol);
    }

    /**
     * Process XML request
     *
     * @param msg XML data in the form of string literal received form client
     * @throws XMLStreamException cf. {@link XMLStreamException}
     */
    public void processXMLString(String msg) throws Exception {

        // EVENTS:
        // start document: 7
        // end document: 8
        // start elements: 1
        // end element: 2
        // attribute: 10
        // characters: 4
        // space: 6
        //

        // Prepare request: strip request of XML prologs
        request = msg.replace("<?xml version=\"1.0\" ?>", "");

        // Quick hack solving invalid XML constructs used by supplied demo client.
        // TODO: Most likely to be removed in future versions.
        request = request.replace("\\", "");
        request = request.replace("<<Poor request/>/>", "<poor-request/>");

        // Prepare request: wrap request in <root/>
        request = "<root>" + request + "</root>";

        // Prepare XML reading
        xif = XMLInputFactory.newInstance();
        xsr = xif.createXMLStreamReader(new StringReader(request));
        // xsr = xif.createXMLStreamReader(new ByteArrayInputStream(request.getBytes()));
        xsr.next(); // skip XMLStreamConstants.START_DOCUMENT
        xsr.next(); // skip <root> : XMLStreamConstants.START_ELEMENT

        // Access protocol's map keys by number
        List<Request> keys = new ArrayList(protocol.keySet());

        ArrayList<Request> clientRequestsStore = protocol.getClientRequests();

        log.info("Validating input...");

        while (xsr.hasNext()) {
            // log.info("In loop...:for1 Validate, processXMLString");
            if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                // NO ORDER HERE!
                for (int i = 0; i < clientRequestsStore.size(); i++) {
                    // log.info("In loop...:for2 Validate, processXMLString");
                    if (clientRequestsStore.get(i).received(xsr) && (i < (clientRequestsStore.size() - 1))) {
                        requests.add(clientRequestsStore.get(i));
                    } else {
                        requests.add(clientRequestsStore.get(keys.size() - 1));
                        //    xsr.next();
                    }
                }
            } else {
                xsr.next();
            }

        } // end of while loop

        log.info("Finished validating input");

        xsr.close();

    } // end of processXMLString()

}
