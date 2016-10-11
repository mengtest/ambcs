package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.UntypedActor;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Standard */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class provides actor listening for new clients.
 */
class Listener extends UntypedActor {

    private final Logger log = LoggerFactory.getLogger(Listener.class);

    // The socket listening for new clients
    private final ServerSocket serverSocket;
    // The port number to listen on
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
