import java.io.*;
import java.net.*;

/**
 * This class will handle most of the jobs required by a client. It will connect
 * to the Server using a Socket then make use of threads to separately handle input
 * and output.
 * 
 * @author Alexander Finnigan - ID: 201084157
 *
 */
public class ClientInstance {
	
	// Declare Socket
	Socket clientSocket;
	
	/**
	 * This constructor will create the socket to connect to the server using the server's
	 * IP address and port number given by the class {@link ClientMain }
	 * 
	 * @param host - the IP address of the server.
	 * @param port - the port number of the server.
	 */
	//--------------------------CONSTRUCTOR--------------------------
	public ClientInstance(String host, int port) {
		// Set clientSocket to connect to the server
		try {
			clientSocket = new Socket(host, port);
		} catch (SocketException se){
			System.err.println("SOCKET ERROR in ClientInstance:");
			System.err.println(se.getMessage());
		} catch (IOException ioe){
			System.err.println("I/O ERROR in ClientInstance:");
			System.err.println(ioe.getMessage());
		}
	
	}
	
	/**
	 * This method will create the input and output thread for use in the server.
	 */
	public void RunClient() {
		// Declare threads
		Thread sender;
		Thread listener;
		
		// Set threads to instances of ClientListener and ClientSender
		listener = new Thread(new ClientListener());
		sender = new Thread(new ClientSender());
		
		// Start threads
		listener.start();
		sender.start();
	}
	
	/**
	 * This class will implement {@link Runnable } so it can be used as a thread.
	 * This class in particular will be used to listen for output from the server.
	 * 
	 * @author Alexander Finnigan - ID: 201084157
	 *
	 */
	class ClientListener implements Runnable{
		
		// Use Socket from ClientInstance
		Socket clientSocket = ClientInstance.this.clientSocket;
		
		/**
		 * This method will handle messages coming in from the server. We are required
		 * to override {@link Runnable#run()} if we want the thread to do what we want.
		 */
		public void run() {
			// Declare BufferedReader for retrieving server messages
			BufferedReader in = null;
			// Variable for server messages
			String serverMessage;
			
			try {
				// Set the BufferedReader for use
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				// Print any messages from server
				while ((serverMessage = in.readLine()) != null){
					System.out.println(serverMessage);
				}
			} catch (IOException ioe){
				// Something went wrong, give details
				System.err.println("I/O ERROR in ClientInstance:");
				System.err.println(ioe.getMessage());
			}
		}
	}
	
	/**
	 * This class will implement {@link Runnable } so it can be used as a thread.
	 * It will handle the input from the user going to the server.
	 * 
	 * @author Alexander Finnigan - ID: 201084157
	 *
	 */
	class ClientSender implements Runnable{
		
		// Use Socket from ClientInstance
		Socket clientSocket = ClientInstance.this.clientSocket;
		
		/**
		 * This method will handle input that the user types and send it to the server.
		 * We are required to override {@link Runnable#run()} if we want the thread to 
		 * do what we want.
		 */
		public void run() {
			// Declare PrintWriter and BufferedReader
			PrintWriter out = null;
			BufferedReader input = null;
			
			try {
				// Set the BufferedReader and PrintWriter for use
				input = new BufferedReader(new InputStreamReader(System.in));
				out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				
				// Send any messages from user to server
				while (true){
					String message = input.readLine();
					out.println(message);
					out.flush();
				}
			} catch (IOException ioe){
				// Something went wrong, give details
				System.err.println("I/O ERROR in ClientInstance:");
				System.err.println(ioe.getMessage());
			}
		}
	}
}
