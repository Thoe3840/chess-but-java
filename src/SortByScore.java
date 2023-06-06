import java.util.Comparator;

public class SortByScore implements Comparator<Move> {

    public int compare(Move a, Move b) {
        return b.getScore() - a.getScore();
    }
}
