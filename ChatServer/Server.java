import java.io.*;
import java.net.*;
import java.util.HashSet;

/**
 * This class is a Chat Server that will accept clients that connect to
 * the machines IP address and the given port in the attributes below.
 * It only allows a given number of clients which can be changed in the
 * attributes. It keeps track of how long the server has been on and
 * how long a client has been connected. Also, when asked to shutdown,
 * the server asks for a password.
 * 
 * @author Alexander Finnigan - ID: 201084157
 *
 */
public class Server{
	
	// Store port number
	private static final int port = 4550;
	// Set max client number
	private static int maxClientCount = 10;
	// Declare sockets
	private static ServerSocket ss;
	private static Socket client;
	// Create array for threads of clients
	private static final ClientThread[] threads = new ClientThread[maxClientCount];
	// Declare username variable for use
	private static String username;
	// Declare boolean variable for shutting down server
	private static boolean shutDownFlag = false;
	// Total number of client connections
	private static int connections = 0;
	// Password to shut down server
	private static String password = "password";
	// Start time variable for server
	private static long serverStartTime;
	// Start time variable for client
	private static long clientStartTime;
	// Use variable to find exact runtime for either server or client
	private static long endTime;
	
	/**
	 * The main method of the {@link Server } class is used to create the <code>Server 
	 * Socket</code> and begin listening for clients trying to connect. When a new
	 * client successfully connects, it will create a new <code>thread</code> of the class
	 * {@link ClientThread } which handles the client actions. It also starts the server 
	 * timer which is used for clients to find out how long the server has been running.
	 * 
	 * @param args - the usual argument for main methods.
	 */
	/*--------------------------MAIN METHOD-------------------------*/
	public static void main(String[] args) {
		try {
			// Create Server Socket
			ss = new ServerSocket(port);
			// Starts Server timer
			serverStartTime = System.currentTimeMillis();
			System.out.println("***** ServerSocket created *****");
		} catch (SocketException se){
			// If the Server hasn't been allowed to shutdown
			if(shutDownFlag == false){
				// Something went wrong with Server Socket, give message
				System.err.println("SOCKET ERROR in Server: ");
				System.err.println(se.getMessage());
				// Clean up
				System.exit(1);
			}
		} catch (IOException ioe){
			// Something went wrong, give message
			System.err.println("I/O ERROR in Server:");
			System.err.println(ioe.getMessage());
			// Clean up
			System.exit(1);
		}
		
		// Listen for connections and check for space on server
		while (true){
			try {
				System.out.println("***** Server listening for clients *****");
				client = ss.accept();
				int i = 0;
				for (i = 0; i < maxClientCount; i++){
					if (threads[i] == null){
						(threads[i] = new ClientThread(client, threads)).start();
						break;
					}
				}
				// If server is full, refuse connection
				if (i == maxClientCount){
					PrintStream out = new PrintStream(client.getOutputStream());
					out.println("***** Server too full. Try again later. *****");
					out.close();
					ss.close();
				}
			} catch (IOException ioe){
				// Something went wrong, give details
				System.err.println("I/O ERROR in Server:");
				System.err.println(ioe.getMessage());
				// Clean up
				System.exit(1);
			}
		}
	}
	
	/**
	 * This method handles the process of shutting down the server. If the <code>shutDownFlag</code>
	 * has been set to <code>true</code> by correctly typing the password required, the the server
	 * will inform each client individually and then disconnect each one before closing the
	 * <code>Server Socket</code>.
	 */
	public static void shutDown(){
		try {
			if (shutDownFlag == true){
				// Individually tell the clients that the server is shutting down
				for (int i = 0; i < maxClientCount; i++){
					if (threads[i] != null){
						threads[i].out.println("***** Server is now shutting down *****");
						threads[i].out.println("***** Goodbye *****");
						threads[i].out.flush();
					}
				}
				// Individually close connection to clients
				for (int i = 0; i < maxClientCount; i++){
					if (threads[i] != null){
						threads[i].clientSocket.close();
					}
				}
				// Shut server down
				ss.close();
			}
		} catch (Exception e) {
			// Something went wrong, give details
			System.err.println("There was a problem shutting down the server!");
			System.err.println(e.getMessage());
			// Clean up
			System.exit(1);
		}
	}
	
	/**
	 * This class extends {@link Thread } which will allow clients to become <code>threads</code>
	 * in the Server to allow concurrency. In this class, everything the client does
	 * will be handled by the method {@link ClientThread#run()} since we need the
	 * client to run as a <code>thread</code>.
	 * 
	 * @author Alexander Finnigan - ID: 201084157
	 *
	 */
	static class ClientThread extends Thread {
		// Declare BufferedReader and PrintWriter
		private BufferedReader in;
		private PrintWriter out;
		// Declare socket for client
		private Socket clientSocket;
		//Declare the maxClientCount and threads for use here
		private int maxClientCount;
		private ClientThread[] threads;
		// Create a HashSet of Strings which will store all of the usernames for connected clients
		private HashSet<String> users = new HashSet<String>();
		
		/**
		 * This constructor takes the variable <code>clientSocket</code> and array <code>threads</code>
		 * to be used in this class.
		 * 
		 * @param clientSocket - the Socket used in {@link Server#main(String[])} for a connected client
		 * @param threads - an array from {@link Server } of type <code>ClientThread</code> which stores
		 * 					all of the clients in threads.
		 */
		//---------------------------------CONSTRUCTOR------------------------------------
		public ClientThread(Socket clientSocket, ClientThread[] threads){
			// Use socket and threads from Server class  
			this.clientSocket = clientSocket;
			this.threads = threads;
			maxClientCount = threads.length;
		}
		
		/**
		 * This method is required if we wish to use the clients as threads. It handles the creation of the
		 * clients username, any input/output from clients and any of the server commands. We are required
		 * to override {@link Thread#run()} if we want the thread to do what we want.
		 */
		public void run(){
			// Use threads from Server
			int maxClientCount = this.maxClientCount;
			ClientThread[] threads = this.threads;
			
			try {
				System.out.println("***** Client connected to Server *****");
				System.out.println("***** Client logging in *****");
				// Set BufferedReader and PrintWriter for use
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream());
				// Ask for username
				out.println("Enter a username: ");
				out.flush();
				username = in.readLine();
				// Check username is unique
				boolean unique = false;
				while (unique == false){
					int totalUsers = users.size();
					users.add(username);
					if (users.size() != totalUsers){
						unique = true;
					} else {
						out.print("***** Username already in use *****");
						out.println("***** Please choose another *****");
						out.flush();
						username = in.readLine();
					}
				}
				// Accept client
				synchronized (this) {	
					out.println("Welcome "+username+" to the chat room!");
					out.println("(Type '!help' for command options)");
					out.flush();
				}
				System.out.println("***** Client successfully logged in *****");
				
				// Inform clients that a new user has joined
				synchronized (this){
					for (int i = 0; i < maxClientCount; i++){
						if (threads[i] != null){
							threads[i].out.println("***** "+username+" has joined the chat room! *****");
							threads[i].out.flush();
						}
					}
				}
				// Increment client connection total
				connections++;
				// Start client timer
				clientStartTime = System.currentTimeMillis();
				
				// Start taking user input
				while (true){
					String line = in.readLine();
					synchronized (this){
						for (int i = 0; i < maxClientCount; i++){
							// If user called command, do not send to other clients
							if (line.trim().startsWith("!")){
								break;
							}
							// Else send to clients
							if(threads[i] != null){
								threads[i].out.println("["+username+"]: "+line);
								threads[i].out.flush();
							}
						}
					}
					// Print options for client
					synchronized (this) {	
						if (line.equals("!help")){
								out.println("Client Commands: !help - shows list of commands");
								out.println("\t\t !quit - leaves the server");
								out.println("\t\t !shutDown - asks for password to shut down the server");
								out.println("\t\t !connections - returns the number of clients currently connected");
								out.println("\t\t !serverTime - returns how long the server as been running for");
								out.println("\t\t !clientTime - returns how long you have been connected to the server");
								out.println("\t\t !serverIP - returns the server IP");
								out.flush();
						}
						// Leave server
						if (line.equals("!quit")){
							break;
						}
						// Shut down server
						if (line.equals("!shutDown")){
							// Ask for password to close server
							out.println("Please enter password to close Server:");
							String attempt = in.readLine();
							if (password == attempt){
								shutDownFlag = true;
								// Call shutDown method in Server
								Server.shutDown();
							}
						}
						// Return total number of client connections
						if (line.equals("!connections")){
								out.println("Total server connections: "+connections);
						}
						// Return how long the server has been running in seconds
						if (line.equals("!serverTime")){
								endTime = System.currentTimeMillis();
								long actualTime = (int) (endTime - serverStartTime) / 1000;
								out.println("Total server runtime: "+actualTime+" seconds");
						}
						// Return how long the client has been connected in seconds
						if (line.equals("!clientTime")){
								endTime = System.currentTimeMillis();
								long actualTime = (int) (endTime - clientStartTime) / 1000;
								out.println("Total time connected: "+actualTime+" seconds");
						}
						// Return server IP
						if (line.equals("!serverIP")){
								InetAddress ip = InetAddress.getLocalHost();
								out.println("Server IP address: "+ip.getHostAddress());
						}
					}
				}
				// Inform clients that another client has left the server
					for (int i = 0; i < maxClientCount; i++){
						if(threads[i] != null && threads[i] != this){
							threads[i].out.println("***** "+username+" has left the chat room! *****");
							threads[i].out.flush();
						}
					}
				// Inform client that they have left
				synchronized (this){
					out.print("***** You has left! *****");
				}
				// Decrement the total client connection number
				connections--;
				
				// Remove client from thread array
				for (int i = 0; i < maxClientCount; i++){
					if (threads[i] == this){
						threads[i] = null;
					}
				}
				
				// Close I/O and sockets
				in.close();
				out.close();
				clientSocket.close();
			} catch (SocketException se){
				// Something went wrong with the socket, give details
				System.err.println("SOCKET ERROR in server:");
				System.err.println(se);
				// Clean up
				System.exit(1);
			} catch (IOException ioe){
				// Something went wrong, give details
				System.err.println("I/O ERROR in server:");
				System.err.println(ioe.getMessage());
				// Clean up
				System.exit(1);
			}
		}
	}
}