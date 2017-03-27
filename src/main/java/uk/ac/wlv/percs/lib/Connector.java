package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.*;
import akka.japi.Creator;
import akka.japi.Function;
import akka.japi.Option;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

/* Standard */
import java.io.IOException;
import java.net.Socket;

/**
 * This actor is at the root of this system. It provides interfacing between
 * different children and handles exceptions.
 */
class Connector extends UntypedActor {

    private final static Logger log = LoggerFactory.getLogger(Connector.class);

    final Validator validator;

    // ActorRef listener;
    final int port;

    /**
     * SupervisorStrategy setup
     */
    private static SupervisorStrategy strategy
            = new OneForOneStrategy(10, Duration.create("1 minute"),
            new Function<Throwable, SupervisorStrategy.Directive>() {
                public SupervisorStrategy.Directive apply(Throwable t) {
                    if (t instanceof IOReadingProblem) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.resume();
                    } else if (t instanceof IOException) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.restart();
                    } else if (t instanceof IOClosingProblem) {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.stop();
                    } else {
                        log.error(t.getMessage(), t);
                        return SupervisorStrategy.escalate();
                    }
                }
            });

    /**
     * Connector's constructor
     *
     * @param validator instance validating stream according to protocol
     * @param port a port number
     */
    public Connector(Validator validator, int port) {
        this.validator = validator;
        this.port = port;
    }

    /**
     * Props for Connector
     *
     * @param validator instance validating stream according to protocol
     * @param port a port number
     * @return the Connector actor
     */
    public static Props props(final Validator validator, final int port) {
        return Props.create(new Creator<Connector>() {
            private static final long serialVersionUID = 1L;

            public Connector create() throws Exception {
                return new Connector(validator, port);
            }
        });
    }

    /**
     * SupervisorStrategy call
     *
     * @return strategy cf. {@link SupervisorStrategy}
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
        // Listener is a child of the Connector actor
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
                    getContext().actorOf(Props.create(IOHandler.class, validator));
            handler.tell(endpoint, getSelf());
        } else if (message.equals("done")) {
            log.info("Received \"done\": Stopping stream handler...");
            context().stop(getSender()); /* stop IOHandler */
            log.info("Listening...");
            //
            // listener.tell("listen", getSelf());
        } else {
            unhandled(message);
        }
    } // end of onReceive()

} // end of Connector{}
