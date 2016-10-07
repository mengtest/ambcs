package uk.ac.wlv.percs.lib;

/* XML */
import javax.xml.stream.XMLStreamConstants;

/**
 * Helper class. Print the XMLStreamConstants and corresponding event types.
 */
final class InfoTool {

    private InfoTool() {
    }

    public static void getCorrespondence() {
        System.out.print("TYPE:\n"
                + "start document: " + XMLStreamConstants.START_DOCUMENT + "\n"
                + "end document: " + XMLStreamConstants.END_DOCUMENT + "\n"
                + "start elements: " + XMLStreamConstants.START_ELEMENT + "\n"
                + "end element: " + XMLStreamConstants.END_ELEMENT + "\n"
                + "attribute: " + XMLStreamConstants.ATTRIBUTE + "\n"
                + "characters: " + XMLStreamConstants.CHARACTERS + "\n"
                + "space: " + XMLStreamConstants.SPACE + "\n");
    } // end of getCorrespondence()

} // end of InfoTool:Response
