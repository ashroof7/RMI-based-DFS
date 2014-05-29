import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Main {

	static int regPort = Configurations.REG_PORT;

	public static void main(String[] args) {
		
		
		try {
			LocateRegistry.createRegistry(regPort);
			MasterServerClientInterface master = new Master();
			MasterServerClientInterface stub = 
					(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);

			Registry registry = LocateRegistry.getRegistry(regPort);
			// Bind the remote object's stub in the registry
			registry.rebind("MasterServer", stub);
			System.err.println("Server ready");
		
			
			// respawn replica servers 
			ReplicaServer rs = new ReplicaServer(0, "./");
			System.out.println("replica server state [@ main] = "+rs.isAlive());
		} catch (RemoteException  e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
