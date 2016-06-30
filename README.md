# uniChess
A minimal chess API for java

<h1>
♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜
♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟
<br>
♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙
♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖
</h1>
***

#### Starting a Game

``` Java
// Create two Player Objects.
Player<String> p1 = new Player<>("player one", Game.Color.WHITE);
Player<String> p2 = new Player<>("player two", Game.Color.BLACK);

// Create a new Game Object using your players.
Game chessGame = new Game(p1, p2);
```

#### Advancing a game

To advance a game, just call chessGame.advance() with a valid [algebraic notation](https://en.wikipedia.org/wiki/Algebraic_notation_(chess)#Notation_for_moves) string. 

``` Java

Game.GameEvent gameResponse = chessGame.advance("ph4");
```

The game will attempt to make the move 'ph4' for the current player. 

Next we need to check what the game response was:

``` Java
switch(gameResponse){
	
	// everything went well and the turn has been switched
	case OK:
		break;
	
	// The algebraic notation string needs to be more specific, the turn is the same
	case AMBIGUOUS:
		System.out.println("Ambiguous Move.");
		break;
	
	// The input was gibberish, the turn is the same
	case INVALID:
		System.out.println("Invalid Move.");
		break; 
	
	// The move was valid, but not legal in the current board state
	case ILLEGAL:
		System.out.println("Illegal Move.");					
		break;
	
	// The move put the opposing player in check
	case CHECK:
		System.out.println("You are in check!");
		break;
	
	// The move put the opposing player in checkmate
	case CHECKMATE:
		System.out.println("Checkmate. "+chessGame.getDormantPlayer().getID()+" wins!");
		System.exit(0);
		break;
	
	// The move put the opposing player in stalemate
	case STALEMATE:
		System.out.println("Stalemate. "+chessGame.getDormantPlayer().getID()+" wins!");
		break;
	
	// Both Player objects had their draw fields set to true
	case DRAW:
		System.out.println("Draw!");
		break;
		
}
```
#### Printing a game
```Java
chessGame.getCurrentBoard().toString();
```

Will return a string representation of a board, using the Game setting for unicode chars. 

<b>
```
   A sample board state:

  A  B  C  D  E  F  G  H
8|♜||♞||♝||♛||♚||♝||♞||♜|8
7|♟||♟||♟||♟||♟||♟||♟||♟|7
6| ||·|| ||·|| ||·|| ||·|6
5|·|| ||·|| ||·|| ||·|| |5
4| ||·|| ||·|| ||·|| ||·|4
3|·|| ||·|| ||·|| ||·|| |3
2|♙||♙||♙||♙||♙||♙||♙||♙|2
1|♖||♘||♗||♕||♔||♗||♘||♖|1
  A  B  C  D  E  F  G  H
```
</b>

#### That's it!

You only have to write code to respond to those events and feed in moves, uniChess will do the rest. 

###More information can be found in the [docs](https://cschlisner.github.io/uniChess/apidocs/index.html)
