import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;

import net.sharkfw.knowledgeBase.AbstractSharkKB;
import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.ContextPoint;
import net.sharkfw.knowledgeBase.Information;
import net.sharkfw.knowledgeBase.Interest;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSTSet;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.kp.KPListener;
import net.sharkfw.peer.KEPConnection;
import net.sharkfw.peer.KnowledgePort;
import net.sharkfw.peer.SharkEngine;
import net.sharkfw.system.L;
import net.sharkfw.system.SharkException;


public class MeshKPort extends KnowledgePort{

	private PeerSemanticTag owner = null;
	private PeerSemanticTag near = null;
	private MeshNode listener;
	private SemanticTag tag;

	protected MeshKPort(SharkEngine se, PeerSemanticTag owner, KPListener list) {
		super(se);
		this.owner = owner;
		this.listener = (MeshNode)list;
	}

	// Looking for tweets
	@Override
	protected void doExpose(SharkCS interest, KEPConnection con){

	}
	// Connection Detected
	@Override
	protected void doInsert(Knowledge know, KEPConnection con) {
		Enumeration<ContextPoint> cpNum = know.contextPoints();
		if (!cpNum.equals(null)) {
			ContextPoint context = cpNum.nextElement();
			//if (context != null) System.out.println("Context found");
			ContextCoordinates coords = context.getContextCoordinates();
			//if (coords != null) System.out.println("Coordinates acquired");
			PeerSemanticTag remote = coords.getRemotePeer();
			//if (remote != null) System.out.println("Remote peer found");
			if (!SharkCSAlgebra.identical(remote, this.owner))	return; // Coordinates mismatch, peer =/= owner
			//try{
			PeerSemanticTag intermediate = coords.getPeer(); //Other peer in transaction
			if (intermediate != null && !intermediate.equals(owner)) {
				this.near = intermediate;
				System.out.println("Handshake complete");
			try {
				(this.listener).user.addPeer((this.near).getName(), (this.near).getSI()[0], (this.near).getAddresses()[0]);
			} catch (SharkKBException e) {
				e.printStackTrace();
			}
			try { 
				(this.listener).addConnection((this.near).getName(), (this.near).getSI()[0], (this.near).getAddresses()[0]); 			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Iterator<Information>  iter = context.getInformation();
			if (iter!=null) {
				String str;
				try {
					str = iter.next().getContentAsString();
					this.receivedMessage(str, intermediate);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			}
		}
		else {
			System.out.println("No context points.");
		}
	}
	
	public void lookingForTweets(String tag) throws SharkKBException{
		AbstractSharkKB know = new InMemoSharkKB();
		PeerSTSet peers = InMemoSharkKB.createInMemoPeerSTSet(); // Ping active nodes
		STSet tags = InMemoSharkKB.createInMemoSTSet();
		tags.createSemanticTag(null, "tweet");
		if (tag != null) tags.createSemanticTag(null, tag);
		Interest interest = InMemoSharkKB.createInMemoInterest(tags, null, peers, null, null, null, SharkCS.DIRECTION_OUT);
		
		Enumeration<ContextPoint> cpNum = know.getContextPoints(interest);
		if (cpNum != null) {
			System.out.println(L.cps2String(cpNum));
		} else {
			System.out.println("No context points found");
		}
	}
	
	public void addNode(KPListener listener) {
		this.listener = (MeshNode) listener;
	}
	
    public void sendMessage(String message) throws SharkException, IOException {
        this.sendMessage(message, this.near);
    }

	private void receivedMessage(String str, PeerSemanticTag peer) throws SharkException, IOException{
		if (listener != null) {
			System.out.println("KP received message");
			(listener).receivedMessage(str, peer);
		}
	}
	
	public void sendMessage(String str, PeerSemanticTag peer) throws SharkException, IOException{
		//Set coordinates
		System.out.println("Sending message");
		ContextCoordinates coords = InMemoSharkKB.createInMemoContextCoordinates(null, this.owner, this.owner, peer, null, null, SharkCS.DIRECTION_OUT);
				
		ContextPoint context = InMemoSharkKB.createInMemoContextPoint(coords);
		
		context.addInformation(str);
		
		Knowledge k = InMemoSharkKB.createInMemoKnowledge();
		
		k.addContextPoint(context);
		
		this.sendKnowledge(k, peer);
		
	}

}
