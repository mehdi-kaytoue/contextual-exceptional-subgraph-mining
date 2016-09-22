package fastrabbit;
import java.io.*;
import java.util.*;

public class Check {
    double mv;
    double me;
    double mqs;
    double mqb;
    int Theta;

    //chi^2
    double seuilChi;
    double seuilQ;
    double[][] p;
    double[][] o;
    //clos
    BitSet closure;

    /*
     * the constructor
     */
    public Check(double mv, double me, double mqs, double mqb, int theta) {
	this.mv = mv;
	this.me = me;
	this.mqs = mqs;
	this.mqb = mqb;
	this.Theta = theta;
	
	//chi^2
	seuilChi = 3.84;//10.83;//3.84//0;//6.64;
	seuilQ = 0;
	p = new double[2][2];
	o = new double[2][2];
	//clos
	closure = new BitSet();
    }
    
    /*
     * return True if p dominates s
     */
    public  Boolean isDominated(CC s, CC p) {
	double sqBarre = (double) s.qSum / (double) s.nbEdges;
	double pqBarre = (double) p.qSum / (double) p.nbEdges;
	if ((s.nbVertices <= p.nbVertices) &&
	    (s.nbEdges <= p.nbEdges) &&
	    (s.qSum <= p.qSum) &&
	    (sqBarre <= pqBarre) && 
	    ((s.nbVertices < p.nbVertices) ||
	     (s.nbEdges < p.nbEdges) ||
	     (s.qSum < p.qSum) ||
	     (sqBarre < pqBarre)) ) {
	    return true;   
	}
	return false;
    }
    
    /*
     * return true if s satisfies the thresholds
     */
    public Boolean threshold(CC s, DataFR data) {
	double sqBarre = (double) s.qSum / (double) s.nbEdges;
	//System.out.println(sqBarre+" "+ mqb+" "+s.qSum+" "+mqs+" "+s.nbEdges+" "+me+" "+s.nbVertices+" "+mv);
	if ((s.nbVertices < mv) ||
	    (s.nbEdges < me) ||
	    (s.qSum < mqs) ||
	    (sqBarre < mqb) ) {
	    return false;   
	}
	return true;
    }
    
    /*
     * consider each dsca pattern and remove it if it does not satisfy
     * the threshold constraints
     */
    
    public void checkThresholds(Graph d) {
	for(int i = 0; i < d.cc.size(); ) {
	    CC s = d.cc.elementAt(i);
	    if(threshold(s, null) == false) {
		d.cc.remove(i);
	    }
	    else
		i++;
	}
    }
    
    /*
     * return true if p is a sky pattern and remove the dominated
     * patterns from skyline
     */
    public Boolean dominateAll(CC p, Vector<CC> skyline, Vector<BitSet> skylineMotif) {
	Boolean isASky = false;
	for(int i = 0; i < skyline.size(); ) {
	    CC s = skyline.elementAt(i);
	    if(isDominated(p,s)) {
		return false;
	    }
	    if(isDominated(s,p)) {
		//s is dominated
		skyline.remove(i);
		skylineMotif.remove(i);
		isASky = true;
	    }
	    else
		i++;
	}
	return true;
    }
    
    /*
     * consider each dsca pattern and add it in the skyline if it is
     * not dominated
     */
    public void computeSkyline(CC pattern, Vector<CC> skyline, BitSet motif, Vector<BitSet> skylineMotif) {
	//if(dominateAll(pattern, skyline, skylineMotif)) {
	    BitSet bt = new BitSet();
	    DataFR.copyBS(bt, motif, 0);
	    skylineMotif.add(bt);
	    CC tmp = new CC(pattern.data);
	    pattern.copyCC(tmp);
	    skyline.add(tmp);
	//}
    }
    
    public double chisquare(double wC, double sumC, double wStar, double sumStar) {
	double r = 0.0;
	p[0][0] = (wStar /// sumStar
		   ) * (sumC/sumStar);
	p[0][1] = (sumStar - wStar)///sumStar
	    * (sumC/sumStar);
	p[1][0] = (wStar // /sumStar
		   ) * (1.0 - sumC/sumStar);
	p[1][1] = (sumStar - wStar) // /sumStar
	    * (1.0 - sumC/sumStar);
	
	o[0][0] = wC;// / sumStar;
	o[0][1] = (sumC - wC);// / sumStar;
	o[1][0] = (wStar - wC); // / sumStar;
	o[1][1] = (sumStar - wStar - sumC + wC); // / sumStar;
	for(int i = 0; i < 2; i++) {
	    for(int j = 0; j < 2; j++) {
		if(p[i][j] != 0)
		    r = r + (o[i][j] - p[i][j]) * (o[i][j] - p[i][j]) / p[i][j];
	    }
	}
	return r; 
    } 
    
    public double qvalue(double wC, double maxC, double wStar, double maxStar) {
	if(maxStar == 0)
	    System.out.println("pb maxStar = " + maxStar);
	if(maxC == 0)
	    System.out.println("pb maxCC = " + maxC);
	return (maxC / maxStar) * ((wC / maxC) - (wStar / maxStar));
    }

    /* 
     * closure
     */
    public void convexInterval(BitSet b, Enumeration e) {
	for(int i = 0; i < e.data.nbAttributes ; i++){
	    // For numerical attribute, 
	    if(e.data.attributeTypes.elementAt(i) != 1) {
		int negatif = e.data.nbBitsOpt.elementAt(i);//(i * e.data.nbBits);
		while(true) { 
		    int positif1 = b.nextSetBit(negatif);
		    if(positif1 < 0)
			break;
		    //System.out.println("i " + i +" "+positif1 + " " + negatif + " "+b.toString());
		    negatif = b.nextClearBit(positif1);
		    int positif2 = b.nextSetBit(negatif);
		    if(positif2 >= (e.data.nbBitsOpt.elementAt(i) + e.data.modalities.elementAt(i).size()))
			//if(positif2 >= ((i * e.data.nbBits) + e.data.modalities.elementAt(i).size()))
			break;
		    for(int j = negatif; j < positif2; j++) {
			b.set(j,true);
		    }
		}
	    }
	}
    }

    public void initClosure(BitSet bits, Enumeration e) {
	for(int i = 0; i < e.data.nbAttributes; i++){///////////////// (vert) enleve le + 2
	    // For symbolic attribute, put the corresponding bits to 1
	    if( (i >= e.data.nbAttributes) || (e.data.attributeTypes.elementAt(i) == 1)) {
		for(int j = e.data.nbBitsOpt.elementAt(i); j < e.data.nbBitsOpt.elementAt(i+1); j++) {
		    bits.set(j,true);
		}
	    }
	    else {
		// For numerical attribute, put the bits to 0
		for(int j = e.data.nbBitsOpt.elementAt(i); j < (e.data.nbBitsOpt.elementAt(i) + e.data.modalities.elementAt(i).size()); j++) {
		    bits.set(j,false);
		}
		bits.set(e.data.nbBitsOpt.elementAt(i + 1) - 1, true);
	    }
	}
    } 
    
    public int computeClosure(BitSet context, int expectedOnes, Enumeration e, Graph gr, CC laCC) {
	int r = 1;
	//check if all the symbolic attributes are stars
	MutableBoolean allStar = new MutableBoolean(false);
	//System.out.println("ici");
	initClosure(closure, e);
	//e.data.printPattern(closure);
	//On parcourt les tuples
	if(laCC == null) {
	    for(int k = 0; k < gr.cc.size(); k++) {
		CC g = gr.cc.elementAt(k);
		//a ne pas faire, sinon faut reclaculer la poubelle...
		///////////////////////////////////if(g.isPruned == false) {
		//System.out.println("ok");
		for(int i = 0; i < g.g.size(); i++) {
		    for(int j = 0; j < g.g.elementAt(i).size(); j++) {
			// (vert)
			Iterator<Tuple> it = g.g.elementAt(i).elementAt(j).tuples.iterator();
			while((it.hasNext()) && (!allStar.get())) {
			    Tuple tuple = it.next();
			    BitSet elem = tuple.t; 
			    closure = e.data.intersection(closure, elem, allStar);
			}
		    }
		}
	    }
	    //}
	}
	else {
	    // sur une seule cc
	    CC g = laCC;
	    if(g.isPruned == false) {
		for(int i = 0; i < g.g.size(); i++) {
		    for(int j = 0; j < g.g.elementAt(i).size(); j++) {
			// (vert)
			Iterator<Tuple> it = g.g.elementAt(i).elementAt(j).tuples.iterator();
			while((it.hasNext()) && (!allStar.get())) {
			    Tuple tuple = it.next();
			    BitSet elem = tuple.t;
			    closure = e.data.intersection(closure, elem, allStar);
			}
		    }
		}
	    }
	}
	convexInterval(closure, e);
	return e.data.compareBS(context, closure);
    }
}


// Local Variables:
// coding: utf-8
// End:
