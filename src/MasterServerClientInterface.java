import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MasterServerClientInterface extends Remote {

	/**
	 * Read file from server
	 * 
	 * @param fileName
	 * @return File data
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RemoteException
	 */
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException,
			IOException, RemoteException;

	/**
	 * Start a new write transaction
	 * 
	 * @param fileName
	 * @return the required info
	 * @throws RemoteException
	 * @throws IOException
	 */
	public WriteAck write(String data) throws RemoteException, IOException;

}
