import java.util.ArrayList;

/** Trieda predstavujuca sparsovany riadok vstupnej duplikacnej historie */
class History {
	String source_species;
	String target_species;
	Double time;
	String event_type;
	Integer strand;
	Integer event_count;
	ArrayList<Atom> source_atoms;
	ArrayList<Atom> target_atoms;
}
