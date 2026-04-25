
import java.util.*;

// 👤 Player

class Player {
    String name;
    int position;

    public Player(String name) {
        this.name = name;
        this.position = 0; // start position
    }
}

// 🎲 Dice
class Dice {
    Random rand = new Random();

    public int roll() {
        return rand.nextInt(6) + 1;
    }
}

// Board
class Board{
    int size;
    Map<Integer, Integer> snakes;
    Map<Integer, Integer> ladders;

    public Board(int size , Map<Integer, Integer> snakes, Map<Integer, Integer> ladders) {
        this.size = size;
        this.snakes = snakes;
        this.ladders = ladders;
    }

    public int move(int position) {
        if (snakes.containsKey(position)) {
            System.out.println("🐍 Snake! Going down...");
            return snakes.get(position);
        } else if (ladders.containsKey(position)) {
             System.out.println("🪜 Ladder! Climbing up...");
            return ladders.get(position);
        }
        return position;
       
    }
}

// Game
class Game{
    Board board;
    List<Player> players;
    Dice dice;

    public Game(Board board, List<Player> players) {
        this.board = board;
        this.players = players;
        this.dice = new Dice();
    }

    public void play() {
        boolean won = false;
        while (!won) {
            for (Player player : players) {
                int roll = dice.roll();
                System.out.println(player.name + " rolled a " + roll);
                player.position += roll;

                if (player.position > board.size) {
                    player.position = board.size - (player.position - board.size); // bounce back
                }

                player.position = board.move(player.position);
                System.out.println(player.name + " is now at position " + player.position);

                if (player.position == board.size) {
                    System.out.println(player.name + " wins! 🎉");
                    won = true;
                    break;
                }
            }
        }
    }

}

class SnakeLadder {
    public static void main(String[] args) {
        Map<Integer, Integer> snakes = new HashMap<>();
        snakes.put(14, 7);
        snakes.put(31, 26);
        snakes.put(38, 3);

        Map<Integer, Integer> ladders = new HashMap<>();
        ladders.put(2, 15);
        ladders.put(5, 20);
        ladders.put(9, 27);

        Board board = new Board(100, snakes, ladders);
        List<Player> players = Arrays.asList(new Player("Alice"), new Player("Bob"));
        Game game = new Game(board, players);
        game.play();
    }
}