package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Starts downloading blockchain from peers
 * without blocking Spring's context initialization.
 */
@Component
@Order(BlockChainDownloader.PRECEDENCE)
public class BlockChainDownloader implements CommandLineRunner {

	public static final int PRECEDENCE = 0;

	private final PeerGroup peerGroup;

	public BlockChainDownloader(PeerGroup peerGroup) {
		this.peerGroup = peerGroup;
	}

	@Override
	public void run(String... args) throws Exception {
		DownloadProgressTracker listener = new DownloadProgressTracker();
		peerGroup.startBlockChainDownload(listener);

		// Block until whole chain finish downloading
		listener.await();
	}
}
