class Atom {
	int id;
	int type;
	int strand;
	boolean isDeleted;

	@Override public boolean equals(Object o) {
		if(! (o instanceof Atom)) return false; //a Person can't be equal to a non-person

		Atom a = (Atom) o;
		return type == a.type && id == a.id;
	}
}
