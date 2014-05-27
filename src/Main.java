import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Main {

	final static int regPort = 50000;
	final static String serverName = "Master";

	public static void main(String[] args) {
		
		
		try {
			LocateRegistry.createRegistry(regPort);
			MasterServerClientInterface master = new Master();
			MasterServerClientInterface stub = 
					(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);

			Registry registry = LocateRegistry.getRegistry(regPort);
			// Bind the remote object's stub in the registry
			registry.rebind("MasterServerClientInterface", stub);
			System.err.println("Server ready");
			
		} catch (RemoteException  e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
