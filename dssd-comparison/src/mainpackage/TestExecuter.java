package mainpackage;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import transactiongenerator.DesignPoint;
import transactiongenerator.TransGenerator;
import utils.Constants;
public class TestExecuter {
	private static final String TEST_FOLDER = "Tests";
	private static final String FAST_RABBIT_FOLDER = "FastRabbit elements";
	private static final String TRANSACTION_GENERATOR_FOLDER = "TransactionGenerator elements";

	
	static String DSSD_ROOT = "C:\\dssd\\";
	static String PYTHON = "C:\\Users\\HADES\\AppData\\Local\\Programs\\Python\\Python35-32\\python.exe";
	static String command = "cmd /c cd \\ & c: & cd \\ & cd dssd/bin & dssd64 dssd-synth.conf";
	
	private static String[] testFolderNames = { "1 - Variation de nb_patterns", "2 - Variation de pattern_size",
		"3 - Variation de weight", "4 - Variation de link_prob", "5 - Variation de noise_rate",
		"6 - Variation de out_factor", "7 - Variation de nb_vertices", "8 - Variation de nb_trans",
		"9 - Variation de nb_att", "10 - Variation de att_domain_size" };
	private static double[][] testedValues = { { 4, 10, 20, 30 }, // nb_patternsTestedValues
		{ 10, 20, 30, 50 }, // pattern_sizeTestedValues
		{ 10, 20, 50, 100 }, // weightTestedValues
		{ 0.25, 0.5, 0.75, 0.9 }, // link_probTestedValues
		{ 0.1, 0.25, 0.5 }, // noise_rateTestedValues
		{ 0.15, 0.25, 0.5 }, // out_factorTestedValues
		{ 500, 1000, 1500, 2000 }, // nb_verticesTestedValues
		{ 5000, 10000, 100000, 500000 }, // nb_transTestedValues
		{ 2, 3, 6 }, // nb_attTestedValues
		{ 10, 30, 50, 100 } }; // att_domain_sizeTestedValues
	private static boolean[] variatedParameters = { false, false, false, false, false, false, false, false, false,
		false };
	public static final int PARAMETERS_TOTAL_NUMBER = 10;

	public static final String PARAMETERS_FILE_NAME = "parametersFile.txt";
	private static int generationRepetitionNumber = 10;

	private static void createDirectory(String path) {
		boolean success = (new File(path)).mkdirs();
		if (!success) {
			throw new RuntimeException("folder creation failed");
		}
	}

	public static void main(String[] args) {

		TestExecuter.removeDirectory(new File("./Tests"));

		if (System.getProperty("os.name").startsWith("Windows")) {
			// includes: Windows 2000,  Windows 95, Windows 98, Windows NT, Windows Vista, Windows XP
			System.out.println("OS is windows");
			Constants.FILE_SEPARATOR="\\";
		} else {
			// everything else
			System.out.println("OS is Unix like");
			Constants.FILE_SEPARATOR="/";
		} 
		readParametersFromFile();
		// creation of test folder
		createDirectory(TEST_FOLDER);
		System.out.println("Begin of tests");
		for (int currentParameterIndex = 0; currentParameterIndex < PARAMETERS_TOTAL_NUMBER; currentParameterIndex++) {
			if (variatedParameters[currentParameterIndex]) {
				System.out.println("current tested parameter : " + testFolderNames[currentParameterIndex]);
				createDirectory(TEST_FOLDER + Constants.FILE_SEPARATOR + testFolderNames[currentParameterIndex]);
				// write tested Values in a file :
				try {
					BufferedWriter parametersOut = new BufferedWriter(new FileWriter(
							TEST_FOLDER + Constants.FILE_SEPARATOR + testFolderNames[currentParameterIndex] + Constants.FILE_SEPARATOR+"testedValues.txt"));
					for (int k = 0; k < testedValues[currentParameterIndex].length; k++) {
						parametersOut.write(Double.toString(testedValues[currentParameterIndex][k]));
						if (k < testedValues[currentParameterIndex].length - 1) {
							parametersOut.write(",");
						}
					}
					parametersOut.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				int i = 0;
				for (double currentValue : testedValues[currentParameterIndex]) {
					System.out.println("current tested value :" + currentValue);
					i++;
					String testFolderPath = TEST_FOLDER + Constants.FILE_SEPARATOR+ testFolderNames[currentParameterIndex] + Constants.FILE_SEPARATOR
							+ TEST_FOLDER + i;
					createDirectory(testFolderPath);
					DesignPoint designPoint = new DesignPoint();
					designPoint.setIemElement(currentParameterIndex, currentValue);
					executeOneDesignPoint(designPoint, testFolderPath);
				}
			}
		}
		System.out.println("Begin of measure computation");
		System.out.println("End");
		
		
		
		

	}

	public static void executeOneDesignPoint(DesignPoint designPoint, String testFolderPath)  {
		for (int i = 1; i <= generationRepetitionNumber; i++) {
			String falseFalseCurrentCaseDirectory = testFolderPath + Constants.FILE_SEPARATOR + "rep" + i;
			createDirectory(falseFalseCurrentCaseDirectory);

			String falseFalseGeneratedDataDirectoryPath = falseFalseCurrentCaseDirectory + Constants.FILE_SEPARATOR
					+ TRANSACTION_GENERATOR_FOLDER;
			String falseFalseOutputRepositoryPath = falseFalseCurrentCaseDirectory + Constants.FILE_SEPARATOR + FAST_RABBIT_FOLDER;

			createDirectory(falseFalseGeneratedDataDirectoryPath);
			createDirectory(falseFalseOutputRepositoryPath);

			// Generate data, transform to ARFF prepare and call DSSD and parse the results.
			String inputFilePath = TransGenerator.generate(designPoint, falseFalseGeneratedDataDirectoryPath);
			HashMap<Integer, String> mapEdges = toArff(inputFilePath,falseFalseGeneratedDataDirectoryPath);
			prepareDSSD(inputFilePath,falseFalseGeneratedDataDirectoryPath, designPoint);
			long time = System.currentTimeMillis();
			String xpDirectory = callDSSD();
			time = (System.currentTimeMillis() - time)/(long)1000;
			if (xpDirectory != null) readDSSDResult(xpDirectory,time,falseFalseOutputRepositoryPath, mapEdges, designPoint);
		}
	}


	private static void readDSSDResult(String xpDirectory, long time, String falseFalseOutputRepositoryPath, HashMap<Integer, String> mapEdges, DesignPoint designPoint) {
		try {
			File file = new File(xpDirectory);

			// nb pattern and exec time
			String ts = file.getName().substring("synth-".length());
			int nbPatterns = new File(file.getAbsolutePath()+ "\\models").listFiles().length;
			String resultIndicator= "execution time (s) : "+time+"\nnumber of patterns : "+nbPatterns;
			Files.write(Paths.get(falseFalseOutputRepositoryPath+"\\resultIndicatorsFile.txt"), resultIndicator.getBytes());

			// read pattern descriptions
			List<String> sgs = new ArrayList<String>();
			BufferedReader reader;
			reader = new BufferedReader(new FileReader(file.getAbsolutePath()+ "\\results3-"+ts+".sg"));
			String line; 
			while ((line = reader.readLine()) != null) sgs.add(line);
			reader.close();

			// read distri data
			File model = new File(file.getAbsolutePath()+ "\\basemodel-" +ts +".csv");
			String distribution = new String(Files.readAllBytes(Paths.get(model.toURI())));
			distribution = distribution.substring(distribution.indexOf("Contingency table (relative)")); // we take relative
			String[] labels = distribution.split("\n");
			List<Double> countD = new ArrayList<Double>();
			for (int j = 0; j < labels.length; j++)
				if ( (labels[j].trim().isEmpty()|| labels[j].toLowerCase().startsWith("c")))
					;
				else
					countD.add(Double.parseDouble(labels[j].split(";")[2]));
			
			// process each pattern
			String patterns="";
			//List<Double> countP = new ArrayList<Double>();
			for (int i = 0; i < nbPatterns; i++) {
				String edges="";
				model = new File(file.getAbsolutePath()+ "\\models\\model-" +ts +"-"+String.format("%04d", i+1)+".csv");
				distribution = new String(Files.readAllBytes(Paths.get(model.toURI())));
				distribution= distribution.substring(distribution.indexOf("Contingency table (relative)")); // we take relative
				labels = distribution.split("\n");

				for (int j = 0; j < labels.length; j++) {
					if ( (labels[j].trim().isEmpty()|| labels[j].toLowerCase().startsWith("c")) ) 
					  ;
					else
					{
						double val  = Double.parseDouble(labels[j].split(";")[2]) - countD.get(i);
						if (val>0.00)
						{
							if (!edges.isEmpty()) edges +=":";
							edges+=  ""+mapEdges.get(Integer.parseInt(labels[j].split(";")[0]))+";0;0;0";
						}
					}
				}
				double qsum=1;
				double maxcc=1;
				double sumcc=1;
				double nbVertex=1;
				double nbEdge=1;
				double qavg=qsum/nbEdge;
				
				String sg = sgs.get(i).replaceAll("&&",";").replaceAll(" ", "");
				String[] pElements = sg.split(";");
				sg="";
				for (int j = 0; j < pElements.length; j++) {
					if (pElements[j].endsWith("=1"))
						 pElements[j]= pElements[j].substring(0, pElements[j].length()-2).replace('_', '=');
					else pElements[j]=null;
				}				
				for (int j = 0; j < designPoint.getNb_att(); j++) {
					boolean done=false;
					for (int j2 = 0; j2 < pElements.length; j2++) {
						if (pElements[j2] != null && pElements[j2].startsWith("att"+j))
						{
							sg+=(pElements[j2]+";");
							done=true;
						}
					}
					if (!done) sg+=("att"+j+"=*;");
				}
				
				if (!edges.isEmpty())
					patterns += edges + "\t " + qsum +"\t"+ maxcc  +"\t"+ sumcc  +"\t"+ nbVertex  +"\t"+ nbEdge  +"\t"+ qavg  +"\t"+ sg + "\n";
			}			
			Files.write(Paths.get(falseFalseOutputRepositoryPath+"\\patterns-output.csv"), patterns.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String callDSSD() {
		String xpPath = null;
		try {
			
			BufferedReader is;
			String line;
			Process p = Runtime.getRuntime().exec(command);
			is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = is.readLine()) != null) {
				System.out.println(line);
				if (line.contains("Check results in:"))
					xpPath= line.substring(line.indexOf(':')+1).trim();
			}
			p.waitFor(); 
		}
		catch (Exception e) {return null;}
		return xpPath;
	}



	
	private static void prepareDSSD(String input, String directory, DesignPoint designPoint)
	{
		try {
			String DATASET_ROOT = DSSD_ROOT + "\\data\\datasets\\synth";
			File dataRoot = new File(DATASET_ROOT);
			if (!dataRoot.exists()) dataRoot.mkdir();
			String source = directory+"\\"+input+".arff";
			String target = DATASET_ROOT+"\\synth.arff";
			Files.copy(Paths.get(source), Paths.get(target), REPLACE_EXISTING);
			String emmContent = "descriptionAtts = ";
			emmContent += Files.readAllLines(Paths.get("tmp")).get(0);
			emmContent += "\nmodelAtts = *" ;
			Files.write(Paths.get(DATASET_ROOT+"\\synth.emm"), emmContent.getBytes());
			new File("tmp").delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<Integer, String> toArff(String input, String directory) {
	
		try {
			String fileIn  = directory+"\\"+input;
			String fileOut = directory+"\\"+input+".arff";
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));
			String command = "cmd /c " + PYTHON + " toArrf.py \"" + new File(fileIn).getAbsolutePath() + "\""; 
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader is;
			String line;
			is = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = is.readLine()) != null) bw.write(line+"\n"); 
			bw.close();
			p.waitFor(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HashMap<Integer, String> m = new HashMap<Integer, String>();
		try {
			int i = 0;
			for (String e: Files.readAllLines(Paths.get("tmp2")))
				m.put(i++, e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		new File("tmp2").delete();
		return m;
	}

	public static void removeDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			if (files != null && files.length > 0) {
				for (File aFile : files) {
					removeDirectory(aFile);
				}
			}
			dir.delete();
		} else {
			dir.delete();
		}
	}


	private static void readParametersFromFile() {
		String[] testedValuesAsString;
		try {
			BufferedReader parametersFile = new BufferedReader(new FileReader(new File(PARAMETERS_FILE_NAME)));
			String line;
			while ((line = parametersFile.readLine()) != null) {
				String[] elements = line.split("=");
				switch (elements[0]) {
				case "nb_patternsTestedValues":
					variatedParameters[0] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[0] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[0][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "pattern_sizeTestedValues":
					variatedParameters[1] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[1] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[1][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "weightTestedValues":
					variatedParameters[2] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[2] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[2][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "link_probTestedValues":
					variatedParameters[3] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[3] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[3][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "noise_rateTestedValues":
					variatedParameters[4] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[4] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[4][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "out_factorTestedValues":
					variatedParameters[5] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[5] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[5][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "nb_verticesTestedValues":
					variatedParameters[6] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[6] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[6][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "nb_transTestedValues":
					variatedParameters[7] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[7] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[7][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "nb_attTestedValues":
					variatedParameters[8] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[8] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[8][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "att_domain_sizeTestedValues":
					variatedParameters[9] = true;
					testedValuesAsString = elements[1].split(",");
					testedValues[9] = new double[testedValuesAsString.length];
					for (int i = 0; i < testedValuesAsString.length; i++) {
						testedValues[9][i] = Double.parseDouble(testedValuesAsString[i]);
					}
					break;
				case "nb_patternsDefaultValues":
					DesignPoint.nb_patternsDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "pattern_sizeDefaultValues":
					DesignPoint.pattern_sizeDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "weightDefaultValues":
					DesignPoint.weightDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "link_probDefaultValues":
					DesignPoint.link_probDefaultValues = Double.parseDouble(elements[1]);
					break;
				case "noise_rateDefaultValues":
					DesignPoint.noise_rateDefaultValues = Double.parseDouble(elements[1]);
					break;
				case "out_factorDefaultValues":
					DesignPoint.out_factorDefaultValues = Double.parseDouble(elements[1]);
					break;
				case "nb_verticesDefaultValues":
					DesignPoint.nb_verticesDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "nb_transDefaultValues":
					DesignPoint.nb_transDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "nb_attDefaultValues":
					DesignPoint.nb_attDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "att_domain_sizeDefaultValues":
					DesignPoint.att_domain_sizeDefaultValues = Integer.parseInt(elements[1]);
					break;
				case "replicationFactorDefaultValue":
					DesignPoint.replicationFactorDefaultValue=Integer.parseInt(elements[1]);
					break;
				case "generationRepetitionNumber":
					generationRepetitionNumber = Integer.parseInt(elements[1]);
					break;
				default:
					throw new RuntimeException("this parameter is unknown : " + elements[0]);
				}
			}
			parametersFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
