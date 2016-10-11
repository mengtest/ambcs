/**
 * PercsServer -- Percs use case example
 * @author <a href="mailto:c.stankiewicz@wlv.ac.uk">cs</a>
 * @version 0.1
 */
package uk.ac.wlv.percs.demo;

import uk.ac.wlv.percs.lib.*;

/**
 * Main class.
 */
public class PercsServer {

    /* Use the custom request/response pairs. */
    private static Protocol protocol = new Protocol();;
    static {
        protocol.put(DemoClientRequest1.class, new DemoServerResponse1());
        protocol.put(DemoClientRequest2.class, new DemoServerResponse2());
        protocol.put(DemoClientRequest3.class, new DemoServerResponse3());
        protocol.put(DemoClientRequest4.class, new DemoEmptyResponse());
        protocol.put(DemoInvalidRequest.class, new DemoPoorRequest());
    }

    /**
     * The entry point.
     * @param args a port.
     */
    public static void main(String[] args) {

        /* Default port */
        int port = 11896;

        /* Create server instance on port, with name, and protocol instance */
        final Server server = Server.create(port, "PercsServer", "Percs.log", protocol);

    } // end of main()

} // end of PercsServer{}

