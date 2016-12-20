import java.io.*;
import java.util.*;
import java.text.*;

public class Enumeration {
    DataFR data;
    Vector<Graph> allGraphs;
    Vector<CC> skyline;
    Vector<BitSet> skylineMotif;
    Vector<BitSet> patterns;
    Vector<MutableInteger> intAs;
    Vector<MutableInteger> intBs;
    Vector<Arete> aretes;
    Vector<Arete> aretesStar;

    Vector<Vector<BitSet>> stacks;
    Vector<Vector<Boolean>> stackBs;
    NumberFormat format;
    int nbEnum;
    int nbPatt;
    int nbPruned;
    //    int nbAretePrunedByUBChi;
    int nbRec = 0;
    //int nbClosed;

    BitSet tmp;
    Boolean debugPrunAlb;
    Boolean debugPrunClos;
    Boolean debugUBQ;

    static int pattId=0;
    static List<Integer> truePatterns = new ArrayList<>();
    
    
    
    public double pruningUBQSumOverEC(Graph gr, CC ccStar, Check c, int numCC, int first) {
	//create vector of aretes 
	//and compute alpha and gamma
	double alpha = 0.0;
	double gamma = 0.0;
	Graph graphStar = allGraphs.elementAt(first);
	aretes.clear();
	aretesStar.clear();
	double s2 = 0.0;
	double t2 = 0.0;
	// we compute alpha, gamma and the vector of edges that will
	// be sorted, compute also s2
	CC cc = gr.cc.elementAt(numCC);
	cc.nbEdges = 0;
	for(int i = 0; i < cc.g.size(); i++) {
	    for(int j = 0; j < cc.g.elementAt(i).size(); j++) {
		Arete ar = cc.g.elementAt(i).elementAt(j);
		Arete estar = ccStar.findAreteInGStar(ar.u, ar.v);
		//if(estar.tuples.size() != ar.wstar)
		//    System.out.println("PROBLEME");
		//  alpha = estar.tuples.size();
		//à voir avec a.wstar??
		//gamma = gamma + estar.tuples.size();

		t2 = t2 + estar.tuples.size();
		//A transformer en vecteur d'entiers
		aretes.add(ar);
		aretesStar.add(estar);
		s2 = s2 + ar.tuples.size();
		cc.nbEdges = cc.nbEdges + 1;
	    }
	}
	alpha = graphStar.maxG;
	double beta = gr.maxG;
	//gamma = graphStar.sumG;
	if(aretes.size() > 0) {
	    //sort aretes
	    Collections.sort(aretes, new vectComp());
	    Collections.sort(aretesStar, new vectComp());
	    /*
	      for(int i = 0; i < aretes.size(); i++) {
	      System.out.print(aretes.elementAt(i).w+ " ");
	      }
	      System.out.println(" fin sort ");
	    */
	    //compute s1 and s2
	    double s1 = (double) aretes.elementAt(0).tuples.size();
	    s2 = s2 - s1;
	    double ubp = 0.0;
	    double ub = (beta / alpha) * ((s1 + s2)/ beta - gamma);
	    //System.out.println(s1 + " "+ s2 +" ub "+ ubp + " " + ub);
	    int j = 1;
	    int k = aretes.size();
	    int t1 = 0;
	    for(int l = 0; l < k; l++){
		t1 = (l + 1) * aretesStar.elementAt(l).tuples.size();
		t2 = t2 - aretesStar.elementAt(l).tuples.size();
	    }
	    gamma = (t1 + t2) / alpha;
	    if(j < k) {
		do {
		    //System.out.print(j+" "+k+ " ");//+ ub+ " "+ubp);
		    s1 = (j + 1) * aretes.elementAt(j).tuples.size();
		    s2 = s2 - aretes.elementAt(j).tuples.size();
		    ubp = ub;
		    ub = (beta / alpha) * ((s1 + s2)/beta - gamma);
		    j = j + 1;
		    //System.out.println("ub "+ ubp + " " + ub);
		} while((ub >= ubp) && (j < k));
	    }
	    if(ubp < ub){
		return ub;
	    }
	    return ubp;
	}
	return c.mqs + 1;
    }


    
    public int estPlusPetitQue(BitSet a, BitSet b, int depth) {
	//1 si ppq, 0 si incomparable, -1 si pgq
	for(int i = 0; i < depth; i++) {
	    BitSet at = data.extractAtt(a, i);
	    BitSet bt = data.extractAtt(b, i);
	    if(at.equals(bt) == false)
		return 0;
	}
	BitSet at = data.extractAtt(a, depth);
	BitSet bt = data.extractAtt(b, depth);
	int v = data.compareValue(bt, at, depth);
	return v;
    }
    
    public int toDo(BitSet pattern, int depth, int expectedOnes, Check c, Boolean right, int first) {
	/*
	 * compute the graph with the transactions associated to the
	 * edges, remove the edges with weight below theta and that do
	 * not satify the X^2 upper_bound and built the SCC's 
	 * return 1 si pas pruning clos (enumeration standard), 0 si
	 * changement des bornes de l'intervalle et 2 si fin
	 * enumeration
	 */
	//System.out.println("Todo : le motif "+depth);
	//data.printPattern(pattern);
	Graph g = allGraphs.elementAt(depth + 1);
	MutableInteger intA = intAs.elementAt(depth + 1);
	MutableInteger intB = intBs.elementAt(depth + 1);
	g.reduceGraph(depth + 1, pattern, expectedOnes, c, this, first);
	//System.out.println("avant prune : "+g.nbCCValides+" "+first+" "+expectedOnes+" "+pattern.toString());
	//g.printGraph(this, true);
	if(g.nbCCValides == 0)
	    return 2;
	CC gstar = allGraphs.elementAt(first).cc.elementAt(1);
	Graph graphStar = allGraphs.elementAt(first);
	for(int k = 0; k < g.cc.size(); k++) {
	    CC cc = g.cc.elementAt(k);
	    if(!cc.isPruned) {
		//test Albrecht
		double qs = pruningUBQSumOverEC(g, gstar, c, k, first);
		if(debugPrunAlb){
		    qs = c.mqs + 1;
		}
		//System.out.println("Albrecht "+qs+ " "+c.mqs);
		if(qs < c.mqs) {
		    //nbPruned = nbPruned + 1;
		    g.cc.elementAt(k).isPruned = true;
		    g.nbCCValides = g.nbCCValides - 1;
		}
	    }
	}
	//System.out.println("apres prune");
	//g.computeConstantesDebug(this, c) ;
	//g.printGraph(this, true);
	//test fermeture
	//System.out.println("pattern");
	//data.printPattern(pattern);
	//g.printGraph(this, true);
	//System.out.println("gloups "+g.nbCCValides);
	int closed = c.computeClosure(pattern, expectedOnes, this, g, null);
	//System.out.println("ferme");
	//data.printPattern(c.closure);
	//pour la verif à enlever
	/*int ok = 0;
	for(int k = 0; k < g.cc.size(); k++) {
	    CC cc = g.cc.elementAt(k);
	    if(!cc.isPruned) {
		ok = 1;
	    }
	}
	*/
	//System.out.println("gloups1 "+g.nbCCValides);
	if(g.nbCCValides == 0) {
	    nbPruned = nbPruned + 1;
	    // System.out.println("pruned "+ g.nbCCValides);
	    //g.computeConstantesDebug(this, c) ;
	    return 2;
	}
	//le suivant à énumerer
	BitSet pb = data.extractAtt(pattern, depth);
	int j = pb.nextClearBit(pb.nextSetBit(0)) - 1;
	BitSet cl = c.closure;

	//si numérique, on met à jour les bornes de l'intervalle
	if(data.attributeTypes.elementAt(depth) != 1) {
	    //attribut numerique: on réduit l'intervalle
	    //on recopie le clos dans le pattern
	    BitSet b = data.extractAtt(cl, depth);
	    int anto = b.nextSetBit(0);
	    int gasp = b.nextClearBit(anto);
	    //System.out.println("inter "+b.toString()+ " "+anto + " "+gasp);
	    if(debugPrunClos == false) {
		//if((depth - 1 > 0) && (data.estEgal(pattern, cl, 0, (depth) * data.nbBits))) {
		if((depth - 1 > 0) && (data.estEgal(pattern, cl, 0, data.nbBitsOpt.elementAt(depth)))) {
		    //System.out.println("ici");
		    if((j == gasp - 1) || (right == false)) {
			//data.printPattern(cl);
			data.copySubPart(pattern, cl, 0, cl.length());
			intA.set(anto);
			intB.set(gasp - 1);
			return 0;      
		    }
		}
		else
		    if(depth-1 < 0) { //<> =!!!
			//je comprends pas pourquoi
			if((j == gasp - 1) || (right == false)) {
			    data.copySubPart(pattern, cl, 0, cl.length());
			    intA.set(anto);
			    intB.set(gasp - 1);
			    return 0;
			}
		    }
		//if ((depth - 1 > 0) && (data.estEgal(pattern, cl, 0, (depth) * data.nbBits) == false)) {
		if ((depth - 1 > 0) && (data.estEgal(pattern, cl, 0, data.nbBitsOpt.elementAt(depth)) == false)) {
		    //on stoppe l'énumeration
		    return 2;
		}
	    }
	}
	//si symbolique, on fait rien puisque il faudra quand même voir les autres valeurs
	//on peut modifier un attribut avant depth, pas bon dans les appels rec
	//donc à ne pas faire
	/**/
	else {
	    //symbolique on va quand même essayer de faire qqch ;-)
	    if(debugPrunClos == false) {
		//on modifie pattern de depth à la fin
		if (depth - 1 > 0) {
		    //data.printPattern(pattern);
		    //data.copySubPart(pattern, cl, data.nbBitsOpt.elementAt(depth), pattern.length());
		    //data.printPattern(cl);
		    //data.printPattern(pattern);
		    //System.out.println(" -- "+depth);
		    //if (//(depth - 1 > 0) &&
		    //    (data.estEgal(pattern, cl, (depth+1) * data.nbBits, cl.length()) == false)) {
		    //    System.out.println("Gloups!!");
		    //on stoppe l'énumeration
		    //return 2;
		}
	    }
	}
	/* */
	return 1;
    }
    
    public void enumerate(int depth, int offset, int idNum, int expectedOnes, Check c, int first, List<Integer> _parentIds, boolean spark, int deb){
	//System.out.println(offset);
	BitSet pattern = patterns.elementAt(depth);
	

	nbRec++;
	//if(nbRec % 100000 == 0)
	//System.out.println(nbEnum);
	//System.out.println("debut enumerate ? " + depth+" "+offset+" "+idNum+" "+expectedOnes);
	//data.printPattern(pattern);
	//allGraphs.elementAt(depth).printGraph(this,true);

	if(depth == data.nbAttributes) {
	    nbEnum = nbEnum + 1;
	    /*
	     * we got a potential pattern
	     * we have to checks the constraints
	     */
	    Graph gr = allGraphs.elementAt(depth);
	    //System.out.println("avant " + gr.sumG + " "+gr.maxG);
	    //data.printPattern(pattern);
	    //gr.printGraph(this, true);
	    //System.out.println("check ");
	    int r = gr.checkAllConstraints(pattern, expectedOnes, this, c, first);
	    
	    
	    //gr.printGraph(this, true);
	    if(r > 0) {
		nbPatt = nbPatt + 1;
		//allGraphs.elementAt(depth).printGraph(this, true);
		//gr.computeConstantesDebug(this, c) ;
		
		/******/
		pattId++;
		truePatterns.add(pattId);
		List<Integer> parentIds = new ArrayList<Integer>();
		parentIds.addAll(_parentIds);
		parentIds.add(pattId);
		/******/
		
		for(int i = 0; i < gr.cc.size(); i++) {
		    if((!gr.cc.elementAt(i).isPruned) && (c.threshold(gr.cc.elementAt(i), data)))
			c.computeSkyline(gr.cc.elementAt(i), skyline, pattern, skylineMotif, parentIds);
		}
		//print Pattern
		//System.out.println("pattern "+nbPatt);
		//data.printPattern(pattern);
		//allGraphs.elementAt(depth).printGraph(this, false);
	    }
	}	
	else {
	    //data.printPattern(pattern); we still have to enumerate
	    // the next attribute "depth"
	    if(depth < data.nbAttributes) {
		//Symbolic attribute
		if(data.attributeTypes.elementAt(depth) == 1) {
		    BitSet bb = data.extractAtt(pattern, depth);
		    //System.out.println(pattern.toString() + "\n bb: " + bb.toString()+ " "+depth);
		    if(bb.cardinality() == 1) {
			//it has not been closed
			//it is still a star
			// For all modalities
			Iterator it = data.modalities.elementAt(depth).entrySet().iterator();
			while (it.hasNext()) {
			    Map.Entry v = (Map.Entry)it.next();
			    //for spark, do not enumerate * for the current attribute
			    if((spark) && (v.getKey().equals(0)) && (depth == deb))
			    	v = (Map.Entry)it.next();
			    BitSet b = (BitSet) v.getValue();
			    BitSet p = patterns.elementAt(depth + 1);
			    data.copyBS(p, pattern, 0);
			    //System.out.println(p.toString());
			    //System.out.println(b.toString()+ " "+offset);
			    data.copyBS(p, b, offset);
			    //System.out.println(p.toString());
			    if(!v.getKey().equals(0))
				expectedOnes = expectedOnes + 1;
			    intAs.elementAt(depth+1).set(0);
			    intBs.elementAt(depth+1).set(0);
			    int r = toDo(p, depth, expectedOnes+1, c, false, first) ;
			    //System.out.println(p.toString()+" "+ r);
			    if(r != 2) {
				if(allGraphs.elementAt(depth + 1).nbCCValides > 0) 
				    //enumerate(depth + 1, offset + data.nbBits, idNum, expectedOnes + 1, c);
					/******/
					pattId++;
					List<Integer> parentIds = new ArrayList<Integer>();
					parentIds.addAll(_parentIds);
					parentIds.add(pattId);
					/******/
					enumerate(depth + 1, data.nbBitsOpt.elementAt(depth + 1), idNum, expectedOnes + 1, c, first,parentIds, spark, deb);
			    }
			    if(!v.getKey().equals(0))
				expectedOnes = expectedOnes - 1;
			}
			//data.starAttr(pattern, depth, depth * data.nbBits);
			data.starAttr(pattern, depth, data.nbBitsOpt.elementAt(depth));
		    }
		    else
			{
			    //System.out.println("fermé symbolique");
			    BitSet p = patterns.elementAt(depth + 1);
			    data.copyBS(p, pattern, 0);
			    expectedOnes = expectedOnes + 1;
			    intAs.elementAt(depth+1).set(0);
			    intBs.elementAt(depth+1).set(0);
			    int r = toDo(pattern, depth, expectedOnes + 1, c, false, first);
			    if(r != 2 ) {
				if(allGraphs.elementAt(depth + 1).nbCCValides > 0) 
				    //enumerate(depth + 1, offset + data.nbBits, idNum, expectedOnes + 1, c);
					/******/
					pattId++;
					List<Integer> parentIds = new ArrayList<Integer>();
					parentIds.addAll(_parentIds);
					parentIds.add(pattId);
					/******/
					enumerate(depth + 1, data.nbBitsOpt.elementAt(depth + 1), idNum, expectedOnes + 1, c, first, parentIds, spark, deb);
			    }
			    expectedOnes = expectedOnes - 1;
			    //data.starAttr(pattern, depth, depth * data.nbBits);
			    data.starAttr(pattern, depth, data.nbBitsOpt.elementAt(depth));
			}
		}
		//Numerical attribute
		else {
		    //For all intervals of modalities
		    int r = -1;
		    boolean modif = false;		   
		    Vector<BitSet> stack = stacks.elementAt(depth);
		    Vector<Boolean> stackB = stackBs.elementAt(depth);
		    stack.clear();
		    stackB.clear();
		    //modif!!
		    BitSet bbs = data.extractAtt(pattern, depth);
		    int i = bbs.nextSetBit(0);
		    int j = bbs.nextClearBit(i) - 1;
		    //System.out.println(i+" "+j);
		    //int i = 0;
		    //int j = data.modalityNum.elementAt(idNum).size() - 1;
		    int l = data.nbBitsOpt.elementAt(depth + 1) - data.nbBitsOpt.elementAt(depth);
		    BitSet bs = data.interval2BitSet(l, i, j);
		    stack.add(bs);
		    Boolean right = false;
		    stackB.add(right);
		    //data.printPattern(pattern);
		    while(stack.size() > 0) {
			//on récupère le dernier intervalle
			bs = stack.elementAt(stack.size() - 1);
			stack.remove(stack.elementAt(stack.size() - 1));

			right = stackB.elementAt(stackB.size() - 1);
			stackB.remove(stackB.elementAt(stackB.size() - 1));

			//on le traite
			BitSet p = patterns.elementAt(depth + 1);
			data.copyBS(p, pattern, 0);
			data.copyBS(p, bs, offset);
			BitSet bb = data.extractAtt(pattern, depth);
			intAs.elementAt(depth+1).set(bb.nextSetBit(0));
			intBs.elementAt(depth+1).set(bb.nextClearBit(intAs.elementAt(depth+1).get()) - 1);
			//System.out.println(p.toString());
			r = toDo(p, depth, expectedOnes + 2, c, right, first);
			//System.out.println(r);
			if( (r != 2) && (allGraphs.elementAt(depth + 1).nbCCValides > 0)) {
			    //enumerate(depth + 1, offset + data.nbBits, idNum + 1, expectedOnes + 2, c);
				/******/
				pattId++;
				List<Integer> parentIds = new ArrayList<Integer>();
				parentIds.addAll(_parentIds);
				parentIds.add(pattId);
				/******/
				enumerate(depth + 1, data.nbBitsOpt.elementAt(depth + 1), idNum + 1, expectedOnes + 2, c, first,parentIds,spark, deb);
			}
			//on prepare un autre intervalle
			i = bs.nextSetBit(0);
			j = bs.nextClearBit(i) - 1;
			//System.out.print(i+ "-------------" + j+ "   ");
			//String t = bs.toString();
			//System.out.println(t);
			if((debugPrunClos == false) && (r == 0)) {
			    i = intAs.elementAt(depth+1).get();
			    j = intBs.elementAt(depth+1).get();
			}
			if(right == false) {
			    //modifie borne droite
			    j--;
			    if(j >= i) {
				bs = data.interval2BitSet(l, i, j);
				stack.add(bs);
				stackB.add(false);
				//System.out.println(i+" -- "+j);
			    }
			    j++;
			}
			//modifie borne gauche
			i++;
			if(j >= i) {
			    bs = data.interval2BitSet(l, i, j);
			    stack.add(bs);
			    stackB.add(true);
			    //System.out.println(i+" -- "+j);
			}
		    }
		    //data.starAttr(pattern, depth, depth * data.nbBits);
		    data.starAttr(pattern, depth, data.nbBitsOpt.elementAt(depth));
		}
	    }
	}
    }
    
    public Enumeration(DataFR d) {
	this.data = d;	
	this.skyline = new Vector<CC>();
	this.skylineMotif = new Vector<BitSet>();
	this.patterns = new Vector<BitSet>();
	this.format = new DecimalFormat("#0.00");
	this.nbEnum = 0;
	this.nbPruned = 0;
	this.nbPatt = 0;
	//this.nbAretePrunedByUBChi = 0;
	this.allGraphs = new Vector<Graph>();
	intAs = new Vector<MutableInteger>();
	intBs = new Vector<MutableInteger>();
	aretes = new Vector<Arete>();
	aretesStar = new Vector<Arete>();	
	stacks = new Vector<Vector<BitSet>>();
	stackBs = new Vector<Vector<Boolean>>();
	tmp = new BitSet();
	debugPrunAlb = false;
	debugPrunClos = false;
	debugUBQ = false;
    }
   
    public void printSky(Vector<CC> d, Vector<BitSet> m, Boolean rc) {
	
    	//FastRabbit.out.write(" {\n\"LesCC\": {\n"); 
	
    	FastRabbit.out.write(" { \"LesCC\": \n [ ");
    	
	for(int i = 0; i < d.size(); i++) {
	    CC s = d.elementAt(i);
	    
	    if(FastRabbit.prettyFileOutput)
		s.writeCC1(data.toStringPattern(m.elementAt(i)),m.elementAt(i),i); // write the pattern in a file for vizu
	    //System.out.println(s.nbVertices+" "+s.nbEdges+" "+s.qSum+" "+s.qSum / (double) s.nbEdges);
	    
	    //   System.out.println("  ");
	    
	    /*for(int j = 0; j < s.vertices.size(); j++) {
		int k = s.vertices.elementAt(j);
		//System.out.print(s.vertices.elementAt(j) + " ");
		String v = data.findKey(data.vertexNames, k);
		System.out.print(v + " ");
	    }
	    
	    double sqBarre = (double) s.qSum / (double) s.nbEdges;
	    if(rc)
		System.out.println("(" + format.format(s.qSum) + ")" + "(" + format.format(sqBarre) + ") (" + s.nbEdges + ") (" + s.nbVertices + ")/ ");
	    else
		System.out.print("(" + format.format(s.qSum) + ")" + "(" + format.format(sqBarre) + ") (" + s.nbEdges + ") (" + s.nbVertices + ")/ ");
	    */
	
	    if(i < d.size() - 1)
		FastRabbit.out.write(",\n");
	}
	//	System.out.println(" ");
	//FastRabbit.out.write(" }\n}");
	FastRabbit.out.write(" ],");
	FastRabbit.out.write("\"timeStart\": " + FastRabbit.startTime + ",\n");
	FastRabbit.out.write("\"endReadDataTime\": " + FastRabbit.endReadDataTime + ",\n");
	FastRabbit.out.write("\"endMinePatternsTime\": " + FastRabbit.endMinePatternsTime );
	FastRabbit.out.write(" \n }");
    }

    class vectComp implements Comparator<Arete> {
	@Override
	public int compare (Arete o1, Arete o2) {
	    return (o1.tuples.size() >o2.tuples.size() ? -1: (o1.tuples.size()==o2.tuples.size() ? 0 : 1));
	}
    }
}

// Local Variables:
// coding: utf-8
// End:
