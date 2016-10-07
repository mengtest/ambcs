package uk.ac.wlv.percs.lib;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Procedure;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * This stream handler, should be fairly versatile, in terms of what kind of user
 * input it can process. It can handle multiline requests, requests with
 * multiple roots etc. However, usually XML requests should be well-formed and conform
 * to the XML syntax rules, it will return runtime errors if given incompatible
 * input such as {@code <<request/>>} or {@code <mess age/>}. See {@link
 * XMLStreamReader} for more information on different kind of it's exceptions.
 */
class StreamHandlerWithBehaviour extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(StreamHandlerWithBehaviour.class);

    private final ActorRef connection; // provides socket
    private final ValidatorI<?> validatorI;
    private final Procedure<Object> writing = new Procedure<Object>() {
        public void apply(Object message) throws Exception {
            if (message instanceof Received) {
                // Placeholder: corresponding answer for now processed XML request received from child worker
            } else if (message instanceof ConnectionClosed) {
                getContext().stop(getSelf());
            }
        }
    };
    private PrintWriter writer = null;
    private BufferedReader reader = null;
    private StringBuffer line;
    private int character;
    private boolean listening = true;

    ;
    /**
     * Given corresponding request, actor will start reading from stream.
     * Input from stream is processed on character by character basis.
     */
    private final Procedure<Object> reading = new Procedure<Object>() {
        public void apply(Object message) throws ReadingProblem, ClosingProblem {
            if (message instanceof CommunicationEndpoint) {
                try {
                    final CommunicationEndpoint endpoint = (CommunicationEndpoint) message;
                    final Socket socket = endpoint.getSocket();
                    final SocketAddress address = socket.getRemoteSocketAddress();
                    log.info("Connected to {}", address);
                    reader = new BufferedReader(
                            new InputStreamReader(endpoint.getSocket().getInputStream()));
                    writer = new PrintWriter(endpoint.getSocket().getOutputStream(), true);
                    line = new StringBuffer();
                    character = reader.read();
                    while (listening) {
                        // log.info("In loop...");
                        if (hasCompleteMessage()) {
                            log.info("Received request from {}", address);
                            // Placeholder: delegate string to child worker for processing
                            // and proceed back to reading from stream
                            line.delete(0, line.length());
                        }
                        line.append((char) character);
                        character = reader.read();
                    }
                } catch (IOException e) {
                    throw new ReadingProblem();
                } finally {
                    close();
                }
            } else if (message instanceof Received) {
                getContext().become(writing);
            }
        }
    };

    /**
     * StreamHandler's constructor
     *
     * @param connection cf. {@link ActorRef}
     * @param validatorI a class implementing {@link ValidatorI} interface
     */
    StreamHandlerWithBehaviour(ActorRef connection, ValidatorI<?> validatorI) {
        this.connection = connection;
        this.validatorI = validatorI;

        // this actor stops when the connection is closed
        getContext().watch(connection);

        // start from reading state
        getContext().become(reading);

    }

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
            if (reader != null) reader.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ClosingProblem();
        }

    }

    /**
     * Overridden onReceive() actor's method.
     */
    @Override
    public void onReceive(Object message) {
    } // end of onReceive()

    /**
     * Placeholder request
     */
    static class Received {
        private final String xmlMessage;

        Received(String xmlMessage) {
            this.xmlMessage = xmlMessage;
        }

        public final String getXMLmessage() {
            return xmlMessage;
        }
    }

    ;

    /**
     * Placeholder request
     */
    static class ConnectionClosed {
    }

    ;

} // end of StreamHandlerWithBehaviour{}
