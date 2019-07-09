package akkaHW2019S;

public class Route implements Comparable<Route>{

	final int start;
	final int end;
	final int des;
	
	public Route(int start, int end, int des) {
		super();
		this.start = start;
		this.end = end;
		this.des = des;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return start&(end<<8)&(des<<16);	
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj==null) {
			return false;
		}
		if(obj==this) {
			return true;
		}
		if(obj instanceof Route) {
			Route o = (Route) obj;
			return o.start==this.start&&o.end==this.end&&o.des==this.des;
		}
		return false;
	}

	@Override
	public int compareTo(Route o) {
		// TODO Auto-generated method stub
		return this.des-o.des;
	}

	@Override
	public String toString() {
		return "Route [start=" + start + ", end=" + end + ", des=" + des + "]";
	}
	
	
}
