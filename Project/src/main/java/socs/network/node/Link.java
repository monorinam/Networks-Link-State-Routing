package socs.network.node;

public class Link {

  RouterDescription router1;
  RouterDescription router2;
  int weight;
  Socket connection;
  ObjectOutputStream outStream;
  ObjectInputStream inStream;
  int exceptionFlag = -1;

  public Link(RouterDescription r1, RouterDescription r2) {
    router1 = r1;
    router2 = r2;
  }
  //Add properties of the link to this
  public Link(RouterDescription r1, RouterDescription r2, int wgt){
  	router1 = r1;
  	router2 = r2;
  	this.weight = wgt;
    try{
      this.connection   = new Socket(r2.processIPAddress, r2.processPortNumber);
      this.outStream       = new ObjectOutputStream(connection.getOutputStream());
      this.inStream        = new ObjectInputStream(connection.getInputStream());
      exceptionFlag = 1;
   }
   catch (Exception e) {
    exceptionFlag = 0;
   }

  }
}
