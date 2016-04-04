import java.io.IOException;

import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.SharkException;

public class MeshMain {

	/**
	 * Creates two nodes and tries to handshake
	 * 
	 * @throws IOException
	 * @throws SharkException
	 */

	public static void main(String[] args) throws IOException, SharkException {
		MeshNode Alice = null;
		MeshNode Bob = null;
		MeshNode Charlie = null;
		Alice = new MeshNode("Alice", "alice", "tcp://localhost:1015", 1015);
		System.out.println("Alice initiated");

		nap(50);
		
		Bob = new MeshNode("Bob", "bob", "tcp://localhost:1016", 1016);
		System.out.println("Bob intiated");
		
		nap(50);
		
		Charlie = new MeshNode("Charlie", "chuck", "tcp://localhost:1017", 1017);
		System.out.println("Charlie intiated");

		PeerSemanticTag recipient = InMemoSharkKB.createInMemoPeerSemanticTag(
				Alice.getName(), "alice", Alice.getAddress());

		Bob.kport.sendMessage("Hello alice", recipient);

		nap(50);

		Alice.kport.sendMessage("Thanks");
		
		nap(500);
		
		Charlie.kport.lookingForTweets(null);
		
		System.out.println("Trying to print users...");
		Alice.user.printUser();
		Bob.user.printUser();
		Charlie.user.printUser();

		
		Alice.stop();
		Bob.stop();
		Charlie.stop();
	}
	
	public static void nap(int time){
		try {
		    Thread.sleep(time);                 //1000 milliseconds is one second.
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}

}
