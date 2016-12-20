import java.io.*;
import java.util.*;
import java.text.*;

public class CC {
    HashMap<Integer, Integer> vertexMap;
    Vector<Vector<Arete>> g;
    DataFR data;
    //measures
    int nbVertices;
    int nbEdges;    
    double qSum;
    //values
    int maxCC;
    int sumCC;
    //flag
    Boolean isPruned;
    
    //SCC
    Vector<Integer> stackMember; 
    Vector<Integer>scc;
    Vector<Integer> tps;
    Vector<Integer> low;
    Vector<Integer> remove;
    MutableInteger numSCC;
    MutableInteger N;
    List<Integer> l;

    
    List<Integer> parentIds;
    
    public CC(DataFR data) {
  
	this.data = data;
	this.g = new Vector<Vector<Arete>>();
	this.vertexMap = new HashMap<Integer, Integer>();

	//SCC
	this.stackMember = new Vector<Integer>();
	this.tps = new Vector<Integer>();
	this.scc = new Vector<Integer>();
	this.low = new Vector<Integer>();
	this.remove = new Vector<Integer>();
	N = new MutableInteger(0);
	numSCC = new MutableInteger(0);
	l = new ArrayList<Integer>();
	//values
	nbVertices = 0;
	nbEdges = 0;
	qSum = 0.0;
	maxCC = 0;
	sumCC = 0;
	isPruned = false;
    }
   
    public void copyCC(CC c) {
	c.vertexMap.putAll(this.vertexMap);
	c.data = this.data;
	c.g.addAll(this.g);
	c.qSum = this.qSum;
	c.nbVertices = this.nbVertices;
	c.nbEdges = this.nbEdges;    
	c.sumCC = this.sumCC;
	c.maxCC = this.maxCC;
    }
    /*
    public void mergeCC(CC c) {
	this.vertexMap.putAll(c.vertexMap);
	this.data = this.data;
	this.g.addAll(this.g);
	this.qSum += c.qSum;
	this.nbVertices += c.nbVertices;
	this.nbEdges += c.nbEdges;    
	this.sumCC += c.sumCC;
	this.maxCC = max(this.maxCC,c.maxCC);

    }
    */
    public void printCC(DataFR data) {
	if(isPruned)
	    System.out.println("isPruned");
	else
	    System.out.println("valide");
	for(int i = 0; i < g.size(); i++) {
	    Integer n = data.findKeyVertex(vertexMap, i);
	    //System.out.println(n);
	    String s = data.findKey(data.vertexNames, n);
	    System.out.print(s+" => ");
	    for(int j = 0; j < g.elementAt(i).size(); j++) {
		Arete a = g.elementAt(i).elementAt(j);
		String ss = data.findKey(data.vertexNames,a.v);
		DecimalFormat df = new DecimalFormat("0.00");
		System.out.print(ss + " ("+ a.tuples.size() + " -- "+a.wstar + " " + df.format(a.q)+") ");
	    }
	    if(i == g.size() - 1)
		System.out.println(" qSum max and sum "+qSum+ " "+maxCC+ " "+sumCC);
	    else
		System.out.println(" ");
	}
    }
    
    
    /**
     * Print a CC into the output file for the vizu tool.
     */
    public void writeCC(String pattern, BitSet pat)     {
	if(isPruned) return; // Should we keep that line? yes!!
	for(int i = 0; i < g.size(); i++) {
	    Integer n = data.findKeyVertex(vertexMap, i);
	    String s = DataFR.findKey(data.vertexNames, n);
	    for(int j = 0; j < g.elementAt(i).size(); j++) {
		Arete a = g.elementAt(i).elementAt(j);
		String ss = DataFR.findKey(data.vertexNames,a.v);
		DecimalFormat df = new DecimalFormat("0.00");//// a changer!!!
		//on a une arete
		FastRabbit.out.write(s + ";" + ss + ";" +  a.tuples.size() + ";" + a.wstar + ";" + df.format(a.q));
		//System.out.print(s + ";" + ss + ";" +  a.tuples.size() + ";" + a.wstar + ";" + df.format(a.q) + "|");
		//calcul de ses tuples
		//Vector<Integer> tuples = new Vector<Integer>();
		/*Iterator<Tuple> it = a.tuples.iterator();
		while(it.hasNext()){
		    Tuple t = it.next();
		    FastRabbit.out.write(t.num+" ");
		    System.out.print(t.num+" ");
		    if(it.hasNext())
		        FastRabbit.out.write("-");
		}
		System.out.println();
		///////////////////
		*/
		if (j ==  g.elementAt(i).size() - 1 && i == g.size() - 1) FastRabbit.out.write("\t");
		else FastRabbit.out.write(":");
	    }
	}
	FastRabbit.out.write(qSum + "\t" + maxCC + "\t" + sumCC + //  qSum max and sum "
			     "\t" + nbVertices + "\t" + nbEdges + "\t" + (qSum / (double) nbEdges) + "\t" + pattern + "\n");
    }

    public void writeCC1(String pattern, BitSet pat, int indexOfThePatternInTheSky){
	// tableaux pour la densite
	Vector<Integer> cubeDimension = new Vector<Integer>();
	Vector<Integer> cubeNumIndex = new Vector<Integer>();
	int numIndex = 0;
	for(int i = 0; i < data.attributeTypes.size(); i++) {
	    if((data.attributeNames[i].equals("Long")) || (data.attributeNames[i].equals("Lat")) || (data.attributeNames[i].equals("Time"))) {
		cubeDimension.add(i);
		cubeNumIndex.add(numIndex);
	    }
	    if(data.attributeTypes.elementAt(i) == 2)
		numIndex = numIndex + 1;
	}
	Vector<Vector<Integer>> cube = new Vector<Vector<Integer>>();
	if(isPruned) return; // Should we keep that line? yes!!
	DecimalFormat df = new DecimalFormat("0.00");
	//FastRabbit.out.write("   \"CC\": {\n    \"Aretes\": [\n");
	FastRabbit.out.write(" {\n    \"Aretes\": [\n");
	for(int i = 0; i < g.size(); i++) {
	    Integer vertexIndex = data.findKeyVertex(vertexMap, i);
	    String vertexName1 = DataFR.findKey(data.vertexNames, vertexIndex);
	    for(int j = 0; j < g.elementAt(i).size(); j++) {
		Arete arete = g.elementAt(i).elementAt(j);
		String vertexName2 = DataFR.findKey(data.vertexNames, arete.v);
		//on a une arete
		FastRabbit.out.write("      {\n        \"In\": \"" + vertexName1 + "\",\n        \"Out\": \"" + vertexName2 +"\",\n        \"NbTuples\": "+
				     arete.tuples.size()+",\n        \"Poids\": "+arete.wstar+", \n        \"Q\": "+ arete.q+",\n");
		Iterator<Tuple> it = arete.tuples.iterator();
		FastRabbit.out.write("        \"Tuples\": [");
		while(it.hasNext()){
		    Tuple tuple = it.next();
		    FastRabbit.out.write(String.valueOf(tuple.num));
		    Vector<Integer>tupleProj = new Vector<Integer>();
		    for(int iter = 0; iter < cubeDimension.size(); iter++) {
			BitSet bs = data.extractAtt(tuple.t, cubeDimension.elementAt(iter));
			Integer val = data.getKey(cubeDimension.elementAt(iter),bs);
			String value = data.getKeyS(cubeDimension.elementAt(iter), val);
			int valueProj = Integer.parseInt(value);
			tupleProj.add(valueProj);
		    }
		    cube.add(tupleProj);
		    if(it.hasNext())
		        FastRabbit.out.write(",");
		}
		FastRabbit.out.write("]\n      }");
		if (j ==  g.elementAt(i).size() - 1 && i == g.size() - 1)
		    FastRabbit.out.write("\n");
		else
		    FastRabbit.out.write(",\n");
	    }
	}
	
	/*
	for(int i = 0; i < cube.size();i++) {
	    for(int j = 0; j < cube.elementAt(i).size(); j++)
		System.out.print(cube.elementAt(i).elementAt(j)+" ");
	    System.out.println();
	}
	System.out.println("------------------------------");
	*/

	//calcul densite
	double density = 0.0;
	double denom = 1.0;
	if(cubeDimension.size() > 0) {
	    for(int i = 0; i < cube.size();i++) { // tuple 1
		for(int j = i + 1; j < cube.size(); j++) { //tuple 2
		    for(int k = 0; k < cube.elementAt(i).size(); k++) {
			density = density + (cube.elementAt(i).elementAt(k) - cube.elementAt(j).elementAt(k)) * (cube.elementAt(i).elementAt(k) - cube.elementAt(j).elementAt(k));
		    }
		}
	    }
	    //normalisation
	    for(int i = 0; i < cubeDimension.size(); i++){
		BitSet tmp = data.extractAtt(pat, cubeDimension.elementAt(i));
		Integer first = new Integer((int) tmp.nextSetBit(0));
		int l = tmp.cardinality();
		Integer last = new Integer((int) first + l-2);
		
		int deb =  Integer.parseInt(data.findKey(data.modalityNames.get(cubeDimension.elementAt(i)), data.modalityNum.get(cubeNumIndex.elementAt(i)).get(first)));
		int fin =  Integer.parseInt(data.findKey(data.modalityNames.get(cubeDimension.elementAt(i)), data.modalityNum.get(cubeNumIndex.elementAt(i)).get(last)));
		//System.out.print("intervalle "+ deb+"--"+fin);
		denom = denom * (fin - deb + 1);
	    }
	}
	//System.out.println();
	FastRabbit.out.write("    ],\n    \"QSum\": "+qSum+",\n    \"MaxCC\": "+maxCC+",\n    \"SumCC\": "+sumCC+
			     ",\n    \"NbVertices\": "+ nbVertices+",\n    \"NbEdges\": "+nbEdges+",\n    \"QBar\": "+(qSum / (double) nbEdges)+",\n");
	FastRabbit.out.write("   \"Pattern\": \""+pattern+"\",\n");
	FastRabbit.out.write("   \"Density\": "+density/denom+"\n");
	FastRabbit.out.write(" }");//fin CC
    }
    
    public void printSCC(DataFR data) {
	Integer m;
	String s;
	for(int i = 0; i < vertexMap.size(); i++) {
	    m = data.findKeyVertex(vertexMap, i);
	    s = "";
	    if(m != null)
		s = data.findKey(data.vertexNames, m);
	    System.out.print("("+ s +","+scc.elementAt(i) + ") ");
	}
    }

    /*
     * find the arete (i,j) and create it if not found
     */
    public Arete findAreteInGStar(int i, int j) {
	Integer x =  vertexMap.get(i);
	for(int k = 0; k < g.elementAt(x).size(); k++) {
	    if(g.elementAt(x).elementAt(k).v == j) {
		return g.elementAt(x).elementAt(k);
	    }
	}
	return null;
    }
    
    /*
     * find the arete (i,j) and create it if not found
     */
    public Arete findArete(int i, int j, int w) {
	Integer x =  vertexMap.get(i);
	if(x == null) {
	    //new vertex
	    Vector<Arete> listeAdj = new Vector<Arete>();
	    g.add(listeAdj);
	    vertexMap.put(i, g.size() - 1);
	    Arete a = new Arete(i, j, w);
	    listeAdj.add(a);
	    nbVertices = nbVertices + 1;
	    nbEdges = nbEdges + 1;
	    return a;
       } 
	else { 
	    for(int k = 0; k < g.elementAt(x).size(); k++) {
		if(g.elementAt(x).elementAt(k).v == j) {
		    return g.elementAt(x).elementAt(k);
		}
	    }
	    Arete a = new Arete(i,j,0);
	    g.elementAt(x).add(a);
	    return a;
	}
    }
    /* add an edge*/
    public void addArete(Arete a) {
	//on met à jour nbEdges, maxCC, sumCC
	Boolean ajout = false;
	Integer x =  vertexMap.get(a.u);
	if(x == null) {
	    //new vertex
	    Vector<Arete> listeAdj = new Vector<Arete>();
	    g.add(listeAdj);
	    vertexMap.put(a.u, g.size() - 1);
	    listeAdj.add(a);
	    if(a.tuples.size() > this.maxCC)
		this.maxCC = a.tuples.size();
	    this.nbEdges = this.nbEdges + 1;
	    nbVertices = nbVertices + 1;
	    ajout = true;
	} 
	else { 
	    for(int k = 0; k < g.elementAt(x).size(); k++) {
		if(g.elementAt(x).elementAt(k).v == a.v) {
		    //on ajoute que les tuples
		    Arete y = g.elementAt(x).elementAt(k);
		    for(int l = 0; l < a.tuples.size(); l++)
			y.tuples.add(a.tuples.elementAt(l));
		    if(y.tuples.size() > this.maxCC)
			this.maxCC = y.tuples.size();
		    ajout = true;
		    break;
		}
	    }
	    if(ajout == false) {
		g.elementAt(x).add(a);
		this.nbEdges = this.nbEdges + 1;
		if(a.tuples.size() > this.maxCC)
		    this.maxCC = a.tuples.size();
	    }
	}
	this.sumCC = this.sumCC + a.tuples.size();
    }

    public int min(int a, int b) {
	int r = a;
	if(r > b)
	    r = b;
	return r;
    }
    
    public void dfs(int node, List<Integer> l, MutableInteger N, MutableInteger numSCC) {
	l.add(node);
	tps.set(node, N.get());
	low.set(node, tps.elementAt(node));
	N.increment();
	stackMember.set(node, 1);
	for(int j = 0; j < g.elementAt(node).size(); j++) {
	    Arete a = g.elementAt(node).elementAt(j);
	    ///////////////////if( (a.chi > 0)) {
	    Integer Madj = vertexMap.get(a.v);
	    if(Madj != null) {
		if (tps.elementAt(Madj) == -1)  { //a.v
		    dfs(Madj, l, N, numSCC);//a.v
		    low.set(node, min(low.elementAt(node), low.elementAt(Madj)));//[a.v]); 
		} else {
		    if (stackMember.elementAt(Madj) != -1) //a.v
			low.set(node, (min(low.elementAt(node), tps.elementAt(Madj))));//a.v 
		}
	    }
	    //////////}
	}
	if(low.elementAt(node) == tps.elementAt(node)) {
	    //we have a new SCC
	    while(l.isEmpty() == false) {
		int e = l.remove(l.size() - 1);
		scc.set(e, numSCC.get());
		stackMember.set(e, -1);
		if(e == node)
		    break;
	    }
	    numSCC.increment();
	}
    }
    
    public void computeSCC(DataFR data) {
	/*
	 * SCC: there is a path from x to y and another one from y to
	 * x thus vertices that have not outing edges are not in a SCC
	 * of size at least 2
	 */
	//initialisation
	numSCC.set(0);
	N.set(0);
	l.clear();

	scc.clear();
	stackMember.clear();
	low.clear();
	tps.clear();
	for(int i = 0; i < vertexMap.size(); i++) {
	    scc.add(-1);
	    stackMember.add(-1);
	    low.add(-1);
	    tps.add(-1);
	}
	for(int i = 0; i < vertexMap.size(); i++) {
	    if(scc.elementAt(i) == -1) {
		dfs(i, l, N, numSCC);
	    }
	}
    }
    /*
     * generate the SCC and compute nbvertices, nbedges and weight but
     * not qSum because we need all the SCC to do it => we do it in
     * reduceGraph and CheckAllConstraints
     * we compute allCCs.max, allCCs.sum
     */
    public void generateAllSCC(Enumeration e, Vector<CC> allCCs, CC poubelle) {
	this.computeSCC(e.data);
	//we add the necessary number of scc
	int deb = allCCs.size();
	//System.out.println("ALORS "+deb);
	for(int i = 0; i < numSCC.get(); i++) {
	    CC gg = new CC(e.data);
	    allCCs.add(gg);
	}
	//we fill in the scc
	for(int i = 0; i < vertexMap.size(); i++) {
	    //pour tous les vertices, on les ajoute à la bonne SCC
	    if(scc.elementAt(i) != -1) {
		CC gg = allCCs.elementAt(scc.elementAt(i) + deb);
		Vector<Arete> listeAdj = new Vector<Arete>();
		gg.g.add(listeAdj);
		Integer n = data.findKeyVertex(vertexMap, i);
		gg.vertexMap.put(n, gg.g.size() - 1);
		gg.nbVertices = gg.nbVertices + 1;
	    }
	    else {
		//on les rajoute éventuellement à la poubelle, si pas déjà dedans
		Integer x =  poubelle.vertexMap.get(i);
		if(x == null) {
		    Vector<Arete> listeAdj = new Vector<Arete>();
		    poubelle.g.add(listeAdj);
		    Integer n = data.findKeyVertex(vertexMap, i);
		    poubelle.vertexMap.put(n, poubelle.g.size() - 1);
		    poubelle.nbVertices = poubelle.nbVertices + 1;
		}
	    }
	}
	//on parcourt les aretes de this.g et on les met dans la bonne cc
	//also compute the max and sum of allCCs as well as maxStar and sumStar
	for(int i = 0; i < this.g.size(); i++) {
	    for(int k = 0; k < this.g.elementAt(i).size(); k++) {
		Arete a = g.elementAt(i).elementAt(k);
		Arete b = a.copy();
		// find u and v
		Integer Mu = vertexMap.get(b.u);
		Integer Mv = vertexMap.get(b.v);
		if((Mv != null) && (scc.elementAt(Mu) == scc.elementAt(Mv)) && (scc.elementAt(Mu) != -1)) {
		    CC gg = allCCs.elementAt(scc.elementAt(Mu) + deb);
		    gg.addArete(b);
		}
		else {
		    poubelle.addArete(b);		    
		}
	    }
	}
	// a voir si plus efficace!!!!
	//on enleve les vides (sans aretes)
	for(int i = deb; i < allCCs.size(); ) {
	    if(allCCs.elementAt(i).nbEdges == 0)
		allCCs.remove(allCCs.elementAt(i));
	    //allCCs.elementAt(i).printCC(data);
	    else
	    	i++;
	}
    }    
    
    // public double uperboundCh2(double wc, double sumWc, double wt, double sumWt) {
    // 	double r = (sumWt * wc-wt* wc) * (sumWt*wc-wt* wc)*sumWt / (wt*(sumWt-wt)*(wc*(sumWt - wc)));
    // 	return r;
    // }

    // public double uperboundQ(double wc, double wt, double maxT) {
    // 	double r = (wc/maxT) * (1 - wt/maxT);
    //  	return r;
    // }


    public void checkAMConstraints(Enumeration e, Check c, CC poubelle, Graph gr, double sumStar, double maxT) {
	for(int i = 0; i < this.g.size(); i++) {
	    for(int j = 0; j < this.g.elementAt(i).size(); ) {
		Arete a = this.g.elementAt(i).elementAt(j);
		int weight = a.tuples.size();
		// double ub = 0.0;
		// if(sumStar == a.wstar)
		//     ub = c.seuilChi + 1;
		// else {
		//     ub = uperboundCh2(weight, gr.sumG, a.wstar, sumStar);
		//     System.out.println("alors "+ub+ " "+weight+" "+ a.wstar+" "+ sumStar);
		// }
		//double ubq = c.seuilQ + 1;
		//if((maxT > 0) && (!e.debugUBQ))
		//    ubq = uperboundQ(weight, a.wstar, maxT);
		if( (weight < c.Theta) 
		    // ||(ub < c.seuilChi) 
		    // || (ubq < c.seuilQ)
		    ) 
		    { // add here the check of Chi_ub if we are able to compute here the current data we have...
		    //System.out.println("ici Theta");
		    poubelle.addArete(a);//, gr);
		    //this.sumCC = this.sumCC - a.tuples.size();
		    this.g.elementAt(i).remove(a);
		    this.nbEdges = this.nbEdges - 1;
		    //if(ubq < c.seuilQ)
		    //	e.nbAretePrunedByUBChi++;
		}
		else
		    j++;
		
	    }
	}

    }
    
    /* on est sur une feuille*/
    public void checkEdgeConstraints(Enumeration e, Check c, CC poubelle, int maxG, int sumG, int first) {

	//attention, être bien sur qu'il existe
	Graph graphStar = e.allGraphs.elementAt(first);
	//System.out.println("graphStar.maxG "+ graphStar.maxG);

	CC gs = graphStar.cc.elementAt(1);
	this.qSum = 0;
	for(int i = 0; i < this.g.size(); i++) {
	    for(int j = 0; j < this.g.elementAt(i).size(); ) {
		Arete a = this.g.elementAt(i).elementAt(j);
		Arete estar = gs.findAreteInGStar(a.u, a.v);
		double chisqr = c.chisquare((double) a.tuples.size(), (double) sumG, (double) a.wstar, (double) graphStar.sumG);
		//System.out.println(a.tuples.size()+" "+ maxG+" "+ estar.tuples.size()+" " +graphStar.maxG);
		double qvalue = c.qvalue((double) a.tuples.size(), (double) maxG, (double) estar.tuples.size(), (double) graphStar.maxG);
		if((chisqr < c.seuilChi) || (qvalue < c.seuilQ)){
		    //on le deplace => pas besoin on est sur une feuille
		    //si parce qu'il faut qu'on recalcule tout... au moins pour le debug ;-)
		    poubelle.addArete(a);//, gr);
		    this.g.elementAt(i).remove(a);
		    this.nbEdges = this.nbEdges - 1;
		} else {
		    a.chi = chisqr;
		    a.q = qvalue;
		    j++;
		    this.qSum = this.qSum + qvalue;
		}
	    }
	}
    }

   
}

// Local Variables:
// coding: utf-8
// End:
