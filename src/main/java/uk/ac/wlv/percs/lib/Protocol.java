package uk.ac.wlv.percs.lib;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Class storing protocol.
 * Requirements: Penultimate message signalises end of communication,
 * while the last one signalises invalid request.
 */
public class Protocol {

    // Non-mutable HashMap storing request/response pairs.
    private final LinkedHashMap<Class<? extends Request>, Response> protocolMap;
    // ArrayList storing client's requests. Used for filling instances with
    // data acquired with received() method which parses XML request,
    // and returns either true or false depending on the validity of request
    private ArrayList<Request> clientRequests;

    /**
     * The default constructor
     */
    public Protocol() {
        protocolMap = new LinkedHashMap();
        clientRequests = new ArrayList<Request>();
    }

    /**
     * Add a request/response pair.
     * @param clientRequest a class corresponding to client's request
     * @param serverResponse an instance of listener's response class
     */
    public void put(Class<? extends Request> clientRequest, Response serverResponse) {
        protocolMap.put(clientRequest, serverResponse);
        try {
            clientRequests.add(clientRequest.getConstructor().newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a list of the instantiated client's requests
     * @return list of the client's requests
     */
    protected ArrayList<Request> getClientRequests() {
        return clientRequests;
    }

    /**
     * Return the set of client's requests classes.
     * @return set of request's.
     */
    protected Set<Class<? extends Request>> keySet() {
        return protocolMap.keySet();
    }

    /**
     * Get a response instance to the supplied request class.
     * @param key a request class.
     * @return response instance.
     */
    protected Response get(Class<? extends Request> key) {
        return protocolMap.get(key);
    }

}
