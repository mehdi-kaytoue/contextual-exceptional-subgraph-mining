package transactiongenerator;

public class DesignPoint {
	public static int nb_patternsDefaultValues = 5;
	public static int pattern_sizeDefaultValues = 10;
	public static int weightDefaultValues = 30;
	public static double link_probDefaultValues = 0.50;
	public static double noise_rateDefaultValues = 0.1;
	public static double out_factorDefaultValues = 0.15;
	public static int nb_verticesDefaultValues = 10000;
	public static int nb_transDefaultValues = 1000000;
	public static int nb_attDefaultValues = 5;
	public static int att_domain_sizeDefaultValues = 30;
	public static int replicationFactorDefaultValue=1;
	
	private int nb_patterns;
	private int pattern_size;
	private int weight;
	private double link_prob;
	private double noise_rate;
	private double out_factor;
	private int nb_vertices;
	private int nb_trans;
	private int nb_att;
	private int att_domain_size;
	private int replicationFactor;

	public DesignPoint() {
		nb_patterns = nb_patternsDefaultValues;
		pattern_size=pattern_sizeDefaultValues;
		weight=weightDefaultValues;
		link_prob=link_probDefaultValues;
		noise_rate=noise_rateDefaultValues;
		out_factor=out_factorDefaultValues;
		nb_vertices=nb_verticesDefaultValues;
		nb_trans=nb_transDefaultValues;
		nb_att=nb_attDefaultValues;
		att_domain_size=att_domain_sizeDefaultValues;
		replicationFactor=replicationFactorDefaultValue;
	}
	public int getReplicationFactor() {
		return replicationFactor;
	}
	public void setReplicationFactor(int replicationFactor) {
		this.replicationFactor = replicationFactor;
	}
	public int getNb_patterns() {
		return nb_patterns;
	}

	public void setNb_patterns(int nb_patterns) {
		this.nb_patterns = nb_patterns;
	}

	public int getPattern_size() {
		return pattern_size;
	}

	public void setPattern_size(int pattern_size) {
		this.pattern_size = pattern_size;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public double getLink_prob() {
		return link_prob;
	}

	public void setLink_prob(double link_prob) {
		this.link_prob = link_prob;
	}

	public double getNoise_rate() {
		return noise_rate;
	}

	public void setNoise_rate(double noise_rate) {
		this.noise_rate = noise_rate;
	}

	public double getOut_factor() {
		return out_factor;
	}

	public void setOut_factor(double out_factor) {
		this.out_factor = out_factor;
	}

	public int getNb_vertices() {
		return nb_vertices;
	}

	public void setNb_vertices(int nb_vertices) {
		this.nb_vertices = nb_vertices;
	}

	public int getNb_trans() {
		return nb_trans;
	}

	public void setNb_trans(int nb_trans) {
		this.nb_trans = nb_trans;
	}

	public int getNb_att() {
		return nb_att;
	}

	public void setNb_att(int nb_att) {
		this.nb_att = nb_att;
	}

	public int getAtt_domain_size() {
		return att_domain_size;
	}

	public void setAtt_domain_size(int att_domain_size) {
		this.att_domain_size = att_domain_size;
	}
	
	public void setIemElement(int index,double element){
		switch (index){
		case 0 :
			nb_patterns=(int) element;
			break;
		case 1 :
			pattern_size=(int) element;
			break;
		case 2 :
			weight=(int) element;
			break;
		case 3 :
			link_prob=element;
			break;
		case 4 :
			noise_rate=element;
			break;
		case 5 :
			out_factor=element;
			break;
		case 6 :
			nb_vertices=(int) element;
			break;
		case 7 :
			nb_trans=(int) element;
			break;
		case 8 :
			nb_att=(int) element;
			break;
		case 9 :
			att_domain_size=(int) element;
			break;
		case 10 :
			replicationFactor=(int) element;
			break;
		}
	}
}
