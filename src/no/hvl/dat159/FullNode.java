package no.hvl.dat159;

import java.util.Map;

import com.sun.org.apache.bcel.internal.util.ByteSequence;

import no.hvl.dat159.util.DateTimeUtil;
import no.hvl.dat159.util.EncodingUtil;
import no.hvl.dat159.util.HashUtil;

/**
 * Contains both the full Blockchain and the full UtxoMap.
 * Also contains the wallet for the mining rewards + fees.
 * 
 * Services:
 * - Receive a transaction, validate the transaction, create and mine 
 * 	 a new block, validate and append the mined block to the 
 *   blockchain + update the UxtoMap.
 * - Provide a limited UtxoMap that matches one particular address 
 *   to wallets.
 */
public class FullNode {
	
	private Blockchain blockchain;
	private UtxoMap utxoMap;
	private Wallet wallet;

	/**
	 * Initializes a node containing a blockchain, a UTXO-map and a wallet
	 * for block rewards. Then initializes the blockchain by mining and
	 * adding a genesis block.
	 */
	public FullNode(String walletId) {
		
		this.wallet = new Wallet(walletId, this);
		this.blockchain = new Blockchain();
		this.utxoMap = new UtxoMap();
	}

	/**
	 * Does what it says.
	 */
	public void mineAndAddGenesisBlock() {
		//1. Create the coinbase transaction
		//2. Add the coinbase transaction to a new block and mine the block
		//3. Validate the block. If valid:
			//4. Add the block to the blockchain
			//5. Update the utxo set
		//else
			//up to you
		System.out.println("Mining Genesis Block> ...");
		CoinbaseTx cx = new CoinbaseTx(blockchain.getHeight(), DateTimeUtil.getTimestamp() + "by M", this.wallet.getAddress());
		
		Block genBlock = new Block("0" , cx, null);
		genBlock.mine();
		
		if(genBlock.isValidAsGenesisBlock() && cx.isValid(utxoMap)) {
			System.out.println("	/> GENESIS BLOCK is valid. ");
			
			this.blockchain.appendBlock(genBlock);
			this.utxoMap.addOutput(new Input(cx.getTxId(), 0), cx.getOutput());
			
		}else {
			
			System.err.println("GENESIS BLOCK VALIDATION ERROR");
		}
		
		
	}
	
	/**
	 * Does what it says.
	 */
	public void mineAndAppendBlockContaining(Transaction tx) {
		
		//1. Create the coinbase transaction
		//2. Add the two transactions to a new block and mine the block
		//3. Validate the block. If valid:
			//4. Add the block to the blockchain
			//5. Update the utxo set with the new coinbaseTx
			//6. Update the utxo set with the new tx
		//else
			//up to you
		
		CoinbaseTx cx = new CoinbaseTx(blockchain.getHeight() , DateTimeUtil.getTimestamp() + "by M", this.wallet.getAddress());
		
		Block block = new Block(blockchain.getLastBlockHash() , cx, tx);
		System.out.println("Mining Block"+block.getBlockHashAsHexString()+"> ...");
		block.mine();
		
		
		if(block.isValid() && cx.isValid(utxoMap) && tx.isValid(utxoMap)) {
			System.out.println("	/> Block "+this.blockchain.getHeight() + " is valid. ");
			
			this.blockchain.appendBlock(block);
			
			for(Input iter_inp : tx.getInputs()){
				this.utxoMap.removeOutput(iter_inp);
			}
			
			this.utxoMap.addOutput(new Input(cx.getTxId(), 0), cx.getOutput());
			
			int i=0;
			for(Output it_out : tx.getOutputs()) {
					this.utxoMap.addOutput(new Input(tx.getTxId(), i), it_out);
					i++;
			}
		}else {
			System.err.println("BLOCK"+ block.getBlockHashAsHexString() +" VALIDATION ERROR");
		}
		
		
	}

	public Blockchain getBlockchain() {
		return blockchain;
	}

	public UtxoMap getUtxoMap() {
		return utxoMap;
	}

	public Wallet getWallet() {
		return wallet;
	}

	public void printOverview() {
		System.out.println();
		System.out.println("Full node overview");
		System.out.println("------------------");
		System.out.println("   Associated wallet");
		wallet.printOverviewIndented();
		System.out.println("   Associated blockchain");
		blockchain.printOverview();
	}
	
}
