import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReplicaServer implements ReplicaServerClientInterface,
		ReplicaMasterInterface, ReplicaReplicaInterface, Remote {

	
	private String dir;
	
	private int id;
	private Map<Long, String> activeTxn; // map between active transactions and file names
	private Map<Long, Map<Long, byte[]>> txnFileMap; // map between transaction ID and corresponding file chunks
	private Map<String,	 List<ReplicaReplicaInterface> > filesReplicaMap; //replicas where files that this replica is its master are replicated  
	private Map<Integer, ReplicaLoc> replicaServersLoc; // Map<ReplicaID, replicaLoc>
	private Map<Integer, ReplicaReplicaInterface> replicaServersStubs; // Map<ReplicaID, replicaStub>
	
	//	TODO move to mesh 3aref eh 
	public static final int CHUNK_SIZE = 1024; // in bytes 
	
	
	
	public ReplicaServer(int id, String dir) {
		this.id = id;
		this.dir = dir+"/";
		txnFileMap = new TreeMap<Long, Map<Long, byte[]>>();
		activeTxn = new TreeMap<Long, String>();
		filesReplicaMap = new TreeMap<String, List<ReplicaReplicaInterface>>();
		replicaServersLoc = new TreeMap<Integer, ReplicaLoc>();
		replicaServersStubs = new TreeMap<Integer, ReplicaReplicaInterface>();
		init();
	}

	@Override
	public void createFile(String fileName) throws IOException {
		File file = new File(dir+fileName);
		file.createNewFile();
	}

	@Override
	public FileContent read(String fileName) throws FileNotFoundException,
			RemoteException, IOException {
		File f = new File(dir+fileName);
		
		@SuppressWarnings("resource")
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(f));
		
		// assuming files are small and can fit in memory
		byte data[] = new byte[(int) (f.length())];
		br.read(data);
		
		FileContent content = new FileContent(fileName, data);
		return content;
	}

	@Override
	public ChunkAck write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException {
		
		// if this is not the first message of the write transaction
		if (!txnFileMap.containsKey(txnID)){
			txnFileMap.put(txnID, new TreeMap<Long, byte[]>());
			activeTxn.put(txnID, data.getFileName());
		}

		Map<Long, byte[]> chunkMap =  txnFileMap.get(txnID);
		chunkMap.put(msgSeqNum, data.getData());
		return new ChunkAck(txnID, msgSeqNum);
	}

	@Override
	public boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, RemoteException, IOException {
		Map<Long, byte[]> chunkMap = txnFileMap.get(txnID);
		if (chunkMap.size() < numOfMsgs)
			throw new MessageNotFoundException();
		
		String fileName = activeTxn.get(txnID);
		List<ReplicaReplicaInterface> slaveReplicas = filesReplicaMap.get(fileName);
		
		for (ReplicaReplicaInterface replica : slaveReplicas) {
			boolean sucess = replica.reflectUpdate(txnID, fileName, chunkMap.values());
			if (!sucess) {
				// TODO handle failure 
			}
		}
		
		// TODO acquire Lock
		BufferedOutputStream bw =new BufferedOutputStream(new FileOutputStream(dir+fileName));
		
		for (Iterator<byte[]> iterator = chunkMap.values().iterator(); iterator.hasNext();) 
			bw.write(iterator.next());
		
		bw.close();
		activeTxn.remove(txnID);
		txnFileMap.remove(txnID);
		// TODO release lock
		
		
		return false;
	}

	@Override
	public boolean abort(long txnID) throws RemoteException {
		activeTxn.remove(txnID);
		filesReplicaMap.remove(txnID);
		return false;
	}

	/**
	 * initializes the replica servers & creates the directories required 
	 */
	private void init(){
		File file = new File(dir);
		if (!file.exists())
			file.mkdir();
		//TODO create replica stub and add to registry 	
	}

	@Override
	public boolean reflectUpdate(long txnID, String fileName, Collection<byte[]> data) throws IOException{
		BufferedOutputStream bw =new BufferedOutputStream(new FileOutputStream(dir+fileName));
		
		for (Iterator<byte[]> iterator = data.iterator(); iterator.hasNext();) 
			bw.write(iterator.next());
		bw.close();
		
		activeTxn.remove(txnID);
		return true;
	}

	@Override
	public void takeCharge(String fileName, List<ReplicaLoc> slaveReplicas) {
		List<ReplicaReplicaInterface> slaveReplicasStubs = new ArrayList<ReplicaReplicaInterface>(slaveReplicas.size());
		
		for (ReplicaLoc loc : slaveReplicas) {
			// if the current locations is this replica .. ignore
			if (loc.getId() == this.id)
				continue;
			
			// if this is a new replica generate stub for this replica
			if (!replicaServersLoc.containsKey(loc.getId())){
				replicaServersLoc.put(loc.getId(), loc);
				replicaServersStubs.put(loc.getId(), genStub(loc));
			}
			
			ReplicaReplicaInterface replicaStub = replicaServersStubs.get(loc.getId());
			slaveReplicasStubs.add(replicaStub);
		}
		
		filesReplicaMap.put(fileName, slaveReplicasStubs);
	}
	
	private ReplicaReplicaInterface genStub(ReplicaLoc loc){
		//TODO manual generated stub [[fakss]]
		return null;
	}

	@Override
	public boolean isAlive() {
		return true;
	}
	
}
