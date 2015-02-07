#include<iostream>
#include<fstream>
#include<string>
#include<vector>
#include<queue>
#include<map>
#include<unordered_map>

using namespace std;
#define MAX_MAZE_DIM 100

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


class Node {
    public:
    point p;
    int heuristicScore;
    Node(point p1, int score) {
        p.setXY(p1.x, p1.y);
        heuristicScore = score;
    } 
};

class GreedyBFSComparator {
public:
    bool operator () (Node& n1, Node& n2) {
        return n1.heuristicScore > n2.heuristicScore;
    }
};




class maze {
    char map[MAX_MAZE_DIM][MAX_MAZE_DIM];
    int heuristic[MAX_MAZE_DIM][MAX_MAZE_DIM];
    int maze_xdim;
    int maze_ydim;
    point start;
    point dest;

public:
    maze(string filename) {
        ifstream ip(filename.c_str());
        int cur_row = 0;
        string line; 
        int last_xdim = 0;
        if (ip.is_open()) {
            while(getline(ip, line)) {
                // first line size shud be x diemnsion
                //maze_xdim = line.size();
                //cout<<line<<endl;
                for (int i = 0;i<line.size();i++) {
                    map[cur_row][i] = line[i];
                    if (line[i] == '.' && !start.isInit()) {
                        start.setXY(i, cur_row); 
                    } else if (line[i] == 'P' && !dest.isInit()) {
                        dest.setXY(i, cur_row);
                    }
                }
                last_xdim = line.size();
                cur_row++; 
            }
            maze_ydim = cur_row;
            maze_xdim = last_xdim;
        }
    }

    void precomputeHeuristic(point target) {
        for (int y = 0; y < maze_ydim;y++) {
            for(int x = 0;x < maze_xdim;x++) {
                heuristic[y][x] = abs(y - target.y) + abs(x - target.x);
            }
        }
    }

    void printMazeParams() {
        cout<<"maze dimensions:"<<maze_xdim<<"X"<<maze_ydim<<endl;
        cout<<"Agent Start Point";
        start.print();
        cout<<"destination:";
        dest.print();
    }
    void printMaze(){
        cout<<endl<<"----maze----"<<endl;
        for (int i = 0;i<maze_ydim;i++) {
            for(int j = 0;j<maze_xdim;j++) {
                cout<<map[i][j];
            }
            cout<<endl;
        }
    } 
    bool isWithinBounds(point s) {
        if (s.x < 0 || s.x >= maze_xdim) return false;
        if (s.y < 0 || s.y >= maze_ydim) return false;
        return true;
    }

    bool isPathPresent(point s) {
        if (!isWithinBounds(s)) return false;
        if (map[s.y][s.x] == '%') {
            return false;
        }
        return true; 
    }

    bool isTarget(point s) {
        if (map[s.y][s.x] == 'P') return true;
        return false;
    }

    void printPath(vector<point>& p) {
        vector<point>::iterator it;
        cout<<"path:"<<endl;
        for (it = p.begin(); it != p.end(); it++) {
            it->print();
        }
        cout<<"path end"<<endl;
    }

    void printMap(std::map<point, point>& p) {
        std::map<point, point>::iterator it;
        cout<<"path:"<<endl;
        for (it = p.begin(); it != p.end(); it++) {
            point w = it->first;
            w.print();
            cout<<"---";
            it->second.print();
            cout<<endl;
        }
        cout<<"path end"<<endl;
    }
    bool getPathDFS(point s, vector<point>&path, bool *visited) {
        if (!isWithinBounds(s)) return false;
        // if seen already seen return false
        //cout<<"inside"<<s.x<<","<<s.y<<endl;
        if (visited[s.y * maze_xdim + s.x] == true) {
            //cout<<"already visited"<<endl;
            return false;
        }
        if (isTarget(s)) {
            path.push_back(s);
            return true;
        }

        //check if path exists from this point as s
        // if yes append self to path n return true
        point ss[8];
        ss[0].setXY(s.x - 1, s.y - 1);
        ss[1].setXY(s.x, s.y - 1);
        ss[2].setXY(s.x + 1, s.y - 1);
        ss[3].setXY(s.x - 1, s.y);
        ss[4].setXY(s.x + 1, s.y);
        ss[5].setXY(s.x - 1, s.y + 1);
        ss[6].setXY(s.x, s.y + 1);
        ss[7].setXY(s.x + 1, s.y + 1);
        bool op = false;
        // mark this point as visited
        visited[s.y * maze_xdim + s.x] = true;
        path.push_back(s);
        for (int i = 0;i<8;i++) {
            if (isPathPresent(ss[i])) {
                op = op || getPathDFS(ss[i], path, visited);
            }
            // break at first match
            if (op == true) break;
        }

        if (op == false) {
            path.pop_back();
        }
        return op;
    }
 
    bool isPointInPath(point p, vector<point>&path) {
        vector<point>::iterator it;
        for (it = path.begin(); it != path.end(); it++) {
            if (it->x == p.x && it->y == p.y) return true;
        }
        return false;
    }
 
    void overlayPathOnMap(vector<point> path) {
        for (int i = 0; i < maze_ydim; i++) {
            for (int j = 0;j< maze_xdim;j++) {
                point p(j, i);
                if (isPointInPath(p, path) && !isTarget(p)) {
                    cout<<".";
                } else {
                    cout<<map[i][j];
                }
            }
            cout<<endl;
        }
    }
    void printPathDFS() {
        vector<point> path;
        bool visited[maze_ydim * maze_xdim];
        for (int i = 0; i< maze_ydim * maze_xdim; i++) {
            visited[i] = false;
        }

        if (getPathDFS(start, path, visited)) {
            cout<<"DFS path found"<<endl; 
            //printPath(path);
            overlayPathOnMap(path);
        } else {
            cout<<"no path"<<endl;
        }
    }

    bool getPathBFS(point s,
                    queue<point> &frontier,
                    vector<point>&path,
                    std::map<point, point>& backtrack,
                    bool *visited) {

        if (!isWithinBounds(s)) return false;
        visited[s.y * maze_xdim + s.x] = true;
        frontier.push(s);
        while(!frontier.empty()) {
            point w = frontier.front();
            frontier.pop();
            //cout<<"---------enter:"<<w.x<<","<<w.y<<endl;
            if (isTarget(w)) {
                //poulate path vector
                path.push_back(w);
                std::map<point, point>::iterator it = backtrack.find(w);
                while(it != backtrack.end()) {
                    //cout<<"found";
                    path.push_back(it->second);
                    it = backtrack.find(it->second);
                }
                return true;
            }

            point v[8];
            v[0].setXY(w.x - 1, w.y - 1);
            v[1].setXY(w.x, w.y - 1);
            v[2].setXY(w.x + 1, w.y - 1);
            v[3].setXY(w.x - 1, w.y);
            v[4].setXY(w.x + 1, w.y);
            v[5].setXY(w.x - 1, w.y + 1);
            v[6].setXY(w.x, w.y + 1);
            v[7].setXY(w.x + 1, w.y + 1);
            for (int i = 0; i < 8; i++) {
                if (isWithinBounds(v[i]) && isPathPresent(v[i]) &&
                    visited[v[i].y * maze_xdim + v[i].x] == false) {
                    visited[v[i].y * maze_xdim + v[i].x] = true;
                    frontier.push(v[i]);
                    backtrack.insert(std::pair<point, point>(v[i], w)); 
                }
            }
            
        }
        return false; 
    }
    
    void printPathBFS() {
        bool visited[maze_ydim * maze_xdim];
        for (int i = 0; i< maze_ydim * maze_xdim; i++) {
            visited[i] = false;
        }
        queue<point> frontier;
        vector<point> p;
        std::map<point, point> backtrack;
        if (getPathBFS(start, frontier, p, backtrack, visited)) {
            cout<<"BFS path Found"<<endl;
            //printMap(backtrack);
            //printPath(p);
            overlayPathOnMap(p);
        } else {
            cout<<"no path"<<endl;
        }
    }

    bool getPathGreedyBFS(point s,
                    priority_queue<Node, vector<Node>, GreedyBFSComparator> &frontier,
                    vector<point>&path,
                    std::map<point, point>& backtrack,
                    bool *visited) {
        if (!isWithinBounds(s)) return false;
        visited[start.y * maze_xdim + start.x] = true;
        Node s_n(s, heuristic[s.y][s.x]);
        frontier.push(s_n);
        while(!frontier.empty()) {
            Node w = frontier.top();
            frontier.pop();
            //cout<<"---------enter:"<<w.x<<","<<w.y<<endl;
            if (isTarget(w.p)) {
                //poulate path vector
                path.push_back(w.p);
                std::map<point, point>::iterator it = backtrack.find(w.p);
                while(it != backtrack.end()) {
                    //cout<<"found";
                    path.push_back(it->second);
                    it = backtrack.find(it->second);
                }
                return true;
            }

            point v[8];
            v[0].setXY(w.p.x - 1, w.p.y - 1);
            v[1].setXY(w.p.x, w.p.y - 1);
            v[2].setXY(w.p.x + 1, w.p.y - 1);
            v[3].setXY(w.p.x - 1, w.p.y);
            v[4].setXY(w.p.x + 1, w.p.y);
            v[5].setXY(w.p.x - 1, w.p.y + 1);
            v[6].setXY(w.p.x, w.p.y + 1);
            v[7].setXY(w.p.x + 1, w.p.y + 1);
            for (int i = 0; i < 8; i++) {
                if (isWithinBounds(v[i]) && isPathPresent(v[i]) &&
                    visited[v[i].y * maze_xdim + v[i].x] == false) {
                    visited[v[i].y * maze_xdim + v[i].x] = true;
                    Node n(v[i], heuristic[v[i].y][v[i].x]);
                    frontier.push(n);
                    backtrack.insert(std::pair<point, point>(v[i], w.p)); 
                }
            }
            
        }
        return false;
    }

    void printPathGreedyBFS() {
        bool visited[maze_ydim * maze_xdim];
        for (int i = 0; i< maze_ydim * maze_xdim; i++) {
            visited[i] = false;
        }
        
        vector<point> p;
        std::map<point, point> backtrack;
        priority_queue<Node, vector<Node>, GreedyBFSComparator> frontier;
        if (getPathGreedyBFS(start, frontier, p, backtrack, visited)) {
            cout<<"Greedy BFS path Found"<<endl;
            //printMap(backtrack);
            //printPath(p);
            overlayPathOnMap(p);
        } else {
            cout<<"no path"<<endl;
        }
    }
};

int main() {
    maze smallm("smallMaze.txt");
    smallm.printMazeParams();
    smallm.printMaze();
    smallm.printPathBFS();
    smallm.printPathDFS();
    smallm.printPathGreedyBFS();
/*
    maze medium("mediumMaze.txt");
    medium.printMazeParams();
    medium.printMaze();
    smallm.printPathBFS();
    medium.printPathDFS();
    
    maze bigm("bigMaze.txt");
    bigm.printMazeParams();
    bigm.printMaze();
    bigm.printPathBFS();
    bigm.printPathDFS();*/
}
