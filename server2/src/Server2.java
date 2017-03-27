import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {

    public static void displayUsageInfo() {
	System.out.println ("Parameters are:");
	System.out.println (" 1) The port number that the server will listen on");
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    displayUsageInfo();
	} else {
	    try {
		ServerSocket sock = new ServerSocket(Integer.parseInt(args[0]));
		boolean finished = false;
		while (!finished) {
		    System.out.println("waiting");
		    Socket connection = sock.accept();
		    System.out.println("connection from " + connection.getRemoteSocketAddress());
		    InputStream is = connection.getInputStream();
		    BufferedReader in = new BufferedReader(new InputStreamReader(is));
		    String line = in.readLine();
		    while (line != null && !finished) {
			if (line.equals("quit")) {
			    finished = true;
			} else {
			    System.out.println("Received " + line);
			}
			if (!finished) {
			    line = in.readLine();
			}
		    }
		    connection.close();
		}
		sock.close();
	    } catch (BindException e) {
		System.out.println ("Cannot bind to port. Is a server already running?");
	    } catch (NumberFormatException e) {
		System.out.println ("Port number should be an integer");
	    } catch (IllegalArgumentException e) {
		System.out.println ("The port number needs to be less than 65536");
	    } catch (Throwable ex) {
		System.out.println ("Exception: " + ex.toString());
	    }
	}
    }
}
