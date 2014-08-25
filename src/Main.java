import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


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
			char[] ss = "File 1 test test END ".toCharArray();
			byte[] data = new byte[ss.length];
			for (int i = 0; i < ss.length; i++) 
				data[i] = (byte) ss[i];
			
			c.write("file1", data);
			byte[] ret = c.read("file1");
			System.out.println("file1: " + ret);
			
			c = new Client();
			ss = "File 1 Again Again END ".toCharArray();
			data = new byte[ss.length];
			for (int i = 0; i < ss.length; i++) 
				data[i] = (byte) ss[i];
			
			c.write("file1", data);
			ret = c.read("file1");
			System.out.println("file1: " + ret);

			c = new Client();
			ss = "File 2 test test END ".toCharArray();
			data = new byte[ss.length];
			for (int i = 0; i < ss.length; i++) 
				data[i] = (byte) ss[i];
			
			c.write("file2", data);
			ret = c.read("file2");
			System.out.println("file2: " + ret);
			
		} catch (NotBoundException | IOException | MessageNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public  static void customTest() throws IOException, NotBoundException, MessageNotFoundException{
		Client c = new Client();
		char[] ss = "[INITIAL DATA!]".toCharArray();
		byte[] data = new byte[ss.length];
		for (int i = 0; i < ss.length; i++) 
			data[i] = (byte) ss[i];
		
		c.write("file1", data);
		
		c = new Client();
		ss = "File 1 test test END ".toCharArray();
		data = new byte[ss.length];
		for (int i = 0; i < ss.length; i++) 
			data[i] = (byte) ss[i];
	
		
		String fileName = "file1";
		byte[] chunk = new byte[Configurations.CHUNK_SIZE];
		
		int seqN = ss.length/Configurations.CHUNK_SIZE;
		
		WriteAck ackMsg = c.masterStub.write(fileName);
		ReplicaServerClientInterface stub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+ackMsg.getLoc().getId());
		
		FileContent fileContent;
		ChunkAck chunkAck;
//		for (int i = 0; i < seqN; i++) {
			System.arraycopy(data, 0*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
			fileContent = new FileContent(fileName, chunk);
			chunkAck = stub.write(ackMsg.getTransactionId(), 0, fileContent);
			
			
			System.arraycopy(data, 1*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
			fileContent = new FileContent(fileName, chunk);
			chunkAck = stub.write(ackMsg.getTransactionId(), 1, fileContent);
			
			// read here 
			List<ReplicaLoc> locations = c.masterStub.read(fileName);
			System.err.println("[@CustomTest] Read1 started ");
			
			// TODO fetch from all and verify 
			ReplicaLoc replicaLoc = locations.get(0);
			ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaLoc.getId());
			fileContent = replicaStub.read(fileName);
			System.err.println("[@CustomTest] data:");
			System.err.println(new String(fileContent.getData()));

			
			// continue write 
			System.arraycopy(data, 2*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
			fileContent = new FileContent(fileName, chunk);
			chunkAck = stub.write(ackMsg.getTransactionId(), 2, fileContent);
			
			
			System.arraycopy(data, 3*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
			fileContent = new FileContent(fileName, chunk);
			chunkAck = stub.write(ackMsg.getTransactionId(), 3, fileContent);
			
			
			//commit
			ReplicaLoc primaryLoc = c.masterStub.locatePrimaryReplica(fileName);
			ReplicaServerClientInterface primaryStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+primaryLoc.getId());
			primaryStub.commit(ackMsg.getTransactionId(), seqN);
			
			// read
			locations = c.masterStub.read(fileName);
			System.err.println("[@CustomTest] Read3 started ");
			
			replicaLoc = locations.get(0);
			replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaLoc.getId());
			fileContent = replicaStub.read(fileName);
			System.err.println("[@CustomTest] data:");
			System.err.println(new String(fileContent.getData()));
		
	}
	
	static Master startMaster() throws AccessException, RemoteException{
		Master master = new Master();
		MasterServerClientInterface stub = 
				(MasterServerClientInterface) UnicastRemoteObject.exportObject(master, 0);
		registry.rebind("MasterServerClientInterface", stub);
		System.err.println("Server ready");
		return master;
	}
	
	public static void main(String[] args) throws IOException {
		
		
		try {
			LocateRegistry.createRegistry(regPort);
			registry = LocateRegistry.getRegistry(regPort);
			
			Master master = startMaster();
			respawnReplicaServers(master);
			
			customTest();
//			launchClients();
			
		} catch (RemoteException | NotBoundException | MessageNotFoundException  e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
