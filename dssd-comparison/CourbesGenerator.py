import time,random
import matplotlib.pyplot as plt
import os
from os import listdir
def getDirName(directory) :
    dirpath=os.path.abspath(directory)
    splitedDirPath=dirpath.strip().split("/")
    return splitedDirPath[len(splitedDirPath)-1]
def getMedian(inputList) :
    sortedList=sorted(inputList)
    if len(sortedList)==0 :
        return 0
    else :
        return sortedList[int(len(sortedList)/2)]

def get2DMedianScore(fastRabbitResultFolder):
    contextScores=[]
    verticeScores=[]
    scores=[]
    with open(fastRabbitResultFolder+"/qualityScores.txt") as f:
        for line in f:
            elements=line.strip().split(",")
            scores.append((float(elements[0])+float(elements[1]))/2)
    return getMedian(scores)



def getMedianTimeAndPatterns(sourcePath) :
    repetitionsFolders=listdir(sourcePath)
    timeValues=[]
    retrievedPatternsValues=[]
    scoresValues=[]
    for repetitionsFolder in repetitionsFolders :
        currentDirName=getDirName(repetitionsFolder)
        with open(sourcePath+"/"+currentDirName+"/FastRabbit elements/resultIndicatorsFile.txt") as f:
                for line in f:
                    if "execution time (s)" in line :
                        timeValues.append(float(line.strip().split(":")[1][1:]))
                    elif "number of patterns" in line :
                        retrievedPatternsValues.append(float(line.strip().split(":")[1][1:]))
        scoresValues.append(get2DMedianScore(sourcePath+"/"+currentDirName+"/FastRabbit elements"))
    #meanTimeValue=float(sum(timeValues))/float(len(timeValues))
    #meanRetrievedPatternsValues=float(sum(retrievedPatternsValues))/float(len(retrievedPatternsValues))
    timeValues=sorted(timeValues)
    meanTimeValue=timeValues[int(len(timeValues)/2)]
    retrievedPatternsValues=sorted(retrievedPatternsValues)
    meanRetrievedPatternsValues=retrievedPatternsValues[int(len(retrievedPatternsValues)/2)]

    scoresValues=sorted(scoresValues)
    meanScoresValue=scoresValues[int(len(scoresValues)/2)]
                            
    
    return [meanTimeValue,meanRetrievedPatternsValues,meanScoresValue]      
        

def drawScoreGraphics(sourcePath,testGroupFolder):
    splitName=testGroupFolder.split(" ")
    nameOfParameter=splitName[len(splitName)-1]
    #if (os.path.isdir(os.path.abspath(testGroupFolder))) :
    #    print os.path.abspath(testGroupFolder)
    #else :
    #    print "not a folder"
    #    print os.path.abspath(testGroupFolder)
    ffTimeList=[]
    ffRetrievedPatternsList=[]
    scoresValuesList=[]
    xValues=[]
    dirName=getDirName(testGroupFolder)
    testCasesFolders=listdir(sourcePath+"/"+dirName)
    
    with open(sourcePath+"/"+dirName+"/testedValues.txt") as f:
        line=f.readline()
        line = line.strip().split(",")
        xValues=map(float,line)
    minValue=xValues[0]
    addToXlabel=""
    intermedList=[]
    for value in xValues :
        if minValue>value:
            minValue=value
    print minValue
    if (minValue>100000) :
        addToXlabel=" (x1000000)"
        for value in xValues :
            intermedList.append(float(value/1000000))
        xValues=intermedList
    elif (minValue>1000) :
        addToXlabel=" (x1000)"
        for value in xValues :
            intermedList.append(int(value/1000))
        xValues=intermedList
    for testCaseFolder in testCasesFolders :
        currentDirName=getDirName(testCaseFolder)
        if (os.path.isdir(sourcePath+"/"+dirName+"/"+currentDirName) and ("Test" in currentDirName)) :
            [meanTime,meanRetrievedPatterns,meanScoresValue]=getMedianTimeAndPatterns(sourcePath+"/"+dirName+"/"+currentDirName)
            ffTimeList.append(meanTime)
            ffRetrievedPatternsList.append(meanRetrievedPatterns)
            scoresValuesList.append(meanScoresValue)
    if (not os.path.exists(sourcePath+"/"+dirName+"/courbesGraphs")) :
        os.makedirs(sourcePath+"/"+dirName+"/courbesGraphs")    
    plt.figure(1)
    plt.clf()
    #plt.xlim([xValues[0],xValues[len(xValues)-1]])
    plt.xlim([0,xValues[len(xValues)-1]])
    #if (max(ffTimeList)-min(ffTimeList)>10) :
     #   plt.ylim(min(ffTimeList),max(ffTimeList))
    #else :
      #  plt.ylim(min(ffTimeList)-5,min(ffTimeList)+5)
    #if "nb_vertices" in nameOfParameter :
    #    plt.ylim(0,250)
    #else :
    plt.ylim(0,max(ffTimeList)+(max(ffTimeList)-min(ffTimeList))*0.2)

    if "nb_vertices" in nameOfParameter :
        nameOfParameter="number of vertices"
    elif "nb_trans" in nameOfParameter :
        nameOfParameter="number of transactions"
    elif "DesignPoint" in nameOfParameter :
        nameOfParameter="replication factor"


    plt.plot(xValues,ffTimeList,color="blue", linewidth=2.5,marker='o',markersize=10)
    plt.xlabel(nameOfParameter+addToXlabel,fontsize=20)
    plt.ylabel("time (s)",fontsize=20)
    plt.tick_params(axis='x', labelsize=20)
    plt.tick_params(axis='y', labelsize=20)
    plt.grid()
    
    plt.gcf().tight_layout()
    plt.savefig(sourcePath+"/"+dirName+"/courbesGraphs/time.png")
    plt.close()

    plt.figure(1)
    plt.clf()
    #plt.xlim([xValues[0],xValues[len(xValues)-1]])
    plt.xlim([0,xValues[len(xValues)-1]])
    plt.ylim(0,max(ffRetrievedPatternsList)+(max(ffRetrievedPatternsList)-min(ffRetrievedPatternsList))*0.2)
    #if (max(ffRetrievedPatternsList)-min(ffRetrievedPatternsList)>10) :
     #   plt.ylim(min(ffRetrievedPatternsList),max(ffRetrievedPatternsList))
    #else :
      #  plt.ylim(min(ffRetrievedPatternsList)-5,min(ffRetrievedPatternsList)+5)
    plt.plot(xValues,ffRetrievedPatternsList,color="blue", linewidth=2.5,marker='o',markersize=10)
    plt.xlabel(nameOfParameter+addToXlabel,fontsize=20)
    plt.ylabel("# patterns",fontsize=20)
    plt.tick_params(axis='x', labelsize=20)
    plt.tick_params(axis='y', labelsize=20)
    plt.grid()
    plt.gcf().tight_layout()
    plt.savefig(sourcePath+"/"+dirName+"/courbesGraphs/retrievedPatterns.png")
    plt.close()

    plt.figure(1)
    plt.clf()
    fig, ax1 = plt.subplots()
    ax2 = ax1.twinx()
    ax1.plot(xValues,ffRetrievedPatternsList,color="blue",linewidth=2.5,marker='o',markersize=10)
    ax2.plot(xValues,scoresValuesList,color="red",linewidth=2.5,marker='v',markersize=10)
    ax1.set_xlabel(nameOfParameter+addToXlabel,fontsize=20)
    #ax1.set_ylabel('number of retrieved patterns', color='blue')
    #ax2.set_ylabel('median score', color='red')
    ax1.legend(["# patterns"], loc='upper left',fontsize=20)
    ax2.legend(["median score"], loc='upper right',fontsize=20)
    plt.tick_params(axis='x', labelsize=20)
    plt.tick_params(axis='y', labelsize=20)
    ax1.tick_params(axis='y', labelsize=20)
    ax1.tick_params(axis='x', labelsize=20)
    ax2.tick_params(axis='y', labelsize=20)
    ax2.tick_params(axis='x', labelsize=20)
    
    plt.xlim([0,xValues[len(xValues)-1]])
    ax1.set_ylim(0,max(ffRetrievedPatternsList)+max(ffRetrievedPatternsList)*0.3)
    ax2.set_ylim(0,1.1)
    #plt.grid()
    ax1.grid()
    plt.gcf().tight_layout()
    plt.savefig(sourcePath+"/"+dirName+"/courbesGraphs/retrivedPattersAndScores.png")
    plt.close()
    
def main(sourcePath="Tests") :

    staringTime=time.time()
    testGroupsFolders=listdir(sourcePath)
    i=0
    for testGroupFolder in testGroupsFolders :
        i+=1
        print "folder"
        print i
        drawScoreGraphics(sourcePath,testGroupFolder)
    #with open(sourcePath) as f:
    #    for line in f:
    #        line = line.strip().split(",")

    
    elapsed_time=time.time()-staringTime
    print "-"*50
    print "End",
    print "(Elapsed time : {0}s).".format(elapsed_time)
    print "-"*50




    #plt.figure(1)
        #plt.clf()
        #plt.plot(range(200),hmmDistancesSum,".")
        
        #plt.xlabel("trace")
        #plt.boxplot(hmmDistancesSum)
        
        #plt.ylabel("similarite")
        #plt.show()
    
main()

