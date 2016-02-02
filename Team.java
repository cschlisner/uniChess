package uniChess;

import java.util.List;
import java.util.ArrayList;
public class Team {
	private Game game;
	private Board board;
    private Game.Color color;


    private List<Piece> pieceSet = new ArrayList<Piece>();
    
    private boolean checked = false;
    private boolean checkMated = false;
    private List<Location> checkedMoves = new ArrayList<Location>();
    private List<Piece> attackers = new ArrayList<Piece>();

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
        
        addToPieceSet(new Piece(g, this, Game.PieceType.ROOK, c, d, new Location(org.x+(d*0), org.y)));
		addToPieceSet(new Piece(g, this, Game.PieceType.ROOK, c, d, new Location(org.x+(d*7), org.y)));

		addToPieceSet(new Piece(g, this, Game.PieceType.KNIGHT, c, d, new Location(org.x+(d*1), org.y)));
		addToPieceSet(new Piece(g, this, Game.PieceType.KNIGHT, c, d, new Location(org.x+(d*6), org.y)));
		
		addToPieceSet(new Piece(g, this, Game.PieceType.BISHOP, c, d, new Location(org.x+(d*2), org.y)));
		addToPieceSet(new Piece(g, this, Game.PieceType.BISHOP, c, d, new Location(org.x+(d*5), org.y)));

		// King and queen are symmetrical
		addToPieceSet(new Piece(g, this, Game.PieceType.KING, c, d, new Location(org.x+(d*((d>0)?4:3)), org.y)));
		addToPieceSet(new Piece(g, this, Game.PieceType.QUEEN, c, d, new Location(org.x+(d*((d>0)?3:4)), org.y)));
		
		for (int i = 0; i < 8; ++i)
			addToPieceSet(new Piece(g, this, Game.PieceType.PAWN, c, d, new Location(i, ((d>0)?org.y+1:org.y-1))));
    }

    public void addToPieceSet(Piece p){
        pieceSet.add(p);
        board.getTile(p.getLocation()).setOccupator(p);
    }

    public Game.Color getColor(){
    	return color;
    }

    public List<Piece> getPieceSet(){
    	return pieceSet;
    }

    public Piece getPiece(Game.PieceType t){
    	for (Piece p : pieceSet)
    		if (p.ofType(t))
    			return p;
    	return null;
    }

    public Piece getPiece(String id){
    	for (Piece p : pieceSet)
    		if (p.getId().equals(id))
    			return p;
    	return null;
    }

    public List<Location> getAllMoves(){
    	List<Location> mvlst = new ArrayList<Location>();
    	for (Piece p : pieceSet){
    		mvlst.addAll(p.getMoveList());
    	}
    	return mvlst;
    }

    public void updateStatus(){
        attackers = updateAttackers();
        checked = !attackers.isEmpty();
        if (checked)
            checkedMoves = updateCheckedMoves();
        if (updateCheckMate())
            checkMated = true;
    }

    public boolean inCheck(){
        return checked;
    }
    public boolean inCheckMate(){
        return checkMated;
    }
    public List<Location> getCheckedMoves(){
        return checkedMoves;
    }
    public List<Piece> getAttackers(){
        return attackers;
    }
    public boolean canMoveWhenChecked(Location move){
        for (Piece a : attackers)
            if (a.canCheck(move))
                return false;
        return true;
    }
    
    private boolean updateCheckMate(){
        if (!getPiece(Game.PieceType.KING).getMoveList().isEmpty())
            return false;

        if ((pieceSet.size() == 1 && getAllMoves().isEmpty()) || (checked && getAllMoves().isEmpty()) || (checked && checkedMoves.isEmpty()))
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


    private List<Location> updateCheckedMoves(){
        List<Location> moves = new ArrayList<Location>();
        if (inCheck()){
            for (Piece p : pieceSet){
                moveloop:
                for (Location move : p.getMoveList()){
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