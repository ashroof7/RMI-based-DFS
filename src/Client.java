import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;


public class Client {

	

    private Client() {
    	
    }

    public static void main(String[] args) {

//        String host = (args.length < 1) ? null : args[0];
    	String host = "localhost";
        String filename = "soso"; 
        
        try {
            Registry registry = LocateRegistry.getRegistry(host, 50000);
            MasterServerClientInterface stub = (MasterServerClientInterface) registry.lookup("MasterServerClientInterface");
            List<ReplicaLoc> response = stub.read(filename);
            
            System.out.println("response: " + response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }	

}
