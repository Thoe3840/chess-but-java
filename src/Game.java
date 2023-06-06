import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Game {

    private boolean active = true;
    private char[][] board = new char[8][8];
    private boolean player; // white : true; black : false;
    private final Stack<String> castling = new Stack<>();
    private final Stack<Pair> epLocation = new Stack<>();
    private final Stack<Integer> halfMoves = new Stack<>();
    private final Stack<Integer> repCount = new Stack<>();
    private final ArrayList<char[][]> boards = new ArrayList<>();
    private final Player whitePlayer;
    private final Player blackPlayer;

    private final Pair[] ROOK_DIRS = {new Pair(1, 0), new Pair(0, 1), new Pair(-1, 0), new Pair(0, -1)};
    private final Pair[] BISHOP_DIRS = {new Pair(1, 1), new Pair(1, -1), new Pair(-1, 1), new Pair(-1, -1)};

    private int fullMoves;
    private String pgn = "";

    final int CONTINUING = 0;
    final int CHECKMATE = 1;
    final int STALEMATE = 2;
    final int REPETITION = 3;
    final int FIFTY_MOVES = 4;
    final int INSUFFICIENT = 5;

    public Game(int whiteBot, int blackBot) {
        this(whiteBot, blackBot, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Game(int whiteBot, int blackBot, String fen) {
        String[] fenSplit = fen.split(" ");

        // parse pieces
        int row = 0;
        int col = 0;
        int space;
        for (int i=0; i < fenSplit[0].length(); i++) {
            char next = fenSplit[0].charAt(i);
            if (Character.isDigit(next)) {
                space = Character.getNumericValue(next);
                for (int j=0; j<space; j++) {
                    board[col + j][row] = '.';
                }
                col += space;
            } else if (next == '/') {
                row ++;
                col = 0;
            } else {
                board[col][row] = next;
                col ++;
            }
        }
        boards.add(deepCopy(board));

        player = fenSplit[1].charAt(0) == 'w';
        castling.push((fenSplit[2].equals("-") ? "" : fenSplit[2]));
        if (!(fenSplit[3].equals("-"))) {
            epLocation.push(new Pair(fenSplit[3].charAt(0) - 97, 8 - Character.getNumericValue(fenSplit[3].charAt(1))));
        } else {
            epLocation.push(null);
        }
        halfMoves.push(Integer.parseInt(fenSplit[4]));
        repCount.push(0);
        fullMoves = Integer.parseInt(fenSplit[5]);
        whitePlayer = whiteBot == 0? new Human(this) : new Bot(this, whiteBot);
        blackPlayer = blackBot == 0? new Human(this) : new Bot(this, blackBot);
    }

    public boolean getActive() {
        return active;
    }

    public boolean getPlayer() {
        return player;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public String getPgn() {
        return pgn;
    }

    public int getBoardHashCode() {
        return Arrays.deepHashCode(board);
    }

    public char getPieceAtLoc(int x, int y) {
        return board[x][y];
    }

    public boolean isPlayersPiece(boolean player, char piece) {
        return ((player && piece >= 'A' && piece <= 'Z') || (!player && piece >= 'a' && piece <= 'z'));
    }

    public boolean isInBoard(int x, int y) {
        return x <= 7 && x >= 0 && y <= 7 && y >= 0;
    }

    public boolean isMoveCapture(Move move) {
        return board[move.getEndX()][move.getEndY()] != '.' ||
                (Character.toLowerCase(board[move.getStartX()][move.getStartY()]) == 'p'
                        && epLocation.peek() != null &&
                        epLocation.peek().equalsInts(move.getEndX(), move.getEndY()));
    }

    private static char[][] deepCopy(char[][] a) {
        char[][] b = new char[a.length][];
        for (int i=0; i<a.length; i++) {
            b[i] = a[i].clone();
        }
        return b;
    }

    public void displayBoard() {
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                System.out.print(board[j][i]);
            }
            System.out.println();
        }
        System.out.println();
    }

    private void movePiece(Move move) {
        char piece = Character.toLowerCase(board[move.getStartX()][move.getStartY()]);
        // take pawn if en passant
        if (piece == 'p' && epLocation.peek() != null && epLocation.peek().equalsInts(move.getEndX(), move.getEndY())) {
            board[move.getEndX()][move.getEndY() + (player ? 1 : -1)] = '.';
        }
        // move rook if castling
        if (piece == 'k') {
            if (move.getEndX() - move.getStartX() == 2) { // kingside
                // move rook
                board[move.getStartX() + 1][move.getStartY()] = board[move.getStartX() + 3][move.getStartY()];
                board[move.getStartX() + 3][move.getStartY()] = '.';
            } else if (move.getEndX() - move.getStartX() == -2) { // queenside
                board[move.getStartX() - 1][move.getStartY()] = board[move.getStartX() - 4][move.getStartY()];
                board[move.getStartX() - 4][move.getStartY()] = '.';
            }
        }
        if (move.getPromotion() == '.') {
            board[move.getEndX()][move.getEndY()] = board[move.getStartX()][move.getStartY()];
        } else {
            board[move.getEndX()][move.getEndY()] = move.getPromotion();
        }
        board[move.getStartX()][move.getStartY()] = '.';
    }

    public void makeMove(Move move) {
        char piece = Character.toLowerCase(board[move.getStartX()][move.getStartY()]);

        String previousCastling = castling.peek();
        String castlingRights = previousCastling;
        // castling possibility
        if (piece == 'k') {
            castlingRights = castlingRights.replace(player ? "K" : "k", "").replace(player ? "Q" : "q", "");
        } else if (piece == 'r') {
            // kingside rook
            if (move.getStartX() == 7) {
                castlingRights = castlingRights.replace(player ? "K" : "k", "");
                // queenside rook
            } else if (move.getStartX() == 0) {
                castlingRights = castlingRights.replace(player ? "Q" : "q", "");
            }
        }
        if (move.getEndY() == (player ? 0 : 7)) {
            if (move.getEndX() == 0) {
                castlingRights = castlingRights.replace(player ? "q" : "Q", "");
            } else if (move.getEndX() == 7) {
                castlingRights = castlingRights.replace(player ? "k" : "K", "");
            }
        }
        castling.push(castlingRights);

        // update halfMoves and repCount
        if (piece == 'p' || board[move.getEndX()][move.getEndY()] != '.') {
            halfMoves.push(0);
            repCount.push(0);
        } else {
            halfMoves.push(halfMoves.peek() + 1);
            if (!(castling.peek().equals(previousCastling) && epLocation.peek() == null)) {
                repCount.push(0);
            } else {
                repCount.push(repCount.peek() + 1);
            }
        }

        // move before updating ep to ensure ep deletes pawn
        movePiece(move);
        boards.add(deepCopy(board));

        // en passant location
        if (piece == 'p' && (move.getStartY() - move.getEndY()) == (player ? 2 : -2)) {
            epLocation.push(new Pair(move.getStartX(), move.getStartY() + (player ? -1 : 1)));
        } else {
            epLocation.push(null);
        }

        player = !player;
    }

    public void undoMove() {
        board = deepCopy(boards.get(boards.size() - 2));
        boards.remove(boards.size() - 1);
        epLocation.pop();
        castling.pop();
        halfMoves.pop();
        repCount.pop();
        player = !player;
    }

    public ArrayList<Move> findPieceMoves(int x, int y) {
        return findPieceMoves(x, y, false);
    }

    public ArrayList<Move> findPieceMoves(int x, int y, boolean ignoreCastling) {
        ArrayList<Move> moves = new ArrayList<>();
        char piece = board[x][y];
        boolean colour = piece >= 'A' && piece <= 'Z';
        int targetX;
        int targetY;

        switch(Character.toLowerCase(piece)) {
            case 'p':
                // normal move
                targetX = x;
                targetY = y + (colour ? -1 : 1);
                if (board[targetX][targetY] == '.') {
                    // if promoting
                    if (targetY == (colour ? 0 : 7)) {
                        moves.add(new Move(x, y, targetX, targetY, (colour ? 'N' : 'n')));
                        moves.add(new Move(x, y, targetX, targetY, (colour ? 'B' : 'b')));
                        moves.add(new Move(x, y, targetX, targetY, (colour ? 'R' : 'r')));
                        moves.add(new Move(x, y, targetX, targetY, (colour ? 'Q' : 'q')));
                    } else {
                        moves.add(new Move(x, y, targetX, targetY));
                    }
                    // double move if pawn is on starting square
                    if (colour && y == 6 || !colour && y == 1) {
                        targetY = y + (colour ? -2 : 2);
                        if (board[targetX][targetY] == '.') {
                            moves.add(new Move(x, y, targetX, targetY));
                        }
                    }
                }
                // captures
                Pair epLocationHead = epLocation.peek();
                targetY = y + (colour ? -1 : 1);
                for (int i=-1; i<=1; i+=2) {
                    targetX = x + i;
                    if (isInBoard(targetX, targetY) && (isPlayersPiece(!colour, board[targetX][targetY]) ||
                            epLocationHead != null && targetX == epLocationHead.getX() && targetY == epLocationHead.getY())) {
                        // if promoting
                        if (targetY == (colour ? 0 : 7)) {
                            moves.add(new Move(x, y, targetX, targetY, (colour ? 'N' : 'n')));
                            moves.add(new Move(x, y, targetX, targetY, (colour ? 'B' : 'b')));
                            moves.add(new Move(x, y, targetX, targetY, (colour ? 'R' : 'r')));
                            moves.add(new Move(x, y, targetX, targetY, (colour ? 'Q' : 'q')));
                        } else {
                            moves.add(new Move(x, y, targetX, targetY));
                        }
                    }
                }
                break;

            case 'n':
                for (int i=-1; i<=1; i+=2) {
                    for (int j=-2; j<=2; j+=4) {
                        targetX = x + i;
                        targetY = y + j;
                        if (isInBoard(targetX, targetY) && !isPlayersPiece(colour, board[targetX][targetY])) {
                            moves.add(new Move(x, y, targetX, targetY));
                        }
                        targetX = x + j;
                        targetY = y + i;
                        if (isInBoard(targetX, targetY) && !isPlayersPiece(colour, board[targetX][targetY])) {
                            moves.add(new Move(x, y, targetX, targetY));
                        }
                    }
                }
                break;

            case 'k':
                for (targetX = x - 1; targetX <= x + 1; targetX++) {
                    for (targetY = y - 1; targetY <= y + 1; targetY++) {
                        if (isInBoard(targetX, targetY) && !isPlayersPiece(colour, board[targetX][targetY])) {
                            moves.add(new Move(x, y, targetX, targetY));
                        }
                    }
                }
                if (ignoreCastling) {
                    break;
                }
                // kingside castling
                if (castling.peek().indexOf(piece) != -1 &&
                        board[x + 1][y] == '.' &&
                        board[x + 2][y] == '.' &&
                        isSquareSafe(x, y, !player) &&
                        isSquareSafe(x + 1, y, !player) &&
                        isSquareSafe(x + 2, y, !player)) {
                    moves.add(new Move(x, y, x + 2, y));
                }
                // queenside castling
                if (castling.peek().indexOf(piece + 6) != -1 &&
                        board[x - 1][y] == '.' &&
                        board[x - 2][y] == '.' &&
                        board[x - 3][y] == '.' &&
                        isSquareSafe(x, y, !player) &&
                        isSquareSafe(x - 1, y, !player) &&
                        isSquareSafe(x - 2, y, !player)) {
                    moves.add(new Move(x, y, x - 2, y));
                }
                break;

            case 'r':
                moves = movesInLine(x, y, ROOK_DIRS, colour);
                break;

            case 'b':
                moves =  movesInLine(x, y, BISHOP_DIRS, colour);
                break;

            case 'q':
                moves = movesInLine(x, y, ROOK_DIRS, colour);
                moves.addAll(movesInLine(x, y, BISHOP_DIRS, colour));
                break;

            default:
                break;
        }
        return moves;
    }

    private ArrayList<Move> findAllPseudoLegalMoves() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int j=0; j<8; j++) {
            for (int i = 0; i < 8; i++) {
                if (isPlayersPiece(player, board[i][j])) {
                    moves.addAll(findPieceMoves(i, j));
                }
            }
        }
        return moves;
    }

    public ArrayList<Move> findAllLegalMoves() {
        return findAllLegalMoves(false);
    }

    public ArrayList<Move> findAllLegalMoves(boolean onlyCaptures) {
        ArrayList<Move> legalMoves = new ArrayList<>();
        for (Move move : findAllPseudoLegalMoves()) {
            if (!(onlyCaptures && !isMoveCapture(move))) {
                makeMove(move);
                if (!isPlayerInCheck(!player)) {
                    legalMoves.add(move);
                }
                undoMove();
            }

        }
        return legalMoves;
    }

    private ArrayList<Move> movesInLine(int x, int y, Pair[] dirs, boolean colour) {
        ArrayList<Move> moves = new ArrayList<>();
        int a, b, i, j;
        boolean movable;
        for (Pair dir : dirs) {
            a = x;
            b = y;
            i = dir.getX();
            j = dir.getY();
            movable = true;
            while (movable) {
                a += i;
                b += j;
                if (!isInBoard(a, b) || isPlayersPiece(colour, board[a][b])) {
                    break;
                }
                moves.add(new Move(x, y, a, b));
                if (isPlayersPiece(!colour, board[a][b])) {
                    movable = false;
                }
            }
        }
        return moves;
    }

    private boolean isSquareSafe(int x, int y, boolean colour) {
        for (int j=0; j<8; j++) {
            for (int i=0; i<8; i++) {
                if (isPlayersPiece(colour, board[i][j])) {
                    for (Move move : findPieceMoves(i, j, true)) {
                        if (move.getEndX() == x && move.getEndY() == y) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isPlayerInCheck(boolean colour) {
        char targetKing = colour ? 'K' : 'k';
        for (int j=0; j<8; j++) {
            for (int i=0; i<8; i++) {
                if (isPlayersPiece(!colour, board[i][j])) {
                    for (Move move : findPieceMoves(i, j)) {
                        if (board[move.getEndX()][move.getEndY()] == targetKing) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isSufficientMaterial() {
        boolean minorFound = false;
        for (int j=0; j<8; j++) {
            for (int i=0; i<8; i++) {
                char piece = Character.toLowerCase(board[i][j]);
                if (piece == 'q' || piece == 'r' || piece == 'p') {
                    return true;
                } else if (piece == 'b' || piece == 'n') {
                    if (minorFound) {
                        return true;
                    }
                    minorFound = true;
                }
            }
        }
        return false;
    }

    public int isGameOver() {
        return isGameOver(findAllLegalMoves().size());
    }

    // player will be the next player to move when called
    public int isGameOver(int numMoves) {
        if (numMoves == 0) {
            if (isPlayerInCheck(player)) {
                return CHECKMATE;
            }
            return STALEMATE;
        }
        int repetitions = 0;
        int boardsLen = boards.size();
        for (int i=2; i<repCount.peek() + 2; i++) {
            if (Arrays.deepEquals(boards.get(boardsLen - i), board)) {
                repetitions ++;
                if (repetitions >= 2) {
                    return REPETITION;
                }
            }
        }
        if (halfMoves.peek() >= 100) {
            return FIFTY_MOVES;
        }
        if (!isSufficientMaterial()) {
            return INSUFFICIENT;
        }
        return CONTINUING;
    }

    // player will be the next player to move when called
    public void endGame(int status) {
        switch (status) {
            case CHECKMATE -> {
                System.out.print(player ? "Black " : "White ");
                System.out.println("wins by checkmate.");
            }
            case STALEMATE -> System.out.println("Game drawn by stalemate.");
            case REPETITION -> System.out.println("Game drawn by three move repetition.");
            case FIFTY_MOVES -> System.out.println("Game drawn by fifty move rule.");
            case INSUFFICIENT -> System.out.println("Game drawn by insufficient material.");
            default -> System.out.println("Game over.");
        }
        active = false;
    }

    // player will be the currently moving player when called
    public void updatePGN(Move move) {
        char piece = board[move.getStartX()][move.getStartY()];
        char pieceUpper = Character.toUpperCase(piece);
        boolean capture = isMoveCapture(move);
        String notation = "";

        if (fullMoves != 1 || !player) {
            pgn += " ";
        }
        if (player) {
            pgn += fullMoves + ". ";
            fullMoves ++;
        }
        // castling
        boolean castling = false;
        if (pieceUpper == 'K') {
            int distance = move.getEndX() - move.getStartX();
            if (distance == 2) {
                castling = true;
                notation += "O-O";
            } else if (distance == -2) {
                castling = true;
                notation += "O-O-O";
            }
        }
        if (pieceUpper == 'P') {
            if (capture) {
                notation += (char) (move.getStartX() + 97) + "x";
            }
        } else if (!castling) {
            notation += pieceUpper;

            boolean differentRow = false;
            boolean sameRow = false;
            for (Move otherPieceMove : findAllLegalMoves()) {
                if (board[otherPieceMove.getStartX()][otherPieceMove.getStartY()] == piece &&
                        !(otherPieceMove.getStartX() == move.getStartX() && otherPieceMove.getStartY() == move.getStartY()) &&
                        otherPieceMove.getEndX() == move.getEndX() &&
                        otherPieceMove.getEndY() == move.getEndY()) {
                    if (otherPieceMove.getStartX() == move.getStartX()) {
                        sameRow = true;
                    } else {
                        differentRow = true;
                    }
                }
            }
            if (differentRow) {
                notation += (char) (move.getStartX() + 97);
            }
            if (sameRow) {
                notation += String.valueOf(8 - move.getStartY());
            }
            if (capture) {
                notation += "x";
            }
        }
        if (!castling) {
            notation += (char) (move.getEndX() + 97) + String.valueOf(8 - move.getEndY());
        }
        // promotion
        if (move.getPromotion() != '.') {
            notation += "=" + Character.toUpperCase(move.getPromotion());
        }
        // check and game end
        makeMove(move);
        int status = isGameOver();
        if (status == CHECKMATE) {
            notation += "#";
            pgn += notation + " " + (player ? 0 : 1) + "-" + (player ? 1 : 0);
        } else if (status == CONTINUING) {
            // check
            if (isPlayerInCheck(player)) {
                notation += "+";
            }
            pgn += notation;
        } else {
            pgn += notation + " 1/2-1/2";
        }
        System.out.println(notation);
        undoMove();
    }

    public int countPositions(int depth) {
        if (depth == 0) {
            return 1;
        }
        int nodes = 0;
        for (Move move : findAllLegalMoves()) {
            makeMove(move);
            nodes += countPositions(depth - 1);
            undoMove();
        }
        return nodes;
    }
}
