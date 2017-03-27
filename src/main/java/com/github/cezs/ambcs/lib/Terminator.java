package com.github.cezs.ambcs.lib;

/* Akka */
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
