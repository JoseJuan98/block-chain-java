package no.hvl.dat159;

import no.hvl.dat159.util.EncodingUtil;
import no.hvl.dat159.util.HashUtil;

/**
 * The basic building block in the blockchain.
 */
public class Block {

	/*
	 * The "block header" consists of the prevBlockHash, merkleRoot and nonce.
	 * The prevBlockHash is a 64 char long hex representation of the previous hash.
	 * The merkleRoot is a "fingerprint" representing all the transactions in the
	 * block. The input to calculating the blockHash is this "block header".
	 */
	private String prevBlockHash;
	private String merkleRoot;
	private long nonce;

	/*
	 * The "block body" contains a coinbaseTx and one ordinary transaction.
	 */
	private CoinbaseTx coinbaseTx;
	private Transaction transaction;

	/* --------------------------------------------------------------------- */

	/**
	 * Constructs a non-mined block. To be a valid block, the block must
	 * then be "mined" so that the blockHash matches the MINING_TARGET 
	 * binary pattern.
	 */
	public Block(String prevBlockHash, CoinbaseTx coinbaseTx, Transaction tx) {

		//Remember to calculate the Merkle root
		this.prevBlockHash = prevBlockHash;
		this.coinbaseTx = coinbaseTx;
		this.transaction = tx;

		if(tx == null) {
			this.merkleRoot = EncodingUtil.bytesToHex(HashUtil.sha256(coinbaseTx.getTxId()));
		}else {
			this.merkleRoot = EncodingUtil.bytesToHex(HashUtil.sha256(coinbaseTx.getTxId() + tx.getTxId()));
		}

		this.nonce = 0;
	}

	/**
	 * "Mines" the block, that is selecting a nonce so that the hash puzzle
	 * requirement is satisfied.
	 */
	public void mine() {
		nonce = 0;
		while(!getBlockHashAsBinaryString().matches(Blockchain.MINING_TARGET)) {
			nonce++;
		}
	}

	/**
	 * Calculates whether the block is valid or not. To be a valid block,
	 * ALL contents must be valid, and the hash of the block must satisfy
	 * the hash puzzle requirement.
	 */
	public boolean isValid() {
		if(this.transaction == null || this.prevBlockHash == null || this.coinbaseTx == null) {
			return false;
		}
		
		if(!this.getBlockHashAsBinaryString().matches(Blockchain.MINING_TARGET)){
			return false;
		}
		return true;
	}

	/**
	 * Calculates whether the block is valid as the genesis block or not. 
	 * The genesis block does not contain an ordinary transactions, just a
	 * coinbase transaction. Other than that:
	 * ALL contents must be valid, and the hash of the block must satisfy
	 * the hash puzzle requirement.
	 */
	public boolean isValidAsGenesisBlock() {
		if(this.coinbaseTx == null ) {
			return false;
		}

		if(!this.getBlockHashAsBinaryString().matches(Blockchain.MINING_TARGET)){
			return false;
		}

		return true;
	}

	/**
	 * Calculates and encodes the block hash as a String of "0"s and "1"s.
	 * Can be useful in the mining process to see if the hash puzzle is solved.
	 */
	public String getBlockHashAsBinaryString() {
		if(transaction == null)
			return EncodingUtil.bytesToBinary(HashUtil.sha256(this.prevBlockHash + this.merkleRoot + this.nonce ));
		else
			return EncodingUtil.bytesToBinary(HashUtil.sha256(this.prevBlockHash + this.merkleRoot + this.nonce +this.transaction.getTxId()));
	}

	/**
	 * Calculates and encodes the block hash as a hexadecimal String.
	 */
	public String getBlockHashAsHexString() {
		if(transaction == null)
			return EncodingUtil.bytesToHex(HashUtil.sha256(this.prevBlockHash + this.merkleRoot + this.nonce));
		else
			return EncodingUtil.bytesToHex(HashUtil.sha256(this.prevBlockHash + this.merkleRoot + this.nonce +this.transaction.getTxId()));	
	}

	public void printOverview() {
		System.out.println();
		System.out.println("Block overview for block  " + getBlockHashAsHexString());
		System.out.print("-----------------------------------------");
		System.out.println("-----------------------------");
		System.out.println("   Prev block hash : " + prevBlockHash);
		System.out.println("   Nonce           : " + nonce);
		System.out.println("   Coinbase tx     : " + coinbaseTx);
		System.out.println("   The other tx    : " + transaction);
	}

	/* --------------------------------------------------------------------- */

}
