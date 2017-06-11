package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.net.discovery.PeerDiscovery;
import org.bitcoinj.net.discovery.PeerDiscoveryException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.params.UnitTestParams;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Configuration
public class BitcoinjConfig {

	@Bean
	public PeerGroup peerGroup(Context context,
	                           AbstractBlockChain blockChain,
	                           PeerDiscovery peerDiscovery,
	                           Wallet wallet) {
		PeerGroup peerGroup = new PeerGroup(context, blockChain);
		peerGroup.addPeerDiscovery(peerDiscovery);
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
	public Context context(NetworkParameters networkParameters) {
		return new Context(networkParameters);
	}

	@Configuration
	@Profile("!test")
	public static class BitcoinjRealConfig {

		@Bean
		public Wallet wallet(Context context) throws UnreadableWalletException {
			File walletFile = new File("wallet/btc-watching-wallet-testnet");
			Wallet wallet = walletFile.exists() ? Wallet.loadFromFile(walletFile) : createNewWallet(context);
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
		public PeerDiscovery peerDiscovery(NetworkParameters params) {
			return new DnsDiscovery(params);
		}

		@Bean
		public NetworkParameters networkParameters() {
			return TestNet3Params.get();
		}
	}

	@Configuration
	@Profile("test")
	public static class BitcoinjUnitTestConfig {

		static final UnitTestParams PARAMS = UnitTestParams.get();

		@Bean
		public Wallet wallet(Context context) throws UnreadableWalletException {
			return createNewWallet(context);
		}

		@Bean
		public BlockStore blockStore(Context context) throws BlockStoreException {
			return new MemoryBlockStore(context.getParams());
		}

		@Bean
		public PeerDiscovery peerDiscovery() {
			return new PeerDiscovery() {
				@Override
				public InetSocketAddress[] getPeers(long services, long timeoutValue, TimeUnit timeoutUnit) throws PeerDiscoveryException {
					return new InetSocketAddress[0];
				}

				@Override
				public void shutdown() {
				}
			};
		}

		@Bean
		public NetworkParameters networkParameters() {
			return PARAMS;
		}
	}

	private static Wallet createNewWallet(Context context) {
		SingleKeyTransactionSigner transactionSigner = new SingleKeyTransactionSigner();

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

		Wallet wallet = Wallet.fromWatchingKey(context.getParams(), watchingKey);
		wallet.addTransactionSigner(transactionSigner);
		return wallet;
	}
}
