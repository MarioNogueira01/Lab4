/**
 * Create bY Mário Nogueira
 * Version 1.2.1
 * Date 30/11/2023
 * Algoritm Monte Carlo Tree Search for M N K
 */

import java.util.*;


/**
 * Represents a MCTS player.
 */
public class MCTSPlayer {

    private static final long simulationTime = 4500;//tempo em milisegundos
    /**
     * Represents a node from MCTS tree
     */
    public static class State implements Cloneable {
        private Ilayout layout;
        private State father;

        private double formulaResult;

        private double wining;

        private double[] winningResults;
        private int visit;
        private int simulations;


        protected PriorityQueue<State> Childrens = null;

        int minMax;

        int move;

        /**
         * Constructor for State class
         * l nó que estou
         * n nó pai
         */
        public State(Ilayout l, State n) {
            layout = l;
            father = n;
            visit = 1;
            if (this.father != null){
                if (this.father.minMax == -1){
                    minMax = 1;
                }else{
                    minMax = -1;
                }
            }else {
                minMax = -1;
            }
            move = l.getMove();
        }

        /**
         * Returns a string representation of the state.
         */
        public String toString() {
            return layout.toString();
        }


        /**
         * Calculates the hash code for the state.
         */
        public int hashCode() {
            return toString().hashCode();
        }

        public double getFormulaResult() {
            return formulaResult;
        }

        public int getMove() {
            return this.move;
        }
    }

    static int play(Ilayout board) {
        // Crie um nó raiz com o estado atual do tabuleiro

        State rootNode = new State(board,null);

        State bestChild = monteCarloTreeSearch(rootNode);

        return (bestChild != null) ? bestChild.getMove() : -1;
    }


    static public State monteCarloTreeSearch(State root) {
        long startTime = System.currentTimeMillis();
        int iteracoes = 0;
        while(checkTime(startTime,simulationTime)){
            iteracoes += 1;
            State selectedNode = selection(root);

            if (!selectedNode.layout.isGameOver()) {

                criationQueue(selectedNode);
                List<Ilayout> children = selectedNode.layout.children();

                for (Ilayout e : children) {
                    State nn = new State(e, selectedNode);
                    selectedNode.Childrens.add(nn);

                    double simulationResult = simulation(nn);

                    backpropagation(nn, simulationResult);
                }
            }
        }
        return root.Childrens.poll();
    }

    private static boolean checkTime(long startTime, long simulationTime) {
        long endTime = System.currentTimeMillis();
        long totalTimeMili = endTime - startTime;
        if (totalTimeMili < simulationTime)
            return true;
        else
            return false;
    }

    private static void backpropagation(State actual, double simulationResult) {
        while(actual.father != null){
            State father = actual.father;

            modify(actual,simulationResult);
            modifyQueue(father,actual);

            actual = father;
        }
        modify(actual, -2);
    }

    //TODO: verificar a criação das priorityQueues
    private static void criationQueue(State selectedNode) {

        if (selectedNode.father != null){
            if(selectedNode.father.minMax == 1){ //se o pai for min cria no children uma priority queue ordenada pelo min
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> Double.compare (s2.getFormulaResult(), s1.getFormulaResult())); // MIN
            }else{ //se o pai for min cria no children uma priority queue ordenada pelo max
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> Double.compare (s1.getFormulaResult(), s2.getFormulaResult())); // MAX
            }
        }else{
            //TODO: fazer a parte do jogo
            //if(selectedNode.layout.isEmptyBoard()){
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> Double.compare (s2.getFormulaResult(), s1.getFormulaResult()));
            /*}else{ //se o pai for min cria no children uma priority queue ordenada pelo max
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> Double.compare (s1.getFormulaResult(), s2.getFormulaResult()));
            }*/
        }
    }

    private static void modify(State toModify, double winning) {
        toModify.visit += 1;
        toModify.simulations += 1;
        toModify.wining += winning;

        if (toModify.father != null && toModify.father.simulations == 0){
            toModify.father.simulations = 1;
        }

        if (winning == -2){
            if (toModify.simulations > toModify.Childrens.size()){
                toModify.simulations -= 1;
            }else if(toModify.simulations != toModify.Childrens.size()){
                toModify.simulations += 1;
            }
        }

        toModify.formulaResult = countResult(toModify);
    }

    private static double countResult(State n) {
        double result = 0;
        if (n.visit != 0 && n.father != null) {
            result = (n.wining / n.simulations) + 1.44 * Math.sqrt(Math.log(n.father.simulations) / n.simulations);
        }
        return result;
    }

    private static void modifyQueue(State father, State newNow) {
        PriorityQueue<State> temp = new PriorityQueue<>(father.Childrens);
        for (int i = 0; i < temp.size();i++){
            State tent = temp.poll();
            if (tent.layout.equals(newNow.layout)){
                father.Childrens.remove(tent);
                father.Childrens.add(newNow);
                break;
            }
        }
         // Remover o elemento atual
         // Adicionar o novo valor à fila
    }

    private static double simulation(State e) {
        Board simulation =(Board) e.layout.clone();
        int position;
        double result;
        while (!simulation.isGameOver()){
            position = XAgent.play(simulation);
            simulation.move(position);
        }
        Ilayout.ID winner = simulation.getWinner();
        if (winner == Ilayout.ID.X){
            result = 1;
        } else if (winner == Ilayout.ID.Blank) {
            result = 0.5;
        }else {
            result = -1;
        }

        return result;
    }

    static State selection(State root){
        State result = root;
        while(result != null && result.Childrens != null && !result.Childrens.isEmpty()){
            result = result.Childrens.poll();
            result.father.Childrens.add(result);
        }
        return result;
    }
}