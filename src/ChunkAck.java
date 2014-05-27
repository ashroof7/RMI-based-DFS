
public class ChunkAck {

	private long transactionId;
	private long seqNo;

	
	public ChunkAck(long tid, long seqNo) {
		this.transactionId = tid;
		this.seqNo = seqNo;
	}
}
