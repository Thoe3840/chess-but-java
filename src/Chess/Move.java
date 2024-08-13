package Chess;

import java.util.Objects;

public class Move {

    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private final char promotion;
    private int score;

    public Move(int startX, int startY, int endX, int endY) {
        this(startX, startY, endX, endY, '.');
    }

    public Move(int startX, int startY, int endX, int endY, char promotion) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.promotion = promotion;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public char getPromotion() {
        return promotion;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return startX == move.startX && startY == move.startY && endX == move.endX && endY == move.endY && promotion == move.promotion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startX, startY, endX, endY, promotion);
    }

    @Override
    public String toString() {
        if (promotion == '.') {
            return (char) (startX + 97) +
                    String.valueOf(8 - startY) +
                    (char) (endX + 97) +
                    String.valueOf(8 - endY);
        }
        return (char) (startX + 97) +
                String.valueOf(8 - startY) +
                (char) (endX + 97) +
                String.valueOf(8 - endY) +
                promotion;

    }
}
