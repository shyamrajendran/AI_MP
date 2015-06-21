
1. Compile
javac *.java

2. For searching in *Maze.txt files which contain single goal, to run all the algorithms use
java MazeSearch

3. For multiple goals, run
java EatAllDots "trickySearch.txt"
java EatAllDots "smallSearch.txt"
java EatAllDots "mediumSearch.txt"
java EatAllDots "bigSearch.txt"


Java Files
1. Point class -> holds x and y coordinates of maze
2. MyPriorityQueue class -> Generic Custom built Priority class which gives the flexibility to maintain priortiy queue as per user demands. Fast supported operations like getContent, contains etc.
3. PriorityQueueWrapper class -> Generic Custom built wrapper over MyPriorityQueue which enables the queue to be used as stack, queue, or a priority_queue depending upon user needs.
4. PacmanGrid class -> Class which can be used to create a gui for custom maze of pacman. Enables client to set pacman location and use it to display on the grid.
5. Node class -> This contains the state of pacman for the *Maze.txt files. Contains cost in a state and the point coordinates of the state.
6. MazeSearch class -> Main class that implements the search algorithm using all the above classes.
7. DisjointSet class -> Custom implementation of DisjointSet which is used by Kruskal's Algorithm.
8. Edge class -> Custom class to represent distances as edges, used by Kruskal's Algorithm.
9. AstarData class -> Contains Path and Cost returned after running Astar.
10. TSPForPacman class -> Solves the travelling salesman problem using Astar and MST heuristic to calculate the order in which goals should be visited.
11. EatAllDots class -> Main class that implements optimal and suboptimal search algorithms when there are multiple goals. Uses TSPForPacman class to find the order in which goals are to be visited.
