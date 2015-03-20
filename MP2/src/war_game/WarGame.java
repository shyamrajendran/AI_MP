package war_game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Random;

import javax.xml.soap.Node;

import org.omg.CORBA.INTERNAL;

/**
 * Created by manshu on 3/14/15.
 */
public class WarGame {
    public boolean chanceBlitz = false;
    public int nodesExpanded = 0;
    public int depth4Counter = 0;


    private int[][] board;
    public static int board_width, board_height;

    private Player current_player;
    private int MAX_DEPTH;
    private static final int DEFAULT_MAX_DEPTH = 3;


    private void readBoard(String file_name) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file_name));
        String in_line;
        ArrayList<String[]> lines = new ArrayList<String[]>();
        while ((in_line = bufferedReader.readLine()) != null) {
            if (in_line.equals("")) continue;
            lines.add(in_line.split("\\t| "));
        }
        board = new int[lines.size()][lines.get(0).length];
        int row = 0, col = 0;
        for (String[] line : lines) {
            for (String word : line) {
                board[row][col++] = Integer.parseInt(word);
            }
            row++;
            col = 0;
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }

        board_height = board.length;
        board_width = board[0].length;
    }


    public WarGame(String file_name) throws IOException {
        readBoard(file_name);
        MAX_DEPTH = DEFAULT_MAX_DEPTH;
    }

    public WarGame(String file_name, int max_depth) throws IOException {
        readBoard(file_name);
        MAX_DEPTH = max_depth;
    }

    private boolean gameOver(BoardState state) {
        return state.getTotalPieces() == (board_width * board_height);
    }

    private ArrayList<Tuple> getAdjacentLocations(Tuple location) {
        ArrayList<Tuple> adjacent_locations = new ArrayList<Tuple>(4);

        if (location.getRow() + 1 < board_height) adjacent_locations.add(new Tuple(location.getRow() + 1, location.getCol()));
        if (location.getRow() - 1 >= 0) adjacent_locations.add(new Tuple(location.getRow() - 1, location.getCol()));
        if (location.getCol() + 1 < board_width) adjacent_locations.add(new Tuple(location.getRow(), location.getCol() + 1));
        if (location.getCol() - 1 >= 0) adjacent_locations.add(new Tuple(location.getRow(), location.getCol() - 1));

        return adjacent_locations;
    }


    private boolean flipCoin() {
        int max = 2;
        int min = 0;
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min)) + min;

        if (randomNum == 1 ) {
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<BoardState> getNextMoves(BoardState state) {
        if ( true ) {

      Set<Tuple> player1_pieces = state.getPlayerInfo(state.getPlayer());
      Set<Tuple> player2_pieces = state.getPlayerInfo(state.getOtherPlayer());


      ArrayList<Tuple> possible_moves = new ArrayList<Tuple>();

//      System.out.println("PLAYER 1 size"+player1_pieces.size());
//      System.out.println("PLAYER 2 size"+player2_pieces.size());
      for (int i = 0; i < board_height; i++) {
          for (int j = 0; j < board_width; j++) {
              Tuple to_move = new Tuple(i, j);
              if (player1_pieces.contains(to_move) || player2_pieces.contains(to_move)) continue;
              possible_moves.add(to_move);
          }
      }
//     System.out.println("possible moves length" + possible_moves.size());
//      System.out.println(possible_moves);
       ArrayList<BoardState> nextBoardStates = new ArrayList<BoardState>(possible_moves.size());

      for (Tuple move : possible_moves) {
          //    	  	System.out.println("CHECKING TO MOVE POSSIBILITY"+move);
          BoardState temp = new BoardState(state);

//    	  Set<Tuple> temp_player1_pieces = temp.getPlayerInfo(state.getPlayer());
//    	  Set<Tuple> temp_player2_pieces = temp.getPlayerInfo(state.getOtherPlayer());

          temp.addLocation(temp.getPlayer(), move);


          ArrayList<Tuple> next_next_tuples = getAdjacentLocations(move);
          ArrayList<Tuple> to_be_blitzed = new ArrayList<Tuple>();
          ArrayList<Tuple> my_adjacent_pieces = new ArrayList<Tuple>();
//    	  if (move.getRow() == 1 && move.getCol() == 1){
////    		  System.out.println("hi");
////    		  System.out.println(temp.getPlayerInfo(temp.getPlayer()));
////    		  System.out.println(temp.getPlayerInfo(temp.getOtherPlayer()));
//
//    	  }

          for (Tuple blitz_tuple : next_next_tuples) {
              if(  player2_pieces.contains(blitz_tuple)){

                  if ( !chanceBlitz  ) {
                      to_be_blitzed.add(blitz_tuple);
                  }else {
                      if (flipCoin()) {
                          to_be_blitzed.add(blitz_tuple);
                      }
                  }
              }else if(player1_pieces.contains(blitz_tuple)){
                  my_adjacent_pieces.add(blitz_tuple);
              }

          }


          if(!my_adjacent_pieces.isEmpty() && !to_be_blitzed.isEmpty() ) {
              for(Tuple opposite_tuple : to_be_blitzed){
                  temp.removeLocation(temp.getOtherPlayer(), opposite_tuple);
                  temp.addLocation(temp.getPlayer(), opposite_tuple);
                  temp.setPlayerScore(temp.getOtherPlayer(), temp.getPlayerScore(temp.getOtherPlayer()) -
                          board[opposite_tuple.getRow()][opposite_tuple.getCol()]);
                  temp.setPlayerScore(temp.getPlayer(), temp.getPlayerScore(temp.getPlayer()) +
                          board[opposite_tuple.getRow()][opposite_tuple.getCol()]);


              }
              temp.setPlayerScore(temp.getPlayer(), temp.getPlayerScore(temp.getPlayer()) +
                      board[move.getRow()][move.getCol()]);

          } else{
              temp.setPlayerScore(temp.getPlayer(), temp.getPlayerScore(temp.getPlayer()) +
                      board[move.getRow()][move.getCol()]);
          }
              temp.calculateUtility();
              temp.setPlayer(temp.getOtherPlayer());
              nextBoardStates.add(temp);
      }

      return nextBoardStates;
        }  else {

            Set<Tuple> player1_pieces = state.getPlayerInfo(state.getPlayer());
            Set<Tuple> player2_pieces = state.getPlayerInfo(state.getOtherPlayer());

            boolean conquer_possible = false;
            ArrayList<Tuple> possible_moves = new ArrayList<Tuple>();

            for (Tuple tuple : player1_pieces) {
                ArrayList<Tuple> next_tuples = getAdjacentLocations(tuple);
                for (Tuple t : next_tuples) {
                    if (player1_pieces.contains(t) || player2_pieces.contains(t)) // location possible ?
                        continue;
                    ArrayList<Tuple> next_next_tuples = getAdjacentLocations(t);
                    for (Tuple t1 : next_next_tuples) {
                        if (player2_pieces.contains(t1)) {
                            conquer_possible = true;
                            possible_moves.add(t);
                        }
                    }
                }
            }

            if (!conquer_possible) {
                for (int i = 0; i < board_height; i++) {
                    for (int j = 0; j < board_width; j++) {
                        Tuple temp = new Tuple(i, j);
                        if (player1_pieces.contains(temp) || player2_pieces.contains(temp)) continue;
                        possible_moves.add(temp);
                    }
                }
            }

            ArrayList<BoardState> nextBoardStates = new ArrayList<BoardState>(possible_moves.size());

            for (Tuple move : possible_moves) {
                BoardState temp = new BoardState(state);
                temp.addLocation(state.getPlayer(), move);

                temp.setPlayerScore(state.getPlayer(), temp.getPlayerScore(temp.getPlayer()) +
                        board[move.getRow()][move.getCol()]);

                if (conquer_possible) {
                    ArrayList<Tuple> next_next_tuples = getAdjacentLocations(move);
                    for (Tuple t1 : next_next_tuples) {
                        if (player2_pieces.contains(t1)) {
                            temp.removeLocation(temp.getOtherPlayer(), t1);
                            temp.addLocation(temp.getPlayer(), t1);
                            temp.setPlayerScore(temp.getOtherPlayer(), temp.getPlayerScore(temp.getOtherPlayer()) -
                                    board[t1.getRow()][t1.getCol()]);
                            temp.setPlayerScore(temp.getPlayer(), temp.getPlayerScore(temp.getPlayer()) +
                                    board[t1.getRow()][t1.getCol()]);
                            temp.calculateUtility();
                        }
                    }
                    temp.setPlayer(temp.getOtherPlayer());
                    nextBoardStates.add(temp);
                } else {
                    temp.setPlayer(temp.getOtherPlayer());
                    nextBoardStates.add(temp);
                }
            }

            return nextBoardStates;
        }
    }

    private int stateUtility(BoardState state) {
        state.calculateUtility();
        return state.getScoreDifference();
    }


    private BoardState alphaBetaAgent(BoardState state){

    	 int alpha = Integer.MIN_VALUE;
    	 int beta = Integer.MAX_VALUE;
    	 if ((state.getPlayer() == Player.BLUE)){
    		 return max_value(state, alpha, beta,0 );
    	 }else{
    		 return min_value(state, alpha, beta,0 );
    	 }
    }

    private BoardState max_value(BoardState state, int alpha, int beta, int depth){

        if (gameOver(state) || depth == MAX_DEPTH) {
            state.calculateUtility();
            return state;
        }
        int max_utility = Integer.MIN_VALUE;
        BoardState desiredNextState = null;
        ArrayList<BoardState> next_moves = getNextMoves(state);
        for (BoardState next_state : next_moves) {
        		nodesExpanded++;
	        	BoardState curr_state = min_value(state, alpha, beta, depth+1);
	        	int utility = curr_state.getUtility();


            if (utility > max_utility ) {
	        		max_utility = utility;
	        		desiredNextState = curr_state;
	        		if (depth == 0) desiredNextState = next_state;
	        }

            if (max_utility >= beta) return desiredNextState;
            alpha = Math.max(alpha, max_utility);
        }
        return desiredNextState;
    }
    private BoardState min_value(BoardState state, int alpha, int beta, int depth){

    	  if (gameOver(state) || depth == MAX_DEPTH) {
              state.calculateUtility();
              return state;
          }
          int min_utility = Integer.MAX_VALUE;
          BoardState desiredNextState = null;
          ArrayList<BoardState> next_moves = getNextMoves(state);
          for (BoardState next_state : next_moves) {
        	  	nodesExpanded++;
  	        	BoardState curr_state = min_value(state, alpha, beta, depth+1);
  	        	int utility = curr_state.getUtility();

                  if (utility < min_utility ) {
                      min_utility = utility;
                      desiredNextState = curr_state;
                      if (depth == 0) desiredNextState = next_state;
                  }

              if (min_utility <= alpha) return desiredNextState;
              beta = Math.min(beta, min_utility);
          }
          return desiredNextState;

    }
//    private BoardState minimaxAgent(BoardState state, int alpha, int beta, int depth, boolean alphaBeta) {
//        if (gameOver(state) || depth == MAX_DEPTH) {
//            state.calculateUtility();
//            depth4Counter++;
//            return state;
//        }
//
//        ArrayList<BoardState> next_moves = getNextMoves(state);
//
//        BoardState desiredNextState = null;
//        if (state.getPlayer() == Player.BLUE) {
//            int max_utility = Integer.MIN_VALUE;
//            for (BoardState next_state : next_moves) {
//                nodesExpanded++;
//                BoardState curr_state = minimaxAgent(next_state, alpha, beta, depth + 1, alphaBeta);
//                //curr_state.calculateUtility();
//
//                if (curr_state.getUtility() > max_utility) {
/////                    System.out.println(max_utility +":" +curr_state.getUtility());
//                    desiredNextState = curr_state;
//                    if (depth == 0) desiredNextState = next_state;
//                    desiredNextState.calculateUtility();
//                    max_utility = desiredNextState.getUtility();
//                }
//
//                if (alphaBeta ){
//                    if (max_utility >= beta) {
//                        return desiredNextState;
//                    }
//                    //int desiredStateUtil = desiredNextState.getUtility();
//                    alpha = Math.max(alpha, max_utility);
//                }
//            }
//        }
//        else if (state.getPlayer() == Player.GREEN) {
//            int min_utility = Integer.MAX_VALUE;
//            for (BoardState next_state : next_moves) {
//                nodesExpanded++;
//                BoardState curr_state = minimaxAgent(next_state, alpha, beta, depth + 1,alphaBeta);
//
//                if (curr_state.getUtility() < min_utility) {
//                    desiredNextState = curr_state;
//                    if (depth == 0) desiredNextState = next_state;
//                    desiredNextState.calculateUtility();
//                    min_utility = desiredNextState.getUtility();
//                }
//                if (alphaBeta){
//                    if (min_utility <= alpha) {
//                        return desiredNextState;
//                    }
//                    //int desiredStateUtil = desiredNextState.getUtility();
//                    beta = Math.min(beta, min_utility);
//                }
//
//            }
//        }
//
//        return desiredNextState;
//    }

    /*new implementation*/
    private BoardState minimaxAgent(BoardState state, int alpha, int beta, int depth, boolean alphaBeta) {
        if (gameOver(state) || depth == MAX_DEPTH) {
            state.calculateUtility();
            depth4Counter++;
            return state;
        }

        ArrayList<BoardState> next_moves = getNextMoves(state);

        BoardState desiredNextState = null;
        if (state.getPlayer() == Player.BLUE) {
            int max_utility = Integer.MIN_VALUE;
            for (BoardState next_state : next_moves) {
                nodesExpanded++;
                BoardState curr_state = minimaxAgent(next_state, alpha, beta, depth + 1, alphaBeta);
                //curr_state.calculateUtility();

                if (curr_state.getUtility() > max_utility) {
///                    System.out.println(max_utility +":" +curr_state.getUtility());
                    // this masterpiece line change helps us!!!!
                    max_utility = curr_state.getUtility();
                    desiredNextState = curr_state;
                    if (depth == 0) desiredNextState = next_state;
                    //desiredNextState.calculateUtility();
                    //max_utility = desiredNextState.getUtility();
                }

                if (alphaBeta ){
                    if (max_utility >= beta) {
                        return desiredNextState;
                    }
                    //int desiredStateUtil = desiredNextState.getUtility();
                    alpha = Math.max(alpha, max_utility);
                }
            }
        }
        else if (state.getPlayer() == Player.GREEN) {
            int min_utility = Integer.MAX_VALUE;
            for (BoardState next_state : next_moves) {
                nodesExpanded++;
                BoardState curr_state = minimaxAgent(next_state, alpha, beta, depth + 1,alphaBeta);

                if (curr_state.getUtility() < min_utility) {
                    min_utility = curr_state.getUtility();
                    desiredNextState = curr_state;
                    if (depth == 0) desiredNextState = next_state;
                    desiredNextState.calculateUtility();
                    //min_utility = desiredNextState.getUtility();
                }
                if (alphaBeta){
                    if (min_utility <= alpha) {
                        return desiredNextState;
                    }
                    //int desiredStateUtil = desiredNextState.getUtility();
                    beta = Math.min(beta, min_utility);
                }

            }
        }

        return desiredNextState;
    }

    public static int getBoard_height() {
        return board_height;
    }

    public static int getBoard_width() {
        return board_width;
    }

    private BoardState minimaxAgent(BoardState state, boolean alphaBeta) { return minimaxAgent(state, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, alphaBeta );}

    public void playGame_Alpha(Player start_player){
        BoardState current_game_state = new BoardState(start_player);
        System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");
        while (!gameOver(current_game_state)) {
            current_game_state = alphaBetaAgent(current_game_state);
            System.out.println(current_game_state.getPlayerInfo(current_game_state.getPlayer()));
            System.out.println(current_game_state.getPlayerInfo(current_game_state.getOtherPlayer()));
            if (gameOver(current_game_state)) break;

            System.out.println(current_game_state);
            System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");

            current_game_state = alphaBetaAgent(current_game_state);
            System.out.println(current_game_state.getPlayerInfo(current_game_state.getPlayer()));
            System.out.println(current_game_state.getPlayerInfo(current_game_state.getOtherPlayer()));
            System.out.println(current_game_state);
            System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");

        }

        System.out.println(current_game_state);

        Player winner = current_game_state.currentWinner();

        if (winner == null)
            System.out.println("Game Tied");
        else
            System.out.println("Winner is " + winner);

        System.out.println("Game Over");
        System.out.println("NODES EXPANDED"+ nodesExpanded );
    }





    public void playGame(Player start_player, boolean alphaBetaP1, boolean alphaBetaP2) {
        chanceBlitz = false;
        boolean debug = true;//false;
        boolean alphaBeta;
        int player1NodeCount = 0;
        int player2NodeCount = 0;

        int p1Count = 0;
        int p2Count = 0;
        long timeElapsedPlayer1 = 0;
        long timeElapsedPlayer2 = 0;
        BoardState current_game_state = new BoardState(start_player);
        System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");
        while (!gameOver(current_game_state)) {
            nodesExpanded = 0;
            long startTime = System.currentTimeMillis();
            current_game_state = minimaxAgent(current_game_state, alphaBetaP1);

            p1Count++;
            long endTime = System.currentTimeMillis();
            timeElapsedPlayer1+=(endTime - startTime);
            player1NodeCount += nodesExpanded;
            if (debug) {
                System.out.println(current_game_state.getPlayerInfo(current_game_state.getPlayer()));
                System.out.println(current_game_state.getPlayerInfo(current_game_state.getOtherPlayer()));
            }
            if (gameOver(current_game_state)) break;
            if (debug) {
                System.out.println(current_game_state);
                System.out.println("PLAYER 1 : NODES EXPANDED: " + player1NodeCount + "| TIMETAKEN :" + timeElapsedPlayer1);
                System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");
            }

            nodesExpanded = 0;
            startTime = System.currentTimeMillis();
            current_game_state = minimaxAgent(current_game_state, alphaBetaP2);

            p2Count++;
            endTime = System.currentTimeMillis();
            timeElapsedPlayer2+= (endTime - startTime);

            player2NodeCount += nodesExpanded;
            if (debug) {
                System.out.println(current_game_state.getPlayerInfo(current_game_state.getPlayer()));
                System.out.println(current_game_state.getPlayerInfo(current_game_state.getOtherPlayer()));
                System.out.println(current_game_state);
                System.out.println("PLAYER 2 : NODES EXPANDED :" + player2NodeCount + "| TIMETAKEN : " + timeElapsedPlayer2);
                System.out.println("Player = " + current_game_state.getPlayer() + "'s turn");
            }

        }

        System.out.println(current_game_state);

        Player winner = current_game_state.currentWinner();
        Player loser = current_game_state.currentLoser();
        float avgP1MoveTime = timeElapsedPlayer1 / p1Count ;
        float avgP2MoveTime = timeElapsedPlayer2 / p2Count ;



        System.out.println("P1 SCORE : "+ current_game_state.getPlayerScore(start_player));
        System.out.println("P2 SCORE : "+ current_game_state.getPlayerScore(current_game_state.getOtherPlayer()));
        System.out.println("P1 Move Count : " + p1Count );
        System.out.println("P2 Move Count : " + p2Count);
        System.out.println("P1 Average Nodes Expanded per Move : " + player1NodeCount );
        System.out.println("P2 Average Nodes Expanded per Move : " + player2NodeCount );
        System.out.println("P1 Average Move Time : " + avgP1MoveTime);
        System.out.println("P2 Average Move Time : " + avgP2MoveTime);

        if (winner == null)
            System.out.println("Game Tied");
        else
            System.out.println("Winner is " + winner);

        System.out.println("Game Over");
    }


    public static void main(String[] args) throws IOException {

//        String file_name = "game_boards/bigest.txt";
        int DEPTH = 4;
        ArrayList<String> gameBoards = new ArrayList<String>();

        gameBoards.add("/Users/saikat/IdeaProjects/MP2/src/war_game/game_boards/Keren.txt");
        String file_name = gameBoards.get(0);
        //gameBoards.add("game_boards/Keren.txt");
//        gameBoards.add("game_boards/Narvik.txt");
//        gameBoards.add("game_boards/Sevastopol.txt");
//        gameBoards.add("game_boards/Smolensk.txt");
//        gameBoards.add("game_boards/Westerplatte.txt");

//        for ( String game_board : gameBoards){
//
//            String file_name = game_board;
//            System.out.println("************************ " + file_name + " *********************************");
            WarGame warGame = new WarGame(file_name, DEPTH);
            //warGame.playGame(Player.BLUE, false, false); // true = alphaBeta set
            warGame.playGame(Player.BLUE,true, true); // true = alphaBeta set
            //warGame.playGame_Alpha(Player.BLUE);
//            warGame.playGame(Player.BLUE,true, false); // true = alphaBeta set
//            warGame.playGame(Player.BLUE,false, true); // true = alphaBeta set
//            System.out.println("************************  DONE  *********************************");
//            System.out.println("");
//        }

    }
}
