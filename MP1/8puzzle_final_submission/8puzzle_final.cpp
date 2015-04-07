#include<iostream>
#include<vector>
#include<list>
#include<map>
#include<math.h>
#include <fstream>
#include<sstream>
using namespace std;

#define MAX_EXPANSION 60000
typedef enum board_type {
    MANHATTAN,
    MISPLACED,
    GASHNIG,
    UNKNOWN
}board_type;

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

class Board {
public:
    int board_array[9];
    board_type type;
    int heuristic_score;
    int pathcost;
    int hashCode;
    
    void computeHashCode() {
        int power = 0;
        int result = 0;
        for (int i = 0; i < 9;i++) {
            result += board_array[i] * pow(3.0, power);
            power++;
        }
        hashCode = result;
    }

    int getHashCode() const {
        return hashCode;
    }

    int getPathCost() const {
        return pathcost;
    }
    
    void setPathCost(int val) {
        pathcost = val;
    }

    int getScore() {
        return heuristic_score;
    }
    
    Board() {
        type = UNKNOWN;
        heuristic_score = 0;
        pathcost = 0;
    }
    
    // WARNING:should only be used in gashnik generating temporary boards!!
    Board(int a[]) {
        for (int i = 0; i < 9;i++) {
            board_array[i] = a[i];
        }
        computeHashCode();
        type = UNKNOWN;
        pathcost = 0;
        //since type is unknown, heuristic not computed
    }
    
    Board(int a[], board_type t) {
        for (int i = 0; i < 9;i++) {
            board_array[i] = a[i];
        }
        computeHashCode();
        type = t;
        pathcost = 0;
        computeHeuristicScore(type);
    }


   
    bool isTarget() {
        for (int i = 0; i < 9;i++) {
            if (i != board_array[i]) return false;
        }
        return true;
    }
    
    list<int> find_missed_value(bool skip_blank) {
        list<int> op;
        for (int i = 0;i<9;i++) {
            if (skip_blank && board_array[i] == 0) {
                continue;
            } else if (i != board_array[i]) {
                op.push_back(board_array[i]);
            }
        }
        return op;
    }
    
    int indexOf(int tile) {
        for (int i = 0;i<9;i++) {
            if (board_array[i] == tile) return i;
        }
        return -1;
    }
    
    int computeMinGasnik(vector<Board> blist, Board& op) {
        int minscore = 10000000;
        for (auto i : blist) {
            int score = computeGashhNig(i);
            if (score < minscore) {
                minscore = score;
                op = i;
            }
        }
        return minscore;
    }
    
    /************Heuristic************************************/
    int computeManhattan() {
        int score = 0;
        int dest_row = 0, cur_row = 0;
        int dest_col = 0, cur_col = 0;
        for (int i = 0; i < 9;i++) {
            dest_row = board_array[i]/3;
            dest_col = board_array[i]%3;
            
            cur_row = i/3;
            cur_col = i%3;
            score += abs(dest_row - cur_row) + abs(dest_col - cur_col);
        }
        return score;
    }

    
    int computeGashhNig(Board board) {
        //skip blank
        // this is a temp board. type doesnt matter
        list<int> res = board.find_missed_value(true);
        res.sort();
        int path_cost = 0;
        
        while(!board.isTarget()) {
            path_cost++;
            int index_of_zero = board.indexOf(0);
            if (index_of_zero > 0) {
                int swap_index = board.indexOf(index_of_zero);
                int temp = board.board_array[swap_index];
                board.board_array[swap_index] = 0;
                board.board_array[index_of_zero] = temp;
                res.remove(index_of_zero);
            } else {
                int checkVal = res.front();
                int to_swap = board.indexOf(checkVal);
                board.board_array[0] = checkVal;
                board.board_array[to_swap] = 0;
            }
        }
        return path_cost;
    }
    
    int computeMisplaced() {
        int sum = 0;
        for (int i = 0;i< 9;i++) {
            if (i != board_array[i])
                sum++;
        }
        return sum;
    }
    
    
    void computeHeuristicScore(board_type t) {
        switch (t) {
            case MANHATTAN:
                heuristic_score = computeManhattan();
                break;
            case MISPLACED:
                heuristic_score = computeMisplaced();
                break;
            case GASHNIG:
                heuristic_score = computeGashhNig(*this);
                break;
            default:
                cout<<"no proper type found!!!!!!!!!"<<endl;
                break;
        }
    }
    
    /************************************************************************/
    

    bool inRange(point p) {
        if (p.x >= 3 || p.y >= 3 || p.x < 0 || p.y < 0) return false;
        return true;
    }
    
    vector<Board> generateNextMoves() {
        vector<Board> op;
        int index_zero = indexOf(0);
        point blank(index_zero%3, index_zero/3);
        vector<point> p = findNeighbourPoints(blank);
        int local_arr[9];
        for (int i = 0;i < 9;i++) {
            local_arr[i] = board_array[i];
        }
        
        for (auto i : p) {
            //swap
            int dest_index = i.y * 3 + i.x;
            int temp = local_arr[index_zero];
            local_arr[index_zero] = local_arr[dest_index];
            local_arr[dest_index] = temp;
            // compute heuristic score in the constructor of the class
            Board b(local_arr, type);
            op.push_back(b);
            
            //restore the swap
            temp = local_arr[index_zero];
            local_arr[index_zero] = local_arr[dest_index];
            local_arr[dest_index] = temp;
        }
        return op;
    }
    
    vector<point> findNeighbourPoints(point blank) {
        vector<point> op;
        point pp[4];
        //right
        pp[0].setXY(blank.x+1,blank.y);
        //left
        pp[1].setXY(blank.x-1,blank.y);
        //up
        pp[2].setXY(blank.x,blank.y - 1);
        //down
        pp[3].setXY(blank.x,blank.y + 1);
        
        for(int i = 0;i<4;i++) {
            if (inRange(pp[i])) {
                op.push_back(pp[i]);
            }
        }
        return op;
    }
    
    /***************COMPARATOR HELPERS***************************/
    // puzzle board must be of same size
    bool operator==(const Board &other) const {
        return this->getHashCode() == other.getHashCode();
    }
    
    bool operator<(const Board &other) const {
        return this->getHashCode() < other.getHashCode();
    }
    
    /************************************************************/
    void printboard(){
        for (int i = 0; i < 3;i++) {
            for (int j = 0; j< 3;j++) {
                cout<<"["<<board_array[i * 3 + j]<<"]";
            }
            cout<<endl;
        }
    }
    
    
};

void findandUpdate(list<Board>& frontier,
                   Board& v,
                   Board& src,
                   map<Board, Board>& backtrack,
                   board_type t) {
    
    list<Board>::iterator it = frontier.begin();
    while (it != frontier.end()) {
        //cout << "SIZE :" << frontier.size() << endl;
        if (v == *it) {
            int totalscoreNew, totalscoreOld;
            totalscoreNew = v.getPathCost() + v.getScore();
            totalscoreOld = it->getPathCost() + it->getScore();
            if (totalscoreNew < totalscoreOld) {
                frontier.erase(it);
                frontier.push_back(v);
                std::map<Board, Board>::iterator it1 = backtrack.find(v);
                if (it1 == backtrack.end()) {
                    cout<<"ERROR!!!!!!"<<endl;
                }
                backtrack.erase(it1);
                backtrack.insert(std::pair<Board, Board>(v, src));
            }
            return;
        }
        it++;
    }
    // i.e. node not in frontier
    frontier.push_back(v);
    backtrack.insert(std::pair<Board, Board>(v, src));
}

/*****************CORE ASTAR*********************************/
bool getPathAstar(Board s,
                  list<Board>& frontier,
                  vector<Board>& path,
                  map<Board, Board>& backtrack,
                  map<Board,int>& visited,
                  board_type t,
                  int& expanded,
                  int& pathlen) {
    int nodesExpanded = 0;
    frontier.push_back(s);
    while(!frontier.empty()) {
        frontier.sort([](Board& b1,Board& b2) {
            return b1.getScore() + b1.getPathCost() < b2.getScore() + b2.getPathCost();
        });
        
        Board w = frontier.front();
        //w.printBoard();
        visited.insert(std::pair<Board, int>(w,1));
        frontier.pop_front();
        //cout<<"POPPED FROM FRONTIER TO VISITED"<<endl;
        nodesExpanded++;
        
        if(nodesExpanded == MAX_EXPANSION) {
            //cout<<"limit reached...aborting!"<<endl;
            expanded = 0;
            pathlen = 0;
            return false;
        }
        
        if (w.isTarget()) {
            //cout<<"found:nodes expadned"<<nodesExpanded<<endl;
            path.push_back(w);
            expanded = nodesExpanded;
            pathlen = (int) path.size();
            std::map<Board, Board>::iterator it = backtrack.find(w);
            while(it != backtrack.end()) {
                path.push_back(it->second);
                it = backtrack.find(it->second);
            }
            return true;
        }
        
        //get next moves
        vector<Board> vv = w.generateNextMoves();
        //cout << "BEFORE SIZE OF FRONTIER" << frontier.size() << endl;
        for (auto v : vv) {
            //if (find(visited.begin(), visited.end(), v) == visited.end()) {
            if (visited.find(v) == visited.end()) {
                v.setPathCost(w.getPathCost() + 1);
                findandUpdate(frontier, v, w, backtrack, t);
            }
        }
    }
    return false;
}


pair<int,int>  printPathAstar(Board& start, board_type t) {
    map<Board, int> visited;
    vector<Board> path;
    map<Board, Board> backtrack;
    pair<int,int> res;
    res.first = 0;
    res.second = 0;
    list<Board> frontier;
    int expanded = 0;
    int pathlen = 0;
    if (getPathAstar(start,
                     frontier,
                     path,
                     backtrack,
                     visited, t, expanded, pathlen)) {
        //cout<<"Astar path Found:length="<<path.size()<<endl;
        //printPath(path);
        res.first = expanded;
        res.second = path.size();
        return res;
    } else {
        //cout<<"no path"<<endl;
        res.first = -1;
        res.second = -1;
        return res;
    }
}

int main() {
    string line;
    //ifstream myfile("/Users/saikat/workspace_ubuntu/practise/cs440mp1/mp1_new/8puzzle/input.txt");
    ifstream myfile("puzzle_input.txt");
    int input_board[50][9];
    int input_num = 0;
    if (myfile.is_open())
    {
        
        while(getline(myfile, line)) {
            istringstream s(line);
            string st;
            int i = 0;
            if (input_num >= 50) {
                goto exit;
            }
            while(getline(s, st, ',')) {
                input_board[input_num][i++] = atoi(st.c_str());
            }
            input_num++;
        }
    } else {
        cout<<"unable to open path"<<endl;
    }
exit:
    myfile.close();
    cout<<"**********************8PUZZLE*****************************************************"<<endl;
    cout<<"PUZZLE|MISPLACED NODES_EXPANDED PATH_COST|MANHATTAN NODES_EXPANDED PATH_COST|GASHNIG NODES_EXPANDED PATH_COST"<<endl; 
    cout<<"***********************************************************************************"<<endl;
    for (int i = 0; i< 50;i++) {
        for (int j = 0;j< 9;j++) {
            cout<<input_board[i][j]<< " ";
        }
        cout<<"|";
        Board b1(input_board[i], MISPLACED);
        Board b2(input_board[i], MANHATTAN);
        Board b3(input_board[i], GASHNIG);
        pair<int, int> op1 = printPathAstar(b1, MISPLACED);
        pair<int, int> op2 = printPathAstar(b2, MANHATTAN);
        pair<int, int> op3 = printPathAstar(b3, GASHNIG);
        cout<<op1.first<<" "<<op1.second<<"|";
        cout<<op2.first<<" "<<op2.second<<"|";
        cout<<op3.first<<" "<<op3.second<<"|";
        cout<<endl;
    }
    cout<<"**********************END*************************/"<<endl;
}
