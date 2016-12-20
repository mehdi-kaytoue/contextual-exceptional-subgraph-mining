import java.io.*;
import java.util.*;

public class FastRabbit {
    // To print the patterns in a file for vizu tool.
    public static PrintWriter out;
    public static boolean prettyFileOutput = true;
    public static String sRootContext = null;
    
    
    public static long startTime;
    public static long endReadDataTime;
    public static long endMinePatternsTime;
    
    
    public static void main(String[] args){
	DataFR data = new DataFR();
	//read the data file in args[0]
	
	startTime = System.currentTimeMillis();
	System.out.println("Début lecture");
	
	if (args.length==10) sRootContext = args[9];
	
	data.readFile(args[0]);
	System.out.println("Fin lecture");
	

	
	//data.printData();
	//create the first pattern full of stars
	BitSet pattern = new BitSet();
	for(int i = 0; i < data.nbAttributes; i++) {
	    data.starAttr(pattern, i, data.nbBitsOpt.elementAt(i));
	}
	int expectedOnes1 = data.nbAttributes + data.modalityNum.size();
	//create context full of stars and the expected ones for the bitset intersection
	BitSet context = new BitSet();
	DataFR.copyBS(context, pattern, 0);
	//set up the thresholds
	double[] threshold = new double[4];
	threshold[0] = 2.0;
	threshold[1] = 1;
	threshold[2] = 0.1;
	threshold[3] = 0.0;
	int i = 1;
	while((i < args.length) && (i < 5)) {
	    threshold[i-1] = Double.parseDouble(args[i]);
	    i++;
	}
	int Theta = Integer.parseInt(args[7]); //5;
	Check c = new Check(threshold[0], threshold[1] , threshold[2], threshold[3], Theta);
	//create the graph and associate the tuples to the corresponding edges
	Enumeration e = new Enumeration(data);
	if(args.length > 5) {
	    double d = Double.parseDouble(args[5]);
	    if(d > 0)
		e.debugPrunAlb = true;
	}
	if(args.length > 6) {
	    double d = Double.parseDouble(args[6]);
	    if(d > 0)
		e.debugPrunClos = true;
	}
	e.intAs.add(new MutableInteger(0));
	e.intBs.add(new MutableInteger(0));
	e.stacks.add(new Vector<BitSet>());
	e.stackBs.add(new Vector<Boolean>());
	//le motif iniial
	//System.out.println("Index : " + pattern);
	MutableInteger nbRest = new MutableInteger(0);
	//MutableInteger nbN = new MutableInteger(0);
	int expectedOnes = data.bsInitPattern(pattern, nbRest);//, nbN);
	//System.out.println(nbRest.get());
	e.patterns.add(pattern);
	//compute gstar
	Graph gs;
	int first = nbRest.get();
	if(nbRest.get() == 0)
	    gs = Graph.gStar(context, expectedOnes1, c, e);
	else
	    gs = new Graph();
	e.allGraphs.add(gs);
	for(i = 1; i <= data.nbAttributes ; i++) {
	    BitSet bs = new BitSet();
	    if(nbRest.get() != data.nbAttributes) {
		if(i == nbRest.get()) {
		    if(i < data.nbAttributes)
			gs = Graph.gStar(context, expectedOnes1, c, e);
		    else {
			gs = Graph.gStar(pattern, expectedOnes, c, e);
		    }
		}
		else
		    gs = new Graph();
	    }
	    else {
		if(i <= data.nbAttributes - 2)
		    gs = new Graph();
		if(i == data.nbAttributes - 1) {
		    gs = Graph.gStar(context, expectedOnes1, c, e);
		    first = i;
		}
		if(i == data.nbAttributes)
		    gs = Graph.gStar(pattern, expectedOnes, c, e);
	    }
	    if(i == nbRest.get())
		DataFR.copyBS(bs, pattern,0);
	    e.allGraphs.add(gs);
	    e.patterns.add(bs);
	    e.intAs.add(new MutableInteger(0));
	    e.intBs.add(new MutableInteger(0));
	    e.stacks.add(new Vector<BitSet>());
	    e.stackBs.add(new Vector<Boolean>());
	}
	
	endReadDataTime = System.currentTimeMillis();
	
	//start the enumeration of the patterns
	System.out.println("Start enumeration with thresolds mv "+threshold[0]+" me "+threshold[1]+" mqs "+threshold[2]+" mqb "+threshold[3]);
	int idNum = 0;
	int index = nbRest.get();
	int offset = e.data.nbBitsOpt.elementAt(index);

	
	
	
	if (FastRabbit.prettyFileOutput)
		try { FastRabbit.out = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[8]))), true);
			  System.out.println("Result will be written in " + args[8]);
		} catch (IOException ex) { System.err.println("Cannot write in " + args[8]); 	}
	
	
	
	List<Integer> l = new ArrayList<>();
	l.add(0);
	e.enumerate(index, offset, idNum, expectedOnes, c, first, l, false, 0);
	//print the skyline
	
	endMinePatternsTime = System.currentTimeMillis();
	
	e.printSky(e.skyline, e.skylineMotif, true);
	
	
	System.out.println(//"NB tot : " + nbPatt +
			   " nb appel rec " + e.nbRec + " NB Pruned " + e.nbPruned + " NB Enum: " + e.nbEnum + " NB Patt: " + e.nbPatt  //+ " NB Closed: "+ e.nbClosed 
			   + " the sky " + e.skyline.size());
	
	if (FastRabbit.prettyFileOutput) FastRabbit.out.close();

	//end print skyline
    }
}

// Local Variables:
// coding: utf-8
// End:
