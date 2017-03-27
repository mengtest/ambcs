/**
 * PERCS
 *
 * @author <a href="mailto:c.stankiewicz@wlv.ac.uk">cs</a>
 * @version 0.1
 */
package uk.ac.wlv.percs.lib;

/* Akka */
import akka.actor.*;
import akka.japi.Creator;
import akka.japi.Function;
import akka.japi.Option;
import akka.japi.Procedure;

/* Logger */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

/* XML */
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/* Utils */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Each different validator has to implement this interface
 *
 * @param <T> data type of input
 */
interface ValidatorI<T> {

    /**
     * A class implementing this interface has to provide this method
     *
     * @param a data to be validated
     */
    ArrayList<String> validate(T a);

} // end of ValidatorI{}

