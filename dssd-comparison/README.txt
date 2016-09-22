
This Java Eclipse project is for genereating artificial data
and testing DSSD run times and ability to retrieve hidden patterns.
To reproduce our results: 

1/ import the project in Eclipse (JAVA 6) on Windows (as we
use the compiled 64 bits version od DSSD available on 
http://patternsthatmatter.org/software.php

2/ Move the folder dssd at the root of the c: drive 
   go it in the dataset folder and uncompress the dataset

3/ install python3 with matplotlib and all its dependencies
(a good solution is to install anaconda3 for windows)

4/ Explore the main java class and set your python exec path
 (static String PYTHON =.. in TestExecuter.java line 28).
 
5/ Run the TestExecuter class from Eclipse.
   Before that, you can modify the parameters
   of the artifical data generation in parametersFile.txt.
   You can also change the DSSD parameters in c:/dssd/bin/dssd-synth.conf

6/ Wait some time ;)

7/ Generate all plots with
	java -jar QualityMeasureComputer.jar
	python ScoreScatterPlotter.py
	python CourbesGenerator.py
	
8/ Explore the results in the folder "Tests"
	Warning: this folder is overwritten if you start again from step 5!


