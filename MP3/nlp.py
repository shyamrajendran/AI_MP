from nltk.stem.porter import *
import time
import os

def stem(fileName):
	st = PorterStemmer()
	fileNameStr = fileName.split("/")
	nf = fileNameStr[-1]
	nfName = nf.split('.')
	ofileName = nfName[0]+"_stemmed.txt"
	fo = open(ofileName,'w')
	count = 0 
	with open(fileName) as f:
		for lines in f:
			count+=1
			# if count > 400:
				# break
			toWrite = []
			lineArray = lines.split(' ')
			lineStr = lineArray[1:]
			toWrite.append(lineArray[0])
			
			for keyValuePair in lineStr:
				toStem = keyValuePair.split(':')
				wordToStem = toStem[0]
				keyToAdd = toStem[1]
				stemmedWord = st.stem(wordToStem)
				toWrite.append(" ")
				toWrite.append(stemmedWord)
				toWrite.append(":")
				toWrite.append(keyToAdd)
			# print toWrite	
			fo.write(''.join(toWrite))


fileName1='/Users/sam/AI_MP/MP3/8category/8category_training.txt'
fileName2='/Users/sam/AI_MP/MP3/8category/8category_testing.txt'
stem(fileName1)
stem(fileName2)