package uk.ac.wlv.percs.lib;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* XML */
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/* Standard */
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class validating stream according to supplied protocol's request/response pairs.
 */
class Validator {

    private final Logger log = LoggerFactory.getLogger(Validator.class);

    // Custom protocol
    private Protocol protocol;
    // Custom port number
    private int port;
    // StAX
    private XMLInputFactory xif;
    private XMLStreamReader xsr;
    // Received information
    private String request;
    // Storing state and responses for sequential processing
    private int state;
    private ArrayList<Request> requests = new ArrayList<Request>();
    // Storing record of transactions
    private Logfile logfile;
    private String nameOfLogfile;

//    public Validator(Protocol service, int port, Logfile logfile) {
//        this.protocol = service;
//        this.port = port;
//        this.logfile = logfile;
//    }

    public Validator(Protocol service, int port, String nameOfLogfile) {
        this.protocol = service;
        this.port = port;
        this.nameOfLogfile = nameOfLogfile;
    }

    /**
     * Use 'protocol' hashmap for responding
     *
     * @param message {@link Request}
     * @return corresponding string literal using 'protocol' map
     */
    private String respond(Request message) {
        String ans = "";
        Response r = (Response) protocol.get(message.getClass());
        if (r != null) {
            ans = r.getXMLResponse(message);
        }
        return ans;
    }

    /**
     * This method provides stateful validation of client input and prints
     * custom log.
     *
     * @param msg XML request from client
     */
    public ArrayList<String> validate(String msg) {
        ArrayList<String> responses = null;
        try {
            logfile = new Logfile(nameOfLogfile);
            // String for output stream
            String response = null;
            responses = new ArrayList();

            // Process client's input first!
            processXMLString(msg);

            // Access our HashMap keys by number
            List keys = new ArrayList(protocol.keySet());

            // State handler and logger. Note that we impose order here
            if (!requests.isEmpty()) {
                for (Request message : requests) {
                    // log.info("In loop...:for 1 Validator, validate()");
                    for (int i = 0; i < keys.size(); i++) {
                        // log.info("In loop...:for 2 Validator, validate()");
                        if ((state == i) && (message.getClass().equals(keys.get(i)))) {
                            state = (state + 1) % (keys.size() - 1); // -1 iff last key is invalid request
                            response = respond(message);
                            responses.add(response);
                            if (logfile != null) {
                                logfile.process(new Date(), port, message.getXMLResponse(), response);
                            }
                            if (state == keys.size() - 2) {
                                // log.info("The last response IS: {}", response);
                                responses.add("done");
                                state = 0; // Just to make sure
                            }
                        }
                    }
                }
            } else {
                log.info("Empty line received");
            }
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            requests.clear();
            logfile.close();
            return responses;
        }
    } // end of validate


    /**
     * Process XML request
     *
     * @param msg XML data in the form of string literal received form client
     * @throws XMLStreamException cf. {@link XMLStreamException}
     */
    private void processXMLString(String msg) throws Exception {

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

        ArrayList<Request> clientResponsesTemp = protocol.getClientRequests();

        log.info("Validating input...");

        while (xsr.hasNext()) {
            // log.info("In loop...:for1 Validate, processXMLString");
            if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT) {
                // NO ORDER HERE!
                for (int i = 0; i < clientResponsesTemp.size(); i++) {
                    // log.info("In loop...:for2 Validate, processXMLString");
                    if (clientResponsesTemp.get(i).received(xsr) && (i < (clientResponsesTemp.size() - 1))) {
                        requests.add(clientResponsesTemp.get(i));
                    } else {
                        requests.add(clientResponsesTemp.get(keys.size() - 1));
                        //    xsr.next();
                    }
                }
            } else {
                xsr.next();
            }

        } // end of while loop

        log.info("Finished validating input");

        xsr.close();

    } // end of parseInput()

} // end of Validator{}
