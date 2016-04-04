import net.sharkfw.knowledgeBase.ContextCoordinates;
import net.sharkfw.knowledgeBase.FragmentationParameter;
import net.sharkfw.knowledgeBase.Knowledge;
import net.sharkfw.knowledgeBase.PeerSemanticTag;
import net.sharkfw.knowledgeBase.PeerTXSemanticTag;
import net.sharkfw.knowledgeBase.PeerTaxonomy;
import net.sharkfw.knowledgeBase.STSet;
import net.sharkfw.knowledgeBase.SemanticNet;
import net.sharkfw.knowledgeBase.SemanticTag;
import net.sharkfw.knowledgeBase.SharkCS;
import net.sharkfw.knowledgeBase.SharkCSAlgebra;
import net.sharkfw.knowledgeBase.SharkKB;
import net.sharkfw.knowledgeBase.SharkKBException;
import net.sharkfw.knowledgeBase.TXSemanticTag;
import net.sharkfw.knowledgeBase.inmemory.InMemoSharkKB;
import net.sharkfw.system.L;

//Twitter user data - username, followers, following
public class MeshUser {

	private String username;
	private PeerTXSemanticTag peerList;
	private PeerTXSemanticTag following; // Specific peers to pull information
											// from
	private PeerTXSemanticTag followers; // Specific peers to push information
											// to
	public SharkKB knowledge;
	private PeerTaxonomy tax;
	private SemanticNet tweets;
	private ContextCoordinates knowledgeCoords;

	public MeshUser(String username) {
		this.username = username;
		this.knowledge = new InMemoSharkKB();
		try {
			this.tax = this.knowledge.getPeersAsTaxonomy();
			this.peerList = tax.createPeerTXSemanticTag("Peers", username+"-p",
					(String) null);
			this.following = tax.createPeerTXSemanticTag("Following", username+"-f",
					(String) null);
			this.followers = tax.createPeerTXSemanticTag("Followers", username+"-z",
					(String) null);
			this.knowledgeCoords = this.knowledge.createContextCoordinates(
					null, null, this.knowledge.getOwner(), null, null, null,
					SharkCS.DIRECTION_INOUT);
			this.tweets = knowledge.getTopicsAsSemanticNet();
		} catch (SharkKBException e) {
			e.printStackTrace();
		}

	}

	public void printUser() throws SharkKBException {
		System.out.println("PrintUser called");
		FragmentationParameter[] backFrag = new FragmentationParameter[SharkCS.MAXDIMENSIONS];
		for (int i = 0; i < SharkCS.MAXDIMENSIONS; i++) {
			backFrag[i] = FragmentationParameter.getZeroFP();
		}
		backFrag[SharkCS.DIM_PEER] = new FragmentationParameter(false, true, 1);
		Knowledge printK = SharkCSAlgebra.extract(knowledge, knowledgeCoords,
				backFrag);
		System.out.println(L.knowledge2String(knowledge.createKnowledge()));
	}

	public boolean isFollowing(PeerSemanticTag peer) {

		return false;
	}
	
	public boolean addTweet(String message, PeerSemanticTag peer, String time) throws SharkKBException{
		String peerName = L.semanticTag2String(peer);
		int begin = peerName.indexOf("\"")+1;
		String from = peerName.substring(begin);
		int end = from.indexOf("\"");
		String name = from.substring(0, end);
		String peerSI = peer.getSI()[0];
		String[] si = {peerSI, "tweet"};
		SemanticTag tweet = tweets.createSemanticTag("@" + name + " : " + message + " | " + time, si);
		if (tweet != null) {
			ContextCoordinates coords = InMemoSharkKB.createInMemoContextCoordinates(tweet, null, null, peer, null, null, SharkCS.DIRECTION_INOUT);
			InMemoSharkKB.createInMemoContextPoint(coords);
			return true;
		}
		return false;
	}

	public boolean addPeer(String name, String si, String addr) throws SharkKBException {
		PeerTXSemanticTag newPeer = tax.createPeerTXSemanticTag(name, si, addr);
		newPeer.move(peerList);
		return true;
	}

	public void addFollowing(PeerSemanticTag peer) throws SharkKBException {
		TXSemanticTag newFollowing = tax.getSemanticTag(peer.getName());
		newFollowing.move(following);

	}

	public void addFollower(PeerSemanticTag peer) throws SharkKBException {

		TXSemanticTag newFollower = tax.getSemanticTag(peer.getName());
		newFollower.move(followers);

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
