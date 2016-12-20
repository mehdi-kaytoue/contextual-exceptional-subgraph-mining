import java.io.*;
import java.util.*;
import java.text.*;

class Arete {
    int u;
    int v;
    Vector<Tuple> tuples;
    double q;
    double chi;
    int wstar;
    public Arete(int i, int j, int ws){
	u = i;
	v = j;
	tuples = new Vector<Tuple>();
	this.q = 0.0;
	this.chi = 0.0;
	wstar = ws;
    }
    public Arete copy() {
	Arete r = new Arete(this.u, this.v, this.wstar);
	r.q = this.q;
	r.chi = this.chi;
	r.wstar = this.wstar;
	r.tuples = new Vector<Tuple>();
	for(int i = 0; i < this.tuples.size(); i++)
	    r.tuples.add(this.tuples.elementAt(i));
	return r;
    }
}

// Local Variables:
// coding: utf-8
// End:
