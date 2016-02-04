# uniChess
##A general chess API for java

This API is intended for use in other java applications where you might need to play a chess game for whatever reason. 


All communication with the game is done through a Game object. 


###Starting a game. 

You can create a new game with the Constructor:

<b>`Game myGame = new Game(String player1Name, String player2Name, boolean imageOutput, String imageExportFile);`</b>

If `imageExportFile` is null, the board images will not be exported, and will need to be retrieved after each input with the `myGame.getImageOutput()` function.

If `imageOutput` is false, the board will be put into the information output buffer (as a uniform grid, with unicode chars for the chess pieces) to be read along with regular text information.

If the alternate constructor is used:

<b>`Game myGame = new Game(String player1Name, String player2Name);`</b>

Then a new game is created with `imageOutput` set to false and `imageExportFile` set to null.

___

###Reading output

Read information output from the game by calling <b>`myGame.getInfoOutput()`</b> which will return a `String` object containing all game information not read from the buffer. Once this is called the buffer is cleared. 

___

###Supplying input

It is up to you to handle which player is supplying input, the game will automatically switch which player it is accepting input from. The white team (player 1) will always initiate the game. 

To suppy a line of input, call <b>`myGame.input(String playerInput)`</b>. Valid commands are:

<b>`move [piece name] [location]`</b> Moves piece with name [piece name] to location [location]. If multiple pieces of the same type can be moved to the same location, then a message will be returned in the information buffer prompting the user to specify which piece to move. This is the only command that will advance the turn.


<b>`get [location]`</b> Gets all of the available moves for the piece at [location] if a piece exists there.


<b>`status`</b> Lists all alive pieces on the player's team, their locations, and their potential moves.

<b>`forfeit`</b> Forfeits the game, resulting in loss (imagine that!)

<b>`draw`</b> Offers a draw to the opponent. This will not advance the turn, so a move must be made in conjunction. If the opponent does not also input draw on their next turn, then the draw offer will expire.

<b>`help`</b> Writes a list of these commands to the buffer. 
