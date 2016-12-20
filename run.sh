#!/bin/bash

# Generate a default artificial dataset
cd data-generator
javac TransGenerator.java
java TransGenerator
rm TransGenerator
cd ..

# Run the algo on this dataset
cd cosmic
make
time java -Xmx6G -Xss1G -cp . FastRabbit ../data-generator/gen1P10S20W0.5D0.1N0.15O3000v50000t3a30c.txt 1 1 0.0000001 0.00001 0 0 1 "output.txt"
mv output.txt ..
make clean
