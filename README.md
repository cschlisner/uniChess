# uniChess
##A general chess API for java

This API is intended for use in other java applications where you might need to play a chess game for whatever reason. 


All communication with the game is done through a Game object. 


###Starting a game. 

You can create a new game with the Constructor:

`Game myGame = new Game(String player1Name, String player2Name, boolean imageOutput, String imageExportFile);`

If imageExportFile is null, the board images will not be exported, and will need to be retrieved after each input with the `myGame.getImageOutput()` function.

If imageOutput is false, the board will be put into the information output buffer to be read along with regular text information.

If the alternate constructor is used:

`Game myGame = new Game(String player1Name, String player2Name);`

Then a new game is created with imageOutput set to <b>false</b> and imageExportFile set to <b>null</b>

___

###Reading output

Read information output from the game by calling `myGame.getInformationOutput()` which will return a `String` object containing all game information not read from the buffer. Once this is called the buffer is cleared. 

___

###Supplying input

It is up to you to handle which player is supplying input, the game will automatically switch which player it is accepting input from. The white team will always initiate the game. 

To suppy a line of input, call `myGame.input(String playerInput)`. Valid commands are:

<code>move [piece name] [location]</code> moves piece with name [piece name] to location [location]. If multiple pieces of the same type can be moved to the same location, then a message will be returned in the information buffer prompting the user to specify which piece to move. 


<code>get [location]</code> gets all of the available moves for the piece at [location] if a piece exists there ljygvSDLJgsladuvghSDLUhgsa;iughsalughliUFGEW
