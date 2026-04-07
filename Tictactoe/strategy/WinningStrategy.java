package Tictactoe.strategy;

import Tictactoe.model.Board;
import Tictactoe.model.Symbol;

public interface WinningStrategy {
    boolean checkWinner(Board board, Symbol symbol);
}