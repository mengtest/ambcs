/**
 * AmbcsServer -- AMBCS use case example
 * @author <a href="mailto:c.stankiewicz@wlv.ac.uk">cs</a>
 * @version 0.1
 */
package com.github.cezs.ambcs.demo;

import com.github.cezs.ambcs.lib.ConverterType;
import com.github.cezs.ambcs.lib.Protocol;
import com.github.cezs.ambcs.lib.Server;

/**
 * Main class.
 */
public class AmbcsServer {

    /* protocol instance */
    private static Protocol protocol = new Protocol();;

    /* Use the custom request/response pairs. */
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

        /* port number */
        int port = 11896;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ex) {
                System.err.println("The supplied argument " + args[0] + " should be an integer.");
                System.exit(1);
            }
            // finally {
            // }
        }
        
        /* name */
        String name = "AmbcsServer";

        /* logging file */
        String log = "ambcs.log";
        
        /* Create server instance */
        final Server server = Server.create (
                port, 
                name,
                log, 
                protocol,
                ConverterType.Stream
        );

    } // end of main()

} // end of AmbcsServer{}

