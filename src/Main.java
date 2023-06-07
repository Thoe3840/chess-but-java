public class Main {

    private static final boolean competitive = true;

    public static void main(String[] args) {
        Game game = new Game(3000, 3000, "8/8/8/8/5K2/5R2/6k1/8 w - - 0 1");
        if (!competitive) {
            game.displayBoard();
        }

        int status;
        while(game.getActive()) {
            Player player = game.getPlayer() ? game.getWhitePlayer() : game.getBlackPlayer();
            player.move();
            if (!competitive) {
                game.displayBoard();
            } else {
                System.out.println();
            }
            status = game.isGameOver();
            if (status != game.CONTINUING) {
                game.endGame(status);
            }
        }
        System.out.println(game.getPgn());
    }
}
