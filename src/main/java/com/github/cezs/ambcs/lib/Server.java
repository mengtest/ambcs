package com.github.cezs.ambcs.lib;

/* Akka */
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
     * @param nameOfServer the name of server
     * @param nameOfLogfile the name of log file
     * @param protocol the request/reponse pairs
     * @return the instance encapsulating all initialisations
     */
    public static Server create(int port, String nameOfServer, String nameOfLogfile, Protocol protocol, ConverterType converterType) {

        final ActorSystem system;
        final ActorRef connection;

        final RequestAccumulator converter;

        system = ActorSystem.create(nameOfServer, ConfigFactory.load());

        if (converterType==ConverterType.Stream) {
            converter = new StreamRequestAccumulator(protocol);
        } else {
            converter = null;
        }

        connection = system.actorOf(Connector.props(new Validator(protocol, converter, port, nameOfLogfile), port), "connection");

        system.actorOf(Props.create(Terminator.class, connection), "terminator");

        if (server == null)
            server = new Server();

        return server;

    } // end of create()

} // end of Server{}
