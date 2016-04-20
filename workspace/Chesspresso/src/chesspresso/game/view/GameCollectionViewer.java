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
 * $Id$
 */
package chesspresso.game.view;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

import chesspresso.game.GameModel;
import chesspresso.pgn.PGN;

/**
 * 
 * 
 * @author  Bernhard Seybold
 * @version $Revision$
 *
 */
public class GameCollectionViewer extends JTable
{
	
	private class MyTableModel extends AbstractTableModel
	{
		
		public int getColumnCount()
		{
			return 5;
		}
		
		public String getColumnName(int col)
		{
			switch(col) {
				case 0: return "White";
				case 1: return "Black";
				case 2: return "EventRoundSite";
				case 3: return "Date";
				case 4: return "Result";
				default: throw new RuntimeException("Unknown Column " + col);
			}
		}
		
		public int getRowCount()
		{
			return m_gameModels.size();
		}
		
		public Object getValueAt(int row, int col)
		{
			GameModel gameModel = (GameModel)m_gameModels.get(row);
			switch(col) {
				case 0: return gameModel.getHeaderModel().getWhite();
				case 1: return gameModel.getHeaderModel().getBlack();
				case 2: return gameModel.getHeaderModel().getSite()
                             + " " + gameModel.getHeaderModel().getEvent()
				             + " [" + gameModel.getHeaderModel().getRound() + "]";
				case 3: return gameModel.getHeaderModel().getDate();
				case 4: return PGN.getShortResult(gameModel.getHeaderModel().getResult());
				default: throw new RuntimeException("Unknown Column " + col);
			}
		}
		
	}

	//======================================================================
	
	private ArrayList m_gameModels;
	
	//======================================================================
		
	public GameCollectionViewer(GameModel[] gameModels)
	{
		super();
		m_gameModels = new ArrayList();
		setGameModels(gameModels);
		getColumnModel().getColumn(0).setWidth(40);
		getColumnModel().getColumn(1).setWidth(40);
		getColumnModel().getColumn(2).setWidth(80);
		getColumnModel().getColumn(3).setWidth(20);
		getColumnModel().getColumn(3).setWidth(10);
	}
	
	public void setGameModels(GameModel[] gameModels)
	{
		for (int i=0; i<gameModels.length; i++) {
			m_gameModels.add(gameModels[i]);
		}
		setModel(new MyTableModel());
	}
	
	public void addGameModel(GameModel gameModel)
	{
		m_gameModels.add(gameModel);
		setModel(new MyTableModel());
	}
		
	public void removeGameModel(GameModel gameModel)
	{
		m_gameModels.remove(gameModel);
		setModel(new MyTableModel());
	}
		
	public GameModel[] getGameModels()
	{
		return (GameModel[])m_gameModels.toArray(new GameModel[m_gameModels.size()]);
	}
	
	public GameModel getSelectedGameModel()
	{
		int index = getSelectedRow();
		if (index >= 0) {
			return (GameModel)m_gameModels.get(index);
		} else {
			return null;
		}
	}
	
	public GameModel[] getSelectedGameModels()
	{
		int num = getSelectedRowCount();
		GameModel[] gameModels = new GameModel[num];
		int index = 0;
		for (int row = 0; row < getRowCount(); row++) {
			if (isRowSelected(row)) {
				gameModels[index++] = (GameModel)m_gameModels.get(row);
			}
		}
		return gameModels;
	}
	
}