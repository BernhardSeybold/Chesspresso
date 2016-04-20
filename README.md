# What Is Chesspresso™?

Chesspresso™ is Java library to help developers in writing any kind of chess program. It is open-source (LGPL) and contains state-of the art algorithms to implement chess concepts.

# What Is Chesspresso™ For?

When you always wanted to write a chess application but feared the effort to implement all the necessary data structures and rules, Chesspresso™ is the just what you might be looking for.
Chesspresso™ wants to be the library you base your next Java chess-program on. It is prepared to a base for all kinds of chess applications, be it databases, game browsers, statistic programs, engines, front-end for internet chess servers, teaching programs, etc.

# Short History

Chesspresso™ was actually not only a chess library, but a full [database program](http://www.chesspresso.org). Feel free to download that version as well (not open-source). Recently, I decided to make the core of the application open-source and call it Chesspresso™ as well. I have not decided yet how to resolve that confusion.

# Power versus Performance

If your goal is to write a world-class chess engine, Java should not be your language of choice. However, if you have the choice to use a speedy versus a slow implementation, you would also choose the former one, wouldn't you? If it is not too expensive both money and time-wise. And it is neither!

Chesspresso™ tries to combine both performance (memory footprint and speed) and programmatical power (through high-level abstractions and interfaces). Chesspresso™ was optimized for speed but only if it did not harm clean design.

# Main Concepts

Browse the entire [Javadoc](http://www.chesspresso.org/javadoc/index.html) to get an impression on the available features.

## Fundamental Models

*   [Chess](http://www.chesspresso.org/javadoc/chesspresso/Chess.html): a class containing general definitions for chess
*   [Move](http://www.chesspresso.org/javadoc/chesspresso/move/Move.html): a class representing chess moves
*   Position: several abstractions of a chess position are available: [immutable](http://www.chesspresso.org/javadoc/chesspresso/position/ImmutablePosition.html), [mutable](javadoc/chesspresso/position/MutablePosition.html), and [moveable](javadoc/chesspresso/position/MoveablePosition.html) position (move, undo move, generateAllMoves) interfaces plus implementations, the [main](javadoc/chesspresso/position/Position.html) one is based on bitboards.
*   [Game](http://www.chesspresso.org/javadoc/chesspresso/game/Game.html): abstraction of a chess game with support for lines, annotations, ability to traverse, walk through the game, etc.

## Views

*   [PositionView](http://www.chesspresso.org/javadoc/chesspresso/position/view/PositionView.html): an interactive panel to display a position, reacts on piece dragging and square clicking and fowrads events to listeners
*   [GameBrowser](http://www.chesspresso.org/javadoc/chesspresso/game/view/GameBrowser.html): an interactive panel to display a game, contains both a textual (based on PGN) and a graphical view (based on the free Chess Cases font), support for browsing the game
*   [HTMLGameBrowser](http://www.chesspresso.org/javadoc/chesspresso/game/view/HTMLGameBrowser.html): produces HTML displaying a game, allowing to browse through the game with a web browser. Uses JavaScript. [Here](http://www.chesspresso.org/chesshtml/luke.html) is an example.

## Standards

*   PGN (portable game notation): the de-facto standard for ASCII-based game collections.
    Chesspresso™ contains a high-speed [PGNReader](http://www.chesspresso.org/javadoc/chesspresso/pgn/PGNReader.html) and [PGNWriter](http://www.chesspresso.org/javadoc/chesspresso/pgn/PGNWriter.html)
*   [FEN](http://www.chesspresso.org/javadoc/chesspresso/position/FEN.html) (Forsyth-Edwards Notation): a standard for describing chess positions using the ASCII character set.
    Chesspresso™ contains support to parse and generate FENs
*   [NAG](http://www.chesspresso.org/javadoc/chesspresso/NAG.html) (Numeric Annotation Glyph): to annotate chess moves and games
    Chesspresso™ understands NAG and symbolic and English descriptions
*   xboard: contains a class to start xboard compatible engines (like crafty)
*   EPD support will follow soon.

## ChesspressoBar, Plugins (future plans)

When you always wanted to create a small chess releated program but feared the overhead for all the basic functionality to get started, the ChesspressoBar is for you.

The ChesspressoBar is an application to work with chess games and collections. It allows to read and write PGN files, to view and create games, and much more. The main feature of the ChesspressoBar, however, is that is allows to house plugins. Plugins comprise some chess-related functionality that can be plugged into a framework.

Possible plugin include playing engines, statistic modules, teaching application. Basically everything that operates on chess games and positions.

The ChesspressoBar will be released within one of the next releases of Chesspresso.

# Who Wrote Chesspresso™?

The author of Chesspresso™ is Bernhard Seybold. That's me. I own a Ph.D. in computer science from the [Federal Institute of Technology in Zurich](http://www.ethz.ch) (ETHZ). No not in chess programming. However, I took all the courses that were slightly related to AI, computer games, algorithms, data-structures etc.
Now, I work as a senior software developer for [ELCA](http://www.elca.ch), a leading supplier of IT services in Switzerland. I play chess since over 20 years and still do (ELO 2100). Sometimes I play on FICS, as BerniMan.
My first game playing program was a connect4 engine, which I wrote competing against [this guy](http://www.fierz.ch). As you can see on his webpage, he still has his version plus a [world-class engine for Checkers](http://www.fierz.ch/checkers.htm). Now I thought it is my turn...
