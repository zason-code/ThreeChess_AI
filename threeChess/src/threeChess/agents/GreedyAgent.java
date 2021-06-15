package threeChess.agents;

import threeChess.*;

import java.util.Random;
import java.util.*;
/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * Done by: Jason Ho, 22360143
 * **/ 
public class GreedyAgent extends Agent{

    private static final String name = "GreedyAgent";
    //private static final Random random = new Random();

    /**
     * A no argument constructor, 
     * required for tournament management.
     * **/
    public GreedyAgent(){
    }
    /**
     * Returns a list of all possible legal moves the player can make
     * 
     * 
     * @param board the current state of the game.
     * @return a 2D array containing the list of all legal moves.
     */
    private Position[][] validMoves(Board board) {
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        Position[] ends = Position.values();
        ArrayList<Position[]> valid_moves = new ArrayList<>();

        for (Position piece: pieces){
            for (Position end: ends){
                Position[] currentmove = new Position[]{piece,end};
                if (board.isLegalMove(piece,end) && !valid_moves.contains(currentmove)){ 
                    valid_moves.add(currentmove);
                }
            }
        }
        //Move: Start in col 0, end in col 1.
        return valid_moves.toArray(new Position[0][0]);
    }

    /**
     * Play a move in the game. 
     * The agent is given a Board Object representing the position of all pieces, 
     * the history of the game and whose turn it is. 
     * They respond with a move represented by a pair (two element array) of positions: 
     * the start and the end position of the move.
     * @param board The representation of the game state.
     * @return a two element array of Position objects, where the first element is the 
     * current position of the piece to be moved, and the second element is the 
     * position to move that piece to.
     * **/
    public Position[] playMove(Board board){
        Position legalmoves[][]= validMoves(board);
        //start position
        Position s=null;
        //end position
        Position e=null;

        int score =0;
        // Trying out all the possible legal moves in the list generated, get the highest one.
        for(int i=0; i<legalmoves.length;i++){
            try{
                Board new_board = (Board) board.clone();
                Position start = legalmoves[i][0];
                Position end =legalmoves[i][1];
                try{
                    new_board.move(start,end);
                }catch(ImpossiblePositionException o){}

                int move_score = new_board.score(board.getTurn());
                // Simple Comparison
                if(score<=move_score){
                    score = move_score;
                    s = start;
                    e = end;

                }
            }catch (CloneNotSupportedException p){}

        }
        return new Position[] {s,e};
    }

    
    /**
     * @return the Agent's name, for annotating game description.
     * **/ 
    public String toString(){return name;}

    /**
     * Displays the final board position to the agent, 
     * if required for learning purposes. 
     * Other a default implementation may be given.
     * @param finalBoard the end position of the board
     * **/
    public void finalBoard(Board finalBoard){}

}

