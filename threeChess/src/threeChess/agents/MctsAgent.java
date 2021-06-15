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
public class MctsAgent extends Agent{

    private static final String name = "MctsAgent";
    private static final Random random = new Random();
    private Colour playercolour;
    private Node root;

    /**
     * A no argument constructor, 
     * required for tournament management.
     * **/
    public MctsAgent(){
    }
    //an inner class Node for creation of the tree for MCTS.
    private class Node {
        private Node parentNode;
        private ArrayList<Node> childNodes;
        private Position[] move;
        private Board state;
        private double reward;
        private int visits;
        public Node(Board s) {
            this.state = s;
            childNodes = new ArrayList<>();
            reward = 0;
            visits = 0;
        }

        public void setParentAndMove(Node parentNode,Position[] move) {
            this.parentNode = parentNode;
            this.move = move;
        }

        public void addVisit(double result) {
            visits++;
            reward += result;
        }

        public Node getParentNode() {
            return parentNode;
        }

        public Board getState() {
            return state;
        }

        public Position[] getMove() {
            return move;
        }

        public ArrayList<Node> getChildNodes() {
            return childNodes;
        }

        public int getVisits() {
            return visits;
        }

        public double getReward() {
            return reward;
        }
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
        playercolour = board.getTurn();
        root = new Node(board);
        long starttime = System.currentTimeMillis();
        //Limit to about 5seconds per move. 
        while(System.currentTimeMillis() < starttime +5000) {
            Node newNode = selection();
            double value = simulate(newNode);
            backPropagation(newNode, value);
        }

        return best_mov_in_tree();
    }

    /**
     * Calculates the UCB value of the node used in most MCTS
     * 
     * 
     * @param node the current node in the tree representing the different states of the game
     * @param parentVisits the no. of time it's been visited
     * @return UCB value. 
     */
    private double UCBval(Node node, int parentVisits) {
        if (node.getVisits() == 0) {
            //Divided by 0 would give infinity. 
            return Integer.MAX_VALUE;
        }
        //double avg = (node.getVisits() * node.getState().score(playercolour))/ (node.getVisits()+1);
        return node.getReward()/ (double) node.getVisits()
        + Math.sqrt(2.0 * Math.log(parentVisits)/(double) node.getVisits());
        //return avg +Math.sqrt(2.0 * Math.log(parentVisits) / (double) node.getVisits());
    }

    /**
     * Selects the node based on UCB value.
     * 
     * 
     * @param nothing
     * @return node with the highest UCB value.
     */
    private Node selection() {
        // root node starting
        Node node = root;

        // While traversing, select the one with the highest UCB value.
        while (node.getChildNodes().size() != 0){
            int parentVisits = node.getVisits();
            node = Collections.max(node.getChildNodes(), Comparator.comparing(c -> UCBval(c, parentVisits)));
        }
        // Expand node if game have not ended
        if (!node.getState().gameOver()){
            expand(node);
            node = randomfromlist(node.getChildNodes());
        }

        return node;
    }

    /**
     * Expands the node by trying out all the different legal moves.
     * 
     * 
     * @param parent the node to be expanded
     * @return nothing
     */
    private void expand(Node parent){
        Board boardState = parent.getState();
        Position[][] vmoves = validMoves(boardState);
        Board newboard = null;

        for(int i=0; i<vmoves.length;i++){
            try{
                newboard = (Board) boardState.clone();
            }catch (CloneNotSupportedException p){}
            try{
                newboard.move(vmoves[i][0],vmoves[i][1]);
            }catch(ImpossiblePositionException o){}
            Node newNode = new Node(newboard);
            newNode.setParentAndMove(parent, new Position[]{vmoves[i][0],vmoves[i][1]});
            parent.getChildNodes().add(newNode);
        }
    }

    /**
     * Simulation
     * 
     * @param node the node which would be converted into a board state
     * @return score for making the move.
     */
    private double simulate(Node node){
        // For the moment, random playout
        Board boardState = node.getState();
        //double avg = 0;
        int counter=0;
        while (!boardState.gameOver() && counter<10) {
            Position[][] vmoves = validMoves(boardState);
            int n = random.nextInt(vmoves.length);
            try{
                boardState.move(vmoves[n][0],vmoves[n][1]);
            }catch(ImpossiblePositionException o){}
            counter++;
        }

        // Evaluation
        if (boardState.getWinner()==playercolour){
            return Double.MAX_VALUE;
        }else if(boardState.getLoser() == playercolour){
            return -Double.MAX_VALUE;
        }else{
            //if game still on going or am the player that didnt have his king stolen.
            return boardState.score(playercolour); 
        }
    }
    /**
     * After determining the value of the newly added node, the remaining tree must be updated.
     * 
     * @param node the node 
     * @param result the evaluated score for backpropagation up the tree
     * @return nothing
     */
    private void backPropagation(Node node, double result) {
        while (node!= null){
            node.addVisit(result);
            node = node.getParentNode();
        }
    }
    /**
     * Deciding the best score of the node for the root node to "move" into.
     * 
     * @param nothing 
     * @return node with the best score. 
     */
    private Position[] best_mov_in_tree(){
        Node bestNode= Collections.max(root.getChildNodes(),Comparator.comparing(Node::getVisits));
        return bestNode.getMove();
    }
    //Helper function to get a random element from a list.
    private <T> T randomfromlist(List<T> l){
        return l.get(randomnum(0, l.size()));
    }

    //Another helper function to get a random number between min & max
    private static int randomnum(int minimum, int maximum){
        if (minimum >= maximum) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r= new Random();
        return r.nextInt(maximum - minimum)+ minimum;
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

