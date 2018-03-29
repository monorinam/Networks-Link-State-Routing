package socs.network.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Implements a thread wrapper for each router
public class serverThread extends Thread {

	private ServerSocketSocket socket;
	private Router thisRouter;

	public serverThread(ServerSocket s, Router r) {

		this.socket = s;
		this.thisRouter = r;

	}
	//start the thread
	public void startThread(){
		start();
	}

}
