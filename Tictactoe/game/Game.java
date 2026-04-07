package Tictactoe.game;

import Tictactoe.model.Board;
import Tictactoe.model.Player;
import Tictactoe.strategy.WinningStrategy;
import java.util.List;

public class Game {

    private Board board;
    private List<Player> players;
    private int currentPlayerIndex;
    private GameStatus status;
    private List<WinningStrategy> strategies;

    public Game(int size, List<Player> players, List<WinningStrategy> strategies) {
        this.board = new Board(size);
        this.players = players;
        this.strategies = strategies;
        this.status = GameStatus.IN_PROGRESS;
    }

    public void playMove(int row, int col) {
        Player player = players.get(currentPlayerIndex);

        boolean moveSuccess = board.makeMove(row, col, player.getSymbol());

        if (!moveSuccess) {
            System.out.println("Invalid move, try again");
            return;
        }

        board.printBoard();

        // check winner
        for (WinningStrategy strategy : strategies) {
            if (strategy.checkWinner(board, player.getSymbol())) {
                status = GameStatus.WIN;
                System.out.println(player.getName() + " wins!");
                return;
            }
        }

        // switch player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public boolean isGameOver() {
        return status != GameStatus.IN_PROGRESS;
    }
}



