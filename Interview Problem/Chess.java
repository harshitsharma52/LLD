import java.util.*;

// 👤 Player
class Player {
    String name;
    char symbol;

    public Player(String name, char symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}

// 🧱 Board
class Board {
    int size;
    char[][] grid;

    public Board(int size) {
        this.size = size;
        grid = new char[size][size];

        for (char[] row : grid) {
            Arrays.fill(row, '-');
        }
    }

    public boolean placeMove(int row, int col, char symbol) {
        if (grid[row][col] == '-') {
            grid[row][col] = symbol;
            return true;
        }
        return false;
    }

    public void printBoard() {
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public boolean checkWin(int row, int col, char symbol) {
        boolean winRow = true, winCol = true, winDiag = true, winAntiDiag = true;

        for (int i = 0; i < size; i++) {
            if (grid[row][i] != symbol) winRow = false;
            if (grid[i][col] != symbol) winCol = false;
            if (grid[i][i] != symbol) winDiag = false;
            if (grid[i][size - i - 1] != symbol) winAntiDiag = false;
        }

        return winRow || winCol || winDiag || winAntiDiag;
    }

    public boolean isFull() {
        for (char[] row : grid) {
            for (char cell : row) {
                if (cell == '-') return false;
            }
        }
        return true;
    }
}

// 🎮 Game
class Game {
    Board board;
    List<Player> players;

    public Game(int size, List<Player> players) {
        this.board = new Board(size);
        this.players = players;
    }

    public void play() {
        Scanner sc = new Scanner(System.in);
        int turn = 0;

        while (true) {
            Player player = players.get(turn % players.size());

            board.printBoard();
            System.out.println(player.name + " (" + player.symbol + ") enter row and col:");

            int row = sc.nextInt();
            int col = sc.nextInt();

            // ✅ input validation
            if (row < 0 || row >= board.size || col < 0 || col >= board.size) {
                System.out.println("Invalid position, try again!");
                continue;
            }

            // ✅ place move
            if (!board.placeMove(row, col, player.symbol)) {
                System.out.println("Cell already filled, try again!");
                continue;
            }

            // ✅ check win
            if (board.checkWin(row, col, player.symbol)) {
                board.printBoard();
                System.out.println(player.name + " wins! 🎉");
                break;
            }

            // ✅ check draw
            if (board.isFull()) {
                board.printBoard();
                System.out.println("It's a draw!");
                break;
            }

            turn++;
        }
    }
}


public class Chess {

     public static void main(String[] args) {

        List<Player> players = Arrays.asList(
                new Player("Alice", 'X'),
                new Player("Bob", 'O')
        );

        Game game = new Game(3, players);
        game.play();
    }

}
