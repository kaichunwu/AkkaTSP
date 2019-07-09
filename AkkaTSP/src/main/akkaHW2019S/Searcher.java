package akkaHW2019S;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * this actor implements the search for a path that satisfies the project requirements 
 *
 * @author M. Kokar
 *
 */
public class Searcher extends UntypedActor {

	private ArrayList<Route> routes;
	private int start;
	private int scale;
	private int n;
	private Set<Route> bestRoute = new HashSet<>();
	private int min = Integer.MAX_VALUE;
	public static int firstStart = 0;
	
	private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	public Searcher(ArrayList<Route> routes) {
		// TODO 
		this.routes = routes;
	}

	@Override
	public void onReceive(Object msg) throws Throwable {
		
		//Code to implement
		if(msg instanceof Messages) {
			Messages message = (Messages) msg;
			MessageType type = message.getType();
//			log.info(message.toString());
			switch(type) {
			case SEARCHER_START: {
				log.info(getSelf().path().name() + " start!!");
				start = message.getStart();
				scale = message.getScale();
				n = message.getNum();
				int min = minPath();
				if(min<=scale) {
					getSender().tell(new Messages(MessageType.FIND_ROUTE,bestRoute,getSelf().path().name(),min), getSelf());
				}else {
					getSender().tell(new Messages(MessageType.NOT_FIND_ROUTE,bestRoute,min,start), getSelf());
				}
				break;
			}
			case SEARCHER_FAIL:{
				getContext().stop(getSelf());
				break;
			}
			case FIND_ROUTE:{
				String winner = message.getWinner();
				if(winner==getSelf().path().name()) {
					log.info(getSelf().path().name() + " Says: " + "I Won");
					getSender().tell(new Messages(MessageType.SOLVER_END,message.getRoutes(),message.getMin(),start), getSelf());
				}else {
					log.info(getSelf().path().name() + " Says: " + winner +" Won");
				}
				log.info(getSelf().path().name() + " Says: My " + Solver.printRoutes(firstStart, bestRoute));
				getContext().stop(getSelf());
				break;			
			}
			case SEARCHER_END:{
				String winner = message.getWinner();
				if(winner==getSelf().path().name()) {
					log.info(getSelf().path().name() + " Says: " + "I Won");
					getSender().tell(new Messages(MessageType.SOLVER_END,message.getRoutes(),message.getMin(),start), getSelf());
				}else {
					log.info(getSelf().path().name() + " Says: " + winner +" Won");
				}
				log.info(getSelf().path().name() + " Says: My " + Solver.printRoutes(firstStart, bestRoute));
				getContext().stop(getSelf());
				break;				
			}
			default: break;
			}
		}else {
			unhandled(msg);
		}
	}

	// Calculate Min Path
	private int minPath() {
		int tmin = Integer.MAX_VALUE;
		bestRoute = new HashSet<>();
		Set<Route> tRoute = null;
		for (int i = 1; i < n; i++) {
			tRoute = new HashSet<>();
			Route first = ithMinPath(i,routes,start);
			Set<Integer> edge = new HashSet<>();
			edge.add(first.start);
			edge.add(first.end);
			tRoute.add(first);
			Set<Integer> arrived = new HashSet<>();
			arrived.add(first.start);
			arrived.add(first.end);
			if((tmin=first.des+startCal(edge,arrived,tRoute))<=scale) {
				bestRoute = tRoute;
				min = tmin;
				break;
			}
			if(tmin<min) {
				bestRoute = tRoute;
				min = tmin;
			}
		}
		return min;
	}
	
	// Start
	private int startCal(Set<Integer> edge,Set<Integer> arrived,Set<Route> nRoute) {
		if(nRoute.size()==n) return 0;
		for(Route r : routes) {
			if(nRoute.contains(r)) {
				continue;
			}
			if(judge(edge,arrived,r,nRoute)) {
				nRoute.add(r);
				return r.des+startCal(edge,arrived,nRoute);
			}
		}
		//System.err.println("err");
		//for(Integer r : edge) {
		//	System.err.println("ed:"+r);
		//}
		//for(Integer r : arrived) {
		//	System.err.println("arr:"+r);
		//}
		//for(Route r : nRoute) {
		//	System.err.println(r);
		//}
		return 0;
	}
	
	// Judge if the Route can be added to the bestRoute
	private boolean judge(Set<Integer> edge, Set<Integer> arrived, Route r, Set<Route> nRoute) {
		if(arrived.contains(r.start)&&arrived.contains(r.end)
				&&edge.contains(r.start)&&edge.contains(r.end)) {
			if(isCircle(nRoute,r)) {
				if(nRoute.size()!=n-1) return false;				
			}
			edge.remove(r.start);
			edge.remove(r.end);
			return true;
		}
		if(!arrived.contains(r.start)&&!arrived.contains(r.end)) {
			edge.add(r.start);
			edge.add(r.end);
			arrived.add(r.start);
			arrived.add(r.end);
			return true;
		}
		if(arrived.contains(r.start)&&edge.contains(r.start)
				&&!edge.contains(r.end)&&!arrived.contains(r.end)) {
			arrived.add(r.end);
			edge.remove(r.start);
			edge.add(r.end);
			return true;
		}
		if(!arrived.contains(r.start)&&!edge.contains(r.start)
				&&edge.contains(r.end)&&arrived.contains(r.end)) {
			arrived.add(r.start);
			edge.remove(r.end);
			edge.add(r.start);
			return true;
		}
		return false;
	}
	
	// Judge if there is a circle when r add into nRoute
	private boolean isCircle(Set<Route> nRoute, Route r) {
		int start = r.start;
		int last = r.end;
		int end = r.end;
		while(true) {
			for(Route route : nRoute) {
				if(route.start==start) {
					start = route.end;
					if(start==end) return true;
					if(start==last) break;
					last = route.start;
				}
				else if(route.end==start) {
					start = route.start;
					if(start==end) return true;
					if(start==last) break;
					last = route.end;
				}
			}
			if(start==last) break;
		}
		return false;
	}

	// Calculate the ith min path for start point i
	private Route ithMinPath(int i,List<Route> routes, int start) {
		for(Route r : routes) {
			if(r.start==start||r.end==start) {
				i--;
			}
			if(i==0) {
				return r;
			}
		}
		return null;
	}
}
