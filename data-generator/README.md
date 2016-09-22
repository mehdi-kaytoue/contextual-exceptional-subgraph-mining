

#TransGenerator
This Java projects generates synthetic data used to process experiments on Cosmic. It produces a synthetic edge attributed graph that contains hidden patterns. The user can choose the dimensions of the generated graph (e.g., number of vertices, number of transactions) and the properties of the hidden patterns (e.g., number of hidden patterns, number of vertices per pattern). This parameters are explained below.


## PATTERN PARAMETERS 
*	-P	 Number of patterns to be hidden (default 1)
*	-S	  Number of vertices involved within the pattern (default 10)
*	-W	  Weight of the edges within the pattern (default 20)
*	-D	  Link probability: Probability of two vertices of the pattern to be linked ... 1 means clique (default 0.5)
*	-N	  Noise rate: probability of a transaction of a patterns to be noisy (default 0.1)
*	-O	  Out Factor: percentage of tuples with context C outside the pattern   (default 0.15)
	
## DATA PARAMETERS 
*	-v	 Total number of vertices  (default 3000)
*	-t	 Number of transactions  (default 50000)
*	-a	 Number of attributes  (default 3)
*	-c	 Cardinality of the attribute domains (default 30)
    
## Usage
An example of a command line is presented in what follows. We suppose that TransGenerator.jar is the generated runnable JAR of the project.
```
java -jar TransGenerator.jar -v 5000 -a 5 -P 2
```
If a parameter is not specified as an argument in the command, this parameter will take its default value.
    
