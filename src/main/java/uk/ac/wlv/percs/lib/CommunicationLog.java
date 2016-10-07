package uk.ac.wlv.percs.lib;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* Standard */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * This class implements custom communication logger
 */
class CommunicationLog {

    private final Logger log = LoggerFactory.getLogger(CommunicationLog.class);

    private FileWriter fw = null;
    private BufferedWriter bw = null;
    private PrintWriter pw = null;
    private Date date = null;

    /**
     * Default constructor for CommunicationLog
     *
     * @throws IOException cf. {@link IOException}
     */
    CommunicationLog() throws IOException {
        fw = new FileWriter("PERCS1427790.log", true);
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw);
    }

    /**
     * This method prints out the transaction information
     *
     * @param date date of transaction
     * @param port the port
     * @param rx request received from client
     * @param tx request sent to client
     */
    public void process(Date date, int port, String rx, String tx) {
        pw.println("Date: " + date.toString() + '\n'
                + "Port: " + port + '\n'
                + "Rx: " + rx + '\n'
                + "Tx: " + tx + '\n');
        log.info("Received: {}", rx); // Prints safe data
    }

    /**
     * A helper clean up method
     */
    public void close() {
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

} //  end of CommunicationLog
