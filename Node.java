import java.util.ArrayList;

/** uzol evolucneho stromu, predstavuje organizmus v danom case a mieste v evolucnom strome */
class Node {
	Double time;
	Node parent;
	Node left;
	Node right;
	String name;
	int events;
	int lineNumber;
	String event_type;

	//variables for PIVO
	/** zoznam chromozomov*/
	ArrayList<Chromosome> chromosomes;

	//variables for DUP
	/** zoznam atomov organizmu v danom case, nahradza funkciu premennej chromosomes */
	ArrayList<Atom> atoms;
	ArrayList<Integer> atomsPos;

	/** referencia na jedine dieta uzlu, pri udalosti roznej od speciacie */
	Node onlyChild;

	/** index, od ktoreho v zozname atomov source_species zacina zduplikovana/vymazana cast */
	int startIndexSource;

	/** index, kam sa ulozi zduplikovana cast v target_species */
	int startIndexTarget;

	/** kolko atomov sa zduplikovalo/vymazalo */
	int howMany;

	ArrayList<Integer> deletionPos;

	Node(String name) {
		time = (double) -1;
		parent = null;
		left = null;
		right = null;
		this.name = name;
		chromosomes = new ArrayList<>();
		events = 0;
		lineNumber = -1;
		atoms = new ArrayList<>();
		onlyChild = null;
		atomsPos = new ArrayList<>();
	}

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
		atomsPos = new ArrayList<>();
		deletionPos = new ArrayList<>();
	}

	int getGeneQuantity(){
		int quantity = 0;
		for (Chromosome ch: chromosomes) {
			quantity+= ch.genes.size();
		}
		return quantity;
	}

	@Override public boolean equals(Object o) {
		if(! (o instanceof Node)) return false;

		Node n = (Node) o;
		return time.equals(n.time);
	}
}
