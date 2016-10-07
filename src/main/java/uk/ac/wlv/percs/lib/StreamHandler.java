package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.UntypedActor;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* XML */
import javax.xml.stream.XMLStreamReader;

/* Standard */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * This stream handler, should be fairly versatile, in terms of what kind of user
 * input it can process. It can handle multiline requests, requests with
 * multiple roots etc. However, usually XML requests should be well-formed and conform
 * to the XML syntax rules, it will return runtime errors if given incompatible
 * input such as {@code <<request/>>} or {@code <mess age/>}. See {@link
 * XMLStreamReader} for more information on different kind of it's exceptions.
 */
class StreamHandler extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(StreamHandler.class);

    private final Validator validator;

    private PrintWriter writer = null;
    private BufferedReader reader = null;
    private StringBuffer line;
    private int character;
    private boolean listening = true;

    /**
     * StreamHandler's constructor
     *
     * @param validator instance validating stream according to protocol
     */
    StreamHandler(Validator validator) {
        this.validator = validator;
    }

    ;

    /**
     * This method returns boolean value indicating whether buffered character
     * stream is empty
     *
     * @return true if buffered character stream is empty and last character
     *         represents newline
     * @throws IOException cf. {@link IOException}
     */
    private boolean hasCompleteMessage() throws IOException {
        boolean ans = false;
        if ((character == 10) && !reader.ready())
            ans = true;
        return ans;
    }

    /**
     * close(): helper clean up method
     *
     * @throws ClosingProblem cf. {@link ClosingProblem}
     */
    private void close() throws ClosingProblem {
        try {
            if (reader != null) {
                reader.close();
                writer.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ClosingProblem();
        }

    }

    /**
     * Overridden onReceive() actor's method. If class containing this method
     * has been instantiated, then given corresponding request, actor will start
     * reading from stream. Input from stream is processed on character by
     * character basis in order to recognise control characters.
     *
     * @param message sent to actor
     * @throws ReadingProblem cf. {@link ReadingProblem}
     * @throws ClosingProblem cf. {@link ClosingProblem}
     */
    @Override
    public void onReceive(Object message) throws ReadingProblem, ClosingProblem {
        if (message instanceof CommunicationEndpoint) {
            try {
                final CommunicationEndpoint endpoint = (CommunicationEndpoint) message;
                final Socket socket = endpoint.getSocket();
                final SocketAddress address = socket.getRemoteSocketAddress();
                log.info("Connected to {}", address);
                reader = new BufferedReader(new InputStreamReader(endpoint.getSocket().getInputStream()));
                writer = new PrintWriter(endpoint.getSocket().getOutputStream(), true);
                line = new StringBuffer();
                character = reader.read();
                while (listening) {
                    // log.info("In loop...:while StreamHandler");
                    // log.info("Receiving characters...");
                    if (hasCompleteMessage()) {
                        log.info("Received request from {}", address);
                        ArrayList<String> ans = validator.validate(line.toString());
                        for (String a : ans) {
                            // log.info("In loop...:for StreamHandler");
                            if (a.equals("done")) {
                                // log.info("DONE!");
                                listening = false;
                                // ans.clear();
                            } else {
                                writer.println(a);
                                writer.flush();
                            }
                        }
                        line.delete(0, line.length());
                    }
                    line.append((char) character);
                    character = reader.read();
                }
                // log.info("Out loop...:while StreamHandler");
            } catch (IOException e) {
                throw new ReadingProblem();
            } finally {
                // log.info("In finally StreamHandler");
                log.info("Closing buffers...");
                close();
                // line = null;
                // character = 0;
                log.info("Sending \"done\"...");
                getSender().tell("done", getSelf());
            }
        }
    } // end of onReceive()

} // end of StreamHandler{}
