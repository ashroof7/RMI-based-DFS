import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class Master implements MasterReplicaInterface, MasterServerClientInterface, Remote{

	Map<String,	 ArrayList<ReplicaLoc> > filesLocationMap;
	Map<String,	 ReplicaLoc> primaryReplicaMap;
	Map<Integer, String> activeTransactions; // active transactions <ID, fileName>
	List<ReplicaLoc> replicaServersLocs;
	List<ReplicaMasterInterface> replicaServersStubs; // TODO init


	Random randomGen;
	int MAX_TRANSACTION = 1000;
	int REPLICATION_N = 3; // number of file replicas
	int nextTID;

	public Master() {
		filesLocationMap = new HashMap<String,	 ArrayList<ReplicaLoc>>();
		primaryReplicaMap = new HashMap<String, ReplicaLoc>();
		activeTransactions = new HashMap<Integer, String>();
		nextTID = 0;
		randomGen = new Random();
		try {
			respawnReplicaServers();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException,
	IOException, RemoteException {
		System.out.println("tezak 7amra howa keda sha3'al");
		ArrayList<ReplicaLoc> replicaLocs= filesLocationMap.get(fileName);
		if (replicaLocs == null)
			throw new FileNotFoundException();
		return replicaLocs;
	}

	@Override
	public WriteAck write(String fileName) throws RemoteException, IOException {
		long timeStamp = System.currentTimeMillis();

		ArrayList<ReplicaLoc> replicaLocs= filesLocationMap.get(fileName);
		int tid = nextTID++;
		if (replicaLocs == null)	// file not found
			createNewFile(fileName);

		ReplicaLoc primaryReplicaLoc = primaryReplicaMap.get(fileName);

		if (primaryReplicaLoc == null)
			throw new IllegalStateException("No primary replica found");

		// if the primary replica is down .. elect a new replica
		if (!primaryReplicaLoc.isAlive()){
			assignNewMaster(fileName);
			primaryReplicaLoc = primaryReplicaMap.get(fileName);
		}

		return new WriteAck(tid, timeStamp,primaryReplicaLoc);
	}


	/**
	 * iterate over all replicas and check state
	 */
	private void checkReplicasAlive(){
		for (ReplicaLoc replicaLoc : replicaServersLocs) {
				replicaServersStubs.get(replicaLoc.getId()).isAlive();
		}
	}
	
	/**
	 * @param fileName
	 * elects a new primary replica for the given file
	 */
	private void assignNewMaster(String fileName){
		List<ReplicaLoc> replicas = filesLocationMap.get(fileName);
		boolean newPrimaryAssigned = false;
		for (ReplicaLoc replicaLoc : replicas) {
			if (replicaLoc.isAlive()){
				newPrimaryAssigned = true;
				primaryReplicaMap.put(fileName, replicaLoc);
				replicaServersStubs.get(replicaLoc.getId()).takeCharge(fileName, filesLocationMap.get(fileName));
				break;
			}
		}
		
		if (!newPrimaryAssigned){
			//TODO a7a ya3ni
		}
	}

	/**
	 * creates a new file @ N replica servers that are randomly chosen
	 * elect the primary replica at random
	 * @param fileName
	 */
	private void createNewFile(String fileName){
		int luckyReplicas[] = new int[REPLICATION_N];
		ArrayList<ReplicaLoc> replicas = new ArrayList<ReplicaLoc>();

		for (int i = 0; i < luckyReplicas.length; i++) {
			while(!replicaServersLocs.get(luckyReplicas[i]).isAlive())
				luckyReplicas[i] = randomGen.nextInt(REPLICATION_N-1);

			// add the lucky replica to the list of replicas maintaining the file
			replicas.add(replicaServersLocs.get(luckyReplicas[i]));
			// create the file at the lucky replicas 
			try {
				replicaServersStubs.get(luckyReplicas[i]).createFile(fileName);
			} catch (IOException e) {
				// failed to create the file at replica server 
				e.printStackTrace();
			}

		}

		// the primary replica is the first lucky replica picked
		int primary = luckyReplicas[0];

		filesLocationMap.put(fileName, replicas);
		primaryReplicaMap.put(fileName, replicaServersLocs.get(primary));

	}

	private void respawnReplicaServers()throws IOException{
		// TODO make file names global
		BufferedReader br = new BufferedReader(new FileReader("repServers.txt"));
		int n = Integer.parseInt(br.readLine().trim());
		replicaServersLocs = new ArrayList<>(n);
		ReplicaLoc replicaLoc;
		String s;

		for (int i = 0; i < n; i++) {
			s = br.readLine().trim();
			replicaLoc = new ReplicaLoc(i, s.substring(0, s.indexOf(':')) , false);
			// TODO respawn replicas here 
			replicaServersLocs.add(replicaLoc);
			// TODO replicaServers.add(......);
		}
		br.close();
	}
	
	//TODO add commit to master to handle meta-data
}
