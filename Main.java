import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;


public class Main {
	private static ArrayList<History> parseHistory(ArrayList<String[]> historyString){
		ArrayList<History> historyArrayList = new ArrayList<>();
		for (int i = historyString.size() - 1; i >= 0; i--) {
			History h = new History();
			h.source_species = historyString.get(i)[0];
			h.target_species = historyString.get(i)[1];
			h.time = Double.parseDouble(historyString.get(i)[2]);
			h.event_type = historyString.get(i)[3];
			h.strand = Integer.parseInt(historyString.get(i)[4]);
			h.event_count = Integer.parseInt(historyString.get(i)[5]);
			h.source_atoms = parseAtoms(historyString.get(i)[6].split(";"));
			h.target_atoms = parseAtoms(historyString.get(i)[7].split(";"));
			historyArrayList.add(h);
		}
		return historyArrayList;
	}

	private static ArrayList<Atom> parseAtoms(String[] line){
		ArrayList<Atom> atoms = new ArrayList<>();
		for(String atomString : line){
			Atom atom = new Atom();
			if (!atomString.equals("-")) {
				if (atomString.startsWith("-")) {
					atom.strand = -1;
					atomString = atomString.substring(1);
				} else atom.strand = 1;
				int j = atomString.lastIndexOf(".");
				atom.type = Integer.parseInt(atomString.substring(0, j));
				atom.id = Integer.parseInt(atomString.substring(j + 1));
			}
			atoms.add(atom);
		}
		return atoms;
	}

	private static Tree parseTree(String str){
		if(str.startsWith("(")){
			//suradnica ciarky
			int i = 1;
			int counter = 0;
			while(!(str.charAt(i) == ',' && counter == 0)){
				if(str.charAt(i) == '(') counter++;
				if(str.charAt(i) == ')') counter--;
				i++;
			}
			//suradnica poslednej zatvorky
			int k = str.length()-1;
			while(!(str.charAt(k)==')')) k--;
			Node left = parseNode(str.substring(1,i));
			Node right = parseNode(str.substring(i+1,k));
			Tree t = new Tree();
			left.parent = t.root;
			right.parent = t.root;
			t.root.left = left;
			t.root.right = right;
			String name;
			if(left.name.contains("-")) name = left.name.substring(0,left.name.lastIndexOf('-'));
			else name = left.name;
			name+="-";
			if(right.name.contains("-")) name += right.name.substring(right.name.lastIndexOf('-')+1);
			else name += right.name;
			t.root.name = name;
			return t;
		}
		return null;
	}

	private static Node parseNode(String str){
		Node node;
		if(str.startsWith("(")){
			int i = 1;
			int counter = 0;
			int ciarkaIndex = 0;
			while(!(str.charAt(i) == ')' && counter == 0)){
				if(str.charAt(i) == '(') counter++;
				if(str.charAt(i) == ')') counter--;
				if(str.charAt(i) == ',' && counter == 0) ciarkaIndex = i;
				i++;
			}
			int koniecIndex = i;
			i++;
			String ancestorName = "";
			while(str.charAt(i) <= 'z' && str.charAt(i) >='a'){
				ancestorName = ancestorName.concat(str.charAt(i) + "");
				i++;
			}
			assert str.charAt(i) == ':';
			i++;
			String time = "";
			while((str.charAt(i) <= '9' && str.charAt(i) >='0') || str.charAt(i) == '.'){
				time = time.concat(str.charAt(i) + "");
				i++;
				if(i == str.length()) break;
			}
			Node left = parseNode(str.substring(1,ciarkaIndex));
			Node right = parseNode(str.substring(ciarkaIndex+1,koniecIndex));
			String name;
			if(!Objects.equals(ancestorName, "")) name = ancestorName;
			else {
				if (left.name.contains("-"))
					name = left.name.substring(0, left.name.lastIndexOf('-'));
				else name = left.name;
				name += "-";
				if (right.name.contains("-"))
					name += right.name.substring(right.name.lastIndexOf('-') + 1);
				else name += right.name;
			}
			node = new Node(name, Double.parseDouble(time));
			left.parent = node;
			right.parent = node;
			node.left = left;
			node.right = right;
			return node;
		}
		else {
			int colonIndex = str.lastIndexOf(':');
			String name = str.substring(0,colonIndex);
			double time = Double.parseDouble(str.substring(colonIndex +1));
			node = new Node(name, time);
			return node;
		}
	}

	public static void main(String[] args) {
		if(args[0].equals("pivo")){
			if(args.length !=3) throw new IllegalArgumentException("Wrong number of arguments");
			File tree_file = new File(args[1]);
			String treeString = "";
			try {
				Scanner s = new Scanner(tree_file);
				while (s.hasNext()) {
					treeString = s.nextLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Tree t = parseTree(treeString);
			File reconstruction_file = new File(args[2]);
			try {
				Scanner s2 = new Scanner(reconstruction_file);
				while (s2.hasNext()) {
					String[] line = s2.nextLine().split("\\s+");
					Node node = t.findNodeByName(line[0], t.root);
					node.events = Integer.parseInt(line[1]);
					int n = line.length;
					ArrayList<Chromosome> chromosomes = new ArrayList<>();
					ArrayList<Integer> genes = new ArrayList<>();
					for (int i = 2; i < n; i++) {
						if (line[i].equals("@")) {
							chromosomes.add(new Chromosome(genes, true));
							genes = new ArrayList<>();
						} else if (line[i].equals("$")) {
							chromosomes.add(new Chromosome(genes, false));
							genes = new ArrayList<>();
						} else genes.add(Integer.parseInt(line[i]));
					}
					node.chromosomes = chromosomes;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			t.sumTime(t.root, 0);
			t.generateOutputPIVO();
			try {
				PrintWriter writer = new PrintWriter(args[1].split("_")[0] + "_output.history", "UTF-8");
				for(String line: t.output) {
					writer.println(line);
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if (args[0].equals("dup")){
			if(args.length !=4) throw new IllegalArgumentException("Wrong number of arguments");
			File tree_file = new File(args[1]);
			String treeString = "";
			try {
				Scanner s = new Scanner(tree_file);
				while (s.hasNext()) {
					treeString = s.nextLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Tree t = parseTree(treeString);
			t.sumTime(t.root, 0);
			File atoms_file = new File(args[2]);
			try {
				Scanner s = new Scanner(atoms_file);
				while (s.hasNext()) {
					String[] line = s.nextLine().split("\\s+");
					Node node = t.findNodeByName(line[0], t.root);
					Atom a = new Atom();
					a.id = Integer.parseInt(line[1]);
					a.type = Integer.parseInt(line[2]);
					a.strand = Integer.parseInt(line[3]);
					node.atoms.add(a);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			File history_file = new File(args[3]);
			ArrayList<String[]> historyString = new ArrayList<>();
			try {
				Scanner s = new Scanner(history_file);
				while (s.hasNext()) {
					historyString.add(s.nextLine().split("\\s+"));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			t.createTreeFromDUPHistory(parseHistory(historyString));
			t.generateOutputDUP();
			try {
				PrintWriter writer = new PrintWriter(args[1].split("\\.")[0] + "-output.history", "UTF-8");
				for(String line: t.output) {
					writer.println(line);
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else throw new IllegalArgumentException("Wrong first argument");
	}
}
