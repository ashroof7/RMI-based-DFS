import java.io.IOException;
import java.rmi.Remote;
import java.util.Collection;


public interface ReplicaReplicaInterface extends Remote {
	
	public boolean reflectUpdate(long txnID, String fileName, Collection<byte[]> data) throws IOException;
	
}
