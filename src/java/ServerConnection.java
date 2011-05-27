import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Class with static methods to connect to the server and communicate with it.
 * @author Mariana Ramos Franco
 */
public class ServerConnection {
	private static Socket sock;
	private static DataOutputStream dos;
    private static DataInputStream dis;

	/**
	 * Connect to the server on the specified host and port.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static void connect() throws UnknownHostException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("config.properties"));
		final String host = props.getProperty("server.host");
		final int port = Integer.parseInt(props.getProperty("server.port"));
		sock = new Socket(host, port);
		dos = new DataOutputStream(sock.getOutputStream());
        dis = new DataInputStream(sock.getInputStream());
	}

	/**
	 * Close the connection to the server.
	 * @throws IOException
	 */
	public static void close() throws IOException {
        dos.flush();
        dos.close();
        sock.close();
	}

	/**
	 * Send the string passed as parameter to the server.
	 * @param msg
	 * 			the message to be sent.
	 * @throws IOException
	 */
	public static void sendMsg(String msg) throws IOException{
		dos.writeChars(msg);
		dos.flush();
	}

	/**
	 * Reads the message sent by the server.
	 * @return the message sent by the server.
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public static String readMsg() throws IOException{
		return dis.readLine();
	}

	/**
	 * Main method used to test the connection to the server.
	 * @param args
	 */
	public static void main(String args[]){
		try {
			ServerConnection.connect();
			ServerConnection.sendMsg("Hello!!");
			System.out.println(ServerConnection.readMsg());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ServerConnection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
