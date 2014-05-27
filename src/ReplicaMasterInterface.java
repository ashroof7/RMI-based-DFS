import java.rmi.Remote;


public interface ReplicaMasterInterface extends Remote{
	
	void writeReply();
	
	void readReply();
	
}
