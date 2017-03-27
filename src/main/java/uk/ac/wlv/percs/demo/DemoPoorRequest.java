package uk.ac.wlv.percs.demo;

import uk.ac.wlv.percs.lib.Request;
import uk.ac.wlv.percs.lib.Response;

/**
 * Class matching listener's response to client's request
 *
 * @see Response
 */
public class DemoPoorRequest implements Response {

    /**
     * Method taking client's request as parameter and returning XML string
     * response of listener
     *
     * @param message a class implementing {@link Request}
     * @return XML in the form of String
     */
    public String getXML(Request message) {
        return "<<poor-request/>/>";
    }

} // end of DemoPoorRequest:Response
