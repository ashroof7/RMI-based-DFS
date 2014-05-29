
public class ChunkAck {

	private long transactionId;
	private long seqNo;

	
	public ChunkAck(long tid, long seqNo) {
		this.transactionId = tid;
		this.seqNo = seqNo;
	}
	
	public long getSeqNo(){
		return seqNo;
	}
	
	public long getTxnID() {
		return transactionId;
	}
}
