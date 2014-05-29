import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;


public interface ReplicaReplicaInterface extends ReplicaInterface {
	
	public boolean reflectUpdate(long txnID, String fileName, ArrayList<byte[]> data) throws RemoteException, IOException;
	
}
