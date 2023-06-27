import java.util.*;

public class Bot extends Player{

    private static final Map<Character, Integer> pieceValues = Map.ofEntries(
            Map.entry('P', 100),
            Map.entry('p', -100),
            Map.entry('N', 305),
            Map.entry('n', -305),
            Map.entry('B', 333),
            Map.entry('b', -333),
            Map.entry('R', 563),
            Map.entry('r', -563),
            Map.entry('Q', 950),
            Map.entry('q', -950),
            Map.entry('K', 0),
            Map.entry('k', 0),
            Map.entry('.', 0)
    );
    private static final int[][] pawnTable = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {50, 50, 50, 50, 50, 50, 50, 50},
        {10, 10, 20, 30, 30, 20, 10, 10},
        {5, 5, 10, 25, 25, 10, 5, 5},
        {3, 0, 5, 20, 20, 0, 0, 3},
        {5, -10, 0, 0, 0, -10, 0, 5},
        {5, 10, 10, -20, -20, 10, 10, 5},
        {0, 0, 0, 0, 0, 0, 0, 0}
    };
    private static final int[][] knightTable = {
            {-50, -40, -30, -30, -30, -30, -40, -50},
            {-40, -20, 0, 0, 0, 0, -20, -40},
            {-30, 0, 10, 15, 15, 10, 0, -30},
            {-30, 5, 15, 20, 20, 15, 5, -30},
            {-30, 0, 15, 20, 20, 15, 0, -30},
            {-30, 5, 10, 15, 15, 10, 5, -30},
            {-30, -20, 0, 5, 5, 0, -20, -30},
            {-40, -30, -20, -20, -20, -20, -30, -40}
    };
    private static final int[][] bishopTable = {
            {-20, -10, -10, -10, -10, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 10, 10, 5, 0, -10},
            {-10, 5, 5, 10, 10, 5, 5, -10},
            {0, 0, 10, 10, 10, 10, 0, 0},
            {-10, 10, 10, 8, 8, 10, 10, -10},
            {-10, 5, 0, 0, 0, 0, 5, -10},
            {-20, -10, -10, -10, -10, -10, -10, -20}
    };
    private static final int[][] rookTable = {
            {0, 0, 0, 0, 0, 0, 0, 0},
            {5, 10, 10, 10, 10, 10, 10, 5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 0, 0, 0, 0, 0, -5},
            {-5, 0, 3, 5, 5, 0, 0, -5}
    };
    private static final int[][] queenTable = {
            {-20, -10, -10, -5, -5, -10, -10, -20},
            {-10, 0, 0, 0, 0, 0, 0, -10},
            {-10, 0, 5, 5, 5, 5, 0, -10},
            {-5, 0, 5, 5, 5, 5, 0, -5},
            {0, 0, 5, 5, 5, 5, 0, -5},
            {-10, 5, 5, 5, 5, 5, 2, -10},
            {-10, 0, 5, 2, 2, 0, 0, -10},
            {-20, -10, -10, -5, -5, -10, -10, -20}
    };
    private static final int[][] earlyKingTable = {
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-30, -40, -40, -50, -50, -40, -40, -30},
            {-20, -30, -30, -40, -40, -30, -30, -20},
            {-10, -20, -20, -20, -20, -20, -20, -10},
            {20, 20, 0, 0, 0, 0, 20, 20},
            {20, 30, 10, 0, 0, 10, 30, 20}
    };
    private static final int[][] lateKingTable = {
            {-50, -40, -30, -20, -20, -30, -40, -50},
            {-30, -20, -10, 0, 0, -10, -20, -30},
            {-30, -10, 20, 30, 30, 20, -10, -30},
            {-30, -10, 30, 40, 40, 30, -10, -30},
            {-30, -10, 30, 40, 40, 30, -10, -30},
            {-30, -10, 20, 30, 30, 20, -10, -30},
            {-30, -30, 0, 0, 0, 0, -30, -30},
            {-50, -30, -30, -30, -30, -30, -30, -50}
    };
    private static final int NEGATIVE_INFINITY = -Integer.MAX_VALUE;
    private final int thinkingTime;
    private long startTime;

    public Bot(Game game, int thinkingTime) {
        super(game);
        this.thinkingTime = thinkingTime;
    }

    public void move() {
        startTime = System.nanoTime();
        int eval;
        int bestEval = NEGATIVE_INFINITY;
        int depthBestEval = NEGATIVE_INFINITY;
        ArrayList<Move> moves = game.findAllLegalMoves();
        Collections.shuffle(moves);
        orderMoves(moves);
        Move depthBestMove = moves.get(0);
        Move bestMove = depthBestMove;

        int depth = 1;
        boolean thinking = true;

        while(thinking) {
            for (Move move : moves) {
                game.makeMove(move);
                eval = -evalMove(NEGATIVE_INFINITY, -depthBestEval, depth - 1);
                game.undoMove();
                move.setScore(eval);
                if (eval > depthBestEval) {
                    depthBestEval = eval;
                    depthBestMove = move;
                }
                if ((System.nanoTime() - startTime)/1000000 > thinkingTime) {
                    thinking = false;
                    break;
                }
                if (eval == Integer.MAX_VALUE) {
                    bestEval = depthBestEval;
                    bestMove = depthBestMove;
                    thinking = false;
                    break;
                }
            }
            if (thinking) {
                bestEval = depthBestEval;
                bestMove = depthBestMove;
                moves.sort(new SortByScore());
                depth ++;
                depthBestEval = NEGATIVE_INFINITY;
            }
        }


        System.out.print("Depth = ");
        System.out.println(depth - 1);
        System.out.print("Time = ");
        System.out.print((System.nanoTime() - startTime)/1000000);
        System.out.println("ms");
        System.out.print("Evaluation = ");
        System.out.println(bestEval);

        game.updatePGN(bestMove);
        game.makeMove(bestMove);
    }

    private int evalMove(int alpha, int beta, int depth) {
        if ((System.nanoTime() - startTime)/1000000 > thinkingTime) {
            return NEGATIVE_INFINITY;
        }

        if (depth == 0) {
            return completeCaptures(alpha, beta);
        }

        ArrayList<Move> moves = game.findAllLegalMoves();
        int status = game.isGameOver(moves.size());
        if (status != game.CONTINUING) {
            if (status == game.CHECKMATE) {
                return NEGATIVE_INFINITY;
            }
            return 0;
        }

        int eval;
        orderMoves(moves);
        for (Move move : moves) {
            game.makeMove(move);
            eval = -evalMove(-beta, -alpha, depth - 1);
            game.undoMove();
            if (eval >= beta) {
                return beta;
            }
            alpha = Integer.max(alpha, eval);
        }
        return alpha;
    }

    private void orderMoves(ArrayList<Move> moves) {
        for (Move move : moves) {
            int score = 0;
            // ignore EP
            char movingPiece = Character.toUpperCase(game.getPieceAtLoc(move.getStartX(), move.getStartY()));
            char capturedPiece = Character.toUpperCase(game.getPieceAtLoc(move.getEndX(), move.getEndY()));
            if (capturedPiece != '.') {
                score += 10 * pieceValues.get(capturedPiece) - pieceValues.get(movingPiece);
            }
            if (move.getPromotion() != '.') {
                score += pieceValues.get(Character.toUpperCase(move.getPromotion()));
            }

            int opponentPawnY = move.getEndY() + (game.getPlayer() ? -1 : 1);
            int opponentPawnX;
            boolean attackedByPawn = false;
            for (int i=-1; i<=1; i+=2) {
                opponentPawnX = move.getEndX() + i;
                if (game.isInBoard(opponentPawnX, opponentPawnY) &&
                        game.getPieceAtLoc(opponentPawnX, opponentPawnY) == (game.getPlayer() ? 'p' : 'P')) {
                    attackedByPawn = true;
                    break;
                }
            }
            if (attackedByPawn) {
                score -= pieceValues.get(movingPiece);
            }
            move.setScore(score);
        }
        moves.sort(new SortByScore());
    }

    private int completeCaptures(int alpha, int beta) {
        int eval = findEval();
        if (eval >= beta) {
            return beta;
        }
        alpha = Integer.max(alpha, eval);

        ArrayList<Move> moves = game.findAllLegalMoves(true);
        orderMoves(moves);
        for (Move move : moves) {
            game.makeMove(move);
            eval = -completeCaptures(-beta, -alpha);
            game.undoMove();
            if (eval >= beta) {
                return beta;
            }
            alpha = Integer.max(alpha, eval);
        }
        return alpha;
    }

    private int findEval() {
        int whiteKingX = -1;
        int whiteKingY = -1;
        int blackKingX = -1;
        int blackKingY = -1;
        int whitePieces = 0;
        int blackPieces = 0;

        boolean player = game.getPlayer();
        int eval = 0;
        for (int j=0; j<8; j++) {
            for (int i=0; i<8; i++) {
                char piece = game.getPieceAtLoc(i, j);
                if (piece != '.') {
                    boolean whitePiece = game.isPlayersPiece(true, piece);
                    piece = Character.toUpperCase(piece);
                    int pieceValue = pieceValues.get(piece);
                    switch (piece) {
                        case 'P' -> pieceValue += pawnTable[whitePiece ? j : 7 - j][i];
                        case 'N' -> pieceValue += knightTable[whitePiece ? j : 7 - j][i];
                        case 'B' -> pieceValue += bishopTable[whitePiece ? j : 7 - j][i];
                        case 'R' -> pieceValue += rookTable[whitePiece ? j : 7 - j][i];
                        case 'Q' -> pieceValue += queenTable[whitePiece ? j : 7 - j][i];
                        case 'K' -> {
                            if (whitePiece) {
                                whiteKingX = i;
                                whiteKingY = j;
                            } else {
                                blackKingX = i;
                                blackKingY = 7 - j;
                            }
                        }
                    }
                    if (whitePiece) {
                        eval += pieceValue;
                        if (piece != 'P') {
                            whitePieces ++;
                        }
                    } else {
                        eval -= pieceValue;
                        if (piece != 'P') {
                            blackPieces ++;
                        }
                    }
                }
            }
        }
        if (blackPieces > 3) {
            eval += earlyKingTable[whiteKingX][whiteKingY];
        } else {
            eval += lateKingTable[whiteKingX][whiteKingY];
        }
        if (whitePieces > 3) {
            eval -= earlyKingTable[blackKingX][blackKingY];
        } else {
            eval -= lateKingTable[blackKingX][blackKingY];
        }
        return player ? eval : -eval;
    }
}
