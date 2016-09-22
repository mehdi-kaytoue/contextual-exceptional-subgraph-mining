import time,random
import matplotlib.pyplot as plt
import os
from os import listdir
def getDirName(directory) :
    dirpath=os.path.abspath(directory)
    splitedDirPath=dirpath.strip().split("\\")
    return splitedDirPath[len(splitedDirPath)-1]

def drawScoreGraphics(fastRabbitResultFolder):
    contextScores=[]
    verticeScores=[]
    with open(fastRabbitResultFolder+"\\qualityScores.txt") as f:
        for line in f:
            elements=line.strip().split(",")
            contextScores.append(float(elements[0]))
            verticeScores.append(float(elements[1]))
    plt.figure(figsize=(6, 5))
    plt.clf()
    plt.xlim([0,1.1])
    plt.ylim([0,1.1])
    plt.xticks([0,0.2,0.4,0.6,0.8,1])
    plt.yticks([0,0.2,0.4,0.6,0.8,1])   
    plt.plot(contextScores,verticeScores,'ro',alpha=0.3,markersize=12)
    plt.xlabel("Sc",fontsize=24)
    plt.ylabel("Sv",fontsize=24)
    
    plt.tick_params(axis='x', labelsize=24)
    plt.tick_params(axis='y', labelsize=24)
  
    #plt.title("weight="+str(params[0])+" link_prob="+str(params[1])+" noise_rate="+str(params[2]),fontsize=22)
    #plt.title("noise_rate="+str(params[2]),fontsize=24)
    plt.gcf().tight_layout()
    plt.savefig(fastRabbitResultFolder+"\\scatterPlot.png")
    plt.close()




    
def main3(sourcePath="Tests") :

    staringTime=time.time()
    testGroupsFolders=listdir(sourcePath)
    i=0
    for testGroupFolder in testGroupsFolders :
        i+=1
        print ("folder")
        print (i)
        groupFolderName=getDirName(testGroupFolder)
        testCaseFolders=listdir(sourcePath+"\\"+groupFolderName)
        for testCaseFolder in testCaseFolders :
            caseFolderName=getDirName(testCaseFolder)
            if "Tests" in caseFolderName :
                testRepetitionFolders=listdir(sourcePath+"\\"+groupFolderName+"\\"+caseFolderName)
                for testRepetitionFolder in testRepetitionFolders :
                    repetitionFolderName=getDirName(testRepetitionFolder)
                    if ("rep" in repetitionFolderName) :
                        drawScoreGraphics(sourcePath+"\\"+groupFolderName+"\\"+caseFolderName+"\\"+repetitionFolderName+"\\FastRabbit elements")                        
        
  
    
    elapsed_time=time.time()-staringTime
    print ("-"*50)
    print ("End",)
    print ("(Elapsed time : {0}s).".format(elapsed_time))
    print ("-"*50)
main3()

