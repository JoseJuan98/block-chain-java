package no.hvl.dat159;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

import jdk.nashorn.internal.runtime.Context.ThrowErrorManager;
import no.hvl.dat159.util.EncodingUtil;
import no.hvl.dat159.util.HashUtil;
import no.hvl.dat159.util.SignatureUtil;

/**
 * A Wallet keeps the keys and creates signed transactions to be
 * sent to the "network"/node.
 * A wallet also has a name/id to make it easier to identify.
 */
public class Wallet {

	private String id;
	private KeyPair keyPair;

	/*
	 * The single node in this "network" that the wallets knows about.
	 */
	private FullNode networkNode;

	/**
	 * 
	 */
	public Wallet(String id, FullNode node) {
		this.id = id;
		this.networkNode = node;
		this.keyPair = SignatureUtil.generateRandomDSAKeyPair();
	}

	/**
	 * 
	 */
	public Transaction createTransaction(long value, String address) throws Exception {

		// 1. Calculate the balance
		// 2. Check if there are sufficient funds --- Exception?
		// 3. Choose a number of UTXO to be spent - We take ALL 
		//   (= the complete wallet balance)!
		// 4. Calculate change
		// 5. Create an "empty" transaction
		// 6. Add chosen inputs (=ALL)
		// 7. Add 1 or 2 outputs, depending on change
		// 8. Sign the transaction
		long balance = this.calculateBalance();

		if(value <= balance) {

			long change = balance - value;
			Transaction tx = new Transaction(this.getPublicKey());

			for (Map.Entry<Input, Output> iter : this.networkNode.getUtxoMap().getUtxosForAddress(this.getAddress())) {
					tx.addInput(iter.getKey());
			}

			Output out_pay = new Output(value , address);
			tx.addOutput(out_pay);

			if(value < balance) {
				Output out_to_me = new Output(change , this.getAddress());
				tx.addOutput(out_to_me);
			}
			
			tx.signTxUsing(this.keyPair.getPrivate());

			return tx;
		}else {
			return null;
		}
	}

	public String getId() {
		return id;
	}

	/**
	 * 
	 */
	public PublicKey getPublicKey() {
		return this.keyPair.getPublic();
	}

	/**
	 * 
	 */
	public String getAddress() {
		return HashUtil.pubKeyToAddress(getPublicKey());
	}

	/**
	 * 
	 */
	public long calculateBalance() {
		long balance = 0;
		for (Map.Entry<Input, Output> iter : this.networkNode.getUtxoMap().getUtxosForAddress(getAddress())) {
			balance += iter.getValue().getValue();
		}

		return balance;
	}

	/**
	 * 
	 */
	public int getNumberOfUtxos() {
		return networkNode.getUtxoMap().getUtxosForAddress(getAddress()).size();
	}

	public void printOverview() {
		System.out.println();
		System.out.println(id + " overview");
		System.out.println("----------------------");
		System.out.println("   Address    : " + getAddress());
		System.out.println("   Balance    : " + calculateBalance());
		System.out.println("   # of UTXOs : " + getNumberOfUtxos());

	}

	public void printOverviewIndented() {
		System.out.println("      " + id + " with address : " + getAddress());
	}

}
