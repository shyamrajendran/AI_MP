#include <algorithm>
#include<iostream>
#include<map>
#include<vector>
#include<list>
#include<set>
#include <cstdlib>
#include <set>
#include <random>
#include <iostream>     // std::cout
#include <array>        // std::array
using namespace std;



#define BOARD_SIZE 3
typedef enum boardScoreType {
    MANHATTAN,
    MISPLACED_TILES,
    GASHNIG,
    UNKNOWN
} boardType;




class point {
public:
    int x;
    int y;
    point() {
        x = -1;
        y = -1;
    }

    point(int a, int b) : x(a),y(b) {}

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }
    bool isInit() {
        if (x == -1 && y ==  -1) return false;
        else return true;
    }
    void setXY(int x, int y) {
        this->x = x;
        this->y = y;
    }

    void print() {
        cout<<"("<<x<<","<<y<<")";
    }
    bool operator==(const point &other) const {
        if (this->x == other.x && this->y == other.y) return true;
        return false;
    }
    
    bool operator<(const point &other) const {
        if (this->x == other.x && this->y != other.y) return this->y < other.y;
        else return this->x < other.x;
    }
};

/*
    puzzleboard(int *a);
    puzzleboard(const puzzleboard& o);
    bool serialize(int *a);
    void initGoalBoard();
    void printBoard();
    void printGoal();
    void printBoardStats();
    int getMisplacedDist();
    int getScore(boardType t);
    int getManhattanDist();
    point findBlank();
    int getGashnig();
    void printNextMoves();
    bool isWithinBounds(point& p);
    vector<puzzleboard> generateNextMoves();
*/


class puzzleboard {
    void swapPoints(point& p1, point& p2) {
        int temp = board[p1.y][p1.x];
        board[p1.y][p1.x] = board[p2.y][p2.x];
        board[p2.y][p2.x] = temp;
    }

    public:
    int board[BOARD_SIZE][BOARD_SIZE];
    int goal[BOARD_SIZE][BOARD_SIZE];
    int pathcost;
    puzzleboard(int *a) {
        pathcost = 0;
        initGoalBoard();
        //default type is misplaced tiles
        int row = 0, col = 0;
        for (int i = 0; i < (BOARD_SIZE * BOARD_SIZE); i++) {
            row = i / BOARD_SIZE;
            col = i % BOARD_SIZE;
            board[row][col] = a[i];
        }
    }

    puzzleboard(const puzzleboard& o) {
        pathcost = o.pathcost;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                this->board[i][j] = o.board[i][j];
                this->goal[i][j] = o.goal[i][j];
            }
        }
    }

    int getPathCost() {
        return pathcost;
    }
    
    void setPathCost(int cost) {
        this->pathcost = cost;
    }
    
    // array is expected to have enough space
    bool serialize(int *a) {
        if (a == NULL) return false;
        int count = 0;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                a[count++] = board[i][j];
            }
        }
        return true;
    }

    void initGoalBoard() {
        int count = 0;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                goal[i][j] = count++;
            }
            cout<<endl;
        }
    }

    void printBoard() {
        cout<<"current board:"<<endl;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                cout<<"["<<board[i][j]<<"]";
            }
            cout<<endl;
        }
        cout<<endl;
    }

    void printGoal() {
        cout<<"goal board:"<<endl;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                cout<<"["<<goal[i][j]<<"]";
            }
            cout<<endl;
        }
        cout<<endl;
    }

    void printBoardStats() {
        printBoard();
        printGoal();
    }

    int getMisplacedDist() const{
        int dist = 0;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (board[i][j] != 0 && board[i][j] != goal[i][j]) {
                    dist++;
                }
            }
        }
        return dist;
    }

    int getManhattanDist() const {
        return 0;
    }

    int getGashnig() const {
        return 0;
    }

    int getScore(boardType t) const {
        switch (t) {
        case MANHATTAN:
            return getManhattanDist();
            break;
        case MISPLACED_TILES:
            return getMisplacedDist();
            break;
        case GASHNIG:
            return getGashnig(); 
            break;
        default:
            return 0;
        }
    }

    // puzzle board must be of same size
    bool operator==(const puzzleboard &other) const {
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (this->board[i][j] != other.board[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    

    bool operator<(const puzzleboard &other) const {
        
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (this->board[i][j] != other.board[i][j]) {
                    return this->board[i][j] < other.board[i][j];
                }
            }
        }

        return true;
    }

    point findBlank() {
        point p;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (this->board[i][j] == 0) {
                    p.setXY(j, i);
                    return p;
                }
            }
        }
        return p;
    }

    bool isWithinBounds(point& p) {
        if (p.x >= BOARD_SIZE || p.y >= BOARD_SIZE || p.x < 0 || p.y < 0) return false;
        return true;
    }

    vector<puzzleboard> generateNextMoves() {
        vector<puzzleboard> l;
        puzzleboard b(*this);
        point blank;
        blank = b.findBlank();

        point pp[4];
        pp[0].setXY(blank.x, blank.y - 1);
        pp[1].setXY(blank.x - 1, blank.y);
        pp[2].setXY(blank.x + 1, blank.y);
        pp[3].setXY(blank.x, blank.y + 1);
        for (int i = 0; i < 4; i++) {
            if (isWithinBounds(pp[i])) {
                b.swapPoints(pp[i], blank);
                l.push_back(b);
                b.swapPoints(pp[i], blank);
            }
        }
        return l;
    }
    
    
    void printNextMoves() {
        vector<puzzleboard> l = generateNextMoves();
        cout<<"Next Moves for current board:"<<endl;
        for (auto i : l) {
            i.printBoard();
        }
    }
    
    bool isTarget() {
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (board[i][j] != goal[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
};

void findandUpdate(list<puzzleboard>& frontier,
                   puzzleboard& v,
                   puzzleboard& src,
                   vector<pair<puzzleboard, puzzleboard> >& backtrack,
                   boardScoreType t) {

    list<puzzleboard>::iterator it = frontier.begin();
    while (it != frontier.end()) {
        if (v == *it) {
            int totalscoreNew, totalscoreOld;
            totalscoreNew = v.getPathCost() + v.getScore(t);
            totalscoreOld = it->getPathCost() + it->getScore(t);
            if (totalscoreNew < totalscoreOld) {
                frontier.erase(it);
                frontier.push_back(v);
                /*std::map<puzzleboard, puzzleboard>::iterator it1 = backtrack.find(v);
                find(backtrack.begin(), backtrack.end(),)
                backtrack.erase(it1);
                backtrack.insert(std::pair<puzzleboard, puzzleboard>(v, src));*/
            }
            return;
        }
        it++;
    }
    // i.e. node not in frontier
    frontier.push_back(v);
    //backtrack.insert(std::pair<puzzleboard, puzzleboard>(v, src));
}

void sortFrontier(list<puzzleboard>& frontier, boardScoreType t) {
    switch (t) {
        case MISPLACED_TILES:
            frontier.sort([](puzzleboard& b1, puzzleboard& b2) {
                return b1.getMisplacedDist() + b1.getPathCost() < b2.getMisplacedDist() + b2.getPathCost();
            });
            break;
        case MANHATTAN:
            frontier.sort([](puzzleboard& b1, puzzleboard& b2) {
                return b1.getManhattanDist() + b1.getPathCost() < b2.getManhattanDist() + b2.getPathCost();
            });
            break;
        case GASHNIG:
            frontier.sort([](puzzleboard& b1, puzzleboard& b2) {
                return b1.getManhattanDist() + b1.getPathCost() < b2.getManhattanDist() + b2.getPathCost();
            });
            break;
        default:
            break;
    }
}


bool getPathAstar(puzzleboard s,
                list<puzzleboard>& frontier,
                vector<puzzleboard>& path,
                vector<std::pair<puzzleboard, puzzleboard> >& backtrack,
                vector<puzzleboard>& visited,
                boardScoreType t) {
    
    int nodesExpanded = 0;
    frontier.push_back(s);
    while(!frontier.empty()) {
        sortFrontier(frontier, t);
        puzzleboard w = frontier.front();
        frontier.pop_front();
        nodesExpanded++;
        if (find(visited.begin(), visited.end(), w) == visited.end()) {
            visited.push_back(w);
        }
        
        if (w.isTarget()) {
            path.push_back(w);
            //std::map<puzzleboard, puzzleboard>::iterator it = backtrack.find(w);
            /*vector<pair<puzzleboard, puzzleboard> >::iterator it = find(backtrack.begin(), backtrack.end(), w);
            while(it != backtrack.end()) {
                //cout<<"found";
                path.push_back(it->second);
                //it = backtrack.find(it->second);
                it = find(backtrack.begin(), backtrack.end(), it->second);
            }*/
            return true;
        }

        //get next moves
        vector<puzzleboard> vv = w.generateNextMoves();
        for (auto v : vv) {
            if (find(visited.begin(), visited.end(), v) == visited.end()) {
                v.setPathCost(w.getPathCost() + 1);
                findandUpdate(frontier, v, w, backtrack, t);
            }
        }
    }
    return false;
}

void printPath(vector<puzzleboard> p) {
    cout<<"solution path is :"<<endl;
    for (auto i : p) {
        i.printBoard();
    }
}

void printPathAstar(puzzleboard start, boardScoreType t) {
    vector<puzzleboard> visited;
    vector<puzzleboard> path;
    vector<pair<puzzleboard, puzzleboard> > backtrack;
    list<puzzleboard> frontier;
    if (getPathAstar(start,
                     frontier,
                     path,
                     backtrack,
                     visited, t)) {
        cout<<"Astar path Found:length="<<path.size()<<endl;
        printPath(path);
        //overlayPathOnMap(p);
    } else {
        cout<<"no path"<<endl;
    }
}

bool findInVector(vector<vector<int>> v, vector<int> s){
    vector<vector<int>>::iterator it;
//    for (std::vector<vector<int>>::iterator it=v.begin(); it!=v.end(); ++it){
//        for (std::vector<int>::iterator itt=(*it).begin(); itt!=(*it).end(); ++itt){
//            cout << *itt;
//        }
//
//    }
    
    it = find (v.begin(), v.end(), s);
    if (it == v.end()){
        return false;
    }else{
        return true;
    }
}
void printVector(vector<int> v){
    for (std::vector<int>::iterator it=v.begin(); it!=v.end(); ++it){
                cout << *it << " ";
    }
}
vector<puzzleboard> generateRandomBoards(puzzleboard base, short const number=50){
    vector<puzzleboard> result;
    vector<vector<int>> alreadyGeneratedList;
    vector<int> l;
    int some_array[10];
    int i=0 ;
    while(l.size() != 9){
        l.push_back(i++);
        
    }
    for(int j = 1;j<=number;j++){
        random_shuffle ( l.begin(), l.end() );
        if(findInVector(alreadyGeneratedList, l)){
            j--;
            continue;
        }else{
            i=0;
            for (std::vector<int>::iterator it=l.begin(); it!=l.end(); ++it,i++){
                some_array[i] = *it;
            }
            result.push_back(puzzleboard(some_array));
            alreadyGeneratedList.push_back(l);
          
        }
       
    }
    return result;
}

int main() {
    int a[] = {1,2,3,4,0,5,6,7,8};
    puzzleboard p(a);
    p.printBoardStats();
    cout<<"dis from goal"<<p.getMisplacedDist()<<endl;
    //std::map<puzzleboard, puzzleboard> m;
    //p.printNextMoves();
    printPathAstar(p, MISPLACED_TILES);
    p.printNextMoves();
    
    vector<puzzleboard> listOfBoards = generateRandomBoards(p);
    cout << listOfBoards.size();
    int i = 1;
    for(std::vector<puzzleboard>::iterator it = listOfBoards.begin(); it != listOfBoards.end(); ++it,i++) {
        cout << "RANDOM BOARD " << i << endl;
        (*it).printBoard();
    }
    
}
