import glob
from random import shuffle
import operator

MAX_LENGTH = 100

class PreprocessData:

	def __init__(self, dataset_type='wsj', model_type_=0):
		self.vocabulary = {}
		self.pos_tags = {}
		self.dataset_type = dataset_type
		self.model_type = model_type_
		# List of most common suffixes
		self.suffix_list = ['acy', 'al', 'nce', 'dom', 'nce', 'er', 'or', 'ism', 'ist', 'ty',
							'ment', 'ness', 'ship', 'ion', 'ate', 'en', 'fy', 'ize', 'ise', 'ble', 'al',
							'al', 'esque', 'ful', 'ic', 'ical', 'ous', 'ish', 'ive', 'less', 'y', 'ship',
							'ary', 'hood', 'age', 'logy', 'ing', 's', 'es']


	## Get standard split for WSJ
	def get_standard_split(self, files):
		if self.dataset_type == 'wsj':
			train_files = []
			val_files = []
			test_files = []
			for file_ in files:
				partition = int(file_.split('/')[-2])
				if partition >= 0 and partition <= 18:
					train_files.append(file_)
				elif partition <= 21:
					val_files.append(file_)
				else:
					test_files.append(file_)
			return train_files, val_files, test_files
		else:
			raise Exception('Standard Split not Implemented for '+ self.dataset_type)

	@staticmethod
	def isFeasibleStartingCharacter(c):
		unfeasibleChars = '[]@\n'
		return not(c in unfeasibleChars)

	## unknown words represented by len(vocab)
	def get_unk_id(self, dic):
		return len(dic)

	def get_pad_id(self, dic):
		return len(dic) + 1

	## get id of given token(pos) from dictionary dic.
	## if not in dic, extend the dic if in train mode
	## else use representation for unknown token
	def get_id(self, pos, dic, mode):
		if pos not in dic:
			if mode == 'train':
				dic[pos] = len(dic)
			else:
				return self.get_unk_id(dic)
		return dic[pos]

	## get the appropriate suffix
	def containsSuffix(self, word):
		for i in range(0, len(self.suffix_list)):
			if word.lower().endswith(self.suffix_list[i]):
				return i;
		return -1

	def containsHyphen(self, word):
		if '-' in word:
			return 1
		return -1

	def startsWithDigit(self, word):
		if word and word[0].isdigit():
			return 1
		return -1

	def startsWithCapital(self, word):
		if word and word[0].isupper():
			return 1
		return -1

	## Process single file to get raw data matrix
	def processSingleFile(self, inFileName, mode):
		matrix = []
		row = []
		with open(inFileName) as f:
			lines = f.readlines()
			for line in lines:
				line = line.strip()
				if line == '':
					pass
				else:
					tokens = line.split()
					for token in tokens:
						## ==== indicates start of new example					
						if token[0] == '=':
							if row:
								matrix.append(row)
							row = []
							break
						elif PreprocessData.isFeasibleStartingCharacter(token[0]):
							wordPosPair = token.split('/')
							if len(wordPosPair) == 2:
								## get ids for word and pos tag
								feature = self.get_id(wordPosPair[0], self.vocabulary, mode)
								if self.model_type != 0:
									# Check for suffix
									suffix = self.containsSuffix(wordPosPair[0])
									# Check for Capital Letter Noun
									startsWithCapital = self.startsWithCapital(wordPosPair[0])
									# Check if word starts with a digit
									startsWithDigit = self.startsWithDigit(wordPosPair[0])
									# Check if word contains hyphen
									hyphen = self.containsHyphen(wordPosPair[0])
									# include all pos tags.
									row.append((feature, suffix, startsWithCapital, startsWithDigit, hyphen, self.get_id(wordPosPair[1],
												self.pos_tags, 'train')))
								else:
									row.append((feature, self.get_id(wordPosPair[1],
												self.pos_tags, 'train')))
		if row:
			matrix.append(row)
		return matrix


	## get all data files in given subdirectories of given directory
	def preProcessDirectory(self, inDirectoryName, subDirNames=['*']):
		if not(subDirNames):
			files = glob.glob(inDirectoryName+'/*.pos')
		else:
			files = [glob.glob(inDirectoryName+ '/' + subDirName + '/*.pos')
					for subDirName in subDirNames]
			files = set().union(*files)
		return list(files)


	## Get basic data matrix with (possibly) variable sized senteces, without padding
	def get_raw_data(self, files, mode):
		matrix = []
		for f in files:
			matrix.extend(self.processSingleFile(f, mode))
		return matrix

	def split_data(self, data, fraction):
		split_index = int(fraction*len(data))
		left_split = data[:split_index]
		right_split = data[split_index:]
		if not(left_split):
			raise Exception('Fraction too small')
		if not(right_split):
			raise Exception('Fraction too big')
		return left_split, right_split

	## Get rid of sentences greater than max_size
	## and pad the remaining if less than max_size
	def get_processed_data(self, mat, max_size):
		X = []
		y = []
		list1 = [0,1,2,3,4,5]
		my_items = operator.itemgetter(*list1)

		original_len = len(mat)
		mat = filter(lambda x: len(x) <= max_size, mat)
		no_removed = original_len - len(mat)

		vocabulary_pad_id = self.get_pad_id(self.vocabulary)
		
		suffix_pad_id = self.get_pad_id(self.suffix_list)
		startsWithDigit_pad_id = 2
		startsWithCapital_pad_id = 2
		hyphen_pad_id = 2

		for row in mat:
			if self.model_type != 0:
				# X_row = [tup[0] for tup in row]
				X_row = [my_items(tup) for tup in row]
				y_row = [tup[5] for tup in row]
				## padded words represented by len(vocab) + 1
				# X_row = X_row + [self.get_pad_id(self.vocabulary)]*(max_size - len(X_row))
				X_row = X_row + [[vocabulary_pad_id, suffix_pad_id, startsWithDigit_pad_id, startsWithCapital_pad_id, hyphen_pad_id]]*(max_size - len(X_row))
				## Padded pos tags represented by -1
				y_row = y_row + [-1]*(max_size-len(y_row))
				X.append(X_row)
				y.append(y_row)
			else:
				X_row = [tup[0] for tup in row]
				y_row = [tup[1] for tup in row]
				## padded words represented by len(vocab) + 1
				X_row = X_row + [self.get_pad_id(self.vocabulary)]*(max_size - len(X_row))
				## Padded pos tags represented by -1
				y_row = y_row + [-1]*(max_size-len(y_row))
				X.append(X_row)
				y.append(y_row)
		return X, y, no_removed
