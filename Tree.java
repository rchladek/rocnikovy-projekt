import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

/** Trieda predstavuje evolucny strom, obsahuje najma koren tohto stromu, rozne metody na spravovanie
 *  a upravu stromu + metody na tvorbu vystupu podla tohto stromu */
class Tree {

	/** koren evolucneho stromu */
	Node root;

	/** premenna, ktora sa pouziva pri generovani vystupu */
	private int currentLineNumber;

	/** predstavuje zoznam riadkov vystupu v novom formate */
	ArrayList<String> output;

	Tree() {
		root = new Node("Root", 0);
		currentLineNumber = 2;
		output = new ArrayList<>();
	}

	Node findNodeByName(String name, Node node){
		if(Objects.equals(node.name, name)) return node;
		Node n1 = null;
		Node n2 = null;
		if(node.left != null) n1 = findNodeByName(name, node.left);
		if(node.right != null) n2 = findNodeByName(name, node.right);
		if(n1 != null) return n1;
		return n2;
	}

	private Node findNodeByTime(Double time, Node node) {
		if(node.time.equals(time)) return node;
		Node n1 = null;
		Node n2 = null;
		if(node.left != null) n1 = findNodeByTime(time, node.left);
		if(node.right != null) n2 = findNodeByTime(time, node.right);
		if(n1 != null) return n1;
		return n2;
	}

	/** spocita dlzky hran stromu smerom z korena do listov, aby v kazdom Node bol cas jeho vzniku */
	void sumTime(Node node, double time){
		node.time+= time;
		if(node.left != null || node.right != null) {
			sumTime(node.left, node.time);
			sumTime(node.right, node.time);
		}
	}

	private String chromosomesOutputPIVO(Node node){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < node.chromosomes.size(); i++) {
			for(Integer gene: node.chromosomes.get(i).genes){
				sb.append(gene).append(" ");
			}
			if(node.chromosomes.get(i).isCircular)
				sb.append("@ ");
			else sb.append("$ ");
		}
		return sb.append("# ").toString();
	}

	private String positionsOutputPIVO(Node node){
		String output = "";
		ArrayList<Integer> theseGenes = new ArrayList<>();
		ArrayList<Integer> parentGenes = new ArrayList<>();
		for(Chromosome ch: node.chromosomes) theseGenes.addAll(ch.genes);
		for(Chromosome ch: node.parent.chromosomes) parentGenes.addAll(ch.genes);
		for(Integer gene: theseGenes){
			for (int i = 0; i < parentGenes.size(); i++) {
				if(Math.abs(parentGenes.get(i)) == Math.abs(gene)){
					output += i + " ";
					break;
				}
			}
		}
		return output;
	}

	private void nodeOutputPIVO(Node node){
		if(node.events > 0){
			String outputLine1 = node.name + " e" + currentLineNumber + " e" +
				node.parent.lineNumber + " " + node.parent.time +
				" sp " + chromosomesOutputPIVO(node.parent);
			int n = node.getGeneQuantity();
			for (int i = 0; i < n; i++) {
				outputLine1 += i + " ";
			}
			output.add(outputLine1);
			currentLineNumber++;
			String outputLine2 = node.name + " e" + currentLineNumber + " e" +
				(currentLineNumber - 1) + " " + (node.time + node.parent.time) / 2 +
				" other " + chromosomesOutputPIVO(node) + positionsOutputPIVO(node);
			node.lineNumber = currentLineNumber;
			currentLineNumber++;
			output.add(outputLine2);
		} else {
			String outputLine = node.name + " e" + currentLineNumber + " e" +
				node.parent.lineNumber + " " + node.parent.time +
				" sp " + chromosomesOutputPIVO(node);
			int n = node.getGeneQuantity();
			for (int i = 0; i < n; i++) {
				outputLine += i + " ";
			}
			node.lineNumber = currentLineNumber;
			currentLineNumber++;
			output.add(outputLine);
		}
		if(node.left!= null || node.right!= null){
			nodeOutputPIVO(node.left);
			nodeOutputPIVO(node.right);
		} else {
			String outputLineLeaf = node.name + " e" + currentLineNumber + " e" +
				(currentLineNumber - 1) + " " + node.time +
				" leaf " + chromosomesOutputPIVO(node);
			int n = node.getGeneQuantity();
			for (int i = 0; i < n; i++) {
				outputLineLeaf += i + " ";
			}
			output.add(outputLineLeaf);
			currentLineNumber++;
		}
	}

	void generateOutputPIVO(){
		//korenovy riadok
		String line = root.name +" e1 root -0.05 root " + chromosomesOutputPIVO(root);
		int n = root.getGeneQuantity();
		for (int i = 0; i < n; i++) {
			line += "-1 ";
		}
		output.add(line);

		//ostatne riadky
		root.lineNumber = 1;
		nodeOutputPIVO(root.left);
		nodeOutputPIVO(root.right);
	}

	/** upravi strom (prida uzly do stromu) podla historie, pripravi strom na generovanie vystupu */
	void createTreeFromDUPHistory(ArrayList<History> history) {
		Node currentNode = null;
		TreeMap<String, String> nameMap = new TreeMap<>();
		for(History h: history){
			if(h.event_type.equals("d")){
				Node node;
				if(currentNode!= null && currentNode.name.equals(h.target_species)) node = currentNode;
				else {
					if(nameMap.containsKey(h.target_species))
						node = findNodeByName(nameMap.get(h.target_species), root);
					else node = findNodeByName(h.target_species, root);
				}
				Node parent;
				if(nameMap.containsKey(h.target_species))
					parent = new Node(nameMap.get(h.target_species), h.time);
				else parent = new Node(h.target_species, h.time);
				ArrayList<Atom> newAtoms = new ArrayList<>();
				newAtoms.addAll(node.atoms);
				newAtoms.removeAll(h.target_atoms);
				parent.howMany = h.source_atoms.size();
				parent.startIndexSource = newAtoms.indexOf(h.source_atoms.get(0));
				parent.startIndexTarget = node.atoms.indexOf(h.target_atoms.get(0));
				parent.atoms = newAtoms;
				parent.onlyChild = node;
				if(node.parent.left.equals(node)) node.parent.left = parent;
				else node.parent.right = parent;
				parent.parent = node.parent;
				node.parent = parent;
				currentNode = parent;
			}
			if(h.event_type.equals("s")){
				Node node = findNodeByTime(h.time, root);
				node.atoms = h.source_atoms;
				nameMap.put(node.left.name, node.name);
				currentNode = node;
			}
			if(h.event_type.equals("x")){
				Node node;
				if(currentNode!= null && currentNode.name.equals(h.target_species)) node = currentNode;
				else {
					if(nameMap.containsKey(h.target_species))
						node = findNodeByName(nameMap.get(h.target_species), root);
					else node = findNodeByName(h.target_species, root);
				}
				Node parent;
				if(nameMap.containsKey(h.target_species))
					parent = new Node(nameMap.get(h.target_species), h.time);
				else parent = new Node(h.target_species, h.time);
				ArrayList<Atom> newAtoms = new ArrayList<>();
				newAtoms.addAll(node.atoms);
				int j = newAtoms.indexOf(h.source_atoms.get(0));
				for(int k = 1; k < h.source_atoms.size() - 1; k++) {
					newAtoms.add(j + k, h.source_atoms.get(k));
				}
				parent.howMany = h.source_atoms.size();
				parent.startIndexSource = j;
				parent.atoms = newAtoms;
				parent.onlyChild = node;
				if(node.parent.left.equals(node)) node.parent.left = parent;
				else node.parent.right = parent;
				parent.parent = node.parent;
				node.parent = parent;
				currentNode = parent;
			}
		}
	}

	private void nodeOutputDUP(Node node){
		if(node.left!= null){
			String line = node.left.name + " e" + currentLineNumber + " e" +
				(currentLineNumber - 1) + " " + node.time + " sp ";
			for(Atom atom: node.atoms){
				if(atom.strand == - 1) line += "-";
				line += atom.type + " ";
			}
			line += "$ # ";
			for (int i = 0; i < node.atoms.size(); i++) {
				line += i + " ";
			}
			node.lineNumber = currentLineNumber;
			output.add(line);
			currentLineNumber++;
			nodeOutputDUP(node.left);

			line = node.right.name + " e" + currentLineNumber + " e" +
				(node.lineNumber - 1) + " " + node.time + " sp ";
			for(Atom atom: node.atoms){
				if(atom.strand == - 1) line += "-";
				line += atom.type + " ";
			}
			line += "$ # ";
			for (int i = 0; i < node.atoms.size(); i++) {
				line += i + " ";
			}
			node.lineNumber = currentLineNumber;
			currentLineNumber++;
			output.add(line);
			nodeOutputDUP(node.right);
		} else if(node.onlyChild!= null) {
			String line = node.name + " e" + currentLineNumber + " e" +
				node.parent.lineNumber + " " + node.time;
			if(node.onlyChild.atoms.size() > node.atoms.size()){
				line += " dup ";
				for(Atom atom: node.onlyChild.atoms){
					if(atom.strand == - 1) line += "-";
					line += atom.type + " ";
				}
				line += "$ # ";
				int i = 0;
				while(i < node.startIndexTarget){
					line += i + " ";
					i++;
				}

				if(node.onlyChild.atoms.get(i).type == node.atoms.get(node.startIndexSource).type){
					for (int j = 0; j < node.howMany; j++) {
						line += (node.startIndexSource + j) + " ";
					}
				} else {
					for (int j = node.howMany - 1; j >= 0 ; j--) {
						line+= (node.startIndexSource + j) + " ";
					}
				}
				for(; i < node.atoms.size(); i++){
					line += i + " ";
				}
				node.lineNumber = currentLineNumber;
				currentLineNumber++;
				output.add(line);
				nodeOutputDUP(node.onlyChild);
			} else {
				line += " del ";
				for(Atom atom: node.onlyChild.atoms){
					if(atom.strand == - 1) line += "-";
					line += atom.type + " ";
				}
				line += "$ # ";
				int i = 0;
				while(i < node.startIndexSource + 1){
					line += i + " ";
					i++;
				}
				i += node.howMany - 2;
				while(i < node.atoms.size()){
					line += i + " ";
					i++;
				}
				node.lineNumber = currentLineNumber;
				currentLineNumber++;
				output.add(line);
				nodeOutputDUP(node.onlyChild);
			}
		} else {
			String line = node.name + " e" + currentLineNumber + " e" +
				node.parent.lineNumber + " " + node.time + " leaf ";

			for(Atom atom: node.atoms){
				if(atom.strand == - 1) line += "-";
				line += atom.type + " ";
			}
			line += "$ # ";

			for (int i = 0; i < node.atoms.size(); i++) {
				line += i + " ";
			}
			output.add(line);
			currentLineNumber++;
		}
	}

	void generateOutputDUP(){
		//korenovy riadok
		String line = root.name +" e1 root -0.05 root ";
		for(Atom atom: root.atoms){
			line += atom.type + " ";
		}
		line += "$ # ";
		for (int i = 0; i < root.atoms.size(); i++) {
			line += "-1 ";
		}
		output.add(line);

		//ostatne riadky
		root.lineNumber = 1;
		nodeOutputDUP(root);

	}
}
