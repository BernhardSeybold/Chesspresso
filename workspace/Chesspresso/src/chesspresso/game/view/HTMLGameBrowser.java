/*
 * Copyright (C) Bernhard Seybold. All rights reserved.
 *
 * This software is published under the terms of the LGPL Software License,
 * a copy of which has been included with this distribution in the LICENSE.txt
 * file.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * $Id: HTMLGameBrowser.java,v 1.3 2003/01/04 16:23:32 BerniMan Exp $
 */

package chesspresso.game.view;

import chesspresso.*;
import chesspresso.game.*;
import chesspresso.move.*;
import chesspresso.position.*;
import ch.seybold.util.StringKit;

import java.io.*;


/**
 * Producer for HTML pages displaying a game.
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.3 $
 */
public class HTMLGameBrowser implements GameListener
{
    
    // TODO version with only one board
    
    private static int MAX_LINE_DEPTH = 100;
    
    private StringBuffer m_moves;
    private StringBuffer m_posData;
    private StringBuffer m_lastData;
    private Game m_game;
    private int m_gameNumber;
    private int m_moveNumber;
    private boolean m_showMoveNumber;
    private int[] m_lasts;

    
    //======================================================================
    // GameListener Methods
    
    public void notifyLineStart(int level)
    {
        m_moves.append(" (");
        m_showMoveNumber = true;
        m_lasts[level + 1] = m_lasts[level];
    }
    
    public void notifyLineEnd(int level)
    {
        m_moves.append(") ");
        m_showMoveNumber = true;
    }

    private static String getPosData(ImmutablePosition pos, boolean first)
    {
        String fen = FEN.getFEN(pos);
        fen = fen.substring(0, fen.indexOf(' '));
        return (first ? "\"" : ",\"") + StringKit.remove(fen, '/') + "\"\n";
    }

    public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level)
    {
        ImmutablePosition pos = m_game.getPosition();
        
        boolean isMainLine = (level == 0);
        String type = isMainLine ? "main" : "line";
        
        m_moves.append("<a name=\"" + m_moveNumber + "\" class=\"" + type + "\" href=\"javascript:go(" + m_gameNumber + "," + m_moveNumber + ")\">");
        if (m_showMoveNumber) {
            m_moves.append((plyNumber / 2 + 1) + ".");
        }
        m_showMoveNumber = Chess.isWhitePly(plyNumber+1);
        
        m_moves.append(move.toString());
        if (nags != null) {
            for (int i=0; i<nags.length; i++) {
                m_moves.append(NAG.getShortString(nags[i]));
            }
            m_showMoveNumber = true;
        }
        m_moves.append("</a> ");
        if (comment != null) {
            m_moves.append("<span class=\"comment\">").append(comment).append("</span> ");
        }
        
        m_posData.append(getPosData(pos, false));
        m_lastData.append(",").append(m_lasts[level]);
        m_lasts[level] = m_moveNumber;
        
        m_moveNumber++;
    }

    //======================================================================
    
    private String[] m_wimgs;
    private String[] m_bimgs;
    private String m_imagePrefix;
    private String m_styleFilename;
    
    //======================================================================
    
    /**
     * Create a new HTMLGameBrowser with default settings.
     */
    public HTMLGameBrowser()
    {
        m_wimgs = new String[] {
            "wkw.gif", "wpw.gif", "wqw.gif", "wrw.gif", "wbw.gif", "wnw.gif", "now.gif",
            "bnw.gif", "bbw.gif", "brw.gif", "bqw.gif", "bpw.gif", "bkw.gif"
        };
        m_bimgs = new String[] {
            "wkb.gif", "wpb.gif", "wqb.gif", "wrb.gif", "wbb.gif", "wnb.gif", "nob.gif",
            "bnb.gif", "bbb.gif", "brb.gif", "bqb.gif", "bpb.gif", "bkb.gif"
        };
        m_imagePrefix = "";
        m_styleFilename = null;
    }
    
    //======================================================================
    
    /**
     * Set the name of the style file. If name is set to null, inline style
     * definition will be used. Default is inline style.<br>
     * When using an external style file, the following styles are expected:
     * <ul>
     *  <li>a.main: the anchor used for moves in the main line
     *  <li>a.line: the anchor used for moves in side-lines
     *  <li>span.comment: used for move comments
     *  <li>table.content: the content table containing the board left and the moves on the right
     * </ul>
     *
     *@param styleFilename the name of the style file
     */
    private void setStyleFilename(String styleFilename)
    {
        m_styleFilename = styleFilename;
    }
    
    /**
     * Set thes prefix for images. The default is empty.
     *
     *@param imagePrefix the prefix for images, must contain trailing slashes
     */
    private void setImagePrefix(String imagePrefix)
    {
        m_imagePrefix = imagePrefix;
    }
    
    /**
     * Sets the name of an square image. The default names are set according to
     * the following scheme: First letter is the color of the stone (b, w), second
     * letter the piece (k, q, r, b, n, p) third letter the square color (b, w),
     * extension is gif. now.gif and nob.gif are used for empty squares.<br>
     * For instance: wkw.gif determines a white king on a white square,
     * bbb.gif is a black bishop on a black square.
     *
     *@param stone the stone displayed
     *@param whiteSquare whether or not the square is white
     *@param name the name of the corresponding image
     */
    private void setStoneImageName(int stone, boolean whiteSquare, String name)
    {
        if (whiteSquare) {
            m_wimgs[stone - Chess.MIN_STONE] = name;
        } else {
            m_bimgs[stone - Chess.MIN_STONE] = name;
        }
    }
    
    /**
     * Returns the name of the image.
     *
     *@param stone the stonbe displayed
     *@param whiteSquare whether or not the square is white
     */
    private String getImageForStone(int stone, boolean isWhite)
    {
        return m_imagePrefix + (isWhite ? m_wimgs[stone - Chess.MIN_STONE] : m_bimgs[stone - Chess.MIN_STONE]);
    }
    
    //======================================================================
    
    private void produceGame(GameModel gameModel, int gameNumber, final PrintWriter writer)
    {
        Game game = new Game(gameModel);
        
        //---------- position -----------
        game.gotoStart();
        ImmutablePosition startPos = m_game.getPosition();
        writer.println("<table cellspacing=\"0\" cellpadding=\"0\">");
        for (int row = Chess.NUM_OF_ROWS-1; row >= 0; row--) {
            writer.println("  <tr>");
            for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
                int sqi = Chess.coorToSqi(col, row);
                writer.print("<td><img src=\"" + getImageForStone(startPos.getStone(sqi), Chess.isWhiteSquare(sqi)) + "\"></td>");
            }
            writer.println("</tr>");
        }
        
        //---------- toolbar -----------
        writer.println("</table>\n");
        writer.println("<center><form name=\"tapecontrol\">");
        writer.println("<input type=button value=\" Flip \" onClick=\"flip(" + gameNumber + ");\" onDblClick=\"flip(" + m_gameNumber + ");\">");
        writer.println("<input type=button value=\" Start \" onClick=\"gotoStart(" + gameNumber + ");\" onDblClick=\"gotoStart(" + m_gameNumber + ");\">");
        writer.println("<input type=button value=\" &lt; \" onClick=\"goBackward(" + gameNumber + ");\" onDblClick=\"goBackward(" + m_gameNumber + ");\">");
        writer.println("<input type=button value=\" &gt; \" onClick=\"goForward(" + gameNumber + ");\" onDblClick=\"goForward(" + m_gameNumber + ");\">");
        writer.println("<input type=button value=\" End \" onClick=\"gotoEnd(" + gameNumber + ");\" onDblClick=\"gotoEnd(" + m_gameNumber + ");\">");
        writer.println("</form></center>");
        writer.println();
        
        //---------- game header -----------
        writer.println("</td><td valign=\"top\">");
        writer.println("<a id=\"game" + gameNumber + "\">");
        writer.print("<b>" + game.getWhite());
        if (game.getWhiteElo() > 0) writer.print(" (" + game.getWhiteElo() + ")");
        writer.print(" - " + game.getBlack());
        if (game.getBlackElo() > 0) writer.print(" (" + game.getBlackElo() + ")");
        writer.println("&nbsp;&nbsp;" + m_game.getResultStr() + "</b></a><br/>");
        writer.print(game.getEvent() + ", Round " + game.getRound() + ", ");
        if (game.getSite() != null) writer.print(game.getSite() + ", ");
        writer.println(game.getDate() + "<br/>");
        if (game.getECO() != null) writer.println(game.getECO() + "<br/>");
        writer.println("<br/>");

        //---------- produce game moves -----------
        game.traverse(this, true);
        m_posData.append("];\n");
        m_lastData.append("];\n");

        m_moves.append("<br/>" + m_game.getResultStr());
        m_moves.append("</td</tr></table>");    
    }
    
    //======================================================================
    
    public synchronized void produceHTML(OutputStream outStream, GameModel gameModel, boolean contentOnly)
    {
        produceHTML(outStream, new GameModel[] {gameModel}, contentOnly);
    }
 
    /**
     * Produces HTML to display a game.
     *
     *@param outStream where the HTML will be sent to
     *@param game the game to display.
     */
    public void produceHTML(OutputStream outStream, GameModel[] gameModel, boolean contentOnly)
    {
        Writer writer = new OutputStreamWriter(outStream);
        produceHTML(writer, gameModel, contentOnly);
        try {writer.flush(); } catch (IOException ex) {ex.printStackTrace();}
    }
    
    public synchronized void produceHTML(Writer writer, GameModel gameModel, boolean contentOnly)
    {
        produceHTML(writer, new GameModel[] {gameModel}, contentOnly);
    }
 
    /**
     * Produces HTML to display a game.
     *
     *@param outStream where the HTML will be sent to
     *@param game the game to display.
     *@param contentOnly if true skip header and footer information, use this if you want to
     *       produce your own header and footer
     */
    public synchronized void produceHTML(Writer writer, GameModel[] gameModels, boolean contentOnly)
    {
        PrintWriter out = new PrintWriter(writer);
        
        m_moves = new StringBuffer();
        m_posData = new StringBuffer();
        m_lastData = new StringBuffer();
        
        m_posData.append("var sq=new Array(" + gameModels.length + ");\n");
        m_lastData.append("var last=new Array(" + gameModels.length + ");\n");
        
        for (m_gameNumber = 0; m_gameNumber < gameModels.length; m_gameNumber++) {
            m_moveNumber = 0;
            m_showMoveNumber = true;
            m_lasts = new int[MAX_LINE_DEPTH]; m_lasts[0] = 0;
            m_game = new Game(gameModels[m_gameNumber]);

            m_posData.append("sq[" + m_gameNumber + "]=[\n ");
            m_lastData.append("last[" + m_gameNumber + "]=[0");
        
            m_game.gotoStart();
            m_posData.append(getPosData(m_game.getPosition(), true));
            m_moveNumber++;
            
            m_moves.append("<table class=\"content\"><tr><td valign=\"top\">\n");

            m_game.gotoStart();
            ImmutablePosition startPos = m_game.getPosition();
            m_moves.append("<table cellspacing=\"0\" cellpadding=\"0\">\n");
            for (int row = Chess.NUM_OF_ROWS-1; row >= 0; row--) {
                m_moves.append("  <tr>");
                for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
                    int sqi = Chess.coorToSqi(col, row);
                    m_moves.append("<td><img src=\"" + getImageForStone(startPos.getStone(sqi), Chess.isWhiteSquare(sqi)) + "\"></td>");
                }
                m_moves.append("</tr>\n");
            }
            m_moves.append("</table>\n");
            m_moves.append("<center><form name=\"tapecontrol\">\n");
            m_moves.append("<input type=button value=\" Flip \" onClick=\"flip(" + m_gameNumber + ");\" onDblClick=\"flip(" + m_gameNumber + ");\">\n");
            m_moves.append("<input type=button value=\" Start \" onClick=\"gotoStart(" + m_gameNumber + ");\" onDblClick=\"gotoStart(" + m_gameNumber + ");\">\n");
            m_moves.append("<input type=button value=\" &lt; \" onClick=\"goBackward(" + m_gameNumber + ");\" onDblClick=\"goBackward(" + m_gameNumber + ");\">\n");
            m_moves.append("<input type=button value=\" &gt; \" onClick=\"goForward(" + m_gameNumber + ");\" onDblClick=\"goForward(" + m_gameNumber + ");\">\n");
            m_moves.append("<input type=button value=\" End \" onClick=\"gotoEnd(" + m_gameNumber + ");\" onDblClick=\"gotoEnd(" + m_gameNumber + ");\">\n");
            m_moves.append("</form></center>");
            m_moves.append("\n");

            //---------- game header -----------
            m_moves.append("</td><td valign=\"top\">\n");
            m_moves.append("<a id=\"game" + m_gameNumber + "\">");
            m_moves.append("<b>" + m_game.getWhite());
            if (m_game.getWhiteElo() > 0) m_moves.append(" (" + m_game.getWhiteElo() + ")");
            m_moves.append(" - " + m_game.getBlack());
            if (m_game.getBlackElo() > 0) m_moves.append(" (" + m_game.getBlackElo() + ")");
            m_moves.append("&nbsp;&nbsp;" + m_game.getResultStr() + "</b></a><br/>");
            m_moves.append(m_game.getEvent() + ", Round " + m_game.getRound() + ", ");
            if (m_game.getSite() != null) m_moves.append(m_game.getSite() + ", ");
            m_moves.append(m_game.getDate() + "<br/>");
            if (m_game.getECO() != null) m_moves.append(m_game.getECO() + "<br/>");
            m_moves.append("<br/>");
        
            //---------- produce game moves -----------
            m_game.traverse(this, true);
            m_posData.append("];\n");
            m_lastData.append("];\n");
        
            m_moves.append("<br/>" + m_game.getResultStr());
            m_moves.append("</td</tr></table>\n");
        }
        

        if (!contentOnly) {
            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"de\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<meta name=\"generator\" content=\"Chesspresso\" />");
            // TODO change title
            out.println("<title>" + m_game + "</title>");
            if (m_styleFilename == null) {
                out.println("<style type=\"text/css\">");
                out.println("   .main {text-decoration:none}");
                out.println("   .line {text-decoration:none}");
                out.println("  a.main {font-weight:bold; color:black}");
                out.println("  a.line {color:black}");
                out.println("  table.content {cell-spacing:40}");
//                out.println("  table.board {cell-padding=0; cell-spacing:0}");
                out.println("  span.comment {font-style:italic}");
                out.println("</style>");
            } else {
                out.println("<link rel=\"stylesheet\" href=\"" + m_styleFilename + "\" type=\"text/css\" />");
            }
        
            out.println("<script language=\"JavaScript\">");
            out.println("img1=new Image();img1.src=\"wkw.gif\";img2=new Image();img2.src=\"wpw.gif\";img3=new Image();img3.src=\"wqw.gif\";img4=new Image();img4.src=\"wrw.gif\";img5=new Image();img5.src=\"wbw.gif\";img6=new Image();img6.src=\"wnw.gif\";img7=new Image();img7.src=\"now.gif\";img8=new Image();img8.src=\"bnw.gif\";img9=new Image();img9.src=\"bbw.gif\";img10=new Image();img10.src=\"brw.gif\";img11=new Image();img11.src=\"bqw.gif\";img12=new Image();img12.src=\"bpw.gif\";img13=new Image();img13.src=\"bkw.gif\";img14=new Image();img14.src=\"wkb.gif\";img15=new Image();img15.src=\"wpb.gif\";img16=new Image();img16.src=\"wqb.gif\";img17=new Image();img17.src=\"wrb.gif\";img18=new Image();img18.src=\"wbb.gif\";img19=new Image();img19.src=\"wnb.gif\";img20=new Image();img20.src=\"ob.gif\";img21=new Image();img21.src=\"bnb.gif\";img22=new Image();img22.src=\"bbb.gif\";img23=new Image();img23.src=\"brb.gif\";img24=new Image();img24.src=\"bqb.gif\";img25=new Image();img25.src=\"bpb.gif\";img26=new Image(); img26.src=\"bkb.gif\";");
            out.print("moveNumber = [0");
            for (int i=0; i<gameModels.length-1; i++) {out.print(",0");}
            out.println("];");
            out.print("fromWhite = [true");
            for (int i=0; i<gameModels.length-1; i++) {out.print(",true");}
            out.println("];");
            out.print("imgs = new Array(");
            for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
                out.print("'" + getImageForStone(stone, true) + "',");
            }
            for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
                out.print("'" + getImageForStone(stone, false) + "'");
                if (stone < Chess.MAX_STONE) out.print(",");
            }
            out.println(");");
        
            // TODO preload images??
            
//        out.println("function go(num) {window.document.anchors[moveNumber-1].style.background=\"white\"; if (num<0) moveNumber=0; else if (num>" + (m_moveNumber - 1) + ") moveNumber=" + (m_moveNumber - 1) + "; else moveNumber=num; for(i=0;i<64;i++){if ((Math.floor(i/8)%2)==(i%2)) window.document.images[i].src=wimgs[sq[num][i]]; else window.document.images[i].src=bimgs[sq[num][i]];}; window.document.anchors[moveNumber-1].style.background=\"black\";}");
            out.println("function go(game,num) {");
            // TODO style for selected move
            out.println("  var gStart=1; for (var i=0; i<game; i++) gStart += sq[i].length;");
            out.println("  var anc = window.document.anchors;");
            out.println("  var wdi = window.document.images;");
            out.println("  if (moveNumber[game]>0) {anc[gStart+moveNumber[game]-1].style.background=\"white\"; anc[gStart+moveNumber[game]-1].style.color=\"black\";}");
            out.println("  if (num<0) moveNumber[game]=0;");
            out.println("  else if (num>=sq[game].length) moveNumber[game]=sq[game].length-1;");
            out.println("  else moveNumber[game]=num;");
            out.println("  var fen=sq[game][moveNumber[game]];");
            out.println("  var empties=0; var ind=0;");
            out.println("  for(var i=0;i<64;i++){");
            out.println("    if (empties==0) {");
            out.println("      switch(fen.charAt(ind++)) {");
            out.println("        case 'K': piece=0; break;");
            out.println("        case 'P': piece=1; break;");
            out.println("        case 'Q': piece=2; break;");
            out.println("        case 'R': piece=3; break;");
            out.println("        case 'B': piece=4; break;");
            out.println("        case 'N': piece=5; break;");
            out.println("        case 'n': piece=7; break;");
            out.println("        case 'b': piece=8; break;");
            out.println("        case 'r': piece=9; break;");
            out.println("        case 'q': piece=10; break;");
            out.println("        case 'p': piece=11; break;");
            out.println("        case 'k': piece=12; break;");
            out.println("        case '1': empties=1; break;");
            out.println("        case '2': empties=2; break;");
            out.println("        case '3': empties=3; break;");
            out.println("        case '4': empties=4; break;");
            out.println("        case '5': empties=5; break;");
            out.println("        case '6': empties=6; break;");
            out.println("        case '7': empties=7; break;");
            out.println("        case '8': empties=8; break;");
            out.println("      }");
            out.println("    }");
            out.println("    if (empties>0) {empties--; piece=6;}");
            out.println("    if (((i>>>3)%2)!=(i%2)) piece+=13;");
            out.println("    if (fromWhite[game]) sqi=i; else sqi = 7-(i&7) + 8*(7-(i>>>3));");
            out.println("    wdi[64*game+sqi].src=imgs[piece];");
//			out.println("    if (wdi[64*game+sqi].src != imgs[piece]) wdi[64*game+sqi].src=imgs[piece];");
            
//            out.println("  for(var i=0;i<64;i++){");
//            out.println("    if ((Math.floor(i/8)%2)==(i%2)) offset=0; else offset=13;");
//            out.println("    piece = sq[game][moveNumber[game]].charCodeAt(i)-0x61;");
//            out.println("    if (fromWhite[game]) index=i; else index = 7-(i&7) + 8*(7-(i>>>3));");
//            out.println("    if (wdi[64*game+index].src != imgs[piece+offset]) wdi[64*game+index].src=imgs[piece+offset];");
            out.println("  }");
            out.println("  if (moveNumber[game]>0) {anc[gStart+moveNumber[game]-1].style.background=\"black\"; anc[gStart+moveNumber[game]-1].style.color=\"white\";}");
            out.println("}");
            out.println("function gotoStart(game) {go(game,0);}");
            out.println("function gotoEnd(game) {go(game,sq[game].length-1);}");
            out.println("function goBackward(game) {go(game,last[game][moveNumber[game]]);}");
            out.println("function goForward(game) {for (i=sq[game].length-1; i>moveNumber[game]; i--) if (last[game][i]==moveNumber[game]) {go(game,i); break;}}");
            out.println("function flip(game) {fromWhite[game] = !fromWhite[game]; go(game,moveNumber[game]);}");
            out.println();
            out.println(m_posData.toString());
            out.println(m_lastData.toString());
            out.println("</script>");
            out.println();

            out.println("</head>");
            out.println();

            out.println("<body>");
        }
        
        out.println("<p><small><b>InstantChesspresso</b> created by <a href=\"http://www.chesspresso.org\">Chesspresso</a></small></p>");
        
        //---------- list of all games -----------
        if (gameModels.length > 1) {
            out.println("<p>");
            for (int i=0; i<gameModels.length; i++) {
                out.println("<a href=\"#game" + i + "\">" + gameModels[i] + "</a><br/>");
            }
            out.println("</p>");
        }
        
        //---------- the games -----------
        out.println(m_moves.toString());
        
        if (!contentOnly) {
            out.println("</body></html>");
        }
    }
    
    public static void main(String[] args)
    {
        try {
        	InputStream in = new FileInputStream(args[0]);
            chesspresso.pgn.PGNReader pgn = new chesspresso.pgn.PGNReader(in, args[0]);
            HTMLGameBrowser html = new HTMLGameBrowser();
            html.produceHTML(System.out, pgn.parseAll(), false);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

}