
public class ReplicaLoc {
	private String address;
	private int id;
	private boolean isAlive;
	
	public ReplicaLoc(int id, String address, boolean isAlive) {
		this.id = id;
		this.address = address;
		this.isAlive = isAlive;
	}
	
	boolean isAlive(){
		return isAlive;
	}
	
	int getId(){
		return id;
	}
	
	void setAlive(boolean isAlive){
		this.isAlive = isAlive;
	}
	
	String getAddress(){
		return address;
	}
	
}
