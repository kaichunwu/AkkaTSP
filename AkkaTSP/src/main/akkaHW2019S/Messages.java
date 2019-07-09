package akkaHW2019S;

import java.util.HashSet;
import java.util.Set;

/**
 * Messages that are passed around the actors are usually immutable classes.
 * Think how you go about creating immutable classes:) Make them all static
 * classes inside the Messages class.
 * 
 * This class should have all the immutable messages that you need to pass
 * around actors. You are free to add more classes(Messages) that you think is
 * necessary
 * 
 * @author Akash Nagesh and M. Kokar
 *
 */
public class Messages {
	
	//Messages defined here

	private final MessageType type;
	private Set<Route> routes = new HashSet<>();
	private int start;
	private int scale;
	private int num;
	private String winner;
	private int min = Integer.MAX_VALUE;;
	
	public Messages(MessageType type) {
		super();
		this.type = type;
	}

	public Messages(MessageType type, Set<Route> routes, int min ,int start) {
		super();
		this.type = type;
		this.routes = routes;
		this.min = min;
		this.start = start;
	}

	public Messages(MessageType type, Set<Route> routes, String winner, int min) {
		super();
		this.type = type;
		this.routes = routes;
		this.winner = winner;
		this.min = min;
	}

	public Messages(MessageType type, int start, int scale, int num) {
		super();
		this.type = type;
		this.start = start;
		this.scale = scale;
		this.num = num;
	}

	public MessageType getType() {
		return type;
	}

	public Set<Route> getRoutes() {
		return routes;
	}

	public int getStart() {
		return start;
	}

	public String getWinner() {
		return winner;
	}

	public int getMin() {
		return min;
	}

	public int getScale() {
		return scale;
	}

	public int getNum() {
		return num;
	}

	@Override
	public String toString() {
		return "Messages [type=" + type + ", start=" + start + ", min=" + min + "]";
	}

	
	
}