package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Starts downloading blockchain from peers
 * without blocking Spring's context initialization.
 */
@Component
@Order(BlockChainDownloader.PRECEDENCE)
@Profile("!test")
public class BlockChainDownloader implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(BlockChainDownloader.class);

	public static final int PRECEDENCE = 0;

	private final PeerGroup peerGroup;

	public BlockChainDownloader(PeerGroup peerGroup) {
		this.peerGroup = peerGroup;
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Starting block chain download...");

		DownloadProgressTracker listener = new DownloadProgressTracker();
		peerGroup.startBlockChainDownload(listener);

		// Block until whole chain finish downloading
		listener.await();

		LOG.info("Whole chain finish downloading");
	}
}
