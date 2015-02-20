import operator,collections
import sys
import heapq
# import queue
import time
import copy
# import heaq
class Board():
	def __init__(self, x, board_type):
		self.board_array = x
		self.board_type = board_type
		self.path_cost  = 0
		# self.pc = pc
		self.score = 0
		self.cal_score(board_type)

	def cal_score(self, board_type):
		if board_type == "MANHATTAN":
			self.score = self.get_manhattan(self)
		elif board_type == "GASHNIK":
			self.score = self.get_gashnik(self)
		elif board_type == "MISPLACED":
			self.score = self.get_misplaced_tile_score()

	def update_path_cost(self, val):
		self.path_cost+=val

	def get_score(self):
		return self.score

	def __eq__(self, other):
		return self.board_array == other.board_array
	def __hash__(self):
		a=0
		i=0
		for n in self.board_array:
			a+=n*(3**i)
			i+=1
		return hash(a)

	def __eq__(self, other):
		if self.board_type == "MANHATTAN":
			return self.path_cost + self.get_manhattan(self) == other.path_cost + other.get_manhattan(other)
		elif self.board_type == "GASHNIK":
			return self.path_cost + self.get_gashnik(self) == other.path_cost + other.get_gashnik(other)
		elif self.board_type == "MISPLACED":
			return self.get_misplaced_tile_score() ==  other.get_misplaced_tile_score()
	def __lt__(self, other):
		if self.board_type == "MANHATTAN":
			return self.path_cost + self.get_manhattan(self) < other.path_cost + other.get_manhattan(other)
		elif self.board_type == "GASHNIK":
			return self.path_cost + self.get_gashnik(self) < other.path_cost + other.get_gashnik(other)
		elif self.board_type == "MISPLACED":
			return self.get_misplaced_tile_score() < other.get_misplaced_tile_score()
	def __gt__(self, other):
		if self.board_type == "MANHATTAN":
			return self.path_cost + self.get_manhattan(self) > other.path_cost + other.get_manhattan(other)
		elif self.board_type == "GASHNIK":
			return self.path_cost + self.get_gashnik(self) > other.path_cost + other.get_gashnik(other)
		elif self.board_type == "MISPLACED":
			return self.get_misplaced_tile_score() > other.get_misplaced_tile_score()
	def __lt__(self, other):
		if self.board_type == "MANHATTAN":
			return self.path_cost + self.get_manhattan(self) < other.path_cost + other.get_manhattan(other)
		elif self.board_type == "GASHNIK":
			return self.path_cost + self.get_gashnik(self) < other.path_cost + other.get_gashnik(other)
		elif self.board_type == "MISPLACED":
			return self.get_misplaced_tile_score() < other.get_misplaced_tile_score()
		
	def get_path_a_star(self):
		visited_map = {}
		path = []
		backtrack = {}
		node_expanded = 0
		frontier = []
		frontier.append(self)
		while frontier:
			frontier.sort()
			w = frontier[0]
			visited_map[w] = True
			del frontier[0]
			node_expanded+=1
			if node_expanded == 50000 :
				print("EXIT REACHED 50000")
				return false
			if w.is_reached():
				print("FOUND PATH", node_expanded)
				path.append(w)
				expanded = node_expanded


			vv = w.generate_swap_boards()
			for v in vv:
				if v in visited_map:
					continue
				else:
					v.path_cost = w.path_cost + 1
					self.find_and_update(frontier, v, w, backtrack, self.board_type)
		return False


	def find_and_update(self, frontier, v, w, backtrack, t ):
		# print("CHECKING",v.printb())
		# for i in frontier:
			# print i.printb(),"|",
		old_index = -1
		for index, iv in enumerate(frontier):
			if iv.board_array == v.board_array:
				old_index = index
				break
		if old_index == -1:
			frontier.append(v)
			backtrack[v] = w
			return
		total_score_new = v.path_cost + v.score
		total_score_old = frontier[old_index].path_cost + frontier[old_index].get_score()
		if total_score_new < total_score_old:
			frontier[old_index].path_cost = v.path_cost
			backtrack[v] = w

	def generate_swap_boards(self):
		neighbor_index = self.findNeighbours()
		print "SAME",neighbor_index
		boards = []
		init_board = self.printb()
		zero_index = init_board.index(0)
		for i in neighbor_index:
			temp = copy.deepcopy(init_board)
			t = temp[i]
			temp[i] = 0
			temp[zero_index] = t
			boards.append(Board(temp,self.board_type))
		return boards
	

	def get_greedy_manhattan(self, board_list):
		min_path = sys.maxint
		min_board = None
		for i in board_list:
			temp_board = copy.deepcopy(i)
			min_temp = self.get_manhattan(i)
			i.score = min_temp
			if  min_temp < min_path:
				min_board = temp_board
				min_path = min_temp
		return min_board,min_path	

	def getMinGasnik(self, board_list):
		min_path = sys.maxint
		min_board = None
		for i in board_list:
			temp_board = copy.deepcopy(i)
			min_temp = self.get_gashnik(i)
			if  min_temp < min_path:
				min_board = temp_board
				min_path = min_temp
		return min_board,min_path

	def findNeighbours(self):
		source_index = self.board_array.index(0)
		source_col = source_index%3
		source_row = source_index//3
		x = []
        # //down
		x.append((source_col,source_row+1))
        # //up
		x.append((source_col,source_row-1))
        # //right
		x.append((source_col+1,source_row))
        # //left
		x.append((source_col-1,source_row))
		return_index = []
		for col,row in x:
			if row>=0 and row<=2 and col>=0 and col<=2 :
				return_index.append(row*3+col)
		return return_index

	def printb(self):
		return self.board_array
	def print_matrix(self):
		for i in range(3):
			for j in range(3):
				print(self.board_array[i*3+j]),
			print ("")
		print("----")

	def is_reached(self):
		for index, i  in enumerate(self.board_array):
			if index != i:
				return False
		return True
	def find_missed_value(self, skip):
		res = []
		for index,i in enumerate(self.board_array):
			if skip and i == 0:
				continue
			if index != i:
				res.append(i)
		return res
	def get_misplaced_tile_score(self):
		return len(self.find_missed_value(0))

	def get_gashnik(self, board2 ):
		board = copy.deepcopy(board2)
		result = []
		res = board.find_missed_value(1)
		res.sort()
		path_cost = 0
		while not board.is_reached():
			# time.sleep(1)
			# print "RES", res
			# print "BOARD",board.printb()
			path_cost+=1
			zero_index = board.board_array.index(0)
			if zero_index != 0 :
				# assume board 6 3 5 2 1 50 4 8 7 
				# index of 0 = 5
				# index of 5 = 2 
				# swap a[2] and a[5] 
				# remove 5 ; index of 0 initially from res
				# case where the 0 is not in the right place
				to_swap = board.board_array.index(zero_index)
				# print "TOSWAP" , zero_index
				# to_swap contains the index of 0 
				board.board_array[to_swap],board.board_array[zero_index] = board.board_array[zero_index], board.board_array[to_swap]
				del res[res.index(zero_index)]
				
			else:
				# case where 0 is in the right place
				# take first of the res
				# swap 
				checkVal = res[0]
				to_swap = board.board_array.index(checkVal)
				# print "TO SWAP -- ", to_swap
				board.board_array[0] = checkVal
				board.board_array[to_swap] = 0
				# self.board_array[self.board_array.index(0)], self.board_array[to_swap] = to_swap, self.board_array[to_swap]
			# print "--"
		return path_cost

	def get_manhattan(self, board):
		man_score = 0
		for index, i in enumerate(board.board_array):
			des_row = i//3
			des_col = i%3
			cur_row = index//3
			cur_col = index%3
			man_score+=abs(cur_row - des_row) + abs(cur_col - des_col)
		return man_score




B = [1,2,3,5,8,7,4,0,6]
A = [1,0,3,5,2,7,4,8,6]
C = [1,2,3,5,7,0,4,8,6]
D = [1,2,3,5,7,0,4,8,6]

# D = [1,3,5,2,7,0,4,8,6]

board_type = "MANHATTAN"
a = Board(A,board_type)	
print a.get_path_a_star()

# # print a.get_path_a_star()
# b = Board(B,board_type)
# c = Board(C,board_type)
# d = Board(D,board_type)

# # print(a.get_manhattan(a))
# # print(b.get_manhattan(b))
# # print(c.get_manhattan(c))
# # print(d.get_manhattan(d))

# a_list =[]
# a_list.append(a)
# a_list.append(b)
# a_list.append(c)
# # a_list.append(d)

# print a_list.index(d)




# for i in a_list:
# 	print("LIST", i.pc)

# print("---")


# a_list.sort()
# print("---")
# for i in a_list:
# 	print("AFTER", i.pc)


# print a_list.index(d)

# a_heap = heapq.heapify(a_list)
# print "--"
# a = len(a_list)
# for i in range(a):
# 	i = heapq.heappop(a_list)
# 	print i.printb()
# a.print_matrix()
# # a.gashnik(a)
# board_list = a.generate_swap_boards()
# min_board = a.getMinGasnik(board_list)
# print min_board[0].print_matrix()
# print min_board[1]





