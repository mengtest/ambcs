package uk.ac.wlv.percs.lib;

/**
 * This interface supplies method to be overridden by class implementing
 * corresponding response to XML request
 */
public interface Response {

    /**
     * Method to be overridden by implementing class
     *
     * @param message a class implementing {@link Request}
     * @return corresponding XML request the in form of a string literal
     */
    String getXML(Request message);

} // end of Response{}
