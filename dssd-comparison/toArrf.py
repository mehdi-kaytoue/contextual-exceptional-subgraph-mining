import sys, csv, math, glob, os, re
from io import FileIO, BufferedWriter

indexLine=0
namesAttr=[]
valuesAttr=[]
labelCount=dict({})
filename=sys.argv[1]

#first pass to count things
with open(filename) as f:
    for line in f:
    	dataline = csv.reader(line.splitlines(), delimiter=';')
    	for l in dataline: tab=l  ### tab is a 'list' object of str

    	if (indexLine==0): 
    		namesAttr=tab
    		del(tab[len(tab)-1])
    	elif (indexLine==1): 
    		for e in namesAttr:	valuesAttr.append(set())
    	else:
            for i in range(0, len(namesAttr)-1):
                if (tab[i]==""): tab[i]="N/A"
                valuesAttr[i].add(tab[i])
            valuesAttr[len(valuesAttr)-1].add(":".join(tab[-2:]))
    	indexLine+=1

for i in range(0, len(valuesAttr)):
	valuesAttr[i]=list(valuesAttr[i])

# second pass to output things
labels=list(valuesAttr[len(valuesAttr)-1])
print("@relation fastrabbit")


emmNames=[]
for i in range(0, len(namesAttr)-1): 
	for x in valuesAttr[i]:
		print("@attribute "+ namesAttr[i]+"_" + x + " {0,1}")
		emmNames.append(namesAttr[i]+"_" + x)

out_file = open("tmp", "wt")
out_file.write(",".join(emmNames))
out_file.close()
	
labelMap=[]	
for x in labels:
	print ("@attribute " + x + " {0,1}")
	labelMap.append(x)

out_file = open("tmp2", "wt")
out_file.write("\n".join(labelMap))
out_file.close()	
	
print("@data")


#print (len(labels+namesAttr)-1)

indexLine=0
with open(filename) as f:
    for line in f:
    	if (indexLine>1):
            dataline = csv.reader(line.splitlines(), delimiter=';')
            for l in dataline: tab=l  ### tab is a 'list' object of str

            s=[]
            for i in range(0, len(namesAttr)-1): 
                #if (tab[i]==""): tab[i]="N/A"
                #valz=list(valuesAttr[i])
                x = valuesAttr[i].index(tab[i])
                for k in range(0, len(valuesAttr[i])):    
                	if k == x:
                		s.append("1")
	                else:
                		s.append("?")

            edge=":".join(tab[-2:])
            x = labels.index(edge)
            for i in range(0, len(labels)):
                if i == x:
                    s.append("1")
                else:
                    s.append("?")
	    	#print (len(tab[:-2] + s)) 
            #print (",".join(tab[:-2] + s))
            print (",".join(s))
    	indexLine+=1








