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
 * $Id: DefaultGameScorer.java,v 1.1 2002/12/08 13:27:33 BerniMan Exp $
 */

package chesspresso.game;

/**
 * Implementation of a games scorer.
 * The score is higher the more information is filled into the game header.
 *
 * <ul>
 *  <li>white, black, event, site: 1 point for each letter
 *  <li>date, result; 8 points if exist
 *  <li>white elo, black elo: 4 points if exist
 *  <li>eco: 1 point if exists
 * </ul>
 *
 * @author  Bernhard Seybold
 * @version $Revision: 1.1 $
 */
public class DefaultGameScorer implements GameScorer
{

    public int getScore(GameModel gameModel)
    {
        String s;
        GameHeaderModel headerModel = gameModel.getHeaderModel();
        GameMoveModel moveModel = gameModel.getMoveModel();
        
        int score = 0;
        score += moveModel.getTotalNumOfPlies()  * 3;
        score += moveModel.getTotalCommentSize() * 1;
        
        s = headerModel.getWhite();  if (s != null) score += s.length() * 1;
        s = headerModel.getBlack();  if (s != null) score += s.length() * 1;
        if (headerModel.getDate()          != null) score += 8;
        if (headerModel.getResultStr()     != null) score += 8;  // TODO "real" result only
        s = headerModel.getEvent();  if (s != null) score += s.length() * 1;
        s = headerModel.getSite();   if (s != null) score += s.length() * 1;
        if (headerModel.getWhiteEloStr()   != null) score += 4;
        if (headerModel.getBlackEloStr()   != null) score += 4;
        if (headerModel.getECO()           != null) score += 1;
        
        return score;
    }
    
}