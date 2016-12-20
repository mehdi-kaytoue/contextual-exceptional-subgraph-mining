import java.io.*;
import java.util.*;

// tuples: vector of bitsets
// modalities: 
// attributeTypes: symbolique (S) 1, numerique (N) 2
// nbAttributes: integer

// The vertices : must START AT 1!!!!
// The graph is oriented!!!!!!!!

public class DataFR {
    //the maximal number of modalities of all attributes
    int globalMax;
    //number of attributes without the vertices
    int nbAttributes;
    //number of symbolic attributes without the vertices
    int nbAttributesSym;
    //number of bits to encode an attribute value
    //int nbBits;
    //en cumulé ;-) le nombre de bits jusqu'à l'attribut i = nbBitsOpt.elementAt(i)
    Vector<Integer> nbBitsOpt;
    int nbBitsNum;
    //associate a bitset to a modality (int) 
    Vector<HashMap<Integer, BitSet>> modalities;
    //associate an integer to a modality (String) 
    Vector<HashMap<String, Integer>> modalityNames;
    //associate an integer to a vertex name (String) 
    HashMap<String, Integer> vertexNames;
    //associate a bitset to a vertex (int) 
    //HashMap<Integer, BitSet> vertices;
    //For the Numerical attributes, the modalities that will be sorted...
    Vector<Vector<Integer>> modalityNum;
    //the vecteur of attribute types: symbolique (S) 1, numerique (N) 2
    Vector<Integer> attributeTypes;
    // the vecteur of attribute names (first line of the data file)
    String[] attributeNames;
    //the dataset with vertices
    Vector<Tuple> tuples;
    //the number of modalities per attribute + the vertices
    int[] nbModalities;
    //number of vertices
    int nbVertices;  
    //the masks  
    BitSet masqueNum;
    BitSet masqueSym;
    Vector<BitSet> masques;
    String[] patternInit;
    
    String[] initPattern(String line) {
	String[] s = line.split(";");
	return s;
    }

    int bsInitPattern(BitSet p, MutableInteger depth) {
	//on met les symbolique fixé, puis les intervalles réduits, puis les autres attributs
	//les attributs numériques doivent être encore énumérés
	int expectedOnes = 0;
	int mi = 0;
	for(int i = 0; i < nbAttributes; i++) {
	    String s = this.patternInit[i];
	    if(s.compareTo("*") != 0) {
		if(s.charAt(0) == '[') {
		    //intervalle
		    s = s.substring(1, s.length());
		    String[] t = s.split(",");
		    t[1] = t[1].substring(0, t[1].length() - 1);
		    int a = findMod(t[0], modalityNames.elementAt(i));
		    int b = findMod(t[1], modalityNames.elementAt(i));
		    BitSet r = interval2BitSet(nbBitsOpt.elementAt(i+1) - nbBitsOpt.elementAt(i), a, b);
		    copyBS(p, r, nbBitsOpt.elementAt(i));
		}
		else {
		    //symbolique
		    mi++;
		    Integer value = findMod(s, modalityNames.elementAt(i));
		    BitSet bs = modalities.elementAt(i).get(value);
		    copyBS(p,bs,nbBitsOpt.elementAt(i));
		    expectedOnes = expectedOnes + 2;
		}
	    }
	}
	depth.set(mi);
	return expectedOnes;
    }
    
    
    /* on à mieux dans computeClosure
       public BitSet findBitSetOfAtt(BitSet patt, int numAtt) {
       BitSet bits = new BitSet();
       int i = numAtt * this.nbBits;
       for(int j = i; j < (i + this.nbBits); j++) {
       bits.set(j-i, patt.get(j));
       }	
       return bits;
       }  
    */
    
    /*
     * return the vertex of a tuple that is either the first one (nb == 0) or the second one (nb == 1)
     * and convert it in int
     */
    /*
    public int extractVertex(BitSet tuple, int nb) {
	int debut, fin;
	//le dernier bits est exclus
	if(nb == 0) {
	    debut = nbBitsOpt.elementAt(nbAttributes);
	    fin = nbBitsOpt.elementAt(nbAttributes + 1);

	}
	else {
	    debut = nbBitsOpt.elementAt(nbAttributes + 1);
	    fin = nbBitsOpt.elementAt(nbAttributes + 1) + nbBitsOpt.elementAt(nbAttributes + 1)-nbBitsOpt.elementAt(nbAttributes);
	}
	//if(nb == 0)
	// fin = nbBitsOpt.elementAt(nbAttributes + 1);
	BitSet b1 = tuple.get(debut, fin);//(nbAttributes + nb) * nbBits, (nbAttributes + 1 + nb) * nbBits);
	return b1.nextSetBit(0);
	}*/

    public BitSet extractAtt(BitSet tuple, int nb) {
	//le dernier bits est exclus
	int debut = nbBitsOpt.elementAt(nb);
	int fin = nbBitsOpt.elementAt(nb+1);
	//System.out.println(debut+" "+fin);
	BitSet b1 = tuple.get(debut, fin);//nb * nbBits, (1 + nb) * nbBits);
	return b1;
    }

    /*
     * create a bitset of nbBits long with all the bits in [a,b] set to 1
     */
    public BitSet interval2BitSet(int longueur, int a, int b) {
	BitSet bits = new BitSet();
	bits.set(longueur - 1);//this.nbBits-1);
	//System.out.println("\n"+v+" "+bits+" "+bits.toString());
	for(int i = a; i <= b; i++) {
	    bits.set(i, true);
	}
	//System.out.println(longueur +" "+ bits.toString());
	return bits;
    }  


    public Integer getKey(int att, BitSet value){
	for(Integer key : modalities.elementAt(att).keySet()) {
	    if(modalities.elementAt(att).get(key).equals(value))
		return key;
	}
	return null;
    }

    public String getKeyS(int att, int value){
	for(String key : modalityNames.elementAt(att).keySet()) {
	    if(modalityNames.elementAt(att).get(key).equals(value))
		return key;
	}
	return null;
    }
    
    /*
     * read the types of the attributes and put them in attributeTypes: 1 for symbolic, vs 2 for numeric
     * and initialize modalities and modalityNames
     * It also initialize the vertices and vertexNames hashmaps
     */    
    public void readTypes(String s) {
	nbAttributesSym = 0;
	String parts[] = s.split(";");
	nbAttributes = parts.length - 2;
	for(int i = 0; i < nbAttributes; i++) {
	    if(parts[i].equals("S")) {
		this.attributeTypes.add(1);
		nbAttributesSym++; 
	    } else {
	    	this.attributeTypes.add(2);
	    }
	    HashMap<String, Integer> mod = new HashMap<String, Integer>();
	    this.modalityNames.add(mod); 
	    
	    HashMap<Integer, BitSet> modb = new HashMap<Integer, BitSet>();
	    this.modalities.add(modb); 
	}
	vertexNames = new HashMap<String, Integer>();
	//vertices = new HashMap<Integer, BitSet>();
    }
    

    /*
     * determine if s is a key of m
     */    
    public static Integer findMod(String s, HashMap<String, Integer> m) {
	return  m.get(s);
    }
    
    /*
     * determine if s is a key of m
     */
    public static BitSet findModBS(Integer s, HashMap<Integer, BitSet> m) {
	return m.get(s);
    }

    /*
     * compute the intersection of two bitsets
     * and put it in a
     */
    public BitSet intersection(BitSet a, BitSet b, MutableBoolean allstar) {
	BitSet x = (BitSet) a.clone();
	BitSet y = (BitSet) b.clone();
	//intersection numerique
	x.and(this.masqueNum);
	y.and(this.masqueNum);
	x.or(y);
	BitSet z = (BitSet) a.clone();
	//intersection symbolique
	z.and(b);
	z.and(this.masqueSym);
	//stars ?
	if((z.cardinality() == nbAttributesSym) && (x.cardinality() == nbBitsNum)) {
	    allstar.set(true);
	}
	//intersection globale
	x.or(z);
	return x;
    }

    /*
     * Update the hashmap modalityNames
     * and the number of malities per attributes
     */
    public void upDateModalities(String s) {
	String parts[] = s.split(";");
	for(int i = 0; i < nbAttributes; i++){
	    if(findMod(parts[i], this.modalityNames.get(i)) == null) {
		//we add
		this.nbModalities[i] = this.nbModalities[i] + 1;
		if(nbModalities[i] > globalMax)
		    globalMax = this.nbModalities[i];
		//Symbolic attribute
		if(this.attributeTypes.elementAt(i) == 1) {
		    this.modalityNames.get(i).put(parts[i], this.nbModalities[i]);
		} else {
		    //Numerical attribute: we keep the numerical value
		    
			//// THE TRICK FOR DOUBLE ////
			
			Double _val = new Double(Double.parseDouble(parts[i]));
			_val = _val * 10000;
			//Integer val = new Integer(Integer.parseInt(parts[i]));
		    Integer val =  _val.intValue();
		    
		    // Can be better by count the necessary shift.
		    
		    //// END THE TRICK FOR DOUBLE ////
		    
		    this.modalityNames.get(i).put(parts[i], val);
		}
	    }
	}
	//the vertices
	for(int i = nbAttributes; i < nbAttributes + 2; i++){
	    if(findMod(parts[i], this.vertexNames) == null) {
		//we add
		this.nbModalities[nbAttributes] = this.nbModalities[nbAttributes] + 1;
		if(nbModalities[nbAttributes] > globalMax)
		    globalMax = this.nbModalities[nbAttributes];
		//nbBitsOpt.add(this.nbModalities[nbAttributes] + 2);
		this.vertexNames.put(parts[i], this.nbModalities[nbAttributes]);
	    }
	}
    }

    /*
     * starification of the iem attribute: put all at 0 for Symbolic or vertices, all at 1 for Numerical
     * also used to initialize bitsets in hashmap, the the value pos 
     * (default value numAtt * nbBits) ids given in parameters
     */
    public void starAttr(BitSet pattern, int numAtt, int pos) {
	if((numAtt >= nbAttributes) || (attributeTypes.elementAt(numAtt) == 1)) {
	    //pattern.set(pos, pos + nbBits -1, false);
	      pattern.set(pos, nbBitsOpt.elementAt(numAtt + 1) - 1, false);
	} else {
	    //pattern.set(pos, pos + modalities.elementAt(numAtt).size(), true);
	    pattern.set(pos, pos + modalities.elementAt(numAtt).size(), true);
	}
	//pattern.set(pos+this.nbBits-1, true);
	pattern.set(pos+nbBitsOpt.elementAt(numAtt + 1) - nbBitsOpt.elementAt(numAtt) - 1, true);
	//System.out.println(pattern);
    }

    /*
     * starification of a vertex: put all at 0 
     */
    /*
    public void starVertex(BitSet pattern, int i) {
	//System.out.println(i);
	for(int j = i; j < (i + this.nbBits-1); j++) {
	    pattern.set(j,false);
	}
	pattern.set(i + this.nbBits-1, true);
	//System.out.println(pattern);
    }
    */

    /*
     * compute the individuals masks (masques) for each modality and
     * also the global masks that return the bits corresponding to
     * numerical or symbolic attributes
     */
    public void computeMasks() {
	nbBitsNum = 0;
	masques = new Vector<BitSet>();
	BitSet m = new BitSet();
	masques.add(m);
	for(int i = 0; i < nbAttributes + 2; i++){
	    m = new BitSet();
	    m.clear();
	    for(int j = 0; j < nbBitsOpt.elementAt(i+1)
		    //((i+1) * this.nbBits)
		    ; j++) {
		m.set(j, true);
	    }
	    // for symbolic attribute, put the corresponding bits to 1
	    if( (i >= nbAttributes) || (this.attributeTypes.elementAt(i) == 1)) {
		//for(int j = (i * this.nbBits); j < ((i+1) * this.nbBits); j++) {
		for(int j = nbBitsOpt.elementAt(i); j < nbBitsOpt.elementAt(i+1); j++) {
		    masqueSym.set(j, true);
		}
	    }
	    // for numerical attributes
	    else {
		//for(int j = (i * this.nbBits); j < ((i * this.nbBits) + modalities.elementAt(i).size()); j++) {
		for(int j = nbBitsOpt.elementAt(i); j < (nbBitsOpt.elementAt(i) + modalities.elementAt(i).size()); j++) {
		    masqueNum.set(j,true);
		    nbBitsNum++;
		}
		//masqueNum.set((i * this.nbBits) + this.nbBits - 1, true);
		masqueNum.set(nbBitsOpt.elementAt(i+1)-1, true);
		nbBitsNum++;
	    }
	    masques.add(m);
	}
	masqueSym.and(masques.elementAt(nbAttributes));
    }

    /*
     * Find the rank of a numerical modality
     */
    public static int findRank(Vector<Integer> modalityNum, int v) {
	int r = -1;
	for(int i = 0; i < modalityNum.size(); i++) {
	    if(modalityNum.elementAt(i) == v) 
		return i;
	}
	return r;
    }

    /*
     * For the symbolic attribute, create the corresponding bitsets as well as the star biset
     */
    public void createModalityBits() {
	for(int i = 0; i < this.nbAttributes; i++) {
	    int l = nbBitsOpt.elementAt(i+1) - nbBitsOpt.elementAt(i);
	    for(Map.Entry<String,Integer> entry : this.modalityNames.elementAt(i).entrySet()){
		// For symbolic attribute, put the corresponding single bit to 1
		if(this.attributeTypes.elementAt(i) == 1) {
		    BitSet bits = interval2BitSet(l, entry.getValue(), entry.getValue());
		    this.modalities.get(i).put(entry.getValue(), bits);
		}
		// For numerical attribute, create a default biset that will be updated later 
		else {
		    //trouver le rang dans le truc trié Vector<Vector<Integer>> modalityNum
		    BitSet bits = interval2BitSet(l, 0, 0);
		    this.modalities.get(i).put(entry.getValue(), bits); 
		}
	    }
	    //For Symbolic attribute, add the modality star (O)
	    if(this.attributeTypes.elementAt(i) == 1) {
		BitSet star = new BitSet();
		starAttr(star, i, 0);
		Integer v = new Integer(0);
		this.modalities.get(i).put(v, star);
		} 
	}
	/*
	//the vertices
	int l = nbBitsOpt.elementAt(nbAttributes + 1) - nbBitsOpt.elementAt(nbAttributes);
	for(Map.Entry<String,Integer> entry : this.vertexNames.entrySet()){
	    BitSet bits = interval2BitSet(l, entry.getValue(), entry.getValue());
	    this.vertices.put(entry.getValue(), bits);
	}
	
	//the star bitset
	BitSet star = new BitSet();
	starAttr(star, nbAttributes, 0);
	Integer v = new Integer(0);
	this.vertices.put(v, star);
	*/
    }
    
    /*
     * Copy the biset b into a with an offset of p
     */
    public static void copyBS(BitSet a, BitSet b, int p) {
	for (int i = 0; i < b.length(); i++) {
	    a.set(i+p, b.get(i));
	}
    }

     /*
     * Let a and b be to bitsets of same size, the function copy the
     * biset b into a from p to q
     */
    public static void copySubPart(BitSet a, BitSet b, int p, int q) {
	for (int i = p; i < q; i++) {
	    a.set(i, b.get(i));
	}
    }

    /*
     * Compare the bisets a and b
     */
    public int compareBS(BitSet a, BitSet b) {
	//System.out.println("trtrtr "+a.toString()+" "+b.toString());
	//printPattern(a);
	//System.out.println(" ");
	//printPattern(b);
	// a < b => -1 ie b=* or have a larger intervalle
	// a == b => 0
	// a > b => 1
	BitSet tmp = new BitSet();
	BitSet tmp1 = new BitSet();
	int r = -1;
	int estEgal = 1;
	for(int i = 0; i < this.nbAttributes; i++) {
	    tmp.clear();
	    tmp = extractAtt(a, i);
	    tmp1.clear();
	    tmp1 = extractAtt(b, i);
	    
	    //System.out.println("les masques "+tmp.toString()+" "+tmp1.toString());
	    if(attributeTypes.elementAt(i) == 1) {
		//System.out.println("ici");
		if(tmp.equals(tmp1) == false){
		    //   System.out.println("gloups");
		    estEgal = 0;
		}
		if((tmp.equals(tmp1) == false) && (tmp1.cardinality() > 1)) {
		    r = 1;
		}
		else {
		    //b == *
		    //System.out.println(" * at "+i);
		}
	    }
	    else {
		int a1 = tmp.nextSetBit(0);
		int b1 = tmp1.nextSetBit(0);
		int a2 = tmp.nextClearBit(a1);
		int b2 = tmp1.nextClearBit(b1);
		//System.out.println("["+a1+","+a2+"] ["+b1+","+b2+"]");
		if((a1 < b1) || (a2 > b2)) {
		    estEgal = 0;
		    r = 1;
		}
		else {
		    if((a1 != b1) || (a2 != b2))
			estEgal = 0;
		}
		
	    }
	}
	if (//(r == -1) && 
	    (estEgal == 1)){
	    //System.out.println("egal ");
	    return 0;
	}
	else
	    {
		/*		if(r < 0)
		    System.out.println(" a<b");
		else
		    System.out.println("autre");
		*/
		return r;
	    }
    }




     /*
     * Compare the values of bisets a and b
     */
    public int compareValue(BitSet a, BitSet b, int i) {
	//return 1 if a < b in the lexicographic order
	//or 0 si not comparable
	if(attributeTypes.elementAt(i) == 1) {
	    //toujours comparable
	    if(a.equals(b))
		return 1;
	    if(a.cardinality() == 1) 
		//a == *
		return 1;
	    return -1;
	}
	else {
	    //attribut numerique
	    int a1 = a.nextSetBit(0);
	    int b1 = b.nextSetBit(0);
	    int a2 = a.nextClearBit(a1) - 1;
	    int b2 = b.nextClearBit(b1) - 1;
	    if((a1 == b1) && (a2 == b2))
		return 1;
	    if((a1 > b1) && (a2 < b2)) {
		//[a1,a2] < [b1,b2]
		return 1;
	    }
	    if((a1 < b1) && (a2 > b2)) {
		//[a1,a2] > [b1,b2]
		return -1;
	    }
	    return 0;
	}
    }

    public Boolean estEgal(BitSet a, BitSet b, int i, int j){
	BitSet ta = a.get(i,j);
	BitSet tb = b.get(i,j);
	if(ta.equals(tb))
	    return true;
	return false;
    }
    
    /*
     * Find the key k in the hashmap m and return the value
     */
    public static String findKey(HashMap<String, Integer> m, Integer k) {
	for(Map.Entry<String,Integer> entry : m.entrySet()){
	    if(k.equals(entry.getValue()))
		return entry.getKey();
	}
	return null;
    }
    
    /*
     * Find the key k in the hashmap of vetices m and return the value
     */
    public Integer findKeyVertex(HashMap<Integer, Integer> m, Integer k) {
	for(Map.Entry<Integer,Integer> entry : m.entrySet()){
	    if(k.equals(entry.getValue()))
		return entry.getKey();
	}
	return null;
    }

    /*
     * Create a tuple from a line of the data file
     */
    public Tuple createTuple(String s, int n) {
	Tuple tup = new Tuple();
	BitSet bits = new BitSet();
	String parts[] = s.split(";");
	int offset = 0;
	for(int i = 0; i < this.nbAttributes; i++){
	    Integer modality = findMod(parts[i], modalityNames.get(i));
	    BitSet b = findModBS(modality,modalities.get(i));
	    //offset = offset + nbBits;
	    offset = nbBitsOpt.elementAt(i);
	    copyBS(bits, b, offset);
	}
	tup.t = bits;
	/*
	//the vertices
	offset = nbBitsOpt.elementAt(nbAttributes);
	for(int i = nbAttributes; i < nbAttributes + 2; i++) {
	    Integer modality = findMod(parts[i], vertexNames);
	    BitSet b = findModBS(modality, vertices);
	    copyBS(bits, b, offset);
	    //offset = offset + nbBits;
	    //offset = offset + nbBitsOpt.elementAt(nbAttributes + 1) - nbBitsOpt.elementAt(nbAttributes);
	    offset = nbBitsOpt.elementAt(nbAttributes+1);
	}
	return bits;
	*/
	tup.v1 = vertexNames.get(parts[nbAttributes]);//Integer.parseInt(parts[nbAttributes]);
	tup.v2 = vertexNames.get(parts[nbAttributes + 1]);//Integer.parseInt(parts[nbAttributes + 1]);
	tup.num = n;
	return tup;
    }

    /*
     * Print a bitset
     */
    public void printPattern(BitSet pattern) {
	int indexNum = 0;
	//	System.out.println(pattern.toString());

	for(int i = 0; i < nbAttributes; i++){
	    BitSet tmp = extractAtt(pattern, i);
	    //Integer val = new Integer((int) bitSet2Int(tmp));
	    if(tmp.cardinality() == 1)
		System.out.print("*;");
	    else {
		if(attributeTypes.elementAt(i) == 1) {
		    Integer val = tmp.nextSetBit(0);
		    System.out.print(//val+"--"+
				     findKey(modalityNames.get(i), val)+";");
		} else {
		    Integer first = new Integer((int) tmp.nextSetBit(0));
		    int l = tmp.cardinality();
		    Integer last = new Integer((int) first + l-2);
		    //System.out.println(first);
		    //System.out.println(last);
		    System.out.print(
				     //tmp.toString() + 
				     "[" + findKey(modalityNames.get(i), modalityNum.get(indexNum).get(first))+ "," + 
				     findKey(modalityNames.get(i), modalityNum.get(indexNum).get(last)) + "];");
		    indexNum++;
		}
	    }
	}
	System.out.print("\n");
    }
    
    
    public String toStringPattern(BitSet pattern) {
	int indexNum = 0;
	String result="";
	for(int i = 0; i < nbAttributes; i++){
		
		result += this.attributeNames[i]+"=";
		
	    BitSet tmp = extractAtt(pattern, i);
	    if(tmp.cardinality() == 1)
	    	result+="*;";
	    else {
		if(attributeTypes.elementAt(i) == 1) {
		    Integer val = tmp.nextSetBit(0);
		    result+=     findKey(modalityNames.get(i), val)+";";
		} else {
		    Integer first = new Integer((int) tmp.nextSetBit(0));
		    int l = tmp.cardinality();
		    Integer last = new Integer((int) first + l-2);
		    result+="[" + (Double.parseDouble(findKey(modalityNames.get(i), modalityNum.get(indexNum).get(first))) / (double)10000.)+ "," + 
		    		(Double.parseDouble(findKey(modalityNames.get(i), modalityNum.get(indexNum).get(last))) / (double)10000.) + "];";
		    		//findKey(modalityNames.get(i), modalityNum.get(indexNum).get(last)) + "];";
		    indexNum++;
		}
	    }
	}
	return result;
    }
    
    /*
     * For the numerical attribute, create the corresponding bitsets when the modalities are sorted
     */
    public void createModNum() {
	for(int i = 0; i < this.nbAttributes; i++) {
	    if(this.attributeTypes.elementAt(i) == 2) {
		Vector<Integer> num = new Vector<Integer>();
		for(Map.Entry<Integer, BitSet> v : this.modalities.elementAt(i).entrySet()){
		    // la valeur BitSet
		    Integer b = v.getKey();
		    num.add(b);
		}
		Collections.sort(num);
		this.modalityNum.add(num);
		for(Map.Entry<Integer, BitSet> v : this.modalities.elementAt(i).entrySet()){
		    int id = findRank(num, v.getKey());
		    int l = nbBitsOpt.elementAt(i+1) - nbBitsOpt.elementAt(i);
		    BitSet bits = interval2BitSet(l, id, id);
		   this.modalities.get(i).put(v.getKey(), bits); 
		}
	    }
	}
    }

    public void printData() {

	Iterator<Tuple> it = tuples.iterator();
	while(it.hasNext()){
	    Tuple t = it.next();
	    System.out.println(t.num + " - " + toStringPattern(t.t));
	}
    }

    /*
     * Process the data file
     */
    public void readFile(String file) {
	System.out.println("WARNING!!\nThe vertices must START AT 1\nThe graph is oriented");
	String line = null;
	BufferedReader ficTexte;
	try {
	    ficTexte = new BufferedReader(new FileReader(new File(file)));
	    int countLine = 0;
	    do {
		line = ficTexte.readLine();
		countLine++;
		if (line != null) {
		    if(countLine == 2){
			readTypes(line);
			this.nbModalities = new int[this.nbAttributes + 1];
			for(int i = 0; i < this.nbAttributes + 1; i++){
			    nbModalities[i] = 0;
			}
		    }
		    else
			if (countLine == 1){
			    this.attributeNames = line.split(";");
			}
			else
			    if (countLine == 3){
			    	if ( !(FastRabbit.sRootContext == null) && !FastRabbit.sRootContext.equals(""))  line=FastRabbit.sRootContext;
			    	this.patternInit = initPattern(line);
			    }
			    else {
				if(countLine > 2){
				    upDateModalities(line);
				}
			    }
		}
	    } while (line != null);
	    ficTexte.close();
	    //les bits optimisé!!!
	    int nbb = 0;
	    //nbBitsOpt.add(0);
	    for(int i = 0; i < this.nbAttributes + 1; i++) {
	    	nbBitsOpt.add(nbb);
		nbb = nbb + this.nbModalities[i] + 2;
	    }
	    //pour les vertex
	    nbBitsOpt.add(nbb);
	    nbb = nbb + this.nbModalities[nbAttributes] + 2;
	    nbBitsOpt.add(nbb);
	    //this.nbBits = globalMax + 2; //1 pour 0 et 1 pour le bits de poids fort tjrs à 1
	    //System.out.println("nbBits " + this.nbBits);
	    createModalityBits();
	    createModNum();
	    this.nbVertices = nbModalities[nbAttributes];
	    ficTexte = new BufferedReader(new FileReader(new File(file)));
	    countLine = 0;
	    do {
		line = ficTexte.readLine();
		countLine++;
		if (line != null) {
		    if(countLine > 3){
			tuples.add(createTuple(line, countLine));
		    }
		}
	    } while (line != null);
	    ficTexte.close();

	} catch (FileNotFoundException e) {
	    System.out.println(e.getMessage());
	} catch (IOException e) {
	    System.out.println(e.getMessage());
	}
	computeMasks();
	//for(int i = 0; i < nbAttributes; i++) {
	//    System.out.println(masques.elementAt(i).toString());
	//}
    }
    
    /*
     * the constructor
     */
    public DataFR() {
	//variables
	globalMax = 0;
	nbAttributes = 0;
	this.modalities = new Vector<HashMap<Integer, BitSet>>();
	this.modalityNum = new Vector<Vector<Integer>>();
	this.modalityNames = new Vector<HashMap<String, Integer>>();
	this.attributeTypes = new Vector<Integer>();
	this.nbBitsOpt = new Vector<Integer>();
	this.tuples = new Vector<Tuple>();
	this.masqueNum = new BitSet();
	this.masqueSym = new BitSet();
    }
}


// Local Variables:
// coding: utf-8
// End:
