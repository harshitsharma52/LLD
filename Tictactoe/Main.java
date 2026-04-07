package Tictactoe;

import Tictactoe.game.Game;
import Tictactoe.model.Player;
import Tictactoe.model.Symbol;
import Tictactoe.strategy.RowWinningStrategy;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter board size: ");
        int size = sc.nextInt();

        Player p1 = new Player("Player1", Symbol.X);
        Player p2 = new Player("Player2", Symbol.O);

        Game game = new Game(
                size,
                Arrays.asList(p1, p2),
                Arrays.asList(new RowWinningStrategy())
        );

        while (!game.isGameOver()) {
            System.out.println("Enter row and col:");
            int row = sc.nextInt();
            int col = sc.nextInt();

            game.playMove(row, col);
        }
    }
}




// ✅ S - Single Responsibility
// Board → only board logic
// Game → only game flow
// WinningStrategy → only winner logic
// ✅ O - Open/Closed
// Add new winning logic without modifying Game
// new DiagonalWinningStrategy()
// ✅ L - Liskov Substitution
// Any WinningStrategy can replace another
// ✅ I - Interface Segregation
// Small interface WinningStrategy
// ✅ D - Dependency Inversion
// Game depends on abstraction:
// List<WinningStrategy>

// not concrete classes

// 🎤 5. How to Explain in Interview (Short Version)

// Say this:

// 👉
// “I designed TicTacToe using OOP and SOLID principles.
// Game logic is separated from board and player.
// Winning logic is implemented using Strategy Pattern so we can easily extend it for rows, columns, diagonals without modifying existing code.
// This follows Open-Closed and Dependency Inversion principles.”

