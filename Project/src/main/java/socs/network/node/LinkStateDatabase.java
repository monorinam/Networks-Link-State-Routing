package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

import java.util.HashMap;

public class LinkStateDatabase {

  //linkID => LSAInstance
  HashMap<String, LSA> _store = new HashMap<String, LSA>();

  private RouterDescription rd = null;

  public LinkStateDatabase(RouterDescription routerDescription) {
    rd = routerDescription;
    LSA l = initLinkStateDatabase();
    _store.put(l.linkStateID, l);
  }

  /**
   * output the shortest path from this router to the destination with the given IP address
   */
  //Djikstra's implementation of shortest path algorithm
  /** S <- {}
  * Q <- <remaining nodes by distance>
  * while Q != {}
  *     u <- extract_min(Q)
  *     S <- S plus {u}
  *     for each node v adj to u
  *         "relax" the cost of v
  * Algorithm adapted from http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
  */
  void buildGraph(ArrayList<String> q, HashMap<String,String> predecessor,HashMap<String,Integer> distance)
  {
    predecessor.put(rd.simulatedIPAddress,null);
    distance.put(rd.simulatedIPAddress,0);

    //Build the graph
    for(LSA lsaVal: _store.values())
    {
      for (LinkDescription linkdesc: _store.get(lsaVal.linkStateID).links)
      {
        String lID = linkdesc.linkID;
        if(!predecessor.containsKey(lID))
          predecessor.put(lID,null);
        if(!distance.containsKey(lID))
          distance.put(lID,Integer.MAX_VALUE);
        if(!q.contains(lID))
          q.add(lID);
      }
      if(!q.contains(lsaVal.linkStateID))
        q.add(lsaVal.linkStateID);
    }
  }
  private int minVertex(HashMap<String,String> distance,String u)
  {
    //find the min vertex
      int min = Integer.MAX_VALUE;
      for(int i = 0; i<q.size();i++)
      {
        if(distance.get(q.get(i)) <= min)
        {
          min = distance.get(i);
          u = q.get(i);
        }
      }
      return min;
  }
  String getShortestPath(String destinationIP) {
    //TODO: fill the implementation here
    //Build the graph here
    ArrayList<String> q = new ArrayList<String>();
    HashMap<String, String> predecessor = new HashMap<String, String>();
    HashMap<String, Integer> distance = new HashMap<String, Integer>();
    buildGraph(q,predecessor,distance);

    // Run the algorithm
    while (!q.isEmpty())
    {
      String u = null;
      int min = minVertex(distance,u);

      if(u.equals(destinationIP))
      {
        String prevIP = destinationIP;
        StringBuilder returnVal = new StringBuilder();
        returnVal.append("The shortest path to"+destinationIP+"is");
        while(!prevIP.equals(rd.simulatedIPAddress)) 
        {
          if(distance.get(prevIP) != null && distance.get(predecessor.get(prevIP)) != null)
          {
            returnVal.append("-> (" + (distance.get(prevIP) - distance.get(predecessor.get(prevIP))) + ") " + prevIP + " " );
            else{
              return "Shortest Path does not exist";
            }
          }
          prevIP = predecessor.get(prevIPr);
        }
        return returnVal.toString();
      }

      //clear up
      q.remove(u);
      LSA lsa = _store.get(u);
      for(LinkDescription l:lsa.links)
      {
        int alt = distance.get(u) + l.tosMetrics;
        if (alt < distance.get(l.linkID)){
          predecessor(l.linkID,u);
          distance.put(l.linkID,alt);

        }
      }
    }




    return null;
  }

  //initialize the linkstate database by adding an entry about the router itself
  private LSA initLinkStateDatabase() {
    LSA lsa = new LSA();
    lsa.linkStateID = rd.simulatedIPAddress;
    lsa.lsaSeqNumber = Integer.MIN_VALUE;
    LinkDescription ld = new LinkDescription();
    ld.linkID = rd.simulatedIPAddress;
    ld.portNum = -1;
    ld.tosMetrics = 0;
    lsa.links.add(ld);
    return lsa;
  }


  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (LSA lsa: _store.values()) {
      sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
      for (LinkDescription ld : lsa.links) {
        sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
                append(ld.tosMetrics).append("\t");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

}
