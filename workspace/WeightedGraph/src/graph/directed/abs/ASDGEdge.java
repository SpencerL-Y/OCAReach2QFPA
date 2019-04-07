package graph.directed.abs;

public class ASDGEdge {
	private ASDGVertex from, to;
	public ASDGEdge(ASDGVertex from, ASDGVertex to) {
		this.setFrom(from);
		this.setTo(to);
	}
	
	//setters and getters
	public ASDGVertex getFrom() {
		return from;
	}
	public void setFrom(ASDGVertex from) {
		this.from = from;
	}
	public ASDGVertex getTo() {
		return to;
	}
	public void setTo(ASDGVertex to) {
		this.to = to;
	}
}
