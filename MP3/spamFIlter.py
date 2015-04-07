def find_spam_prob(training_file):
	not_spam = 0
	spam = 0
	with open(training_file) as f:
		for lines in f:
			# print lines
			flag = lines.split(' ')[0]

			if  flag == 0:
				not_spam+=1
				print flag
			else:
				spam+=1
				print "ONE" +flag
	return spam,not_spam

email_file='spam_detection/train_email.txt'
print find_spam_prob(email_file)	
