# Cosmic: Mining Contextual Exceptional Subgraphs

>Kaytoue, M., Plantevit, P., Zimmermann, A., Bendimerad, A., Robardet, C., Exceptional Contextual
>Subgraph Mining, Machine Learning, Springer (2016) – accepted

## Compile Cosmic
The algorithm is written in Java 8. SDK should be installed. 
```
cd cosmic && make
```

## Input data
Basically, you will find the following. The three first lines are headers. Then transactions appear with the values of their attributes (att0,att1,att2) and the depart/arrival nodes of the edge on which the transaction is attached. The first header lines gives the attributes names and has to finished with IN;OUT. Next line specifies the attributes types (N for numerical; S for symbolic). The third line tells where to start the search, that is from which pattern and not necessarilly from the most general one ```<*,*,...,*>``` (Important: after a start *, only start can appear, you may need to rearrange the columns)
```
att0;att1;att2;IN;OUT
S;S;S;S;S
*;*;*;*;*
0;0;0;1;2
0;13;23;1;2
....
```
You may simply generate an artificial dataset (with default parameters) to understand its basic CSV format: ```cd data-generator ; javac TransGenerator.java && java TransGenerator```

## Run Cosmic
We provide an example in the script ```./run.sh```
The algorithm takes several parameters:
* filename 
* minimal number of nodes in a pattern
* minimal number of edges in a pattern
* minimal quality on a pattern edge
* minimal average quality of a pattern
* 0|1 = de|activate closed description enumeration
* 0|1 = de|activate upper bound pruning
* minimal support: minimal number of transaction an edge should support for a context to be considered in a pattern

## Publication Supplementary Material
See the branch [https://github.com/mehdi-kaytoue/contextual-exceptional-subgraph-mining/tree/publication]
