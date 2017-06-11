package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BlockChainDownloaderTest {

	@Mock private PeerGroup peerGroup;
	@Mock private Peer peer;

	@Test(timeout = 10000)
	public void testWaitingForFinishingDownload() throws Exception {
		AtomicBoolean finished = new AtomicBoolean(false);

		doAnswer(invocation -> {
			PeerDataEventListener listener = invocation.getArgumentAt(0, PeerDataEventListener.class);

			// Simulate downloading data
			listener.onChainDownloadStarted(peer, 100);
			Thread.sleep(100);

			listener.onChainDownloadStarted(peer, 50);
			Thread.sleep(100);

			finished.set(true);
			listener.onChainDownloadStarted(peer, 0);

			return null;
		}).when(peerGroup).startBlockChainDownload(any());

		BlockChainDownloader blockChainDownloader = new BlockChainDownloader(peerGroup);
		blockChainDownloader.run();

		assertTrue(finished.get());
	}
}
