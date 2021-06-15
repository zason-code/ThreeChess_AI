package threeChess.agents;

import threeChess.*;

import java.util.*;
/**
 * Done by: Jason Ho, 22360143
 */
public class ParanoidAgent extends Agent{

    private static final String name = "ParanoidAgent2.0";
    private static final int DEPTH = 3;
    Colour myColour = null;
    public ParanoidAgent() {
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
        // Set as my player's colour permanently
        myColour = board.getTurn();
        return bestmove(board);
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
     * Returns an array containing the highest score/optimal move found
     * 
     * 
     * @param board the current state of the game.
     * @return best_move, an array containing the highest score/optimal move found
     */
    private Position[] bestmove(Board board) {
        Position[] best_move = null;
        int max = Integer.MIN_VALUE;

        try {

            Position[][] legalmov = validMoves(board);
            for (int i = 0; i < legalmov.length; i++) {
                Position[] move = {legalmov[i][0],legalmov[i][1]};
                Board new_board = (Board) board.clone();
                new_board.move(move[0], move[1]);
                int value = minimax(new_board, DEPTH,0,0);
                if(value > max) {
                    max = value;
                    best_move = move;
                }
            }
        } catch (ImpossiblePositionException | CloneNotSupportedException e) {}

        return best_move;
    }
    /**
     * Paranoid Algorithm implemented using minimax. The order would be max,min,min.
     * 
     * 
     * @param board the current state of the game.
     * @param depth how many moves the agent would look ahead
     * @param alpha for pruning
     * @param beta for pruning
     * @return the score.
     */
    private int minimax(Board board, int depth,int alpha, int beta) {
        if(depth == 0 || board.gameOver()) {
            int score = 2 * board.score(myColour)-board.score(Colour.values()[(myColour.ordinal() + 1) % 3])
                -board.score(Colour.values()[(myColour.ordinal() + 2) % 3]);
            return score;
        }

        Position[][] possiblemov = validMoves(board);

        //MaxPlayer is me.
        if(board.getTurn() == myColour) {
            int max_eval = Integer.MIN_VALUE;
            for (int i = 0; i < possiblemov.length; i++) {
                try {
                    Board new_board = (Board) board.clone();
                    new_board.move(possiblemov[i][0], possiblemov[i][1]);
                    int eval = minimax(new_board, depth-1,alpha, beta);
                    max_eval = Math.max(eval, max_eval);
                    alpha = Math.max(alpha, eval);
                    //pruning.
                    if(beta <= alpha)
                        break;
                } catch (ImpossiblePositionException | CloneNotSupportedException e) {}
            }
            return max_eval;
        }
        else {
            //the 2 opponents would come here.
            int min_eval = Integer.MAX_VALUE;
            for (int i = 0; i < possiblemov.length; i++) {
                try{
                    Board new_board = (Board) board.clone();
                    new_board.move(possiblemov[i][0],possiblemov[i][1]);
                    int eval = minimax(new_board,depth-1,alpha, beta);
                    min_eval = Math.min(eval, min_eval);
                    beta = Math.min(beta, eval);
                    if(beta <= alpha)
                        break;
                }catch (ImpossiblePositionException | CloneNotSupportedException e) {}

            }return min_eval;
        }
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

