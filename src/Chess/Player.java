package Chess;

public abstract class Player {

    protected final Game game;

    public Player(Game game) {
        this.game = game;
    }

    public abstract void move();

    public void setMoveTime(int moveTime) {
    }
}
