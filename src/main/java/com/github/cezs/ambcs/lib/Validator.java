package com.github.cezs.ambcs.lib;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* XML */
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/* Standard */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class validating stream according to the supplied protocol's
 * request and response pairs.
 *
 * Note: Because current implementation uses Logfile the validation
 * contains side-effecting IO operations.
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
    private RequestAccumulator converter;

//    public Validator(Protocol service, int port, Logfile logfile) {
//        this.protocol = service;
//        this.port = port;
//        this.logfile = logfile;
//    }

    public Validator(Protocol service, RequestAccumulator converter, int port, String nameOfLogfile) {
        this.protocol = service;
        this.converter = converter;
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
            ans = r.getXML(message);
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
            requests = converter.getRequests(msg);

            // Access our HashMap keys by number
            List keys = new ArrayList(protocol.keySet());

            // State handler and logger. Note that we impose order here
            if (!requests.isEmpty()) {
                for (Request message : requests) {
                    for (int i = 0; i < keys.size(); i++) {
                        if ((state == i) && (message.getClass().equals(keys.get(i)))) {
                            state = (state + 1) % (keys.size() - 1); // -1 iff last key is invalid request
                            response = respond(message);
                            responses.add(response);
                            if (logfile != null) {
                                logfile.process(new Date(), port, message.getXML(), response);
                            }
                            if (state == keys.size() - 2) {
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

} // end of Validator{}
