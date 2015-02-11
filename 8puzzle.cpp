#include <algorithm>
#include<iostream>
#include<map>
#include <math.h>
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
    
    int getManhattan(point p) {
        return abs(this->x - p.x) + abs(this->y - p.y);
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
        //update balnk if needed
        if (isBlank(p1)) {
            blank = p1;
        } else if (isBlank(p2)) {
            blank = p2;
        }
    }

    public:
    
    int board[BOARD_SIZE][BOARD_SIZE];
    int goal[BOARD_SIZE][BOARD_SIZE];
    int pathcost;
    point blank;
    puzzleboard(int *a) {
        pathcost = 0;
        initGoalBoard();
        //default type is misplaced tiles
        int row = 0, col = 0;
        for (int i = 0; i < (BOARD_SIZE * BOARD_SIZE); i++) {
            row = i / BOARD_SIZE;
            col = i % BOARD_SIZE;
            board[row][col] = a[i];
            if(a[i] == 0) blank.setXY(col,row);
        }
    }

    puzzleboard(const puzzleboard& o) {
        pathcost = o.pathcost;
        blank = o.blank;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                this->board[i][j] = o.board[i][j];
                this->goal[i][j] = o.goal[i][j];
            }
        }
    }
    int getHashCode() const {
        int power = 0;
        int result=0;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                result+=board[i][j]*pow(3.0, power);
                power++;
               
            }
        }
        return result;
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

    // solvability: grid with odd width && even no inversion
    
    bool isSolvable() {
        int a[BOARD_SIZE * BOARD_SIZE];
        serialize(a);
        vector<int> v;
        for (auto i : a) {
            if (i != 0)
                v.push_back(i);
        }
        int inv = 0;
        for (int i = 0; i < v.size() - 1;i++) {
            for (int j = i + 1; j < v.size();j++) {
                    if (v[j] < v[i]) {
                        inv++;
                    }
            }
        }

        return (inv % 2 == 0) ? true : false;
    }
    
    void initGoalBoard() {
        int count = 1;
        int size = BOARD_SIZE * BOARD_SIZE;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                goal[i][j] = count % size;
                count++;
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
        // say we know the goal board
        point goal,cur;
        int sum = 0;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                switch(board[i][j]) {
                    case 0:
                        goal.setXY(2,2);
                        break;
                    case 1:
                        goal.setXY(0,0);
                        break;
                    case 2:
                        goal.setXY(1,0);
                        break;
                    case 3:
                        goal.setXY(2,0);
                        break;
                    case 4:
                        goal.setXY(0,1);
                        break;
                    case 5:
                        goal.setXY(1,1);
                        break;
                    case 6:
                        goal.setXY(2,1);
                        break;
                    case 7:
                        goal.setXY(0,2);
                        break;
                    case 8:
                        goal.setXY(1,2);
                        break;
                }
                cur.setXY(j,i);
                sum +=cur.getManhattan(goal);
            }
        }
        return sum;
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
//        cout << "THIS";
//        this->printBoard();
//        cout << "THIS";
//        other.printBoard();
        
        return this->getHashCode() == other.getHashCode();
    }
    
    

    bool operator<(const puzzleboard &other) const {
        return this->getHashCode() < other.getHashCode();
    }

    bool isBlank(point p) {
        if (p.x == -1 || p.y == -1) return false;
        if (board[p.y][p.x] == 0) return true;
        return false;
    }

    point findBlank() {
        return blank;
        /*point p;
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                if (this->board[i][j] == 0) {
                    p.setXY(j, i);
                    return p;
                }
            }
        }
        return p;*/
    }

    bool isWithinBounds(point& p) {
        if (p.x >= BOARD_SIZE || p.y >= BOARD_SIZE || p.x < 0 || p.y < 0) return false;
        return true;
    }

    vector<puzzleboard> generateNextMoves() {
        vector<puzzleboard> l;
        puzzleboard b(*this);
        point blank = b.findBlank();

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
                   map<puzzleboard, puzzleboard>& backtrack,
                   boardScoreType t) {

    list<puzzleboard>::iterator it = frontier.begin();
    while (it != frontier.end()) {
        //cout << "SIZE :" << frontier.size() << endl;
        if (v == *it) {
            int totalscoreNew, totalscoreOld;
            totalscoreNew = v.getPathCost() + v.getScore(t);
            totalscoreOld = it->getPathCost() + it->getScore(t);
            if (totalscoreNew < totalscoreOld) {
                frontier.erase(it);
                frontier.push_back(v);
                std::map<puzzleboard, puzzleboard>::iterator it1 = backtrack.find(v);
                if (it1 == backtrack.end()) {
                    cout<<"ERROR!!!!!!"<<endl;
                }
                backtrack.erase(it1);
                backtrack.insert(std::pair<puzzleboard, puzzleboard>(v, src));
            }
            return;
        }
        it++;
    }
    // i.e. node not in frontier
    frontier.push_back(v);
    backtrack.insert(std::pair<puzzleboard, puzzleboard>(v, src));
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
                map<puzzleboard, puzzleboard>& backtrack,
                map<puzzleboard,int>& visited,
                boardScoreType t) {
    
    int nodesExpanded = 0;
    frontier.push_back(s);
    while(!frontier.empty()) {
        //cout << "FRONTIER SIZE :" << frontier.size() << "VISITED SIZE" << visited.size() <<   endl;
        sortFrontier(frontier, t);
        puzzleboard w = frontier.front();
        //w.printBoard();
        visited.insert(std::pair<puzzleboard, int>(w,1));
        frontier.pop_front();
        //cout<<"POPPED FROM FRONTIER TO VISITED"<<endl;
        nodesExpanded++;

        if(nodesExpanded == 50000){
            cout<<"limit reached"<<endl;
            return false;
        }
//        if (find(visited.begin(), visited.end(), w) == visited.end()) {
//            visited.push_back(w);
//        }
        
        if (w.isTarget()) {
            cout<<"found:nodes expadned"<<nodesExpanded<<endl;
            path.push_back(w);
            std::map<puzzleboard, puzzleboard>::iterator it = backtrack.find(w);
            while(it != backtrack.end()) {
                path.push_back(it->second);
                it = backtrack.find(it->second);
            }
            return true;
        }

        //get next moves
        vector<puzzleboard> vv = w.generateNextMoves();
        //cout << "+++++++++++++MAX OF generate moves" << vv.size();
        //cout << "BEFORE SIZE OF FRONTIER" << frontier.size() << endl;
        for (auto v : vv) {
            //cout << "========================================= LOOP BEGINNGING" << endl;
            //if (find(visited.begin(), visited.end(), v) == visited.end()) {
            if (visited.find(v) == visited.end()) {
                v.setPathCost(w.getPathCost() + 1);
                findandUpdate(frontier, v, w, backtrack, t);
            }
            //cout << "END BEGINNGING" << endl;
        }
        //cout << "AFTER SIZE OF FRONTIER" << frontier.size() << endl;
    }
    return false;
}

void printPath(vector<puzzleboard> p) {
    cout<<"solution path is :"<<endl;
    vector<puzzleboard>::reverse_iterator it = p.rbegin();
    while(it != p.rend()) {
        it->printBoard();
        it++;
    }
}

pair<int,int>  printPathAstar(puzzleboard start, boardScoreType t) {
    //vector<puzzleboard> visited;
    map<puzzleboard, int> visited;
    vector<puzzleboard> path;
    map<puzzleboard, puzzleboard> backtrack;
    pair<int,int> res;
    res.first = 0;
    res.second = 0;
    list<puzzleboard> frontier;
    if (getPathAstar(start,
                     frontier,
                     path,
                     backtrack,
                     visited, t)) {
        cout<<"Astar path Found:length="<<path.size()<<endl;
        printPath(path);
        //overlayPathOnMap(p);
        
        res.first = path.size();
        res.second = visited.size();
        return res;
    } else {
        cout<<"no path"<<endl;
        return res;
    }
}

bool findInVector(vector<vector<int>> v, vector<int> s){
    vector<vector<int>>::iterator it;
    
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
vector<puzzleboard> generateRandomBoards(puzzleboard base, short const number=10){
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

vector<pair<int,int>> plotHelper(puzzleboard p){
    vector<pair<int,int>>  res;
    pair<int, int> respair;
    vector<puzzleboard> listOfBoards = generateRandomBoards(p,20);
    int i = 0;
    for(std::vector<puzzleboard>::iterator it = listOfBoards.begin(); it != listOfBoards.end(); ++it,i++) {
        cout<<"______BOARD"<<i<<"________"<<endl;
        if (it->isSolvable()) {
            //cout<<"______BOARD"<<i<<"________"<<endl;
            res.push_back(printPathAstar((*it), MANHATTAN));
            cout<<"_______________________"<<endl;
        } else {
            cout<<"not solvable board:"<<i<<endl;
        }
    }
    return res;
    
}
/*
+class AstarCompare {
    +    public:
    +    bool operator()(const puzzleboard& b1, const puzzleboard& b2) {
        +        return b1.getMisplacedDist() + b1.getPathCost() > b2.getMisplacedDist() + b2.getPat
        +    }
+};*/
int main() {
    //int a[] = {8,1,3,4,0,2,7,6,5};//works 10000
    //int a[] = {0,1,2,4,5,3,7,8,6};//works
    //int a[] = {4,1,2,0,5,3,7,8,6};//works
    //int a[] = {1,2,0,5,6,3,4,7,8};//works
    //int a[] = {1,2,3,4,6,8,7,0,5};//works
    //int a[] = {0,1,3,4,2,5,7,8,6};//works 10000
    //int a[] = {2,3,5,1,0,4,7,8,6};//works
    //int a[] = {1,0,2,7,5,4,8,6,3};//works
    //int a[] = {5,1,8,2,7,3,0,4,6};//works
    //int a[] = {3,1,2,4,7,5,6,0,8};//suhas
    int a[] = {7,2,4,5,0,6,8,3,1};//suhas
    //int a[] = {5,1,8,2,7,3,4,0,6};//works
    //int a[] = {5,1,8,7,0,3,2,4,6};//works
    //int a[] = {5,1,8,0,7,3,2,4,6};//works
    //int a[] = {5,1,8,7,3,0,2,4,6};//works
    //int a[] = {3,1,2,0,4,5,6,7,8};//
    //int a[] = {5,6,2,1,8,4,7,3,0};//no path
    //int a[] = {1,2,7,0,4,3,6,5,8};
    //int a[] = {1,6,4,7,0,8,2,3,5};
    int b[] = {0,2,3,4,1,5,6,7,8};
    puzzleboard p(a), p1(b);
   
    if (p.isSolvable()) { 
        printPathAstar(p, MISPLACED_TILES);
    } else {
        cout<<"no solution"<<endl;
    }
    
    /*map<puzzleboard, int> m;
    m.insert(pair<puzzleboard,int>(p,1));
    map<puzzleboard, int>::iterator it = m.find(p);*/
    int i = 1;
    //vector<pair<int,int>> res = plotHelper(p);
    /*for(std::vector<pair<int,int>>::iterator it = res.begin(); it != res.end(); ++it,i++) {
        cout << "BOARD - "<<i << " : "<< (*it).first << "," << (*it).second << endl;
    }*/
    //p.printNextMoves();
    /*
    vector<puzzleboard> listOfBoards = generateRandomBoards(p);
    cout << listOfBoards.size();
    int i = 1;
    for(std::vector<puzzleboard>::iterator it = listOfBoards.begin(); it != listOfBoards.end(); ++it,i++) {
        cout << "RANDOM BOARD " << i << endl;
        (*it).printBoard();
    }*/
    
}
