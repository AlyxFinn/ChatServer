import java.util.Scanner;

/**
 * This class will be used to ask for an IP address to connect to and the port number
 * the server is running on. It will then instantiate the class {@link ClientInstance } 
 * to handle connected to the server and handling the input and output.
 * 
 * @author Alexander Finnigan - ID: 201084157
 *
 */
public class ClientMain {
	
	/**
	 * The main method will ask for the IP address and port number of the server. It
	 * will then use that information and instantiate {@link ClientInstance } to connect
	 * to the server. It will then execute {@link ClientInstance#RunClient()} to handle 
	 * use of the client in the server.
	 * 
	 * @param args - the usual argument in a main method.
	 */
	/*-------------------MAIN METHOD------------------ */
	public static void main(String[] args){
		
		// Declare host and port variables
		int port;
		String host;
		// Declare boolean variable for closing the Scanner
		boolean done = false;
		
		// Instantiate Scanner
		Scanner input = new Scanner(System.in);
		
		// Ask for host IP
		System.out.println("Please enter an IP to connect to:");
		host = input.nextLine();
		
		// Ask for host port
		System.out.println("Please enter the server's port number:");
		port = input.nextInt();
		
		// Instantiate ClientInstance with IP and port
		ClientInstance main = new ClientInstance(host, port);
		
		// Run the client
		main.RunClient();
		
		// Close Scanner
		if (done){
			input.close();
		}
	}
	
}
