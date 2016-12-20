
import java.io.*;
import java.util.*;
import java.text.*;

public class Graph { 
    Vector<CC> cc;
    int sumG;
    int maxG;
    Vector<CC> ccs;
    int nbCCValides;
    
    public Graph(){
	this.cc = new Vector<CC>();
	this.sumG = 0;
	this.maxG = 0;
	this.ccs = new Vector<CC>();
	this.nbCCValides = 0;
    }

    public static Graph gStar(BitSet pattern, int expectedOnes, Check c, Enumeration e) {
	BitSet tmp;
	BitSet tmp1;
	//a single graph is generated from the data
	//with the correct set of tuples associated to each edge
	CC graph = new CC(e.data);
	// (vert)
	Iterator<Tuple> it = e.data.tuples.iterator();
	while(it.hasNext()){
	    Tuple tuple = it.next();
	    BitSet elem = tuple.t;
	    tmp = (BitSet) pattern.clone();
	    tmp.and(elem);
	    if(tmp.cardinality() == expectedOnes){
		// (vert)
		//int u = e.data.extractVertex(elem, 0); 
		//int v = e.data.extractVertex(elem, 1);
		int u = tuple.v1;
		int v = tuple.v2;
		Arete a = graph.findArete(u, v, 0);
		a.tuples.add(tuple);
		graph.sumCC = graph.sumCC + 1;
		a.wstar = a.wstar + 1;
	    }
	}
	Graph aSingleGraph = new Graph();
	CC poubelle = new CC(e.data);
	//poubelle.isNotACC = true;
	poubelle.isPruned = true;
	aSingleGraph.cc.add(poubelle);
	aSingleGraph.cc.add(graph);
	//remove edges with Theta and chi_ub in all the potential ccs
	graph.checkAMConstraints(e, c, poubelle, aSingleGraph, graph.sumCC, graph.maxCC);
	//compute maxG and sumG
	for(int i = 0; i < graph.g.size(); i++) {
	    for(int j = 0; j < graph.g.elementAt(i).size(); j++) {
		Arete arete =  graph.g.elementAt(i).elementAt(j);
		int weight = arete.tuples.size();
		arete.wstar = weight;
		aSingleGraph.sumG = aSingleGraph.sumG + weight;
		if(weight > aSingleGraph.maxG)
		    aSingleGraph.maxG = weight;
	    }
	}
	graph.sumCC = aSingleGraph.sumG;
	graph.maxCC = aSingleGraph.maxG;
	aSingleGraph.nbCCValides = 0;
	return aSingleGraph;
    }

    public void reduceGraph(int depth, BitSet pattern, int expectedOnes, Check c, Enumeration e, int first) {
	BitSet tmp;
	BitSet tmp1;
	if((depth < e.allGraphs.size())) {
	    //the graph is generated from the all the CCs of the graph at depth - 1
	    Graph previousGraphs = e.allGraphs.elementAt(depth - 1);
	    //make previousGraphs and graphs of same size
	    while(this.cc.size() < previousGraphs.cc.size()) {
		cc.add(new CC(e.data));
	    }
	    while(cc.size() > previousGraphs.cc.size()) {
		this.cc.remove(this.cc.size() - 1);
	    }
	    //travel all the edges of all the cc and update the tuples wrt pattern
	    this.nbCCValides = 0;
	    this.sumG = 0;
	    for(int k = 0; k < previousGraphs.cc.size(); k++) {
		CC gPreced = previousGraphs.cc.elementAt(k);
		CC graph = this.cc.elementAt(k);
		graph.sumCC = 0;
		graph.g.clear();
		graph.vertexMap.clear();   
		//flags
		graph.isPruned = gPreced.isPruned;
		if(!graph.isPruned)
		    this.nbCCValides = this.nbCCValides + 1;
		for(int i = 0; i < gPreced.g.size(); i++) {
		    for(int j = 0; j < gPreced.g.elementAt(i).size(); j++) {
			// (vert)
			Iterator<Tuple> it = gPreced.g.elementAt(i).elementAt(j).tuples.iterator();
			while(it.hasNext()){
			    Tuple tuple = it.next();
			    BitSet elem = tuple.t;
			    tmp = (BitSet) pattern.clone();
			    tmp1 = (BitSet) elem.clone();
			    tmp1.and(e.data.masques.elementAt(depth));
			    tmp.and(tmp1);
			    if(tmp.cardinality() == expectedOnes){
				int u = tuple.v1;
				int v = tuple.v2;
				Arete a = graph.findArete(u, v, 0);
				a.tuples.add(tuple);
				a.wstar = gPreced.g.elementAt(i).elementAt(j).wstar;
				graph.sumCC = graph.sumCC + 1;
			    } 
			}
		    } 
		}
		this.sumG = this.sumG + graph.sumCC;
	    }
	}
	//System.out.println("bon");
	//printGraph(e,true);
	//Here we have only the sumCC
	//AM constraints and computation of CC's
	
	this.ccs.clear();
	for(int k = 0; k < this.cc.size(); k++) {
	    CC graph = this.cc.elementAt(k);
	    if(graph.isPruned == false) {
		//remove edges with Theta and chi_ub in all the potential ccs
		graph.checkAMConstraints(e, c, this.cc.elementAt(0), this, e.allGraphs.elementAt(first).sumG, e.allGraphs.elementAt(first).maxG);
		graph.generateAllSCC(e, ccs, this.cc.elementAt(0));
	    }
	    else
		this.ccs.add(this.cc.elementAt(k));
	}
	//System.out.println("trtr nb scc = "+ccs.cc.size());
	//here we have CC.nbvertices, CC.nbEdges and CC.sumCC on
	//remplace les CC's et on calcule qsum, and also this.max and
	//this.sum, this.maxStar and this.sumStar

	//Graph gStar= e.allGraphs.elementAt(0);
	//CC ccStar = gStar.cc.elementAt(1);
	this.cc.clear();
	this.nbCCValides = 0;
	for(int i = 0; i < ccs.size(); i++) {
	    CC gcc = ccs.elementAt(i);
	    this.cc.add(gcc);
	    if(!gcc.isPruned)
		this.nbCCValides = this.nbCCValides + 1;
	}
	computeConstantes();
    }
    
    public void computeConstantes() {
	//calcul constantes
	maxG = 0;
	sumG = 0;
	for(int k = 0; k < this.cc.size(); k++) {
	    CC xc = this.cc.elementAt(k);
	    xc.nbEdges = 0;
	    xc.nbVertices = xc.vertexMap.size();
	    xc.qSum = 0.0;
	    xc.maxCC = 0;
	    xc.sumCC = 0;
	    for(int i = 0; i < xc.g.size(); i++) {
		for(int j = 0; j < xc.g.elementAt(i).size();j++) {
		    xc.nbEdges = xc.nbEdges + 1;
		    int w = xc.g.elementAt(i).elementAt(j).tuples.size();
		    xc.sumCC = xc.sumCC + w;
		    if(xc.maxCC < w)
			xc.maxCC = w;
		    xc.qSum = xc.qSum + xc.g.elementAt(i).elementAt(j).q;
		}
	    }
	    sumG = sumG + xc.sumCC;
	    if(maxG < xc.maxCC)
		maxG= xc.maxCC;
	}
    }
    /*    
    public void computeConstantesDebug(Enumeration e, Check c) {
	//calcul constantes
	maxG = 0;
	sumG = 0;
	for(int k = 0; k < this.cc.size(); k++) {
	    CC xc = this.cc.elementAt(k);
	    xc.nbEdges = 0;
	    xc.qSum = 0.0;
	    xc.maxCC = 0;
	    xc.sumCC = 0;
	    for(int i = 0; i < xc.g.size(); i++) {
		for(int j = 0; j < xc.g.elementAt(i).size();j++) {
		    xc.nbEdges = xc.nbEdges + 1;
		    int w = xc.g.elementAt(i).elementAt(j).tuples.size();
		    xc.sumCC = xc.sumCC + w;
		    if(xc.maxCC < w)
			xc.maxCC = w;
		    xc.qSum = xc.qSum + xc.g.elementAt(i).elementAt(j).q;
		}
	    }
	    sumG = sumG + xc.sumCC;
	    if(maxG < xc.maxCC)
		maxG = xc.maxCC;
	}
	//
	Graph graphStar = e.allGraphs.elementAt(0);
	CC gs = graphStar.cc.elementAt(1);
	for(int k = 0; k < this.cc.size(); k++) {
	    CC xc = this.cc.elementAt(k);
	    xc.qSum = 0.0;
	    xc.nbVertices = xc.vertexMap.size();
	    for(int i = 0; i < xc.g.size(); i++) {
		for(int j = 0; j < xc.g.elementAt(i).size();j++) {
		    Arete a = xc.g.elementAt(i).elementAt(j);
		    Arete estar = gs.findAreteInGStar(a.u, a.v);
		    //System.out.println(a.tuples.size()+" " + maxG + " "+ estar.tuples.size()+".0 "+ graphStar.maxG);
		    a.chi = c.chisquare((double) a.tuples.size(), (double) sumG, (double) a.wstar, (double) graphStar.sumG);
		    a.q = c.qvalue((double) a.tuples.size(), (double) maxG, (double) estar.tuples.size(), (double) graphStar.maxG);
		    if(a.q > c.seuilQ)
			xc.qSum = xc.qSum + a.q;
		}
	    }
	}
     }
    */
    public int checkAllConstraints(BitSet pattern, int expectedOnes, Enumeration e, Check c, int first) {
	//System.out.println("check all constraints");
	//printGraph(e);
	//Check constraints and computation of CC's
	//for the moment chi^2
	computeConstantes();
	ccs.clear();
	//this.printGraph(e, true);	
	CC poubelle = this.cc.elementAt(0);
	for(int k = 0; k < this.cc.size(); k++) {
	    CC graph = this.cc.elementAt(k);
	    int closed = 1;
	    if(graph.isPruned == false) {
		//remove edges with chi and q	
		graph.checkEdgeConstraints(e, c, poubelle, this.maxG, this.sumG, first);
		closed = c.computeClosure(pattern, expectedOnes, e, this, graph);
		if((closed == 0)) {
		    if(!graph.isPruned)
			graph.generateAllSCC(e, ccs, poubelle);
		    else {
			ccs.add(this.cc.elementAt(k));
		    }
		}
		else {
		    //on prune tout
		    graph.isPruned = true;
		    
		}
	    }
	    else
		ccs.add(this.cc.elementAt(k));
	}
	//on remplace les CC's
	this.cc.clear();
	this.nbCCValides = 0;
	for(int i = 0; i < ccs.size(); i++) {
	    CC gcc = ccs.elementAt(i);
	    this.cc.add(gcc);
	    if(!gcc.isPruned)
		this.nbCCValides = this.nbCCValides + 1;
	}
	computeConstantes();
	//printGraph(e,true);
	//on teste les contraintes maintenant qu'on a calculé les quantités
	int r = 0;
	for(int i = 0; i < this.cc.size();i++ ) {
	    CC gcc = this.cc.elementAt(i);
	    if(!gcc.isPruned) {
		if (c.threshold(gcc, e.data) == false) {
		    //System.out.print("rrr!!\n");
		    gcc.isPruned = true;
		}
		else
		    r = 1;
	    }
	}
	//System.out.print("gg\n");
	//printGraph(e,true);
	return r;
    }


    public void printGraph(Enumeration e, boolean b) {
	//if(sum.get() > 0) {
	//System.out.println("Print Graph "); //+cc.size()+ " "+sum.get());
	for(int k = 0; k < cc.size(); k++) {
	    CC s = cc.elementAt(k);
	    if((!s.isPruned) || b) {
		s.printCC(e.data);
		double sqBarre = s.qSum / (double) s.nbEdges;
		System.out.println("(" + e.format.format(s.qSum) + ")" + "(" + e.format.format(sqBarre) + ") (" + s.nbEdges + ") (" + s.nbVertices + ")/ ");
		//System.out.println(" || "+s.qSum + " "+s.weight+" "+s.nbEdges+" "+s.nbVertices);
		System.out.println("---------------------");
	    }
	}
	System.out.println(" sumG "+sumG+" maxG "+maxG);
    }


    public void verif(String s){
	/*
	System.out.println(s);
	for(int k = 0; k < this.cc.size(); k++) {
	    CC c = this.cc.elementAt(k);
	    if(!c.isPruned)
		System.out.println("est valide "+ k + " " +c.nbVertices+" = "+c.vertexMap.size());
	    else
		System.out.println("poubelle " + k);
	    c.printCC(c.data);
	    int nbe = 0;
	    double qs = 0.0;
	    int max = 0;
	    int sum = 0;
	    for(int i = 0; i < c.g.size(); i++) {
		for(int j = 0; j < c.g.elementAt(i).size();j++) {
		    nbe++;
		    int w = c.g.elementAt(i).elementAt(j).tuples.size();
		    sum = sum + w;
		    if(max < w)
			max = w;
		    qs = qs + c.g.elementAt(i).elementAt(j).q;
		}
	    }
	    System.out.println("edge "+c.nbEdges+" = "+nbe+ " squm "+ c.qSum+" = "+qs+" sumCC "+sum+" = "+c.sumCC+" maxCC "+max +" = "+c.maxCC);
	}
	System.out.println("sumG "+sumG+" maxG "+maxG);
	*/
    }
}

// Local Variables:
// coding: utf-8
// End:
