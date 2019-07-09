package akkaHW2019S;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Main class for your estimation actor system.
 *
 * @author Akash Nagesh and M. Kokar
 *
 */
public class User {

	public static void main(String[] args) throws Exception {		
		ActorSystem system = ActorSystem.create("EstimationSystem");
		
		/*
		 * Create the Solver Actor and send it the StartProcessing
		 * message. Once you get back the response, use it to print the result.
		 * Remember, there is only one actor directly under the ActorSystem.
		 * Also, do not forget to shutdown the actorsystem
		 */
		String fileName = "cities/cities.txt";
		Props solverProps = Props.create(Solver.class, fileName);
		ActorRef solver = system.actorOf(solverProps, "solver");
		
		int start = 3;
		int length = 108;
		
		solver.tell(new Messages(MessageType.SOLVER_START,start,length,0), null);
		
	}

}
