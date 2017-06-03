/** Trieda predstavujuca jeden "atom" - obsahuje vlakno, typ a id atomu */
class Atom {
	int id;
	int type;
	int strand;
	boolean isDeleted;

	@Override public boolean equals(Object o) {
		if(! (o instanceof Atom)) return false;

		Atom a = (Atom) o;
		return id == a.id;
	}
}
