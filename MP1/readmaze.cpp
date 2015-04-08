#include<iostream>
#include<fstream>
#include<string>
#include<vector>
#include<queue>
#include<map>
#include<unordered_map>
#include<list>
using namespace std;
#define MAX_MAZE_DIM 100

//bool DEBUG = false; // DISABLE TO STOP DEBUG MSG
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
    bool isGreedy; 
    int startCost;//set to zero for greedy
    int heuristicScore;
    Node(point p1, int startcost, int score, bool isgreedy) {
        p.setXY(p1.x, p1.y);
        startCost = (isgreedy == true) ? 0 : startcost;
        heuristicScore = score;
        isGreedy = isgreedy;
    } 
    
    bool operator==(const Node &other) const {
        return ((this->startCost + this->heuristicScore) == (other.startCost + other.heuristicScore));
    }
    
    bool operator<(const Node &other) const {
        return ((this->startCost + this->heuristicScore) < (other.startCost + other.heuristicScore));
        
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
                for (int i = 0;i<line.size();i++) {
                    map[cur_row][i] = line[i];
                    if (line[i] == 'P' && !start.isInit()) { // why ?
                        start.setXY(i, cur_row); 
                    } else if (line[i] == '.' && !dest.isInit()) {
                        dest.setXY(i, cur_row);
                    }
                }
                last_xdim = line.size();
                cur_row++; 
            }
            maze_ydim = cur_row;
            maze_xdim = last_xdim;
            precomputeHeuristic(dest);
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
        if (s == dest) return true;
        return false;
    }

    bool isSource(point s) {
        if (s == start) return true;
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
        point ss[4];
        //up
        ss[0].setXY(s.x, s.y - 1);
        //right
        ss[1].setXY(s.x + 1, s.y);
        //left
        ss[2].setXY(s.x - 1, s.y);
        //bottom
        ss[3].setXY(s.x, s.y + 1);
        bool op = false;
        // mark this point as visited
        visited[s.y * maze_xdim + s.x] = true;
        path.push_back(s);
        for (int i = 0;i<4;i++) {
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
                if (isPointInPath(p, path) && !isSource(p)) {
                    cout<<".";
                } else {
                    cout<<map[i][j];
                }
            }
            cout<<endl;
        }
    }
    void printPathDFS() {
        vector<point> p;
        bool visited[maze_ydim * maze_xdim];
        for (int i = 0; i< maze_ydim * maze_xdim; i++) {
            visited[i] = false;
        }

        if (getPathDFS(start, p, visited)) {
            cout<<"DFS path Found:length="<<p.size()<<endl;
            //printPath(path);
            overlayPathOnMap(p);
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

            point v[4];
            //up
            v[0].setXY(w.x, w.y - 1);
            //right
            v[1].setXY(w.x + 1, w.y);
            //left
            v[2].setXY(w.x - 1, w.y);
            //bottom
            v[3].setXY(w.x, w.y + 1);
            for (int i = 0; i < 4; i++) {
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
            cout<<"BFS path Found:length="<<p.size()<<endl;
            //printMap(backtrack);
            //printPath(p);
            overlayPathOnMap(p);
        } else {
            cout<<"no path"<<endl;
        }
    }

    bool getPathGreedyorHeuristic(point s,
                    list<Node> &frontier,
                    vector<point>&path,
                    std::map<point, point>& backtrack,
                    bool*visited,
                    bool isGreedy) {
        if (!isWithinBounds(s)) return false;
		int nodesExpanded = 0;
        // point, startcost, heuristic, isgreedy
        Node s_n(s, 0, heuristic[s.y][s.x], isGreedy);
        frontier.push_back(s_n);
        while(!frontier.empty()) {
            frontier.sort();
            Node w = frontier.front();
            frontier.pop_front();
            //Node w = frontier.back();
            //frontier.pop_back();
			nodesExpanded++;
            visited[w.p.y * maze_xdim + w.p.x] = true;
            //cout<<"---------enter:"<<w.p.x<<","<<w.p.y<<endl;
            if (isTarget(w.p)) {
                //poulate path vector
				//cout << "NODES EXPANDED IN GBFS*" << nodesExpanded << endl;
                path.push_back(w.p);
                std::map<point, point>::iterator it = backtrack.find(w.p);
                while(it != backtrack.end()) {
                    //cout<<"found";
                    path.push_back(it->second);
                    it = backtrack.find(it->second);
                }
                return true;
            }

            point v[4];
            //up
            v[0].setXY(w.p.x, w.p.y - 1);
            //right
            v[1].setXY(w.p.x + 1, w.p.y);
            //left
            v[2].setXY(w.p.x - 1, w.p.y);
            //bottom
            v[3].setXY(w.p.x, w.p.y + 1);
            for (int i = 0; i < 4; i++) {
                if (isWithinBounds(v[i]) && isPathPresent(v[i])  && visited[v[i].y * maze_xdim + v[i].x] == false) {
                    Node n(v[i], w.startCost + 1, heuristic[v[i].y][v[i].x], isGreedy);
                    findandUpdate(frontier, n, w, backtrack);
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
        list<Node> frontier;
        if (getPathGreedyorHeuristic(start, frontier, p, backtrack, visited, true)) {
            cout<<"Greedy BFS path Found:length="<<p.size()<<endl;
            //printMap(backtrack);
            //printPath(p);
            overlayPathOnMap(p);
        } else {
            cout<<"no path"<<endl;
        }
    }

    void printPathAstar() {
        bool visited[maze_ydim * maze_xdim];
        for (int i = 0; i< maze_ydim * maze_xdim; i++) {
            visited[i] = false;
        }
        
        vector<point> p;
        std::map<point, point> backtrack;
        list<Node> frontier;
        if (getPathGreedyorHeuristic(start, frontier, p, backtrack, visited, false)) {
            cout<<"Astar path Found:length="<<p.size()<<endl;
            //printMap(backtrack);
            //printPath(p);
            overlayPathOnMap(p);
        } else {
            cout<<"no path"<<endl;
        }
    }
    

    /*
     * this function updates the cost of the node if its found in frontier.
     * if not found adds to frontier
     **/
    void findandUpdate(list<Node>& l, Node& v, Node& src, std::map<point, point>& backtrack) {
        list<Node>::iterator it = l.begin();
        while (it != l.end()) {
            //Node n = *it;
			//cout << "V," << v.p.x << "," << n.p.x <<  "||" << v.p.y << "," << n.p.y << endl;
            if (v.p == it->p) {
                // found match. now check score
//				if(DEBUG) {
//					cout << "v.startCost,n.startCost : " << v.startCost << n.startCost << endl;
//				}
                int totalscoreNew, totalscoreOld;
                //startcost is 0 for greedy BFS.
                totalscoreNew = v.startCost + v.heuristicScore;
                totalscoreOld = it->startCost + it->heuristicScore;
                if (totalscoreNew < totalscoreOld) {
                    l.erase(it);
                    l.push_back(v);
                    std::map<point, point>::iterator it1 = backtrack.find(v.p);
                    backtrack.erase(it1);
                    backtrack.insert(std::pair<point, point>(v.p, src.p)); 
                }
                return;
            }
            it++;
        }
        // i.e. node not in frontier
        l.push_back(v);
        backtrack.insert(std::pair<point, point>(v.p, src.p)); 
    }
};

int main() {
    maze smallm("/Users/saikat/workspace_ubuntu/practise/cs440mp1/mp1_git/AI_MP/smallMaze.txt");
    smallm.printMazeParams();
    smallm.printMaze();
    smallm.printPathBFS();
    smallm.printPathDFS();
    smallm.printPathGreedyBFS();
    smallm.printPathAstar();
    
    maze medium("/Users/saikat/workspace_ubuntu/practise/cs440mp1/mp1_git/AI_MP/mediumMaze.txt");
    medium.printMazeParams();
    medium.printMaze();
    medium.printPathBFS();
    medium.printPathDFS();
    medium.printPathGreedyBFS();
    medium.printPathAstar();

    maze bigm("/Users/saikat/workspace_ubuntu/practise/cs440mp1/mp1_git/AI_MP/bigMaze.txt");
    bigm.printMazeParams();
    bigm.printMaze();
    bigm.printPathBFS();
    bigm.printPathDFS();
    bigm.printPathGreedyBFS();
    bigm.printPathAstar();
}