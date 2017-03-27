package com.github.cezs.ambcs.demo;

import com.github.cezs.ambcs.lib.Request;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;

/**
 * Class matching client's XML request
 *
 * @see Request
 */
public class DemoClientRequest3 extends Request<XMLStreamReader> {

    private final Logger log = LoggerFactory.getLogger(DemoClientRequest3.class);

    public DemoClientRequest3(){};

    /**
     * This method signalises whether processed XML data corresponds to a request
     * implemented with this class.
     *
     * @param xsr a stream containing XML data
     * @return true if XML string corresponds to the expected request
     * @throws XMLStreamException {@link XMLStreamException}
     */
    public final boolean received(XMLStreamReader xsr) throws XMLStreamException {
        boolean ans = false;

        if (xsr.getEventType() == XMLStreamConstants.START_ELEMENT
                && xsr.getLocalName().equals("date")) {

            StringWriter sw = new StringWriter();
            XMLOutputFactory of = XMLOutputFactory.newInstance();
            XMLEventWriter xw = null;
            XMLInputFactory f = XMLInputFactory.newInstance();
            XMLEventReader xr = f.createXMLEventReader(xsr);

            while (xr.hasNext()) {
                // log.info("In loop...");
                XMLEvent e = xr.nextEvent();
                if (e.isStartElement()
                        && ((StartElement) e).getName().getLocalPart().equals("report")) {
                    xw = of.createXMLEventWriter(sw);
                } else if (e.isEndElement()
                        && ((EndElement) e).getName().getLocalPart()
                        .equals("report")) {
                    break;
                } else if (xw != null) {
                    xw.add(e);
                }
            }

            xw.close();

            JSONObject jsondata = XML.toJSONObject(sw.toString());

            FileWriter fw = null;
            BufferedWriter bw = null;
            PrintWriter pw = null;

            try {
                fw = new FileWriter("CSS3P.json", true);
                bw = new BufferedWriter(fw);
                pw = new PrintWriter(bw);
                pw.println(jsondata.toString(4));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                if (pw != null)
                    pw.close();
                try {
                    if (bw != null)
                        bw.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                try {
                    if (fw != null)
                        fw.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            ans = true;
        }
        return ans;
    } // End of received()

    /**
     * This method returns a string representing received request
     *
     * @return request string
     */
    public String getXML() {
        return "<?xml version=\"1.0\" ?> <MESSAGE CONTENT>";
    }

} // end of DemoClientRequest3:Request
