package Tictactoe.strategy;

import Tictactoe.model.Board;
import Tictactoe.model.Cell;
import Tictactoe.model.Symbol;

public class RowWinningStrategy implements WinningStrategy {

    @Override
    public boolean checkWinner(Board board, Symbol symbol) {
        Cell[][] grid = board.getGrid();

        for (Cell[] row : grid) {
            boolean win = true;
            for (Cell cell : row) {
                if (cell.getSymbol() != symbol) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }
        return false;
    }
}