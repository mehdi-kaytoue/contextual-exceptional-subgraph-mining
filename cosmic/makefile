JC = javac
JFLAGS = -g

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

classfiles= \
	MutableInteger.java \
	MutableBoolean.java \
	Arete.java \
	DataFR.java \
	CC.java \
	Graph.java \
	Check.java \
	Enumeration.java \
	Tuple.java \
	FastRabbit.java 
	
default: classfiles

classfiles: $(classfiles:.java=.class)

all: $(classfiles)

%.class: %.java
	$(JC) $(JFLAGS) $<

clean:
	$(RM) *.class
