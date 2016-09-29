/**
 * PERCS
 * @author <a href="mailto:c.stankiewicz@wlv.ac.uk">cs</a>
 * @version 0.1
 */
package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.*;
import akka.japi.Creator;
import akka.japi.Function;
import akka.japi.Option;
import akka.japi.Procedure;
import com.typesafe.config.ConfigFactory;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

/* JSON */
import org.json.JSONObject;
import org.json.XML;

/* XML */
import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/* Utils */
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

/**
 * This actor is at the root of this system. It provides interfacing between
 * different children and handles exceptions.
 */
class Connection extends UntypedActor {

    private final static Logger log = LoggerFactory.getLogger(Connection.class);

    final Validator validator;
    final int port;

    // ActorRef listener;

    /**
     * Props for Connection
     *
     * @param validator instance validating stream according to protocol
     * @param port a port number
     * @return the Connection actor
     */
    public static Props props(final Validator validator, final int port) {
        return Props.create(new Creator<Connection>() {
            private static final long serialVersionUID = 1L;

            public Connection create() throws Exception {
                return new Connection(validator, port);
            }
        });
    }

    /**
     * Connection's constructor
     *
     * @param validator instance validating stream according to protocol
     * @param port a port number
     */
    public Connection(Validator validator, int port) {
        this.validator = validator;
        this.port = port;
    }

    /**
     * SupervisorStrategy setup
     */
    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                public SupervisorStrategy.Directive apply(Throwable t) {
                    if (t instanceof ReadingProblem) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.resume();
                    }
                    else if (t instanceof IOException) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.restart();
                    }
                    else if (t instanceof ClosingProblem) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.stop();
                    }
                    else {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.escalate();
                    }
                }
            });

    /**
     * SupervisorStrategy call
     *
     * @return strategy cf. {@link akka.actor.SupervisorStrategy}
     */
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    /**
     * Overridden preStart()
     */
    @Override
    public void preStart() {
        // Listener is a child of the Connection actor
        final ActorRef listener = getContext().actorOf(Props.create(Listener.class, port), "listener");
        listener.tell("listen", getSelf());
        log.info("Listening...");
    }

    /**
     * Overridden postRestart(). Overriding postRestart in order to ensure that
     * there is only one call to preStart(), and thus disable creation of new
     * ActorRef for child (i.e., 'listener') during restarts.
     *
     * @param reason cf. {@link Throwable}
     */
    @Override
    public void postRestart(Throwable reason) {

    }

    /**
     * Overridden. The default implementation of preRestart() stops all
     * the children of the actor. To opt-out from stopping the children, we have to
     * override preRestart()
     *
     * @param reason cf. {@link Throwable}
     * @param message cf. {@link Option}
     * @throws Exception cf. {@link Exception}
     */
    public void preRestart(Throwable reason, Option<Object> message)
            throws Exception {
        postStop(); /* Keep the call to postStop(), but no stopping of children */
    }

    /**
     * Overridden onReceive() actor's method
     *
     * @param message {@link Socket}
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof CommunicationEndpoint) {
            final CommunicationEndpoint endpoint = (CommunicationEndpoint) message;
            final ActorRef handler =
                    getContext().actorOf(Props.create(StreamHandler.class, validator));
            handler.tell(endpoint, getSelf());
        } else if (message.equals("done")) {
            log.info("Received \"done\": Stopping stream handler...");
            context().stop(getSender()); /* stop StreamHandler */
            log.info("Listening...");
            //
            // listener.tell("listen", getSelf());
        } else {
            unhandled(message);
        }
    } // end of onReceive()

} // end of Connection{}

/**
 * This class provides actor listening for new clients.
 */
class Listener extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(Listener.class);

    private final ServerSocket serverSocket;
    private final int port;

    /**
     * Listener's constructor
     *
     * @param port a port number
     * @throws IOException {@link IOException}
     */
    Listener(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(this.port);
    }

    /**
     * Overridden onReceive() actor's method
     *
     * @param message telling this actor to listen on port provided with an instantiation
     */
    @Override
    public void onReceive(Object message) {
        if (message.equals("listen")) {
            try {
                while (true) {
                    final Socket clientSocket = serverSocket.accept();
                    getSender().tell(new CommunicationEndpoint(clientSocket), getSelf());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            unhandled(message);
        }
    }

} // end of Listener

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
    };

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
                final CommunicationEndpoint endpoint = (CommunicationEndpoint)message;
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

/**
 * Class validating stream according to supplied protocol's request/response pairs.
 */
class Validator {

    private final Logger log = LoggerFactory.getLogger(Validator.class);

    private Protocol protocol;
    private int port;

    public Validator(Protocol service, int port) {
        this.protocol = service;
        this.port = port;
    }

    // StAX
    private XMLInputFactory xif;
    private XMLStreamReader xsr;

    // Received information
    private String request;

    // Storing state and responses for sequential processing
    private int state;
    private ArrayList<Request> requests = new ArrayList<Request>();

    // Storing record of transactions
    private CommunicationLog communicationLog;

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
            communicationLog = new CommunicationLog();
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
                            communicationLog.process(new Date(), port, message.getXMLRequest(), response);
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
            communicationLog.close();
            return responses;
        }
    } // end of validate


    /**
     * Process XML request
     *
     * @param msg XML data in the form of string literal received form client
     * @throws XMLStreamException cf. {@link XMLStreamException}
     */
    private void processXMLString(String msg) throws XMLStreamException {

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

        // Quick hack solving invalid XML constructs used by supplied client.
        // TODO: Most likely to be removed in future versions.
        request = request.replace("\\","");
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
                    }
                    else {
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

/**
 * Each different validator has to implement this interface
 *
 * @param <T> data type of input
 */
interface ValidatorI<T> {

    /**
     * A class implementing this interface has to provide this method
     *
     * @param a data to be validated
     */
    ArrayList<String> validate(T a);

} // end of ValidatorI{}


/**
 * Immutable request providing socket. This class encapsulates socket in
 * immutable request.
 */
class CommunicationEndpoint {

    final private Socket socket;

    /**
     * CommunicationEndpoint's constructor
     *
     * @param socket socket to be encapsulated by this class
     */
    CommunicationEndpoint(Socket socket) { this.socket = socket; }

    /**
     * This method returns socket
     *
     * @return socket {@link Socket}
     */
    final public Socket getSocket() {
        return socket;
    }

} // end of CommunicationEndpoint:Response

/**
 * Internal exception
 */
class ReadingProblem extends Exception {
    private static final long serialVersionUID = 1L;
} // end of ReadingProblem:Exception

/**
 * Internal exception
 */
class ClosingProblem extends Exception {
    private static final long serialVersionUID = 1L;
} // end of ClosingProblem:Exception

/**
 * This actor observes top parent and shuts down system upon receiving
 * {@link akka.actor.Terminated} request
 */
class Terminator extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(Terminator.class);
    private final ActorRef ref;

    /**
     * Terminator's constructor
     *
     * @param ref an actor to be watched
     */
    public Terminator(ActorRef ref) {
        this.ref = ref;
        getContext().watch(ref);
    }

    /**
     * Overridden onReceive() actor's method
     *
     * @param message has to contain instance of {@link akka.actor.Terminated}
     */
    @Override
    public void onReceive(Object message) {
        if (message instanceof Terminated) {
            log.info("{} has terminated, shutting down system", ref.path());
            getContext().system().terminate();
        } else {
            unhandled(message);
        }
    }

} // end of Terminator{}

/**
 * This class implements custom communication logger
 */
class CommunicationLog {

    private final Logger log = LoggerFactory.getLogger(Validator.class);

    private FileWriter fw = null;
    private BufferedWriter bw = null;
    private PrintWriter pw = null;
    private Date date = null;


    /**
     * Default constructor for CommunicationLog
     *
     * @throws IOException cf. {@link IOException}
     */
    CommunicationLog() throws IOException {
        fw = new FileWriter("PERCS1427790.log", true);
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw);
    }

    /**
     * This method prints out the transaction information
     *
     * @param date date of transaction
     * @param port the port
     * @param rx request received from client
     * @param tx request sent to client
     */
    public void process(Date date, int port, String rx, String tx) {
        pw.println("Date: " + date.toString() + '\n'
                + "Port: " + port + '\n'
                + "Rx: " + rx + '\n'
                + "Tx: " + tx + '\n');
        log.info("Received: {}", rx); // Prints safe data
    }

    /**
     * A helper clean up method
     */
    public void close() {
        if (pw != null)
            pw.close();
        try {
            if (bw != null)
                bw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (fw != null)
                fw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

} //  end of CommunicationLog

/**
 * Helper class. Print the XMLStreamConstants and corresponding event types.
 */
final class InfoTool {

    private InfoTool() {
    }

    public static void getCorrespondence() {
        System.out.print("TYPE:\n"
                + "start document: " + XMLStreamConstants.START_DOCUMENT + "\n"
                + "end document: " + XMLStreamConstants.END_DOCUMENT + "\n"
                + "start elements: " + XMLStreamConstants.START_ELEMENT + "\n"
                + "end element: " + XMLStreamConstants.END_ELEMENT + "\n"
                + "attribute: " + XMLStreamConstants.ATTRIBUTE + "\n"
                + "characters: " + XMLStreamConstants.CHARACTERS + "\n"
                + "space: " + XMLStreamConstants.SPACE + "\n");
    } // end of getCorrespondence()

} // end of InfoTool:Response

/**
 * This stream handler, should be fairly versatile, in terms of what kind of user
 * input it can process. It can handle multiline requests, requests with
 * multiple roots etc. However, usually XML requests should be well-formed and conform
 * to the XML syntax rules, it will return runtime errors if given incompatible
 * input such as {@code <<request/>>} or {@code <mess age/>}. See {@link
 * XMLStreamReader} for more information on different kind of it's exceptions.
 */
class StreamHandlerWithBehaviour extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(StreamHandler.class);

    private final ActorRef connection; // provides socket
    private final ValidatorI<?> validatorI;

    private PrintWriter writer = null;
    private BufferedReader reader = null;
    private StringBuffer line;
    private int character;
    private boolean listening = true;


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

    };

    /**
     * Given corresponding request, actor will start reading from stream.
     * Input from stream is processed on character by character basis.
     */
    private final Procedure<Object> reading = new Procedure<Object>() {
        public void apply(Object message) throws ReadingProblem, ClosingProblem {
            if (message instanceof CommunicationEndpoint) {
                try {
                    final CommunicationEndpoint endpoint = (CommunicationEndpoint)message;
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
            }
            else if (message instanceof Received) {
                getContext().become(writing);
            }
        }
    };

    private final Procedure<Object> writing = new Procedure<Object>() {
        public void apply(Object message) throws Exception {
            if (message instanceof Received) {
                // Placeholder: corresponding answer for now processed XML request received from child worker
            }
            else if (message instanceof ConnectionClosed) {
                getContext().stop(getSelf());
            }
        }
    };

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
    };

    /**
     * Placeholder request
     */
    static class ConnectionClosed {
    };

} // end of StreamHandlerWithBehaviour{}
