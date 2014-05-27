import java.rmi.Remote;


public interface MasterReplicaInterface extends Remote{
	
	/**
	 * creates the file at the replica server 
	 * @param fileName
	 * @return the operation state 
	 */
	boolean createFile(String fileName);
}
