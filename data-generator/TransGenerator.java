import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Marc1
 *
 */
public class TransGenerator {

	
	
	// --  format: att1;...;attn;In;Out
	//						S|N;...;S;S
	
	public final static ArrayList<String>  gen_cc(int first_node, int last_node, double link_prob) {
		double threshold= 1.0 - link_prob;
		int nb_edges=0; 
		ArrayList<String>res = new ArrayList<String>();
		Boolean[] present = new Boolean[last_node + 1 - first_node];
		for (int i = 0; i < present.length; i++) {
			 present[i]=false;
			
		}
		
		
		for (int i = first_node; i < last_node; i++) {
			for(int j=i+1; j<=  last_node; j++){ // traite la paire (i,j)
				if(Math.random()>= threshold){
					res.add(new String(i+";"+j));
					res.add(new String(j+";"+i));
					//System.out.println(i +" "+ j);
					//System.out.println(j + " "+i);
					nb_edges +=2;
					present[i-first_node]=true;
					present[j-first_node]=true;
				}
	
			}
			
		}
		System.out.println("nb edges = " + nb_edges);	
		for (int i = 0; i < present.length; i++) {
			if(!present[i])
			System.out.println((i+first_node) + " " + present[i]);
			
		}
		return res;
	}
	
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		//----------------		
				int nb_patterns = 1;  //-P
				int pattern_size = 10; //-S
				int weight = 20; //-W
				double link_prob = 0.5;//-D
				double noise_rate =0.1; //-N
				double  out_factor = 0.15; //-O
				
				int nb_vertices = 3000; //-v
				//double outside_prob= 0.5;		
				int nb_trans = 50000; // -t
				int nb_att =3; //-a
				int att_domain_size =30; // -c
				
		for(int a = 0; a < args.length; a++){
			if(args[a].charAt(0) == '-'){
				if(args[a].charAt(1) == 'P'){ // nb patterns to hide
					a++;
					nb_patterns = Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'S'){
					a++;
					pattern_size= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'W'){
					a++;
					weight= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'D'){
					a++;
					link_prob= Double.parseDouble(args[a]);
				}else if(args[a].charAt(1) == 'N'){
					a++;
					noise_rate= Double.parseDouble(args[a]);
				}else if(args[a].charAt(1) == 'O'){
					a++;
					out_factor= Double.parseDouble(args[a]);
				}else if(args[a].charAt(1) == 'v'){
					a++;
					nb_vertices= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 't'){
					a++;
					nb_trans= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'a'){
					a++;
					nb_att= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'c'){
					a++;
					att_domain_size= Integer.parseInt(args[a]);
				}else if(args[a].charAt(1) == 'h'){
					System.out.println("TransGenerator usage:");
					System.out.println("-------- PATTERN PARAMETERS -------- ");
					System.out.println("\t-P\t Number of patterns to be hidden (default " + nb_patterns  + ")");
					System.out.println("\t-S\t  Number of vertices involved within the pattern (default " +  pattern_size  + ")");
					System.out.println("\t-W\t  Weight of the edges within the pattern (default " + weight  + ")");
					System.out.println("\t-D\t  Link probability: Probability of two vertices of the pattern to be linked ... 1 means clique (default " + link_prob + ")");
					System.out.println("\t-N\t  Noise rate: probability of a transaction of a patterns to be noisy (default " + noise_rate + ")");
					System.out.println("\t-O\t  Out Factor: percentage of tuples with context C outside the pattern   (default " + out_factor + ")");
					System.out.println("-------- DATA PARAMETERS -------- ");
					System.out.println("\t-v\t Total number of vertices  (default " + nb_vertices + ")");
					System.out.println("\t-t\t Number of transactions  (default " + nb_trans + ")");
					System.out.println("\t-a\t Number of attributes  (default " + nb_att  + ")");
					System.out.println("\t-c\t Cardinality of the attribute domains (default " + att_domain_size + ")");
					System.exit(0);

				}
						
			}
		}
		
		
		
		
		//----------------
		int nb_C_outsidePattern = (int) (out_factor * link_prob* pattern_size * (pattern_size -1) * weight);
		System.out.println(nb_C_outsidePattern +" tuples for C not(e)");
		int first_node = 1;
		int last_node = pattern_size  ;
		String context;
		ArrayList<String>[] patterns_edges = new ArrayList[nb_patterns];
		int nb_gen_tuples=0;
		int nb_gen_outside=0;
		int nb_gen_noisy=0;
		String filename;
		filename="gen"+nb_patterns+"P"+pattern_size+"S"+weight +"W"+link_prob + "D"+noise_rate +"N"+out_factor +"O"+nb_vertices +"v"+nb_trans +"t"+nb_att +"a"+att_domain_size +"c.txt";
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
				//"gen"+nb_patterns +"pat"+pattern_size+"patt_size" +nb_vertices+"vert"+((int)(100*link_prob))+"link_prob"+nb_att+"nb_att" + att_domain_size +"card_domain"+ weight+"weight" + out_factor +"out_factor"+ nb_trans+ "trans.txt" )); 
		String entete1="", entete2="", entete3="";
		
		for (int i = 0; i < nb_att; i++) {
			entete1+="att"+i+";";
			entete2+="S;";
            entete3+="*;";
		}
		entete1+="IN;OUT\n";
		entete2+="S;S\n";
        entete3+="*;*\n";
		out.write(entete1+entete2+entete3);
		
		//----------------
		for (int i = 0; i < nb_patterns; i++) {
			patterns_edges[i] = gen_cc(first_node,last_node,link_prob);
			first_node +=pattern_size;
			last_node += pattern_size;
		}
		
		
		// generating context for each pattern edges; 
		String context_bidon="";
		ArrayList<String> edges; 
		String curr_edges;
		String line;
		for (int i = 0; i < patterns_edges.length; i++) {
			edges=patterns_edges[i]; 
			// generation du contexte: 
			context=""+i+";";
			for (int j = 1; j < nb_att; j++) {
				context+=i+";";
			}
			System.out.println("context for pattern " + i + ": " + context);
			// parcours des edges 
			for (int j = 0; j < edges.size(); j++) {
				curr_edges= edges.get(j);
				for (int j2 = 0; j2 < weight; j2++) {
					if(Math.random() >= noise_rate){ //generating good tuples (pattern)
						out.write(context + curr_edges + "\n") ;
						nb_gen_tuples++;
					}
					else { // introducing noise
						context_bidon="" + (int)(Math.random()*att_domain_size) +";";
						for (int k = 1; k < nb_att; k++) {
							context_bidon+= (int)(Math.random()*att_domain_size) +";";
						}						
						line=context_bidon+curr_edges+"\n";
						nb_gen_noisy++;
						out.write(line);
						
						
					}

				}
				
				// generation of not(C),e
				
				context_bidon="" + (int)(Math.random()*att_domain_size) +";";
				for (int k = 1; k < nb_att; k++) {
					context_bidon+= (int)(Math.random()*att_domain_size) +";";
				}
				
				line=context_bidon+curr_edges+"\n";
				out.write(line);
				nb_gen_outside++;
				
				
			}
			// -----------------
			// Generating some tuples for C not(e)
			// -----------------			
			int node1, node2;
			
			for (int j = 0; j < nb_C_outsidePattern; j++) {
				node1= Math.max(1,  (int) (Math.random() * nb_vertices));
				node2= Math.max(1,(int) (Math.random() * nb_vertices));
				if(node1!=node2){
					out.write(context+node1+";"+node2+"\n");
					//out.write(context+node2+";"+node1+"\n");
					 //nb_gen_outside+=2;	
					nb_gen_outside++;
				}
			}
			
			
			
		}
		System.out.println(nb_gen_tuples + " tuples" );
		System.out.println(nb_gen_noisy + " noisy tuples");
		System.out.println(nb_gen_outside + " outside tuples");

		nb_gen_tuples+=nb_gen_outside;
		
		
		// ---- Adding some other transactions 
		int node1, node2;

		while(nb_gen_tuples  < nb_trans){
			context="" + (int)(Math.random()*att_domain_size) +";";
			for (int i = 1; i < nb_att; i++) {
				context+= (int)(Math.random()*att_domain_size) +";";
			}
			node1= Math.max(1,  (int) (Math.random() * nb_vertices));
			node2= Math.max(1,(int) (Math.random() * nb_vertices));
			
			if(node1!=node2){
				out.write(context+node1+";"+node2+"\n");
				//nb_gen_tuples++;
				out.write(context+node2+";"+node1+"\n");
				nb_gen_tuples+=2;
			}
			
		}
		
		
		
		
		
		System.out.println(nb_gen_tuples + " tuples" );
		
		//------ Adding a very strong edge for the normalisation max W_*(x)
		
		for (int ii = 0; ii < weight*2; ii++) {
			context="" + (int)(Math.random()*att_domain_size) +";";
			for (int i = 1; i < nb_att; i++) {
				context+= (int)(Math.random()*att_domain_size) +";";
			}
			node1=  nb_vertices+1;
			node2= nb_vertices+2;
			
			if(node1!=node2){
				out.write(context+node1+";"+node2+"\n");
				//nb_gen_tuples++;
				out.write(context+node2+";"+node1+"\n");
				nb_gen_tuples+=2;
			}
		
		}
		// ----
		
		out.close();
		
		
		//gen_cc(1,100,1);
		
		
	}

}
