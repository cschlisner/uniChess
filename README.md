# uniChess
##A general chess API for java

This API is intended for use in other java applications where you might need to play a chess game for whatever reason. 

###Starting a game. 

All you need to do to initiate a game is call the Game constructor with two Strings representing each player name (each player holds the white and black teams respectively) and an optional boolean for board output as an image instead of text with unicode characters.

Game myGame = new Game("player1", "player2", false);



and supply input with the input() method, while reading output using the getOutput() method. It is up to you to handle which player is supplying input, the game will automatically switch which player it is accepting input from. The white team will always initiate the game. 

