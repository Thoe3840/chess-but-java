package Chess;

import java.util.Scanner;

public class Human extends Player{

    Scanner scanner = new Scanner(System.in);

    public Human(Game game) {
        super(game);
    }

    public void move() {
        boolean moved = false;
        String input;
        int inputLength;
        Move move;
        while (!moved) {
            input = scanner.nextLine();
            inputLength = input.length();
            if (inputLength == 4) {
                move = new Move(
                        input.charAt(0) - 97,
                        8 - Character.getNumericValue(input.charAt(1)),
                        input.charAt(2) - 97,
                        8 - Character.getNumericValue(input.charAt(3))
                );
            } else if (inputLength == 5) {
                move = new Move(
                        input.charAt(0) - 97,
                        8 - Character.getNumericValue(input.charAt(1)),
                        input.charAt(2) - 97,
                        8 - Character.getNumericValue(input.charAt(3)),
                        game.getPlayer() ? Character.toUpperCase(input.charAt(4)) : input.charAt(4)
                );
            } else {
                System.out.println("Invalid input");
                continue;
            }
            if (game.findAllLegalMoves().contains(move)) {
                //game.updatePGN(move);
                game.makeMove(move);
                moved = true;
            } else {
                System.out.println("Illegal move");
            }
        }
    }
}
