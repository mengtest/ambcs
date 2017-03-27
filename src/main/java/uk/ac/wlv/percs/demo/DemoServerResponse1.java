package uk.ac.wlv.percs.demo;

import uk.ac.wlv.percs.lib.Request;
import uk.ac.wlv.percs.lib.Response;

/**
 * Class matching listener's response to client's request
 *
 * @see Response
 */
public class DemoServerResponse1 implements Response {

    /**
     * Method taking client's request as parameter and returning XML string
     * response of listener
     *
     * @param message a class implementing {@link Request}
     * @return XML in the form of String
     */
    public final String getXML(Request message) {
        return "<inform id=\\\"1427790\\\"><pk>CSS3P</pk></inform>";
    }

} // end of DemoServerResponse1:Response
