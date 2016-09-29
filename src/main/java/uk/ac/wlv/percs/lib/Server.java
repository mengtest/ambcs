package uk.ac.wlv.percs.lib;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

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
     * @param name the name of listener
     * @param protocol the request/reponse pairs
     * @return the instance encapsulating all initialisations
     */
    public static Server create(int port, String name, Protocol protocol) {
        final ActorSystem system = ActorSystem.create(name, ConfigFactory.load());
        final ActorRef connection = system.actorOf(Connection.props(new Validator(protocol,port), port), "connection");
        system.actorOf(Props.create(Terminator.class, connection), "terminator");
        if (server == null)
            server = new Server();
        return server;
    }
}
