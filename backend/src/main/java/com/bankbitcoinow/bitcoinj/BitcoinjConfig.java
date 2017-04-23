package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class BitcoinjConfig {

	@Bean
	public DownloadProgressTracker downloadProgressTracker(PeerGroup peerGroup) throws InterruptedException {
		DownloadProgressTracker listener = new DownloadProgressTracker();
		peerGroup.startBlockChainDownload(listener);

		// Block context initialization until whole chain finish downloading
		listener.await();

		return listener;
	}

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
		File walletFile = new File("wallet/btc-wallet-v2-testnet");

		Wallet wallet;
		if (walletFile.exists()) {
			wallet = Wallet.loadFromFile(walletFile);
		} else {
			wallet = new Wallet(context);
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
