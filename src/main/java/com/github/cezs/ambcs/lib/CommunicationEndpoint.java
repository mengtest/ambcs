package com.github.cezs.ambcs.lib;

/* Standard */
import java.net.Socket;

/**
 * This class encapsulates socket in immutable message.
 */
class CommunicationEndpoint {

    final private Socket socket;

    /**
     * CommunicationEndpoint's constructor
     *
     * @param socket socket to be encapsulated by this class
     */
    CommunicationEndpoint(Socket socket) {
        this.socket = socket;
    }

    /**
     * This method returns socket
     *
     * @return socket {@link Socket}
     */
    final public Socket getSocket() {
        return socket;
    }

} // end of CommunicationEndpoint:Response
