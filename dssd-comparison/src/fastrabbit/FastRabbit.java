package fastrabbit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Vector;

import utils.Constants;

public class FastRabbit {

	 // To print the patterns in a file for vizu tool.
    public static PrintWriter out;
    public static boolean prettyFileOutput = true;

    public static void retrievePatterns(String inputFilePath, String outputRepositoryPath, boolean debugPrunAlb,
			boolean debugPrunClos,int replicationFactor) {
    long tStart = System.currentTimeMillis();
	DataFR data = new DataFR();
	//read the data file in args[0]
	
	System.out.println("Début lecture");
	data.readFile(inputFilePath);
	System.out.println("Fin lecture");
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
	
	int Theta = 6*replicationFactor;
	Check c = new Check(threshold[0], threshold[1] , threshold[2], threshold[3], Theta);
	//create the graph and associate the tuples to the corresponding edges
	Enumeration e = new Enumeration(data);
	if (debugPrunAlb) {
			e.debugPrunAlb = true;
		}
		if (debugPrunClos) {
			e.debugPrunClos = true;
		}
	try {
		BufferedWriter parametersOut = new BufferedWriter(
				new FileWriter(outputRepositoryPath + Constants.FILE_SEPARATOR + "parameters.txt"));
		parametersOut.write("threshold[0] = " + threshold[0] + "\n");
		parametersOut.write("threshold[1] = " + threshold[1] + "\n");
		parametersOut.write("threshold[2] = " + threshold[2] + "\n");
		parametersOut.write("threshold[3] = " + threshold[3] + "\n");
		parametersOut.write("theta = " + Theta + "\n");
		parametersOut.write("debugPrunAlb = " + e.debugPrunAlb + "\n");
		parametersOut.write("debugPrunClos = " + e.debugPrunClos + "\n");
		parametersOut.close();
	} catch (IOException e1) {
		e1.printStackTrace();
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
	//start the enumeration of the patterns
	System.out.println("Start enumeration with thresolds mv "+threshold[0]+" me "+threshold[1]+" mqs "+threshold[2]+" mqb "+threshold[3]);
	int idNum = 0;
	int index = nbRest.get();
	int offset = e.data.nbBitsOpt.elementAt(index);

	
	
	
	if (FastRabbit.prettyFileOutput)
		try { FastRabbit.out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputRepositoryPath + Constants.FILE_SEPARATOR + "patterns-output.csv"))), true);
			  System.out.println("Result will be written in " + outputRepositoryPath + Constants.FILE_SEPARATOR + "patterns-output.csv");
		} catch (IOException ex) { System.err.println("Cannot write in " + outputRepositoryPath + Constants.FILE_SEPARATOR + "patterns-output.csv"); 	}
	
	
	
	e.enumerate(index, offset, idNum, expectedOnes, c, first);
	//print the skyline
	e.printSky(e.skyline, e.skylineMotif, true);
	
	
	System.out.println(//"NB tot : " + nbPatt +
			   " nb appel rec " + e.nbRec + " NB Pruned " + e.nbPruned + " NB Enum: " + e.nbEnum + " NB Patt: " + e.nbPatt  //+ " NB Closed: "+ e.nbClosed 
			   + " the sky " + e.skyline.size());
	
	if (FastRabbit.prettyFileOutput) FastRabbit.out.close();
	//end print skyline
	long tEnd = System.currentTimeMillis();
	long tDelta = tEnd - tStart;
	double elapsedSeconds = tDelta / 1000.0;
	try {
		BufferedWriter indicatorsFile = new BufferedWriter(
				new FileWriter(outputRepositoryPath + Constants.FILE_SEPARATOR+"resultIndicatorsFile.txt"));
		indicatorsFile.write("execution time (s) : " + Double.toString(elapsedSeconds) + "\n");
		indicatorsFile.write("number of patterns : " + e.skyline.size());
		indicatorsFile.close();
	} catch (IOException e1) {
		e1.printStackTrace();
	}
    }
}

// Local Variables:
// coding: utf-8
// End:
