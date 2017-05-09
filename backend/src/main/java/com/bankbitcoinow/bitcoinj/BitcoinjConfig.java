package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class BitcoinjConfig {

	@Bean
	public PeerGroup peerGroup(Context context,
	                           AbstractBlockChain blockChain,
	                           Wallet wallet) {
		PeerGroup peerGroup = new PeerGroup(context, blockChain);
		peerGroup.addPeerDiscovery(new DnsDiscovery(context.getParams()));
		peerGroup.addWallet(wallet);
		peerGroup.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Context.propagate(context);
			peerGroup.stop();
		}, "Peer group shutdown hook"));

		return peerGroup;
	}

	@Bean
	public AbstractBlockChain blockChain(Context context,
	                                     BlockStore blockStore,
	                                     Wallet wallet) throws BlockStoreException {
		AbstractBlockChain blockChain = new BlockChain(context, blockStore);
		blockChain.addWallet(wallet);
		return blockChain;
	}

	@Bean
	public Wallet wallet(Context context) throws UnreadableWalletException {
		File walletFile = new File("wallet/btc-watching-wallet-testnet");

		Wallet wallet;
		if (walletFile.exists()) {
			wallet = Wallet.loadFromFile(walletFile);
		} else {
			SingleKeyTransactionSigner transactionSigner = transactionSigner();

			// We will generate, store and use private keys by ourselves,
			// but we need Wallet to watch for incoming transactions.
			// We need to create wallet from watching key to be able to add
			// other watching keys - Wallet does not allow to contain mixed keys.
			// Watching key is not important, because keys (and addresses)
			// generated from it will not receive any transactions.
			DeterministicKey masterKey = HDKeyDerivation.createMasterPrivateKey("123456789".getBytes());
			ChildNumber childNumber = new ChildNumber(0, true);
			DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(masterKey, childNumber);
			DeterministicKey watchingKey = accountKey.dropPrivateBytes().dropParent();

			wallet = Wallet.fromWatchingKey(context.getParams(), watchingKey);
			wallet.addTransactionSigner(transactionSigner);
		}

		wallet.autosaveToFile(walletFile, 5, TimeUnit.SECONDS, null);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				Context.propagate(context);
				wallet.saveToFile(walletFile);
			} catch (IOException e) {
				throw new RuntimeException("Error while saving wallet", e);
			}
		}, "Wallet shutdown hook"));

		return wallet;
	}

	/**
	 * <p>Creates new transaction signer.</p>
	 *
	 * <p>Used instead of {@code @Component} and autowired argument in {@link #wallet(Context)}
	 * to prevent creating unnecessary bean instance when Wallet is loaded from file.
	 *
	 * <p>Alternatively {@link org.springframework.beans.factory.BeanFactoryAware},
	 * {@link org.springframework.beans.factory.BeanFactory#getBean(Class)}
	 * and {@link org.springframework.stereotype.Component} could be used.</p>
	 */
	@Bean
	@Lazy
	public SingleKeyTransactionSigner transactionSigner() {
		return new SingleKeyTransactionSigner();
	}

	@Bean
	public BlockStore blockStore(Context context) throws BlockStoreException {
		File blockChainFile = new File("wallet/spv-block-store-testnet");
		return new SPVBlockStore(context.getParams(), blockChainFile);
	}

	@Bean
	public Context context(NetworkParameters networkParameters) {
		return new Context(networkParameters);
	}

	@Bean
	public NetworkParameters networkParameters() {
		return TestNet3Params.get();
	}
}
