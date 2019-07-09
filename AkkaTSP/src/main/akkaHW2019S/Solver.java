package akkaHW2019S;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * This is the main actor and the only actor that is created directly under the
 * {@code ActorSystem} This actor creates 4 child actors
 * {@code Searcher}
 * 
 * @author Akash Nagesh and M. Kokar
 *
 */
public class Solver extends UntypedActor {

	private String fileName;
	private List<ActorRef> searchers;
	private int n = 0;
	private int minPath = Integer.MAX_VALUE;
	private Set<Route> bestRoute;
	private ActorRef bestSearcher;
	private List<ActorRef> fails;
	private int scale;
	private int start;
	private int firstStart;
	
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public Solver(String fileName) {
		this.fileName = fileName;
		this.searchers = new ArrayList<>();
		this.fails = new ArrayList<>();
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		
		//Code to implement
		if(msg instanceof Messages) {			
			Messages message = (Messages) msg;
			MessageType type = message.getType();
//			log.info(message.toString());
			switch(type) {
			case SOLVER_START: {
				log.info(getSelf().path().name() + " start!!");
				firstStart = message.getStart();
				start = firstStart;
				Searcher.firstStart = firstStart;
				scale = message.getScale();
				solverStart();
				break;
			}
			case FIND_ROUTE:findRoute(message,getSender());break;
			case NOT_FIND_ROUTE:notFindRoute(message,getSender());break;
			case SOLVER_END:{
				log.info(getSelf().path().name() + " Says: " + "Min Path Length : " + minPath);
				log.info(getSelf().path().name() + " Says: All " + printRoutes(firstStart,bestRoute));
				getContext().stop(getSelf());
				getContext().system().shutdown();
				break;
			}
			default: break;
			}
		}else {
			log.error("Error!!");
			unhandled(msg);
		}
		
	}
	
	// start actions
	private void solverStart() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line;
		int[][] adj = null;
		n = 0;
		while((line=br.readLine())!=null) {
			String[] str = line.split(" ");
			if(adj==null) {
				adj = new int[str.length][str.length];
			}
			int j = 0;
			for(String s : str) {
				adj[n][j++] = Integer.parseInt(s);
			}
			n++;
		}
		br.close();
		if(adj.length!=adj[0].length) {
			//System.err.println("Row and Column Error!!");
			log.error("Row and Column Error!!");
			getContext().system().shutdown();
			return;
		}
		for (int j = 0; j < adj.length; j++) {
			for (int k = 0; k < adj[0].length; k++) {
				if(adj[j][k]!=adj[k][j]) {
					//System.err.println("The Matrix not Pair-Wise!!");
					log.error("The Matrix not Pair-Wise!!");
					getContext().system().shutdown();
					return;
				}
			}
		}		
		ArrayList<Route> routes = new ArrayList<>();
		for (int j = 0; j < adj.length; j++) {
			for (int k = 0; k < j; k++) {
				routes.add(new Route(j,k,adj[j][k]));
			}
		}
		Collections.sort(routes);
		Props searcherProps = Props.create(Searcher.class,routes);
		ActorRef search1 = getContext().actorOf(searcherProps, "search1");
		ActorRef search2 = getContext().actorOf(searcherProps, "search2");
		ActorRef search3 = getContext().actorOf(searcherProps, "search3");
		ActorRef search4 = getContext().actorOf(searcherProps, "search4");
		getContext().watch(search1);
		getContext().watch(search2);
		getContext().watch(search3);
		getContext().watch(search4);
		searchers.add(search1);
		searchers.add(search2);
		searchers.add(search3);
		searchers.add(search4);
		if(start>=n-1) {
			//System.err.println("Start Point Error!!");
			log.error("Start Point Error!!");
			getContext().system().shutdown();
			return;
		}
		search1.tell(new Messages(MessageType.SEARCHER_START,start,scale,n), getSelf());
		if(++start>=n-1) start = 0;	
		search2.tell(new Messages(MessageType.SEARCHER_START,start,scale,n), getSelf());
		if(++start>=n-1) start = 0;
		search3.tell(new Messages(MessageType.SEARCHER_START,start,scale,n), getSelf());
		if(++start>=n-1) start = 0;
		search4.tell(new Messages(MessageType.SEARCHER_START,start,scale,n), getSelf());
	}
	
	// find route
	private void findRoute(Messages msg,ActorRef sender) {
		bestRoute = msg.getRoutes();
		minPath = msg.getMin();
		bestSearcher = sender;
		for(ActorRef s : searchers) {
			s.tell(msg, getSelf());
		}
	}
	
	// when not find the route that fit the requirement
	private void notFindRoute(Messages msg,ActorRef sender) {
		int min = msg.getMin();
		if(min<minPath) {
			bestRoute = msg.getRoutes();
			minPath = min;
			bestSearcher = sender;
		}
		int start = msg.getStart();
		if(start+4<n) {
			sender.tell(new Messages(MessageType.SEARCHER_START,start+4,scale,n), getSelf());
			return;
		}else {
			fails.add(sender);
		}
		if(fails.size()==searchers.size()) {
			log.warning("Sorry!!Not Find the path that smaller than the given length!");
			for(ActorRef s : searchers) {
				s.tell(new Messages(MessageType.SEARCHER_END,bestRoute,bestSearcher.path().name(),minPath), getSelf());
			}
		}
	}
	
	public static String printRoutes(int firstStart, Set<Route> bestRoute) {
		String res = "Best Route Till Now : " + firstStart;
		int x = firstStart;
		while(true) {
			for(Route r : bestRoute) {
				if(r.start==x) {
					res += "->" + r.end;
					x = r.end;
					if(x==firstStart) return res;
				}else if(r.end==x) {
					res += "->" + r.start;
					x = r.start;
					if(x==firstStart) return res;
				}
			}
			if(x==firstStart) break;
		}		
		return res;
	}

}
