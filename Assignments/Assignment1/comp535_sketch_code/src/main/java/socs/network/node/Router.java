package socs.network.node;

import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class Router {

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    lsd = new LinkStateDatabase(rd);
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

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
          System.out.println("Trying to attach to current router, not possible");
          return;
      }

      for(int i = 0; i < 4; i++)
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
      router2.status = INIT;//TODO:CHECK
      Link linkTemp = new Link(rd,router2);
      port[firstFreePortIndex] = linkTemp;
      //Should I do a socket connection here
      //What to do with the weight??




  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {

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

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {

  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

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

}
