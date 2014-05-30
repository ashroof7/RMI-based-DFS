import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class Main {

	static int regPort = Configurations.REG_PORT;

	static Registry registry ;
	
	/**
	 * respawns replica servers and register replicas at master
	 * @param master
	 * @throws IOException
	 */
	static void respawnReplicaServers(Master master)throws IOException{
		System.out.println("[@main] respawning replica servers ");
		// TODO make file names global
		BufferedReader br = new BufferedReader(new FileReader("repServers.txt"));
		int n = Integer.parseInt(br.readLine().trim());
		ReplicaLoc replicaLoc;
		String s;

		for (int i = 0; i < n; i++) {
			s = br.readLine().trim();
			replicaLoc = new ReplicaLoc(i, s.substring(0, s.indexOf(':')) , true);
			ReplicaServer rs = new ReplicaServer(i, "./"); 

			ReplicaInterface stub = (ReplicaInterface) UnicastRemoteObject.exportObject(rs, 0);
			registry.rebind("ReplicaClient"+i, stub);
			
			master.registerReplicaServer(replicaLoc, stub);
			
			System.out.println("replica server state [@ main] = "+rs.isAlive());
		}
		br.close();
	}
	
	public static void launchClients(){
		try {
			Client c = new Client();
			char[] ss = " Appending to the file \\n".toCharArray();
			byte[] data = new byte[ss.length];
			for (int i = 0; i < ss.length; i++) 
				data[i] = (byte) ss[i];
			
			c.write("file1", data);
			byte[] ret = c.read("file1");
			System.out.println("response: " + ret);
			
		} catch (NotBoundException | IOException | MessageNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		
		try {
			LocateRegistry.createRegistry(regPort);
			registry = LocateRegistry.getRegistry(regPort);
			Master master = new Master();
			MasterServerClientInterface stub = 
					(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);
			registry.rebind("MasterServerClientInterface", stub);
			System.err.println("Server ready");
			
			respawnReplicaServers(master);
			
			launchClients();
			
		} catch (RemoteException  e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
