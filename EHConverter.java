import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;


public class EHConverter {
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
			if (atomString.equals("-")) {
				atom.isDeleted = true;
				atom.id = -1;
			} else {
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
		} else {
			Tree t = new Tree();
			t.root.name = str;
			return t;
		}
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
			String time = "";
			if(koniecIndex != str.length() - 1) {
				while (str.charAt(i) <= 'z' && str.charAt(i) >= 'a') {
					ancestorName = ancestorName.concat(str.charAt(i) + "");
					i++;
				}
				assert str.charAt(i) == ':';
				i++;
				while ((str.charAt(i) <= '9' && str.charAt(i) >= '0') || str.charAt(i) == '.') {
					time = time.concat(str.charAt(i) + "");
					i++;
					if (i == str.length()) break;
				}
			}
			Node left = parseNode(str.substring(1,ciarkaIndex));
			Node right = parseNode(str.substring(ciarkaIndex+1,koniecIndex));

			if(koniecIndex == str.length() - 1){
				time = Double.toString(Math.min(left.time, right.time)*2/3);
			}

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
		} else {
			int colonIndex = str.lastIndexOf(':');
			String name;
			double time;
			if(colonIndex == -1){
				int i = 0;
				while (str.charAt(i) <= 'z' && str.charAt(i) >= 'a') i++;
				name = str;
				time = 0.35;
			} else {
				name = str.substring(0, colonIndex);
				time = Double.parseDouble(str.substring(colonIndex + 1));
			}
			node = new Node(name, time);
			return node;
		}
	}

	public static void main(String[] args) throws IOException {
		//hodnota o kolko je posunuty cas, na zaciatku sa v polke tohto casu odohra udalost, pocas ktorej sa nic neudialo
		//prva udalost zacina potom v tomto case
		double time_offset = 0.01;

		if(args[0].equals("pivo")){
			if(args.length !=3) throw new IllegalArgumentException("Wrong number of arguments");
			File tree_file = new File(args[1]);
			String treeString = "";

			Scanner s = new Scanner(tree_file);
			while (s.hasNext()) {
				treeString = s.nextLine();
			}
			s.close();

			Tree t = parseTree(treeString);
			File reconstruction_file = new File(args[2]);

			Scanner s2 = new Scanner(reconstruction_file);

			while (s2.hasNextLine()) {
				String[] line = s2.nextLine().split("\\s+");
				Node node;
				int zaciatok = 0;
				if(line[0].startsWith("#")){
					line = s2.nextLine().split("\\s+");
					node = t.findNodeByName(line[0].substring(1), t.root);
					node.events = 1;
					line = s2.nextLine().split("\\s+");
				} else {
					node = t.findNodeByName(line[0], t.root);
					node.events = Integer.parseInt(line[1]);
					zaciatok = 2;
				}
				int n = line.length;
				ArrayList<Chromosome> chromosomes = new ArrayList<>();
				ArrayList<Integer> genes = new ArrayList<>();
				for (int i = zaciatok; i < n; i++) {
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
			s2.close();

			t.time_offset = time_offset;
			t.sumTime(t.root, time_offset);
			t.generateOutputPIVO();

			PrintWriter writer = new PrintWriter(args[1].split("_")[0] + "_output.history", "UTF-8");
			for(String line: t.output) {
				writer.println(line);
			}
			writer.close();
		} else if (args[0].startsWith("dup")){
			boolean isReal = args[0].equals("dup-real");
			boolean isGenerated = args[0].equals("dup-gen");
			if(!isReal && !isGenerated) throw new IllegalArgumentException("Wrong first argument");
			if(args.length !=4 && isGenerated) throw new IllegalArgumentException("Wrong number of arguments");
			if(args.length !=5 && isReal) throw new IllegalArgumentException("Wrong number of arguments");
			File tree_file = new File(args[1]);
			String treeString = "";

			Scanner s = new Scanner(tree_file);
			while (s.hasNext()) {
				treeString = s.nextLine();
			}
			s.close();

			Tree t = parseTree(treeString);
			if(isReal){
				t.root.time = Double.parseDouble(args[4]);
			}
			t.time_offset = time_offset;
			t.sumTime(t.root, time_offset);
			File atoms_file = new File(args[2]);

			Scanner s2 = new Scanner(atoms_file);
			while (s2.hasNext()) {
				String[] line = s2.nextLine().split("\\s+");
				Node node = t.findNodeByName(line[0], t.root);
				Atom a = new Atom();
				a.id = Integer.parseInt(line[1]);

				a.type = Integer.parseInt(line[2]);
				a.strand = Integer.parseInt(line[3]);
				node.atoms.add(a);
			}
			s2.close();

			File history_file = new File(args[3]);
			ArrayList<String[]> historyString = new ArrayList<>();
			Scanner s3 = new Scanner(history_file);
			while (s3.hasNext()) {
				String[] line = s3.nextLine().split("\\s+");
				if(!line[0].equals("===")) historyString.add(line);
			}
			s3.close();

			t.createTreeFromDUPHistory(parseHistory(historyString), isReal);
			if(isReal) {
				t.correctTime(t.root, time_offset);
			}
			t.generateOutputDUP();

			PrintWriter writer = new PrintWriter(args[1].split("\\.")[0] + "-output.history", "UTF-8");
			for(String line: t.output) {
				writer.println(line);
			}
			writer.close();

			} else throw new IllegalArgumentException("Wrong first argument");
	}
}
