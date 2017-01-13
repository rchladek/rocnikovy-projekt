import java.util.ArrayList;

class Node {
	Double time;
	Node parent;
	Node left;
	Node right;
	String name;

	//variables for PIVO
	ArrayList<Chromosome> chromosomes;
	int events;
	int lineNumber;

	//variables for DUP
	ArrayList<Atom> atoms;
	Node onlyChild;
	int startIndexSource;
	int startIndexTarget;
	int howMany;

	Node(String name, double time) {
		this.time = time;
		parent = null;
		left = null;
		right = null;
		this.name = name;
		chromosomes = new ArrayList<>();
		events = 0;
		lineNumber = -1;
		atoms = new ArrayList<>();
		onlyChild = null;
	}

	int getGeneQuantity(){
		int quantity = 0;
		for (Chromosome ch: chromosomes) {
			quantity+= ch.genes.size();
		}
		return quantity;
	}

	@Override public boolean equals(Object o) {
		if(! (o instanceof Node)) return false; //a Person can't be equal to a non-person

		Node n = (Node) o;
		return time.equals(n.time);
	}
}
