package Chess;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = "";
        while (!input.equals("uci")) {
            input = scanner.nextLine();
        }
        System.out.println("id name pogsauce");
        System.out.println("id author Theo Michell");
        System.out.println("option name Move Overhead type spin default 100");
        System.out.println("option name Threads type spin default 2");
        System.out.println("option name Hash type spin default 256");
        System.out.println("option name SyzygyPath type string default \"./syzygy/\"");
        System.out.println("uciok");

        while (!input.equals("isready")) {
            input = scanner.nextLine();
        }
        System.out.println("readyok");

        String firstWord = "";
        String[] inputSplit = {};
        while (!firstWord.equals("position")) {
            input = scanner.nextLine();
            inputSplit = input.split(" ");
            firstWord = inputSplit[0];
        }
        String fen = inputSplit[1];
        if (fen.equals("startpos")) {
            fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        }
        boolean isWhite = inputSplit.length <= 2 || inputSplit.length % 2 == 1;
        // this is a horrible way to make comments in your code please fix later
        // this is a horrible way to handle this please fix later
        Game game = new Game(isWhite ? 2000 : 0, isWhite ? 0 : 2000, fen);
        if (inputSplit.length > 2) {
            for (int i=3; i < inputSplit.length; i++) {
                input = inputSplit[i];
                makeUCIMove(game, input);
            }
        }
        int status;
        while(game.getActive()) {
            Player player = game.getPlayer() ? game.getWhitePlayer() : game.getBlackPlayer();
            while (true) {
                input = scanner.nextLine();
                if (input.equals("isready")) {
                    System.out.println("readyok");
                }

                inputSplit = input.split(" ");

                // human playing
                if (inputSplit[0].equals("position")) {
                    makeUCIMove(game, inputSplit[inputSplit.length - 1]);
                    break;
                }
                // bot playing
                if (inputSplit[0].equals("go")) {
                    player.setMoveTime(Integer.parseInt(inputSplit[inputSplit.length - 1]));
                    player.move();
                    break;
                }
            }
            status = game.isGameOver();
            if (status != game.CONTINUING) {
                game.endGame(status);
            }
        }
    }

    private static void makeUCIMove(Game game, String input) {
        if (input.length() == 4) {
            game.makeMove(
                    new Move(
                            input.charAt(0) - 97,
                            8 - Character.getNumericValue(input.charAt(1)),
                            input.charAt(2) - 97,
                            8 - Character.getNumericValue(input.charAt(3))
                    )
            );
        } else if (input.length() == 5) {
            game.makeMove(
                    new Move(
                            input.charAt(0) - 97,
                            8 - Character.getNumericValue(input.charAt(1)),
                            input.charAt(2) - 97,
                            8 - Character.getNumericValue(input.charAt(3)),
                            game.getPlayer() ? Character.toUpperCase(input.charAt(4)) : input.charAt(4)
                    )
            );
        } else {
            System.out.println("Invalid input");
        }
    }
}
