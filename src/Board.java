import java.util.*;
// import java.util.ArrayList;

/**
 * Represents a board.
 */
public class Board implements Ilayout,Cloneable{
    private ID[][] board;
    private ID playersTurn;
    private ID winner;
    private HashSet<Integer> movesAvailable;

    private int moveCount;
    private boolean gameOver;

    private int move;


    Board() {
        board = new ID[N][M];
        movesAvailable = new HashSet<>();
        reset();
    }

    /**
     * Set the cells to be blank and load the available moves (all the moves are
     * available at the start of the game).
     */
    private void initialize () {
        for (int row = 0; row < N; row++)
            for (int col = 0; col < M; col++) {
                board[row][col] = ID.Blank;
            }
        movesAvailable.clear();

        for (int i = 0; i < N*M; i++) {
            movesAvailable.add(i);
        }
    }

    /**
     * Restart the game with a new blank board.
     */
    public void reset() {
        moveCount = 0;
        gameOver = false;
        playersTurn = ID.X;
        winner = ID.Blank;
        initialize();
    }

    /**
     * Places an X or an O on the specified index depending on whose turn it is.
     * @param index     position starts in 0 and increases from left to right and from top to bottom
     * @return          true if the move has not already been played
     */
    public boolean move (int index) {
        return move(index % M, index / M);
    }

    public int getMove(){
        return this.move;
    }

    /**
     * Places an X or an O on the specified location depending on who turn it is.
     * @param x         the x coordinate of the location
     * @param y         the y coordinate of the location
     * @return          true if the move has not already been played
     */
   public boolean move (int x, int y) {

        if (gameOver) {
            throw new IllegalStateException("Game over. No more moves can be played.");
        }

        if (board[y][x] == ID.Blank) {
            board[y][x] = playersTurn;
        } else {
            return false;
        }

        moveCount++;
        movesAvailable.remove(y * N + x);

       // Verificar se há um vencedor após o movimento
       if (checkWinner(x, y)) {
           winner = playersTurn;
           gameOver = true;
       }

        // The game is a draw.
        if (moveCount == N * M) {
            winner = ID.Blank;
            gameOver = true;
        }

        // Check for a winner


        playersTurn = (playersTurn == ID.X) ? ID.O : ID.X;
        return true;
    }

    //TODO mudar este função para ser para uma matriz diferente porque isto ta a ver 4x4 ver o que esta a falhar nas linhas
    private boolean checkWinner(int x, int y) {
        // Verificar linha
        if (board[y][0] == playersTurn && board[y][1] == playersTurn && board[y][2] == playersTurn && board[y][3] == playersTurn) {
            return true;
        }

        // Verificar coluna
        if (board[0][x] == playersTurn && board[1][x] == playersTurn && board[2][x] == playersTurn && board[3][x] == playersTurn) {
            return true;
        }

        // Verificar diagonal principal
        if (x == y && board[0][0] == playersTurn && board[1][1] == playersTurn && board[2][2] == playersTurn && board[3][3] == playersTurn) {
            return true;
        }

        // Verificar diagonal secundária
        if (x + y == 3 && board[0][3] == playersTurn && board[1][2] == playersTurn && board[2][1] == playersTurn && board[3][0] == playersTurn) {
            return true;
        }

        return false;
    }

    /**
     * Check to see if the game is over (if there is a winner or a draw).
     * @return          true if the game is over
     */
    public boolean isGameOver () {
        return gameOver;
    }



    /**
     * Check to see who's turn it is.
     * @return          the player who's turn it is
     */
    public ID getTurn () {
        return playersTurn;
    }

    /**
     * @return          the player who won (or Blank if the game is a draw)
     */
    public ID getWinner () {
        if (!gameOver) {
            throw new IllegalStateException("Not over yet!");
        }
        return winner;
    }

    /**
     * Get the indexes of all the positions on the board that are empty.
     * @return          the empty cells
     */
    public HashSet<Integer> getAvailableMoves () {
        return movesAvailable;
    }



    /**
     * @return  an deep copy of the board
     */
    public Object clone () {
    	try {
	        Board b = (Board) super.clone();

	        b.board = new ID[N][M];
	        for (int i=0; i<N; i++)
	        	for (int j=0; j<M; j++)
	        		b.board[i][j] = this.board[i][j];

	        b.playersTurn       = this.playersTurn;
	        b.winner            = this.winner;
	        b.movesAvailable    = new HashSet<Integer>();
	        b.movesAvailable.addAll(this.movesAvailable);
	        b.moveCount         = this.moveCount;
	        b.gameOver          = this.gameOver;
	        return b;
    	}
    	catch (Exception e) {
    		throw new InternalError();
    	}
    }

    public Object cloneRotated () {
        try {
            Board b = (Board) super.clone();

            b.board = new ID[M][N];
            for (int i=0; i<N; i++)
                for (int j=0; j<M; j++)
                    b.board[j][N - 1 - i] = this.board[i][j];

            b.playersTurn       = this.playersTurn;
            b.winner            = this.winner;
            b.movesAvailable    = new HashSet<Integer>();
            b.movesAvailable.addAll(this.movesAvailable);
            b.moveCount         = this.moveCount;
            b.gameOver          = this.gameOver;
            return b;
        }
        catch (Exception e) {
            throw new InternalError();
        }
    }

    @Override
    public String toString () {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < M; x++) {
                if (board[y][x] == ID.Blank) {
                    sb.append("-");
                } else {
                    sb.append(board[y][x].name());
                }
                sb.append(" ");
            }
            if (y != N -1) {
                sb.append("\n");
            }
        }
        return new String(sb);
    }

     /**
         *
         * @return the children of the receiver.
     */
     public List<Ilayout> children() {
         List<Ilayout> children = new ArrayList<>();
         for (int index : movesAvailable) {

             Board newBoard = (Board) this.clone();

             newBoard.move = index;
             newBoard.move(index);
             if (checkRotation(children,newBoard)) {
                 children.add(newBoard);
             }
         }
         return children;
     }

    private boolean checkRotation(List<Ilayout> children, Board newBoard) {
         Board rotated =(Board) newBoard.cloneRotated();
        //Verificar se existe a board igual com rotação de 90 graus
         if (children.contains(rotated)){
             return false;
         }else {
             rotated = (Board) rotated.cloneRotated();
         }
        //Verificar se existe a board igual com rotação de 180 graus
        if (children.contains(rotated)){
            return false;
        }else {
            rotated = (Board) rotated.cloneRotated();
        }
        //Verificar se existe a board igual com rotação de 270 graus
        if (children.contains(rotated)){
            return false;
        }else {
            return true;
        }
    }


    @Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null) return false;
		if (getClass() != other.getClass()) return false;
		Board that = (Board) other;

		for (int x=0; x<N; x++)
            for (int y=0; y<M; y++)
            	if (board[x][y] != that.board[x][y]) return false;
        return true;
	}

	@Override
	public int hashCode() {
		return board.hashCode();
	}

	public boolean isBlank (int index) {
		int x=index/M;
		int y=index%M;
        return (board[x][y] == ID.Blank);
	}

    public ID[][] getBoard(){
         return this.board;
    }

    public boolean isEmptyBoard(){
         int possibleMoves = N * M;
         int avaibleMoves = this.movesAvailable.size();
         if (avaibleMoves == possibleMoves){
             return true;
         }
         return false;
    }
}
