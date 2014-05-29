import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;


public class Client {


	MasterServerClientInterface masterStub;
	static Registry registry;
	int regPort = Configurations.REG_PORT;
	String regAddr = Configurations.REG_ADDR;
	int chunkSize = Configurations.CHUNK_SIZE; // in bytes 
	
	private Client() {
		try {
			registry = LocateRegistry.getRegistry(regAddr, regPort);
			masterStub =  (MasterServerClientInterface) registry.lookup("MasterServer");
			System.out.println("[@client] Master Stub fetched successfuly");
		} catch (RemoteException | NotBoundException e) {
			// fatal error .. no registry could be linked
			e.printStackTrace();
		}
	}

	private byte[] read(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> locations = masterStub.read(fileName);
		System.out.println("[@client] Master Granted read operation");
		
		// TODO fetch from all and verify 
		ReplicaLoc replicaLoc = locations.get(0);

		ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+replicaLoc.getId());
		FileContent fileContent = replicaStub.read(fileName);
		System.out.println("[@client] read operation completed successfuly");
		return fileContent.getData();
	}
	
	private void write (String fileName, byte[] data) throws IOException, NotBoundException, MessageNotFoundException{
		WriteAck ackMsg = masterStub.write(fileName);
		ReplicaServerClientInterface replicaStub = (ReplicaServerClientInterface) registry.lookup("ReplicaClient"+ackMsg.getLoc().getId());
		
		System.out.println("[@client] Master granted write operation");
		
		byte[] chunk = new byte[chunkSize];
		int segN = (int) Math.ceil(data.length/chunkSize);
		FileContent fileContent = new FileContent(fileName);
		ChunkAck chunkAck;
		
		for (int i = 0; i < segN; i++) {
			System.arraycopy(data, i*chunkSize, chunk, 0, chunkSize);
			fileContent.setData(chunk);
			do { 
				chunkAck = replicaStub.write(ackMsg.getTransactionId(), i, fileContent);
			} while(chunkAck.getSeqNo() == i);
			 
		}
		System.out.println("[@client] write operation complete");
		replicaStub.commit(ackMsg.getTransactionId(), segN);
		System.out.println("[@client] commit operation complete");
	}
	
	public static void main(String[] args) {
		//String host = (args.length < 1) ? null : args[0];
		try {
			Client c = new Client();
			char[] ss = "real madrid won la decima 10".toCharArray();
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

}
