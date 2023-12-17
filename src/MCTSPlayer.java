/**
 * Create bY Mário Nogueira
 * Version 1.2.1
 * Date 30/11/2023
 * Algoritm Monte Carlo Tree Search for M N K
 */

import java.util.*;

public class MCTSPlayer {
    private static final int SIMULATION_COUNT = 1000000;// Número de simulações por iteração MCTS

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
            visit = 0;
            if (this.father != null){
                if (this.father.minMax == -1){
                    minMax = 1;
                }else{
                    minMax = -1;
                }
            }else {
                minMax = 1;
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
        public State getChildWithMaxScore() {
            return this.Childrens.poll();
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

        for (int i = 0; i < SIMULATION_COUNT; i++) {
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
        return root.getChildWithMaxScore();
    }

    private static void backpropagation(State actual, double simulationResult) {
        while(actual.father != null){
            State father = actual.father;

            modify(actual,simulationResult);
            modifyQueue(father,actual);

            actual = father;
        }
    }

    //TODO: verificar a criação das priorityQueues
    private static void criationQueue(State selectedNode) {

        if (selectedNode.father != null){
            if(selectedNode.father.minMax == -1){ //se o pai for max cria no children uma priority queue ordenada pelo min
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> (int) Math.signum(s1.getFormulaResult() - s2.getFormulaResult()));
            }else{ //se o pai for min cria no children uma priority queue ordenada pelo max
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> (int) Math.signum(s2.getFormulaResult() - s1.getFormulaResult()));
            }
        }else {
            //TODO: modificar o metodo isEmptyBoard porque nao posso ter isto assim
            if(selectedNode.layout.isEmptyBoard()){
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> (int) Math.signum(s1.getFormulaResult() - s2.getFormulaResult()));
            }else{
                selectedNode.Childrens = new PriorityQueue<>(10,
                        (s1, s2) -> (int) Math.signum(s2.getFormulaResult() - s1.getFormulaResult()));
            }
        }
    }

    private static void modify(State toModify, double winning) {
        toModify.visit += 1;
        toModify.simulations += 1;
        toModify.winningResults = adicionarElemento(toModify.winningResults, winning);
        toModify.wining = calcularMedia(toModify.winningResults);
        toModify.formulaResult = countResult(toModify);
    }

    private static void printing(double[] array){
        for(int i = 0; i < array.length ; i++){
            System.out.print(array[i] + ",");
        }
        System.out.print("\n");
    }
    private static double[] adicionarElemento(double[] arrayOriginal, double elemento) {
        double[] result;
        if (arrayOriginal != null){
            int tamanhoOriginal = arrayOriginal.length;
            result = new double[tamanhoOriginal + 1];

            for (int i = 0; i < tamanhoOriginal; i++) {
                result[i] = arrayOriginal[i];
            }
            result[tamanhoOriginal] = elemento;
        }else{
            result = new double[1];
            result[0] = elemento;
        }
        return result;
    }

    private static double calcularMedia(double[] array) {
        double soma = 0;

        for (double elemento : array) {
            soma += elemento;
        }

        if (array.length > 0) {
            return soma / array.length;
        } else {
            return 0.0;
        }
    }

    private static double countResult(State n) {
        double result = 0;
        if (n.visit != 0 && n.father != null) {
            result = (n.wining / n.visit) + 1.44 * Math.sqrt(Math.log(n.father.simulations) / n.visit);
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

    //TODO: verificar esta função
    static State selection(State root){
        State result = root;
        while(result != null && result.Childrens != null && !result.Childrens.isEmpty()){
            result = result.Childrens.poll();
            result.father.Childrens.add(result);
        }
        return result;
    }
}