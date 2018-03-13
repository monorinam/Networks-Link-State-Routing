package socs.network.node;

import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Router {

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];

  public Router(Configuration config) throws IOException, ClassNotFoundException{
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processPortNumber = config.getShort("socs.network.router.portNum");
    rd.processIPAddress = "127.0.0.1";
    lsd = new LinkStateDatabase(rd);
    //start the thread
    thread = new serverThread(new serverSocket(rd.processPortNumber),this);
    thread.start();
    //executor service timer
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {
  	String shortest = lsd.getShortestPath(destinationIP);
  	if(shortest != null)
  		System.out.println(shortest);
  	else if (shortest == 0)
  		System.out.println("This is your own IP");
  	else
  		System.out.println("The shortest distance is "+shortest);//TODO: CHANGE when getShortest is finished

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) throws Exception {
  	if(portNumber == null || portNumber < 0 || portNumber >= ports.length)
  	{
  		System.out.println("Invalid port number, cannot be removed");
  	}
  	if(ports[portNumber].router2.simulatedIPAddress != rd.simulatedIPAddress)
  	{
  		//remove the port
  		lsd.removeLinkFromLSA(rd.simulatedIPAddress,ports[portNumber].router2.simulatedIPAddress);
  		ports[portNumber] = null;
  		sendLSAUpdate();

  	}
  	else
  		System.out.println("Current ip, cannot be disconnected, terminating..");

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
      //Attach strategy:
      //1. Check if the simulatedIP is already connected to one of the ports
      //2. Check if any ports are available
      //If yes to either, return
      //If not, then create new RouterDescription to store details of router
      //this. router is client
      //and remote (new one) is server)
      //
      //Should we also start a new thread? with the remote router??
      //Link has two routers, 1 and 2.
      //Make sure both the routers are not connected to the simulatedIP
      int firstFreePortIndex = -1;
      if(rd.processIPAddress.equals(processIP))
      {
          System.out.println("Trying to attach to router to itself, not possible");
          return;
      }

      for(int i = 0; i < ports.length; i++)
      {
          //there are 4 available ports
        if(ports[i] != null)
             if(ports[i].router2.processIPAddress.equals(processIP) && ports[i].router1.processIPAddress.equals(rd.processIPAddress))
              {
                  System.out.println("This router is already attached, not reattaching....");
                  return;
              }
          else
              firstFreePortIndex = i;
      }
      if(firstFreePortIndex == -1)
      {
          System.out.println("No ports left to attach to, terminating....");
          return;
      }
      //Free port found, and router is not attached
      RouterDescription router2 = new RouterDescription();
      router2.processIPAddress = processIP;
      router2.processPortNumber = processPort;
      router2.simulatedIPAddress = simulatedIP;
      router2.status = RouterStatus.INIT;//TODO:CHECK
      Link linkTemp = new Link(rd,router2, weight);
      if(linkTemp.exceptionFlag == 0)
      {
      	System.out.println("Connection failed..");
      	return;
      }
      ports[firstFreePortIndex] = linkTemp;
      System.out.println("Connection established");
  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    //Router 1 broadcasts hello through all ports
  	for(int i = 0; i < ports.length; i++)
  	{
  		if(ports[i] == null && ports[i].exceptionFlag != 1 && ports[i].router2.status == RouterStatus.TWO_WAY)
        continue;
  		try{
			    SOSPFPacket packetClient = new SOSPFPacket(ports[i].router1, ports[i].router2,0); 
          ports[i].outStream.writeObj(packetClient);
          //Read hello that is sent out  
          SOSPFPacket packetServer = (SOSPFPacket) ports[i].inStream.readObject();
          if(packetServer.sospfType == 0)
          {
            System.out.println("Starting.....");
            System.out.println("Received hello from "+packetServer.neighborID);
            System.out.println("Two way communication establish for"+ports[i].router2.simulatedIPAddress);
            ports[i].router2.status = RouterStatus.TWO_WAY;
            //Broadcast another hello
            (new ObjectOutputStream(ports[i].connection.getOutputStream())).writeObj(packetClient);
          }
          ports[i].connection.close();
      }
      catch (IOException e) {
          System.out.println("Error, process could not be started");
          e.printStackTrace();

      }    
  	}

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {
      processAttach(processIP, processPort, simulatedIP, weight);
      processStart();


  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    int count = 0;
    for(int i = 0; i < ports.length; i++)
    {
      if(ports[i] == null || ports[i].router2.status = RouterStatus.TWO_WAY)
        continue;
      System.out.println("IP address of neighbour "+i+"  is "+ports[i].router2.simulatedIPAddress);
    }

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {
    System.exit(0);

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendLSAUpdate() throws UnknownHostException, IOException{
  	for(Link port:ports)
  	{
  		if(port == null || port.router2.status = RouterStatus.TWO_WAY)
  		{
  			return;
  		}
  		else
  		{
  			SOSPFPacket packetClient = new SOSPFPacket();
  			packetClient.srcIP = rd.simulatedIPAddress;	  
			packetClient.dstIP = lk.router2.simulatedIPAddress; 
			packetClient.routerID = rd.simulatedIPAddress;
			packetClient.neighborID = rd.simulatedIPAddress;

			packetClient.srcProcessIP = rd.processIPAddress;
			packetClient.srcProcessPort = rd.processPortNumber;
			
			packetClient.sospfType = 1;
			
			packetClient.lsaArray = new Vector<LSA>();

			for(LSA lsaVal:lsd._store.values())
			{
				if(lsaVal != null)
				{
					packetClient.lsaArray.addElement(lsaVal);
				}
			}

			Socket client = new Socket(port.router2.processIPAddress, port.router2.processPortNumber);
			 ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
			 System.out.println("sending LSAUPDATE to " + packetClient.dstIP);
			 out.writeObject(packetClient);
			 client.close();

  		}
  	}
  }

}
