package com.github.cezs.ambcs.demo;

import com.github.cezs.ambcs.lib.Request;
import com.github.cezs.ambcs.lib.Response;

/**
 * Class matching listener's response to client's request
 *
 * @see Response
 */
public class DemoEmptyResponse implements Response {

    /**
     * Method taking client's request as parameter and returning XML string
     * response of listener
     *
     * @param message a class implementing {@link Request}
     * @return XML in the form of String
     */
    public final String getXML(Request message) {
        return "NO MESSAGE SENT";
    }

} // end of DemoEmptyResponse:Response
