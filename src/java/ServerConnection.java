import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class with methods to connect to the server and communicate with it.
 * 
 * @author Mariana Ramos Franco
 */
public class ServerConnection {
	private Socket sock;
	private DataOutputStream dos;
    private DataInputStream dis;

	/**
	 * Connect to the server on the specified host and port.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect(String host, int port)
			throws UnknownHostException, IOException {
		sock = new Socket(host, port);
		dos = new DataOutputStream(sock.getOutputStream());
        dis = new DataInputStream(sock.getInputStream());
	}

	/**
	 * Close the connection to the server.
	 * @throws IOException
	 */
	public void close() throws IOException {
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
	public void sendMsg(String msg) throws IOException{
		msg = msg + '\u0000';
		byte[] b = msg.getBytes("UTF-8");
		dos.write(b);
		dos.flush();
	}

	/**
	 * Reads the message sent by the server.
	 * @return the message sent by the server.
	 * @throws IOException
	 */
	public String readMsg() throws IOException{
		StringBuilder sb = new StringBuilder();
		while (true) {
			int ch = dis.read();
			if (ch == -1) throw new EOFException();
			if (ch == 0) break; // you read a NULL
			sb.append((char)ch);
		}
		String str = sb.toString();
		return str;
	}

	/**
	 * Main method used to test the connection to the server.
	 * @param args
	 */
	public static void main(String args[]){
		ServerConnection server = null;
		try {
			server = new ServerConnection();
			server.connect("localhost", 12300);
			server.sendMsg(Messages.createAuthRequestMsg("1", "1"));
			System.out.println("Authentication message sent.");
			while(true){
				Thread.sleep(1000);	// wait 1 second
				System.out.println(server.readMsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
