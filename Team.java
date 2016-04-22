package uniChess;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Team {
    private Game game;
    private Board board;
    private Game.Color color;


    private List<Piece> pieceSet = new ArrayList<Piece>();

    private boolean checked = false;
    private boolean checkMated = false;
    private List<Move> checkedMoves = new ArrayList<Move>();
    private List<Piece> attackers = new ArrayList<Piece>();

    public Team(Game g, Board b, Team other){
        game = g;
        board = b;
        color = other.getColor();
        
        for (Piece p : other.getPieceSet()){
            Piece clone = new Piece(g, board, this, p.getType(), color, p.getMoveSetDir(), p.getLocation());
            this.pieceSet.add(clone);
            board.getTile(p.getLocation()).setOccupator(clone);
        }
    }

    /**
     * Creates new team of Game.Color c at position top or bottom 
     * 
     * if (position==top) org [Location of bottom left Rook] = (7,0)
     * else org = (0,7)
     * 
     */
    public Team(Game g, Game.Color c){
        game = g;
        board = g.getBoard();
        color = c;

        int d = (c.equals(Game.Color.BLACK))?-1:1;
        Location org = (d>0)?new Location(0,0):new Location(7,7);

        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.ROOK, c, d, new Location(org.x+(d*0), org.y)));
        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.ROOK, c, d, new Location(org.x+(d*7), org.y)));

        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.KNIGHT, c, d, new Location(org.x+(d*1), org.y)));
        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.KNIGHT, c, d, new Location(org.x+(d*6), org.y)));

        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.BISHOP, c, d, new Location(org.x+(d*2), org.y)));
        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.BISHOP, c, d, new Location(org.x+(d*5), org.y)));

        // King and queen are symmetrical
        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.KING, c, d, new Location(org.x+(d*((d>0)?4:3)), org.y)));
        addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.QUEEN, c, d, new Location(org.x+(d*((d>0)?3:4)), org.y)));

        for (int i = 0; i < 8; ++i)
            addToPieceSet(new Piece(g, g.getBoard(), this, Game.PieceType.PAWN, c, d, new Location(i, ((d>0)?org.y+1:org.y-1))));
    }

    public void addToPieceSet(Piece p){
        pieceSet.add(p);    
        board.getTile(p.getLocation()).setOccupator(p);
    }

    public void addTeamToBoard(){
        for (Piece p : pieceSet)
            board.getTile(p.getLocation()).setOccupator(p);
    }

    public Game.Color getColor(){
    	return color;
    }

    public List<Piece> getPieceSet(){
    	return pieceSet;
    }

    public void setPieceSet(List<Piece> ps){
        pieceSet = ps;
    }

    public Piece getPiece(Game.PieceType t){
    	for (Piece p : pieceSet)
    		if (p.ofType(t))
    			return p;
    	return null;
    }

    public Map<Piece, List<Move>> getMoveMap(){
        Map<Piece, List<Move>> moveMap = new HashMap<Piece, List<Move>>();

    	for (Piece p : pieceSet)
            if (p.getMoveList().size()>0)
    		  moveMap.put(p, p.getMoveList());

        return moveMap;
    }

    public List<Move> getMoveList(){
        List<Move> moveList = new ArrayList<Move>();

        for (Piece p : pieceSet)
            if (p.getMoveList().size()>0)
                moveList.addAll(p.getMoveList());

        return moveList;
    }

    public void updateStatus() throws GameException{
        for (Piece p : pieceSet)
            p.update();
        attackers = updateAttackers();
        checked = !attackers.isEmpty();
        if (checked)
            checkedMoves = updateCheckedMoves();
        if (updateCheckMate())
            throw new GameException(GameException.CHECKMATE, game.getPlayer(color)+" in Checkmate!");
        // if (!checked && getMoveList().isEmpty())
        //     throw new GameException(GameException.STALEMATE, game.getPlayer(color)+" in Stalemate!");
    }

    public boolean inCheck(){
        return checked;
    }
    public boolean inCheckMate(){
        return checkMated;
    }
    public List<Move> getCheckedMoves(){
        return checkedMoves;
    }
    public List<Piece> getAttackers(){
        return attackers;
    }
    public boolean canMoveWhenChecked(Move move){
        for (Piece a : attackers)
            if (a.canCheck(move))
                return false;
        return true;
    }
    
    private boolean updateCheckMate(){
        if (!getPiece(Game.PieceType.KING).getMoveList().isEmpty())
            return false;

        if ((pieceSet.size() == 1 && getMoveMap().size()==0) || (checked && getMoveMap().size()==0) || (checked && checkedMoves.isEmpty()))
            return true;

        return false;
    }

    private List<Piece> updateAttackers(){
        List<Piece> attackers = new ArrayList<Piece>();
        for (Piece p : (this.equals(game.player1.getTeam())?game.player2.getTeam().getPieceSet():game.player1.getTeam().getPieceSet())){
            if (p.hasCheck()){
                attackers.add(p);
            }
        }
        return attackers;
    }


    private List<Move> updateCheckedMoves(){
        List<Move> moves = new ArrayList<Move>();
        if (inCheck()){
            for (Piece p : pieceSet){
                moveloop:
                for (Move move : p.getMoveList()){
                    if (p.ofType(Game.PieceType.KING)){
                        moves.add(move);
                        continue moveloop;
                    }
                   for (Piece a : attackers)
                        if (a.canCheck(move))
                            continue moveloop;
                    moves.add(move);
                }
            }
        }
    	return moves;
    }
}
