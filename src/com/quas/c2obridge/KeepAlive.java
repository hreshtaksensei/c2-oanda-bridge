package com.quas.c2obridge;


import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.MessagingException;

public class KeepAlive implements Runnable {

	private IMAPFolder folder;

	private volatile boolean running;

	public KeepAlive(IMAPFolder folder) {
		this.folder = folder;

		this.running = true;
	}

	@Override
	public void run() {
		while (running) {
			try {
				Thread.sleep(60000); // once a minute

				// Perform a NOOP just to keep alive the connection
				String minute = C2OBridge.getCurrentMinute();
				int minuteInt = Integer.parseInt(minute);
				if (minuteInt % 60 == 0) {
					System.out.println("-----------------------------\n| NEW HOUR (" + C2OBridge.getCurrentTime() + ") |\n-----------------------------\n");
				} else {
					System.out.print(minute + " ");
				}
				folder.doCommand(new IMAPFolder.ProtocolCommand() {
					public Object doCommand(IMAPProtocol p) {
						try {
							p.simpleCommand("NOOP", null);
						} catch (ConnectionException ce) {
							System.err.println("Encountered ConnectionException: " + ce);
							System.err.println("Shutting down thread.");
							running = false;
						} catch (ProtocolException pe) {
							System.err.println("Encountered ProtocolException: " + pe);
							System.err.println("Shutting down application completely...");
							System.exit(1);
						}
						return null;
					}
				});
			} catch (InterruptedException e) {
				// Ignore, just aborting the thread...
				System.out.println("Keep alive thread was interrupted, shouldn't be a biggie");
			} catch (MessagingException e) {
				// Shouldn't really happen...
				System.err.println("Unexpected exception while keeping alive the IDLE connection: " + e);
				e.printStackTrace(System.err);
				System.err.println("Shutting down application completely...");
				System.exit(1);
			}
		}
		System.out.println("Join should occur right now:");
	}
}