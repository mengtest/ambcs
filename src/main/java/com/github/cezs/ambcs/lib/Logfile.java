package com.github.cezs.ambcs.lib;

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
 * This class implements custom logger
 */
class Logfile {

    private final Logger log = LoggerFactory.getLogger(Logfile.class);

    private FileWriter fw = null;
    private BufferedWriter bw = null;
    private PrintWriter pw = null;
    private Date date = null;

    /**
     * Default constructor for Logfile
     *
     * @throws IOException cf. {@link IOException}
     */
    Logfile(String filename) throws IOException {
        fw = new FileWriter(filename, true);
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

} //  end of Logfile
