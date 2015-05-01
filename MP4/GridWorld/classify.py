path = "/home/manshu/Templates/EXEs/team_retinaa/AI_MP/MP4/GridWorld/digitdata/"
training_data_file = open(path + "trainingimages")
training_labels_file = open(path + "traininglabels")
test_data_file = open(path + "testimages")
test_labels_file = open(path + "testlabels")

TRAINING_IMAGES = 5000
TEST_IMAGES = 1000
ROW_SIZE = 28
COL_SIZE = 28

def giveData(data_file, data_labels, NUM_DATA):
	lines = data_file.readlines()
	label_lines = data_labels.readlines()

	data_vector = []
	labels = []
	for image_num in range(0, NUM_DATA):
		image_data = []
		for row in range(0, ROW_SIZE):
			line = lines[image_num * ROW_SIZE + row]
			for col in range(0, COL_SIZE):
				if line[col] != ' ':
					image_data.append(1)
				else:
					image_data.append(0)
		data_vector.append(image_data)
		labels.append(label_lines[image_num])

	return (data_vector, labels)

training_data, training_label = giveData(training_data_file, training_labels_file, TRAINING_IMAGES)
test_data, test_label = giveData(test_data_file, test_labels_file, TEST_IMAGES)

from sklearn import svm

clf = svm.SVC()
clf = clf.fit(training_data, training_label)

predicted_label = clf.predict(test_data)

num_mismatch = 0
for i in range(0, len(test_label)):
	if test_label[i] != predicted_label[i]:
		num_mismatch += 1

percentage_accuracy = 100.0 - (100.0 * num_mismatch) / len(test_label)
print "SVM Accuracy is " + str(percentage_accuracy)


from sklearn import tree

clf = tree.DecisionTreeClassifier()
clf = clf.fit(training_data, training_label)
predicted_label = clf.predict(test_data)

num_mismatch = 0
for i in range(0, len(test_label)):
	if test_label[i] != predicted_label[i]:
		num_mismatch += 1

percentage_accuracy = 100.0 - (100.0 * num_mismatch) / len(test_label)
print "Decision Tree Accuracy is " + str(percentage_accuracy)
