import java.io.IOException;
import java.rmi.Remote;


public interface ReplicaMasterInterface extends Remote{
	
	/**
	 * creates the file at the replica server 
	 * @param fileName
	 * @throws IOException 
	 */
	void createFile(String fileName) throws IOException;
	
	void writeReply();
	
	void readReply();
	
}
