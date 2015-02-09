#include<iostream>
#include<map>
#include<vector>
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

    puzzleboard(int *a) {
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
        for (int i = 0; i < BOARD_SIZE;i++) {
            for (int j = 0; j < BOARD_SIZE;j++) {
                this->board[i][j] = o.board[i][j];
                this->goal[i][j] = o.goal[i][j];
            }
        }
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
        if (p.x > BOARD_SIZE || p.y > BOARD_SIZE || p.x < 0 || p.y < 0) return false;
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
};


int main() {
    int a[] = {1,2,6,8,0,7,3,4,9};
    puzzleboard p(a);
    p.printBoardStats();
    cout<<"dis from goal"<<p.getMisplacedDist()<<endl;
    //std::map<puzzleboard, puzzleboard> m;
    p.printNextMoves();
     
}
