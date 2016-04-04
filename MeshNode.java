//All the ugly backend - IP addresses, engine, communication
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sharkfw.kep.SharkProtocolNotSupportedException;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.J2SEAndroidSharkEngine;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;

public class MeshNode implements KPListener{
	private SharkEngine engine = null;
	protected final MeshKPort kport;
	private String address;
	private int port;
	public MeshUser user;
	private String semantic;
	private PeerSTSet peerList; // Raw peer list
	
	public MeshNode(String usrname, String si, String addr, int port) throws SharkProtocolNotSupportedException, IOException, SharkKBException{
		//Start the engine
		this.engine = new J2SEAndroidSharkEngine();
		this.port = port;
		this.user = new MeshUser(usrname);
		this.semantic = si;
		this.address = addr;
		//Construct a peer
		PeerSemanticTag peer = InMemoSharkKB.createInMemoPeerSemanticTag(user.getUsername(), this.semantic, this.address);
		this.user.knowledge.setOwner(peer);
		//Initiate Knowledge Port with Peer
		this.kport = new MeshKPort(this.engine, peer, this);
		this.kport.addListener(this);
		//Initiate node
		this.engine.startTCP(this.port);
		this.peerList = (this.user).knowledge.getPeerSTSet();
	}
	
	public void stop() {
		this.engine.stop();
		System.out.println("Node " + user.getUsername() + " has stopped");
	}

	public void receivedMessage(String str, PeerSemanticTag peer) throws SharkKBException {
		if (!str.isEmpty() ){
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
			Date date = new Date();

			if (this.user != null)
				this.user.addTweet(str, peer, ""+df.format(date));
		}
	}
	
	public String getName(){
		return this.user.getUsername();
	}
	
	public String getAddress(){
		return this.address;
	}
	
	public boolean inConnectionList(PeerSemanticTag peer) {
		try {
			return !(peerList.fragment(peer).equals(null));
		} catch (SharkKBException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void addConnection(String name, String si, String addr) {
		try {
			peerList.createPeerSemanticTag(name, si, addr);
		} catch (SharkKBException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void exposeSent(KnowledgePort arg0, SharkCS arg1) {
		System.out.println("exposeSent triggered");
		
	}

	@Override
	public void insertSent(KnowledgePort arg0, Knowledge arg1) {
		System.out.println("insertSent triggered");
	}

	@Override
	public void knowledgeAssimilated(KnowledgePort arg0, ContextPoint arg1) {
		System.out.println("knowledgeAssimilated triggered");
		
	}
}
