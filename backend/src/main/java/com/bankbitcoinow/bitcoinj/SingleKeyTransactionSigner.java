package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.StatelessTransactionSigner;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;
import org.springframework.util.Assert;

import java.util.EnumSet;

/**
 * {@link TransactionSigner} that uses single key provided by user to sign transactions.
 * <b>Not thread-safe!</b>
 *
 * @see org.bitcoinj.signers.LocalTransactionSigner LocalTransactionSigner for source implementation
 */
public class SingleKeyTransactionSigner extends StatelessTransactionSigner {

	private static final Logger LOG = LoggerFactory.getLogger(SingleKeyTransactionSigner.class);

	/**
	 * Verify flags that are safe to use when testing if an input is already
	 * signed.
	 */
	private static final EnumSet<Script.VerifyFlag> MINIMUM_VERIFY_FLAGS = EnumSet.of(Script.VerifyFlag.P2SH,
			Script.VerifyFlag.NULLDUMMY);

	private ECKey key;
	private KeyParameter aesKey;

	@Override
	public boolean isReady() {
		return true;
	}

	void setKey(ECKey key, KeyParameter aesKey) {
		Assert.notNull(key, "Key cannot be null");

		if (key.isEncrypted()) {
			Assert.notNull(aesKey, "AES key cannot be null when key is encrypted");
		} else {
			Assert.isTrue(key.hasPrivKey(), "Key have to contain private part");
		}

		this.key = key;
		this.aesKey = aesKey;
	}

	void clearKey() {
		this.key = null;
		this.aesKey = null;
	}

	/**
	 * Based on {@link org.bitcoinj.wallet.DecryptingKeyBag#maybeDecrypt(ECKey)}.
	 */
	private ECKey maybeDecryptKey() {
		if (key == null) {
			return null;
		}

		if (!key.isEncrypted()) {
			return key;
		}

		if (aesKey != null) {
			return key.decrypt(aesKey);
		}

		throw new ECKey.KeyIsEncryptedException();
	}

	@Override
	public boolean signInputs(ProposedTransaction propTx, KeyBag keyBag) {
		Assert.notNull(key, "Key was not set - call setKey(ECKey, KeyParameter) first.");

		Transaction tx = propTx.partialTx;
		int numInputs = tx.getInputs().size();
		for (int i = 0; i < numInputs; i++) {
			TransactionInput txIn = tx.getInput(i);
			if (txIn.getConnectedOutput() == null) {
				LOG.warn("Missing connected output, assuming input {} is already signed.", i);
				continue;
			}

			try {
				// We assume if its already signed, its hopefully got a SIGHASH type that will not invalidate when
				// we sign missing pieces (to check this would require either assuming any signatures are signing
				// standard output types or a way to get processed signatures out of script execution)
				txIn.getScriptSig().correctlySpends(tx, i, txIn.getConnectedOutput().getScriptPubKey(), MINIMUM_VERIFY_FLAGS);
				LOG.warn("Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.", i);
				continue;
			} catch (ScriptException e) {
				// Expected.
			}

			RedeemData redeemData = txIn.getConnectedRedeemData(keyBag);
			if (redeemData == null) {
				LOG.warn("No redeem data found for input {}", i);
				continue;
			}

			Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
			if (!scriptPubKey.isSentToAddress()) {
				LOG.warn("SingleKeyTransactionSigner works only with pay-to-address transactions");
				return false;
			}

			// For pay-to-address and pay-to-key inputs RedeemData will always contain only one key
			ECKey pubKey = redeemData.keys.get(0);
			ECKey key = maybeDecryptKey();
			if (key == null || !key.getPubKeyPoint().equals(pubKey.getPubKeyPoint())) {
				LOG.warn("No local key found for input {}", i);
				continue;
			}

			Script inputScript = txIn.getScriptSig();
			// script here would be either a standard CHECKSIG program for pay-to-address or pay-to-pubkey inputs
			byte[] script = redeemData.redeemScript.getProgram();
			try {
				TransactionSignature signature = tx.calculateSignature(i, key, script, Transaction.SigHash.ALL, false);

				// at this point we have incomplete inputScript with OP_0 in place of one or more signatures. We already
				// have calculated the signature using the local key and now need to insert it in the correct place
				// within inputScript. For pay-to-address and pay-to-key script there is only one signature and it always
				// goes first in an inputScript (sigIndex = 0).
				int sigIndex = 0;
				inputScript = scriptPubKey.getScriptSigWithSignature(inputScript, signature.encodeToBitcoin(), sigIndex);
				txIn.setScriptSig(inputScript);
			} catch (ECKey.KeyIsEncryptedException e) {
				throw e;
			} catch (ECKey.MissingPrivateKeyException e) {
				LOG.warn("No private key in keypair for input {}", i);
			}
		}

		return true;
	}

}
