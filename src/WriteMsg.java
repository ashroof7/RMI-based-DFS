
public class WriteMsg {
	private long transactionId;
	private long timeStamp;
	private ReplicaLoc loc;

	
	public WriteMsg(long tid, long timeStamp, ReplicaLoc replicaLoc) {
		this.transactionId = tid;
		this.timeStamp = timeStamp;
		this.loc = replicaLoc;
	}
}
