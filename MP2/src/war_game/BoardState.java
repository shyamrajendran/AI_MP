package war_game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by manshu on 3/14/15.
 */
public class BoardState {
    private Player player;
    private ArrayList<Set<Tuple>> player_info;
    private int[] player_scores;
    private int state_utility;

    public BoardState(Player p) {
        player_info = new ArrayList<Set<Tuple>>(2);
        player_info.add(new LinkedHashSet<Tuple>());
        player_info.add(new LinkedHashSet<Tuple>());
        player_scores = new int[2];
        player_scores[0] = 0;player_scores[1] = 0;
        player = p;
        state_utility = 0;
    }


    public BoardState(BoardState state) {
        player_info = new ArrayList<Set<Tuple>>(2);
        player_info.add(new LinkedHashSet<Tuple>());
        player_info.add(new LinkedHashSet<Tuple>());
        this.player_scores = new int[2];
        this.player = state.player;
        this.player_scores[0] = state.player_scores[0]; this.player_scores[1] = state.player_scores[1];
        for (Tuple t : state.player_info.get(0)) {
            player_info.get(0).add(new Tuple(t.getRow(), t.getCol()));
        }
        for (Tuple t : state.player_info.get(1)) {
            player_info.get(1).add(new Tuple(t.getRow(), t.getCol()));
        }
        state_utility = state.getUtility();
    }

    public int getUtility() {
        return state_utility;
    }

    public void calculateUtility() {
        state_utility = getScoreDifference();
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOtherPlayer() {
        return Player.values()[(this.player.ordinal() + 1) % Player.values().length];
    }

    public Set<Tuple> getPlayerInfo(Player p) {
        return player_info.get(p.ordinal());
    }

    public int getPlayerScore(Player p) {
        return player_scores[p.ordinal()];
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void addLocation(Player p, Tuple tuple) {
        player_info.get(p.ordinal()).add(tuple);
    }

    public boolean searchLocation(Player p, Tuple tuple) {
        return player_info.get(p.ordinal()).contains(tuple);
    }

    public void removeLocation(Player p, Tuple tuple) {
        player_info.get(p.ordinal()).remove(tuple);
    }

    public void setPlayerScore(Player p, int player_score) {
        player_scores[p.ordinal()] = player_score;
    }

    public int getTotalPieces() {
        return player_info.get(0).size() + player_info.get(1).size();
    }

    public int getScoreDifference() {
        return player_scores[0] - player_scores[1];
    }

    public Player currentWinner() {
        if (player_scores[0] > player_scores[1])
            return Player.values()[0];
        else if (player_scores[0] < player_scores[1])
            return Player.values()[1];
        else
            return null;
    }
    public Player currentLoser(){
        if (player_scores[0] < player_scores[1])
            return Player.values()[0];
        else if (player_scores[0] > player_scores[1])
            return Player.values()[1];
        else
            return null;
    }
    @Override
    protected void finalize() throws Throwable {
        Iterator<Tuple> iterator = player_info.get(0).iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }
        iterator = player_info.get(1).iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }
        player_info.get(0).clear();
        player_info.get(1).clear();
        player_info = null;
        player_scores = null;
        player = null;
    }

    @Override
    public String toString() {
    		if (false){
    			StringBuffer stringBuffer = new StringBuffer();
    			stringBuffer.append("\n=================================\n");
    			stringBuffer.append("Player = ").append(player).append("\n");
    			stringBuffer.append("Player 1 Score = ").append(player_scores[0]).append("\n");
    			stringBuffer.append("Player 2 Score = ").append(player_scores[1]).append("\n");
    			stringBuffer.append("Player 1 Pieces = ");
    			for (Tuple t : player_info.get(0)) {
    				stringBuffer.append(t).append(", ");
    			}
    			stringBuffer.append("\n");
    			stringBuffer.append("Player 2 Pieces = ");
    			for (Tuple t : player_info.get(1)) {
    				stringBuffer.append(t).append(", ");
    			}
    			stringBuffer.append("\n=================================\n");

    			return stringBuffer.toString();
        } else{

        	StringBuffer stringBuffer = new StringBuffer();
          String[][] multi = new String[WarGame.getBoard_height()][WarGame.getBoard_width()];
          for(int i=0; i<WarGame.getBoard_height(); i++){
      	  	for(int j = 0;j<WarGame.getBoard_width() ; j++){
      	  		multi[i][j] = "  ";
      	  	}
          }
          int row, col;
          for (Tuple t : player_info.get(0)) {
		    	  row = t.getRow();
		    	  col = t.getCol();
		    	  multi[row][col] = "B ";
		    	  //            stringBuffer.append(t).append(", ");
          }
          for (Tuple t : player_info.get(1)) {
	        	  row = t.getRow();
	        	  col = t.getCol();
	        	  multi[row][col] = "G ";
          }stringBuffer.append("Player = ").append(player).append("\n");
      	stringBuffer.append("Player 1 Score = ").append(player_scores[0]).append("\n");
		stringBuffer.append("Player 2 Score = ").append(player_scores[1]).append("\n");
          for(int i=0; i< WarGame.getBoard_height(); i++){
        	  	for(int j = 0;j<WarGame.getBoard_width() ; j++){
        	  		stringBuffer.append(multi[i][j]);
        	  	}
        	  	stringBuffer.append("\n");
          }
          stringBuffer.append("\n=================================\n");
        	return stringBuffer.toString();

        }
    }

    public static void main(String[] args) {
        BoardState b = new BoardState(Player.BLUE);
        b.addLocation(Player.BLUE, new Tuple(1, 2));
        b.addLocation(Player.GREEN, new Tuple(2, 3));
        b.setPlayerScore(Player.BLUE, 20);
        System.out.println(b);

        BoardState b1 = new BoardState(b);
        b1.setPlayerScore(Player.GREEN, 30);
        b1.setPlayerScore(Player.BLUE, 40);
        b1.addLocation(Player.BLUE, new Tuple(5, 6));
        b1.addLocation(Player.GREEN, new Tuple(2, 4));
        b1.removeLocation(Player.GREEN, new Tuple(2, 3));
        b1.setPlayer(b1.getOtherPlayer());
        System.out.println(b1.searchLocation(Player.GREEN, new Tuple(2, 5)));

        System.out.println(b);
        System.out.println(b1);

    }
}
