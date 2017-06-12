import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Trieda predstavuje evolucny strom, obsahuje najma koren tohto stromu, rozne metody na spravovanie
 * a upravu stromu + metody na tvorbu vystupu podla tohto stromu
 */
class Tree {

	/** koren evolucneho stromu */
	Node root;

	/** premenna, ktora sa pouziva pri generovani vystupu */
	private int currentLineNumber;

	/** predstavuje zoznam riadkov vystupu v novom formate */
	ArrayList<String> output;

	/** o kolko posunieme cas v celom strome aby nezacinala prva speciacia v case 0 */
	double time_offset = 0;

	Tree() {
		root = new Node("Root", 0);
		root.parent = new Node("Helping-root", -1);
		root.parent.onlyChild = root;
		currentLineNumber = 2;
		output = new ArrayList<>();
	}

	Node findNodeByName(String name, Node node) {
		if (Objects.equals(node.name, name)) return node;
		Node n1 = null;
		Node n2 = null;
		if (node.onlyChild == null) {
			if (node.left != null) n1 = findNodeByName(name, node.left);
			if (node.right != null) n2 = findNodeByName(name, node.right);
			if (n1 != null) return n1;
			return n2;
		} else {
			return findNodeByName(name, node.onlyChild);
		}
	}

	private Node findNodeByTime(Double time, Node node) {
		if (node.time.equals(time)) return node;

		Node n1 = null;
		Node n2 = null;
		if (node.onlyChild == null) {
			if (node.left != null) n1 = findNodeByTime(time, node.left);
			if (node.right != null) n2 = findNodeByTime(time, node.right);
			if (n1 != null) return n1;
			return n2;
		} else {
			return findNodeByTime(time, node.onlyChild);
		}
	}

	/** spocita dlzky hran stromu smerom z korena do listov, aby v kazdom Node bol cas jeho vzniku */
	void sumTime(Node node, double time) {
		node.time += time;
		if (node.left != null || node.right != null) {
			sumTime(node.left, node.time);
			sumTime(node.right, node.time);
		}
	}

	/** priradi casy pre vrcholy podla casov v speciaciach a listoch */
	void correctTime(Node node, double time) {
		node.time = time;

		int howMany = 0;
		Node nextSpeciation = node;

		while (nextSpeciation.onlyChild != null){
			howMany++;
			nextSpeciation = nextSpeciation.onlyChild;
		}
		howMany--;
		Double nextTime = nextSpeciation.time;

		Node helpNode;
		if(nextSpeciation.left == null){
			helpNode = nextSpeciation.parent;
		} else {
			helpNode = nextSpeciation;
		}

		int i = howMany;
		while (helpNode!= node){
			helpNode.time = node.time + i*(nextTime - node.time)/(howMany + 1);
			helpNode = helpNode.parent;
			i--;
		}

		if(nextSpeciation.left == null) return;
		correctTime(nextSpeciation.left, nextTime);
		correctTime(nextSpeciation.right, nextTime);

	}

	private String chromosomesOutputPIVO(Node node) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < node.chromosomes.size(); i++) {
			for (Integer gene : node.chromosomes.get(i).genes) {
				sb.append(gene).append(" ");
			}
			if (node.chromosomes.get(i).isCircular) {
				sb.append("@ ");
			} else {
				sb.append("$ ");
			}
		}
		return sb.append("# ").toString();
	}

	private String positionsOutputPIVO(Node node) {
		String output = "";
		ArrayList<Integer> theseGenes = new ArrayList<>();
		ArrayList<Integer> parentGenes = new ArrayList<>();
		for (Chromosome ch : node.chromosomes) theseGenes.addAll(ch.genes);
		for (Chromosome ch : node.parent.chromosomes) parentGenes.addAll(ch.genes);
		for (Integer gene : theseGenes) {
			for (int i = 0; i < parentGenes.size(); i++) {
				if (Math.abs(parentGenes.get(i)) == Math.abs(gene)) {
					output += i + " ";
					break;
				}
			}
		}
		return output;
	}

	private void nodeOutputPIVO(Node node) {
		if (node.events > 0) {
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
		if (node.left != null || node.right != null) {
			nodeOutputPIVO(node.left);
			nodeOutputPIVO(node.right);
			// ODSTRANENE LEAFY
//		} else {
//			String outputLineLeaf = node.name + " e" + currentLineNumber + " e" +
//				(currentLineNumber - 1) + " " + node.time +
//				" leaf " + chromosomesOutputPIVO(node);
//			//int n = node.getGeneQuantity();
//			for (int i = 0; i < n; i++) {
//				outputLineLeaf += i + " ";
//			}
//			output.add(outputLineLeaf);
//			currentLineNumber++;
		}
	}

	void generateOutputPIVO() {
		//korenovy riadok
		String line = root.name + " e0 root 0 root " + chromosomesOutputPIVO(root);
		int n = root.getGeneQuantity();
		for (int i = 0; i < n; i++) {
			line += "-1 ";
		}
		output.add(line);

		line = root.name + " e1 e0 " + time_offset / 2 + " other " + chromosomesOutputPIVO(root);
		n = root.getGeneQuantity();
		for (int i = 0; i < n; i++) {
			line += i + " ";
		}
		output.add(line);

		//ostatne riadky
		root.lineNumber = 1;
		if (root.left != null || root.right != null) {
			nodeOutputPIVO(root.left);
			nodeOutputPIVO(root.right);
		}
	}

	/** upravi strom (prida uzly do stromu) podla historie, pripravi strom na generovanie vystupu */
	void createTreeFromDUPHistory(ArrayList<History> history, boolean isReal) {
		Node currentNode = null;
		TreeMap<String, String> nameMap = new TreeMap<>();
		Node lastLeftChild = null;
		Node lastRightChild = null;
		for (History h : history) {
			if (h.event_type.equals("d")) {
				Node node;
				if (currentNode != null && currentNode.name.equals(nameMap.get(h.target_species))) {
					node = currentNode;
				} else {
					if (!nameMap.containsKey(h.target_species)) {
						nameMap.put(h.target_species, h.target_species);
					}
					node = findNodeByName(nameMap.get(h.target_species), root);
				}
				Node parent = new Node(nameMap.get(h.target_species));
				ArrayList<Atom> newAtoms = new ArrayList<>();
				newAtoms.addAll(node.atoms);
				node.event_type = "dup";
				if(isReal){
					if(node.time <= 0) node.time = h.time;
				} else node.time = h.time + time_offset;
				newAtoms.removeAll(h.target_atoms);
				int startIndexNewAtoms = newAtoms.indexOf(h.source_atoms.get(0));
				parent.howMany = h.source_atoms.size();
				parent.startIndexSource = node.atoms.indexOf(h.source_atoms.get(0));
				parent.startIndexTarget = node.atoms.indexOf(h.target_atoms.get(0));

				if (parent.startIndexSource < parent.startIndexTarget) {
					int k = 0;
					while (k < parent.startIndexSource) {
						node.atomsPos.add(k);
						k++;
					}

					int howManyDeleted = 0;

					for (int i = 0; i < h.source_atoms.size(); i++) {
						if (h.source_atoms.get(i).isDeleted) {
							Atom leftAnchor = h.source_atoms.get(i - 1);
							int j = i;
							ArrayList<Atom> deletedAtoms = new ArrayList<>();
							while (h.source_atoms.get(j).isDeleted) {
								int atomIndex;
								if (h.strand == 1) {
									atomIndex = j;
								} else {
									atomIndex = h.source_atoms.size() - j - 1;
								}
								deletedAtoms.add(h.target_atoms.get(atomIndex));
								howManyDeleted++;
								j++;
							}
							int leftIndex = newAtoms.indexOf(leftAnchor);
							newAtoms.addAll(leftIndex + 1, deletedAtoms);
							i = j;
						}
						node.atomsPos.add(k + i);
					}

					k += h.source_atoms.size();
					while (k < (parent.startIndexTarget + howManyDeleted)) {
						node.atomsPos.add(k);
						k++;
					}

					for (int i = 0; i < h.target_atoms.size(); i++) {
						if (h.target_atoms.get(i).isDeleted) {
							int j = i;
							while (h.target_atoms.get(j).isDeleted) {
								j++;
							}
							i = j;
						}
						if (h.strand == 1) {
							node.atomsPos.add(parent.startIndexSource + i);
						} else {
							node.atomsPos.add(parent.startIndexSource + (h.target_atoms.size() - i - 1));
						}
					}

					while (k < newAtoms.size()) {
						node.atomsPos.add(k);
						k++;
					}
				} else {
					int k = 0;
					while (k < parent.startIndexTarget) {
						node.atomsPos.add(k);
						k++;
					}
					int howManyDeleted = 0;
					for (int i = 0; i < h.target_atoms.size(); i++) {
						if (h.target_atoms.get(i).isDeleted) {
							int j = i;
							while (h.target_atoms.get(j).isDeleted) {
								howManyDeleted++;
								j++;
							}
							i = j;
						}
						if (h.strand == 1) {
							node.atomsPos.add(startIndexNewAtoms + i);
						} else {
							node.atomsPos.add(startIndexNewAtoms + (h.target_atoms.size() - i - 1));
						}
					}

					while (k < (startIndexNewAtoms + howManyDeleted)) {
						node.atomsPos.add(k);
						k++;
					}

					for (int i = 0; i < h.source_atoms.size(); i++) {
						if (h.source_atoms.get(i).isDeleted) {
							Atom leftAnchor = h.source_atoms.get(i - 1);
							int j = i;
							ArrayList<Atom> deletedAtoms = new ArrayList<>();
							while (h.source_atoms.get(j).isDeleted) {
								int atomIndex;
								if (h.strand == 1) {
									atomIndex = j;
								} else {
									atomIndex = h.source_atoms.size() - j - 1;
								}
								deletedAtoms.add(h.target_atoms.get(atomIndex));
								j++;
							}
							int leftIndex = newAtoms.indexOf(leftAnchor);
							newAtoms.addAll(leftIndex + 1, deletedAtoms);
							i = j;
						}
						node.atomsPos.add(k + i);
					}

					k += h.source_atoms.size();

					while (k < newAtoms.size()) {
						node.atomsPos.add(k);
						k++;
					}
				}
				parent.atoms = newAtoms;
				parent.onlyChild = node;
				if (node.parent.onlyChild != null) {
					node.parent.onlyChild = parent;
					root = parent;
				} else if (node.parent.left.equals(node)) {
					node.parent.left = parent;
					lastLeftChild = parent;
				} else {
					node.parent.right = parent;
					lastRightChild = parent;
				}
				parent.parent = node.parent;
				node.parent = parent;
				currentNode = parent;
			}
			if (h.event_type.equals("s")) {
				String nameString = lastLeftChild.name + "-" + lastRightChild.name;
				Node ancestor = findNodeByName(nameString, root);
				if(ancestor == null){
					ancestor = findNodeByTime(h.time + time_offset, root);
				}

				ArrayList<Atom> newAtoms = new ArrayList<>();
				for (int i = 0; i < h.source_atoms.size(); i++) {
					if (h.source_atoms.get(i).isDeleted) {
						int j = i;
						ArrayList<Atom> deletedAtoms = new ArrayList<>();
						while (h.source_atoms.get(j).isDeleted) {
							int atomIndex;
							if (h.strand == 1) {
								atomIndex = j;
							} else {
								atomIndex = h.source_atoms.size() - j - 1;
							}
							deletedAtoms.add(h.target_atoms.get(atomIndex));
							if (j == h.source_atoms.size() - 1) break;
							j++;
						}
						newAtoms.addAll(deletedAtoms);
						i = j;
						if (!h.source_atoms.get(i).isDeleted) {
							ancestor.left.atomsPos.add(i);
							newAtoms.add(h.source_atoms.get(i));
						}
					} else {
						newAtoms.add(h.source_atoms.get(i));
						ancestor.left.atomsPos.add(i);
					}
				}

				for (int i = 0; i < h.target_atoms.size(); i++) {
					if (h.target_atoms.get(i).isDeleted) {
						int j = i;
						while (h.target_atoms.get(j).isDeleted) {
							j++;
						}
						i = j;
					}
					ancestor.right.atomsPos.add(i);
				}

				if(h.event_count > 1){
					Node speciationNodeLeft = new Node(ancestor.left.name);
					Node speciationNodeRight = new Node(ancestor.right.name);
					ancestor.left.event_type = "del";
					ancestor.right.event_type = "del";
					speciationNodeLeft.atoms.addAll(newAtoms);
					speciationNodeRight.atoms.addAll(newAtoms);
					for (int i = 0; i < newAtoms.size(); i++) {
						speciationNodeLeft.atomsPos.add(i);
						speciationNodeRight.atomsPos.add(i);
					}
					speciationNodeLeft.onlyChild = ancestor.left;
					ancestor.left.parent = speciationNodeLeft;
					ancestor.left = speciationNodeLeft;
					speciationNodeLeft.parent = ancestor;

					speciationNodeRight.onlyChild = ancestor.right;
					ancestor.right.parent = speciationNodeRight;
					ancestor.right = speciationNodeRight;
					speciationNodeRight.parent = ancestor;
				}

				ancestor.left.event_type = "sp";
				ancestor.right.event_type = "sp";
				ancestor.left.time = (h.time + time_offset);
				ancestor.right.time = (h.time + time_offset);
				ancestor.atoms = newAtoms;
				nameMap.put(ancestor.left.name, ancestor.name);
				currentNode = ancestor;
			}
			if (h.event_type.equals("x")) {
				Node node;
				if (currentNode != null && currentNode.name.equals(h.target_species)) {
					node = currentNode;
				} else {
					if (!nameMap.containsKey(h.target_species)) {
						nameMap.put(h.target_species, h.target_species);
					}
					node = findNodeByName(nameMap.get(h.target_species), root);
				}
				Node parent = new Node(nameMap.get(h.target_species));
				ArrayList<Atom> newAtoms = new ArrayList<>();
				newAtoms.addAll(node.atoms);
				node.event_type = "del";
				if(isReal){
					if(node.time <= 0) node.time = h.time;
				} else node.time = h.time + time_offset;
				parent.howMany = h.source_atoms.size();
				parent.startIndexSource = node.atoms.indexOf(h.source_atoms.get(0));

				int k = 0;
				while(k < parent.startIndexSource){
					node.atomsPos.add(k);
					k++;
				}


				for (int i = 0; i < h.target_atoms.size(); i++) {
					if (h.target_atoms.get(i).isDeleted) {
						Atom leftAnchor = h.source_atoms.get(i - 1);
						int j = i;
						ArrayList<Atom> deletedAtoms = new ArrayList<>();
						while (h.target_atoms.get(j).isDeleted) {
							int atomIndex;
							if (h.strand == 1) {
								atomIndex = j;
							} else {
								atomIndex = h.source_atoms.size() - j - 1;
							}
							deletedAtoms.add(h.source_atoms.get(atomIndex));
							if (j == h.source_atoms.size() - 1) break;
							j++;
						}
						newAtoms.addAll(newAtoms.indexOf(leftAnchor) + 1, deletedAtoms);
						i = j;
						if (!h.target_atoms.get(i).isDeleted) {
							node.atomsPos.add(k + i);
						}
					} else {
						node.atomsPos.add(k + i);
					}
				}

				k+=h.source_atoms.size();
				while (k < newAtoms.size()){
					node.atomsPos.add(k);
					k++;
				}

				parent.atoms = newAtoms;
				parent.onlyChild = node;
				if (node.parent.onlyChild != null) {
					node.parent.onlyChild = parent;
					root = parent;
				} else if (node.parent.left.equals(node)) {
					node.parent.left = parent;
					lastLeftChild = parent;
				} else {
					node.parent.right = parent;
					lastRightChild = parent;
				}
				parent.parent = node.parent;
				node.parent = parent;
				currentNode = parent;
			}
		}
	}

	private void nodeOutputDUP(Node node) {
		String line = node.name + " e" + currentLineNumber + " e" +
			node.parent.lineNumber + " " + node.time + " " + node.event_type + " ";
		for (Atom atom : node.atoms) {
			if (atom.strand == -1) line += "-";
			line += atom.type + " ";
		}
		line += "$ # ";
		for (int i = 0; i < node.atomsPos.size(); i++) {
			line += node.atomsPos.get(i) + " ";
		}
		node.lineNumber = currentLineNumber;
		output.add(line);
		currentLineNumber++;
		if (node.onlyChild != null) {
			nodeOutputDUP(node.onlyChild);
		} else if (node.left != null) {
			nodeOutputDUP(node.left);
			nodeOutputDUP(node.right);
		} else {
			//ak chceme leafy na konci
			//leafy v konecnom dosledku koncia o time_offset + (leaf.time - leaf.parent.time)
			line = node.name + " e" + currentLineNumber + " e" +
				(currentLineNumber - 1) + " " + (2*node.time - node.parent.time) + " leaf ";
			for (Atom atom : node.atoms) {
				if (atom.strand == -1) line += "-";
				line += atom.type + " ";
			}
			line += "$ # ";
			for (int i = 0; i < node.atomsPos.size(); i++) {
				line += i + " ";
			}
			output.add(line);
			currentLineNumber++;
		}
	}

	void generateOutputDUP() {
		//korenovy riadok
		String line = root.name + " e0 root 0 root ";
		for (Atom atom : root.atoms) {
			if (atom.strand == -1) line += "-";
			line += atom.type + " ";
		}
		line += "$ # ";
		for (int i = 0; i < root.atoms.size(); i++) {
			line += "-1 ";
		}
		output.add(line);

		//udalost, v ktorej sa nic neudialo
		line = root.name + " e1 e0 " + time_offset / 2 + " other ";
		for (Atom atom : root.atoms) {
			if (atom.strand == -1) line += "-";
			line += atom.type + " ";
		}
		line += "$ # ";
		for (int i = 0; i < root.atoms.size(); i++) {
			line += i + " ";
		}
		output.add(line);

		//ostatne riadky
		currentLineNumber = 2;
		root.lineNumber = 1;
		if (root.onlyChild != null) {
			nodeOutputDUP(root.onlyChild);
		} else if (root.left != null || root.right != null) {
			nodeOutputDUP(root.left);
			nodeOutputDUP(root.right);
		}
	}
}
