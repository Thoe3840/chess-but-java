package Chess;

import java.util.Objects;

public class Pair {

    private int x;
    private int y;

    public Pair() {
        this(-1, -1);
    }

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean equalsInts(int x, int y) {
        return x == this.x && y == this.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair position = (Pair) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Chess.Pair{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
