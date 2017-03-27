/**
 * PERCS
 *
 * @author <a href="mailto:c.stankiewicz@wlv.ac.uk">cs</a>
 * @version 0.1
 */
package com.github.cezs.ambcs.lib;

/* Akka */

/* Logger */

/* XML */

/* Utils */
import java.util.ArrayList;

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

