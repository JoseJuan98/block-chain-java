package no.hvl.dat159;

import no.hvl.dat159.util.EncodingUtil;
import no.hvl.dat159.util.HashUtil;

/**
 * 
 */
public class CoinbaseTx {
	
	/*
	 * The block number wherein this coinbase tx is located.
	 * This is to ensure unique txIds, see BIP34
	 */
	private int blockHeight; 
	
	/*
	 * The message for this coinbase tx.
	 */
	private String message;
	
	/*
	 * The out put for this coinbase tx.
	 */
	private Output output;

	/* --------------------------------------------------------------------- */

	public CoinbaseTx(int blockHeight, String message, String walletAddress) {
		
		this.blockHeight = blockHeight;
		this.message = message;
		this.output = new Output(Blockchain.BLOCK_REWARD, walletAddress); 
	}
	
	public boolean isValid(UtxoMap utxoMap) {
		
		if(this.output.getValue() <= 0) {
			return false;
		}
		
		
		return true;
	}

	public String getMessage() {
		return message;
	}

	public Output getOutput() {
		return output;
	}

	/**
	 *	The block hash as a hexadecimal String. 
	 */
	public String getTxId() {
		
		String sumHash = this.message + this.output.toString();
		
		return EncodingUtil.bytesToHex(HashUtil.sha256(sumHash));
	}
	
	@Override
	public String toString() {
		return getTxId() + "\n\tmessage    : " + message + "\n\toutput(0)  : " + output;
	}
	
}
