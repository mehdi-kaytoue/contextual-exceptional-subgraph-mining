package transactiongenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import utils.Constants;

/**
 * 
 */

/**
 * @author Marc1
 *
 */
public class TransGenerator {

	// -- Céline's format: att1;...;attn;In;Out
	// S|N;...;S;S

	private final static ArrayList<String> gen_cc(int first_node, int last_node, double link_prob) {
		double threshold = 1.0 - link_prob;
		int nb_edges = 0;
		ArrayList<String> res = new ArrayList<String>();
		Boolean[] present = new Boolean[last_node + 1 - first_node];
		for (int i = 0; i < present.length; i++) {
			present[i] = false;

		}

		for (int i = first_node; i < last_node; i++) {
			for (int j = i + 1; j <= last_node; j++) { // traite la paire (i,j)
				if (Math.random() >= threshold) {
					res.add(new String(i + ";" + j));
					res.add(new String(j + ";" + i));
					// System.out.println(i +" "+ j);
					// System.out.println(j + " "+i);
					nb_edges += 2;
					present[i - first_node] = true;
					present[j - first_node] = true;
				}

			}

		}
		System.out.println("nb edges = " + nb_edges);
		for (int i = 0; i < present.length; i++) {
			if (!present[i])
				System.out.println((i + first_node) + " " + present[i]);

		}
		return res;
	}

	public static String generate(DesignPoint designPoint, String generatedDataDirectoryPath) {
		int replicationFactor = designPoint.getReplicationFactor();

		int nb_patterns = designPoint.getNb_patterns();
		int pattern_size = designPoint.getPattern_size();
		int weight = designPoint.getWeight();
		double link_prob = designPoint.getLink_prob();
		double noise_rate = designPoint.getNoise_rate();
		double out_factor = designPoint.getOut_factor();
		int nb_vertices = designPoint.getNb_vertices();
		int nb_trans = designPoint.getNb_trans();
		int nb_att = designPoint.getNb_att();
		int att_domain_size = designPoint.getAtt_domain_size();

		// write parameters in a file :
		try {
			BufferedWriter parametersOut = new BufferedWriter(
					new FileWriter(generatedDataDirectoryPath + Constants.FILE_SEPARATOR + "parameters.txt"));
			parametersOut.write("nb_patterns = " + nb_patterns + "\n");
			parametersOut.write("pattern_size = " + pattern_size + "\n");
			parametersOut.write("weight = " + weight + "\n");
			parametersOut.write("link_prob = " + link_prob + "\n");
			parametersOut.write("noise_rate = " + noise_rate + "\n");
			parametersOut.write("out_factor = " + out_factor + "\n");
			parametersOut.write("nb_vertices = " + nb_vertices + "\n");
			parametersOut.write("nb_trans = " + nb_trans + "\n");
			parametersOut.write("nb_att = " + nb_att + "\n");
			parametersOut.write("att_domain_size = " + att_domain_size + "\n");
			parametersOut.write("replicationFactor = " + replicationFactor + "\n");			
			parametersOut.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// ----------------
		int nb_C_outsidePattern = (int) (out_factor * link_prob * pattern_size * (pattern_size - 1) * weight);
		System.out.println(nb_C_outsidePattern + " tuples for C not(e)");
		int first_node = 1;
		int last_node = pattern_size;
		String context;
		ArrayList<String>[] patterns_edges = new ArrayList[nb_patterns];
		int nb_gen_tuples = 0;
		int nb_gen_outside = 0;
		int nb_gen_noisy = 0;
		String filename;
		filename = "gen" + nb_patterns + "P" + pattern_size + "S" + weight + "W" + link_prob + "D" + noise_rate + "N"
				+ out_factor + "O" + nb_vertices + "v" + nb_trans + "t" + nb_att + "a" + att_domain_size + "c.txt";
		String fileNameOfPatterns = "patterns" + filename;
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter(generatedDataDirectoryPath + Constants.FILE_SEPARATOR + filename));
			BufferedWriter patternsOut = new BufferedWriter(
					new FileWriter(generatedDataDirectoryPath + Constants.FILE_SEPARATOR + fileNameOfPatterns));
			String entete1 = "", entete2 = "";

			for (int i = 0; i < nb_att; i++) {
				entete1 += "att" + i + ";";
				entete2 += "S;";
			}
			entete1 += "IN;OUT\n";
			entete2 += "S;S\n";
			out.write(entete1 + entete2);

			// ----------------
			for (int i = 0; i < nb_patterns; i++) {
				patterns_edges[i] = gen_cc(first_node, last_node, link_prob);
				first_node += pattern_size;
				last_node += pattern_size;
			}

			// generating context for each pattern edges;
			String context_bidon = "";
			ArrayList<String> edges;
			String curr_edges;
			String line;
			for (int i = 0; i < patterns_edges.length; i++) {
				edges = patterns_edges[i];
				// generation du contexte:
				context = "" + i + ";";
				for (int j = 1; j < nb_att; j++) {
					context += i + ";";
				}
				System.out.println("context for pattern " + i + ": " + context);
				// parcours des edges
				for (int j = 0; j < edges.size(); j++) {
					curr_edges = edges.get(j);
					int nbGood = 0;
					int nbTotal = 0;
					for (int j2 = 0; j2 < weight; j2++) {
						if (Math.random() >= noise_rate) { // generating good
															// tuples
															// (pattern)
							writeInFile(out, replicationFactor, context + curr_edges + "\n");
							nb_gen_tuples++;
							nbGood++;
						} else { // introducing noise
							context_bidon = "" + (int) (Math.random() * att_domain_size) + ";";
							for (int k = 1; k < nb_att; k++) {
								context_bidon += (int) (Math.random() * att_domain_size) + ";";
							}
							line = context_bidon + curr_edges + "\n";
							nb_gen_noisy++;
							writeInFile(out, replicationFactor, line);
							if (context_bidon.equals(context)) {
								nbGood++;
							}
						}
						nbTotal++;

					}

					// generation of not(C),e

					context_bidon = "" + (int) (Math.random() * att_domain_size) + ";";
					for (int k = 1; k < nb_att; k++) {
						context_bidon += (int) (Math.random() * att_domain_size) + ";";
					}

					line = context_bidon + curr_edges + "\n";
					writeInFile(out, replicationFactor, line);
					nbTotal++;
					nb_gen_outside++;
					if (context_bidon.equals(context)) {
						nbGood++;
					}
					nbGood*=replicationFactor;
					nbTotal*=replicationFactor;
					patternsOut.write(curr_edges + ";" + nbGood + ";" + nbTotal);
					if (j < edges.size() - 1) {
						patternsOut.write(":");
					} else {
						patternsOut.write("\t");
					}
				}
				patternsOut.write(pattern_size + "\t" + edges.size() + "\t" + context + "\n");
				// -----------------
				// Generating some tuples for C not(e)
				// -----------------
				int node1, node2;

				for (int j = 0; j < nb_C_outsidePattern; j++) {
					node1 = Math.max(1, (int) (Math.random() * nb_vertices));
					node2 = Math.max(1, (int) (Math.random() * nb_vertices));
					if (node1 != node2) {
						writeInFile(out, replicationFactor, context + node1 + ";" + node2 + "\n");
						nb_gen_outside++;
					}
				}

			}
			System.out.println(nb_gen_tuples + " tuples");
			System.out.println(nb_gen_noisy + " noisy tuples");
			System.out.println(nb_gen_outside + " outside tuples");

			nb_gen_tuples += nb_gen_outside;

			// ---- Adding some other transactions
			int node1, node2;

			while (nb_gen_tuples < nb_trans) {
				context = "" + (int) (Math.random() * att_domain_size) + ";";
				for (int i = 1; i < nb_att; i++) {
					context += (int) (Math.random() * att_domain_size) + ";";
				}
				node1 = Math.max(1, (int) (Math.random() * nb_vertices));
				node2 = Math.max(1, (int) (Math.random() * nb_vertices));

				if (node1 != node2) {
					writeInFile(out, replicationFactor, context + node1 + ";" + node2 + "\n");
					writeInFile(out, replicationFactor, context + node2 + ";" + node1 + "\n");
					nb_gen_tuples += 2;
				}

			}

			System.out.println(nb_gen_tuples + " tuples");

			// ------ Adding a very strong edge for the normalisation max W_*(x)

			for (int ii = 0; ii < weight * 2; ii++) {
				context = "" + (int) (Math.random() * att_domain_size) + ";";
				for (int i = 1; i < nb_att; i++) {
					context += (int) (Math.random() * att_domain_size) + ";";
				}
				node1 = nb_vertices + 1;
				node2 = nb_vertices + 2;

				if (node1 != node2) {
					writeInFile(out, replicationFactor, context + node1 + ";" + node2 + "\n");
					writeInFile(out, replicationFactor, context + node2 + ";" + node1 + "\n");
					nb_gen_tuples += 2;
				}

			}
			out.close();
			patternsOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return filename;

	}

	private static void writeInFile(BufferedWriter out, int replicationFactor, String line) throws IOException {
		for (int i = 0; i < replicationFactor; i++) {
			out.write(line);
		}

	}
}
