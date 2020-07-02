package no.hvl.dat159;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.hvl.dat159.util.EncodingUtil;
import no.hvl.dat159.util.HashUtil;
import no.hvl.dat159.util.SignatureUtil;

/**
 * 
 */
public class Transaction {
	
	private List<Input> inputs = new ArrayList<>();
	private List<Output> outputs = new ArrayList<>();
	
	/*
	 * Simplification!:
	 * In reality, each input should have a public key and a signature. To simplify 
	 * things, we assume that all inputs belong to the same public key => We can 
	 * store the public key in the transaction and sign for all inputs in one go.
	 */
	private PublicKey senderPublicKey;
	private byte[] signature; 
	
	public Transaction(PublicKey senderPublicKey) {
		this.senderPublicKey = senderPublicKey;
	}
	
	/**
	 * 
	 */
	public void signTxUsing(PrivateKey privateKey) {
		String message = this.outputs.toString() + this.inputs.toString();
		signature = SignatureUtil.signWithDSA(privateKey,  message );
	}

	/**
	 * 
	 */
	public boolean isValid(UtxoMap utxoMap) {
	    
	    //None of the data must be null 
        //Inputs or outputs cannot be empty
	    //No outputs can be zero or negative
	    //All inputs must exist in the UTXO-set
	    //All inputs must belong to the sender of this transaction
        //No inputs can be zero or negative
        //The list of inputs must not contain duplicates
        //The total input amount must be equal to (or less than, if we 
        //allow fees) the total output amount
        //The signature must belong to the sender and be valid
        //The transaction hash must be correct
		if(this.inputs.isEmpty() || this.outputs.isEmpty() || this.senderPublicKey == null || this.signature == null) {
			return false;
		}
		
		int total_outputs = 0;
		
		if(this.getInputs().isEmpty() || this.getOutputs().isEmpty()){
			return false;
		}
		
		for (Output out_p : this.outputs) {
			
			total_outputs++;
			
			if(out_p.getValue() <= 0) {
				return false;
			}
		}
		
		boolean input_exist = false;
		int total_inputs = 0;
		
		for (Input in_p : this.inputs) {
			
			total_inputs++;
			
			for (Map.Entry<Input, Output> entry : utxoMap.getAllUtxos()) {
				if(entry.getKey().hashCode() == in_p.hashCode()) {
					input_exist = true;
				}
				
			}
			
			if(!input_exist) {
				return false;
			}else {
				input_exist = false;
			}
		}
		
		for (Input in_aux : this.inputs) {
			for (Input in_aux2 : this.inputs) {
				if(!in_aux.equals(in_aux2)) {
					if(in_aux.hashCode() == in_aux2.hashCode()) {
						return false;
					}
				}
			}
		}
		
		if(total_inputs > total_outputs ) {
			return false;
		}
		
		return true;
	}
	
	/**
	 *	The block hash as a hexadecimal String. 
	 */
	public String getTxId() {
		String inputs_s = "";
		for (Input inp : this.getInputs()) {
			inputs_s += inp.toString();
		}
		
		String outputs_s = "";
		for (Input out : this.getInputs()) {
			outputs_s += out.toString();
		}
		
		
		String sumHash = outputs_s + inputs_s + getSignature().hashCode();
		
		return EncodingUtil.bytesToHex(HashUtil.sha256(sumHash));
	}

	public void addInput(Input input) {
		inputs.add(input);
	}
	
	public void addOutput(Output output) {
		outputs.add(output);
	}
	
	public List<Input> getInputs() {
		return inputs;
	}

	public List<Output> getOutputs() {
		return outputs;
	}

	public PublicKey getSenderPublicKey() {
		return senderPublicKey;
	}

	public byte[] getSignature() {
		return signature;
	}

	@Override
	public String toString() {
		String s = getTxId();
		for (int i=0; i<inputs.size(); i++) {
			s += "\n\tinput(" + i + ")   : " + inputs.get(i);
		}
		for (int i=0; i<outputs.size(); i++) {
			s += "\n\toutput(" + i + ")  : " + outputs.get(i);
		}
		return s;
	}

	
}
