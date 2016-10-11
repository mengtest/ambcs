package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;

/**
 * The singleton class encapsulating all the necessary
 * initialisations.
 */
public class Server {
    private static Server server = null;

    private Server(){};

    /**
     * Create the listener.
     * @param port the port to listen on
     * @param nameOfServer the name of server
     * @param nameOfLogfile the name of log file
     * @param protocol the request/reponse pairs
     * @return the instance encapsulating all initialisations
     */
    public static Server create(int port, String nameOfServer, String nameOfLogfile, Protocol protocol) {

        final ActorSystem system;
        final ActorRef connection;
//        Logfile log = null;
//
//        if (!nameOfLogfile.isEmpty()) {
//            try {
//                log = new Logfile(nameOfLogfile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        connection = system.actorOf(Connection.props(new Validator(protocol, port, log), port), "connection");

        system = ActorSystem.create(nameOfServer, ConfigFactory.load());

        connection = system.actorOf(Connection.props(new Validator(protocol, port, nameOfLogfile), port), "connection");

        system.actorOf(Props.create(Terminator.class, connection), "terminator");

        if (server == null)
            server = new Server();

        return server;

    } // end of create()

} // end of Server{}
