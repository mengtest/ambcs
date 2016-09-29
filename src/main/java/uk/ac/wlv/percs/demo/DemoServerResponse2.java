package uk.ac.wlv.percs.demo;

import uk.ac.wlv.percs.lib.Request;
import uk.ac.wlv.percs.lib.Response;

/**
 * Class matching listener's response to client's request
 *
 * @see Response
 */
public class DemoServerResponse2 implements Response {

    /**
     * Method taking client's request as parameter and returning XML string
     * response of listener
     *
     * @param message a class implementing {@link Request}
     * @return XML in the form of String
     */
    public final String getXMLResponse(Request message) {
        DemoClientRequest2 cr = (DemoClientRequest2) message;
        return "<request cmd=\\\"GRAB\\\"><tk>" + cr.getTk() + "</tk></request>";
    }

} // end of DemoServerResponse2:Response
