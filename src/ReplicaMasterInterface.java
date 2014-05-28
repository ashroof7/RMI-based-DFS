import java.io.IOException;
import java.rmi.Remote;
import java.util.List;


public interface ReplicaMasterInterface extends Remote{
	
	/**
	 * creates the file at the replica server 
	 * @param fileName
	 * @throws IOException 
	 */
	void createFile(String fileName) throws IOException;
	
	/**
	 * makes the current replica the master of the passed file
	 * @param fileName 
	 * @param slaveReplicas another replicas having the files
	 */
	void takeCharge(String fileName, List<ReplicaLoc> slaveReplicas) ;
	
	/**
	 * @return true if the replica alive and received the call .. no return otherwise,
	 */
	boolean isAlive();
	
}
