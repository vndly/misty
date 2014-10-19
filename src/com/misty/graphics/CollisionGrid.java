package com.misty.graphics;

import java.util.ArrayList;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.SparseArray;
import com.misty.graphics.textures.Texture;
import com.misty.kernel.Process;

public class CollisionGrid
{
	private final SparseArray<List<Process>> grid = new SparseArray<List<Process>>();
	private static int CELL_SIZE = 32;
	private static int CELLS_PER_ROW = (int)Math.sqrt(Integer.MAX_VALUE);
	
	public static void initialize(int cellSize)
	{
		CollisionGrid.CELL_SIZE = cellSize;
	}
	
	public void clear()
	{
		this.grid.clear();
	}
	
	public void addProcess(Process process)
	{
		int[] cells = getProcessCells(process);
		
		int cellLeftBottom = cells[0];
		int cellLeftTop = cells[1];
		int cellRightTop = cells[2];
		int cellLRightBottom = cells[3];
		
		addProcessToTable(process, cellLeftBottom);
		
		if (cellLeftTop != cellLeftBottom)
		{
			addProcessToTable(process, cellLeftTop);
		}
		
		if ((cellRightTop != cellLeftBottom) && (cellRightTop != cellLeftTop))
		{
			addProcessToTable(process, cellRightTop);
		}
		
		if ((cellLRightBottom != cellLeftBottom) && (cellLRightBottom != cellLeftTop) && (cellLRightBottom != cellRightTop))
		{
			addProcessToTable(process, cellLRightBottom);
		}
	}
	
	private int[] getProcessCells(Process process)
	{
		int cellLeftBottom = getCellId(process.x, process.y);
		int cellLeftTop = getCellId(process.x, process.y + process.height);
		int cellRightTop = getCellId(process.x + process.width, process.y + process.height);
		int cellLRightBottom = getCellId(process.x + process.width, process.y);
		
		return new int[]
			{
				cellLeftBottom, cellLeftTop, cellRightTop, cellLRightBottom
			};
	}
	
	private int getCellId(float x, float y)
	{
		int cellX = (int)Math.floor(x / CollisionGrid.CELL_SIZE);
		int cellY = (int)Math.floor(y / CollisionGrid.CELL_SIZE);
		
		return cellX + (cellY * CollisionGrid.CELLS_PER_ROW);
	}
	
	private void addProcessToTable(Process process, int cellId)
	{
		List<Process> list = this.grid.get(cellId);
		
		if (list == null)
		{
			List<Process> newList = new ArrayList<Process>();
			newList.add(process);
			
			this.grid.put(cellId, newList);
		}
		else
		{
			list.add(process);
		}
	}
	
	public List<Process> getCollisions(Process process, Class<?>... classes)
	{
		List<Process> result = new ArrayList<Process>();
		
		int[] cells = getProcessCells(process);
		
		int cellLeftBottom = cells[0];
		int cellLeftTop = cells[1];
		int cellRightTop = cells[2];
		int cellLRightBottom = cells[3];
		
		checkProcessCollisions(process, cellLeftBottom, result, classes);
		
		if (cellLeftTop != cellLeftBottom)
		{
			checkProcessCollisions(process, cellLeftTop, result, classes);
		}
		
		if ((cellRightTop != cellLeftBottom) && (cellRightTop != cellLeftTop))
		{
			checkProcessCollisions(process, cellRightTop, result, classes);
		}
		
		if ((cellLRightBottom != cellLeftBottom) && (cellLRightBottom != cellLeftTop) && (cellLRightBottom != cellRightTop))
		{
			checkProcessCollisions(process, cellLRightBottom, result, classes);
		}
		
		return result;
	}
	
	private void checkProcessCollisions(Process process, int cellId, List<Process> result, Class<?>... classes)
	{
		List<Process> list = this.grid.get(cellId);
		
		if (list != null)
		{
			int size = list.size();
			
			for (int i = 0; i < size; i++)
			{
				Process currentProcess = list.get(i);
				
				if ((currentProcess != process) && (!result.contains(currentProcess)) && (isValidClass(currentProcess, classes)))
				{
					if (CollisionGrid.collide(process, currentProcess))
					{
						result.add(currentProcess);
					}
				}
			}
		}
	}
	
	private boolean isValidClass(Process process, Class<?>... classes)
	{
		boolean result = false;
		
		for (Class<?> classe : classes)
		{
			if (process.getClass().equals(classe))
			{
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	public static boolean collide(Process processA, Process processB)
	{
		boolean result = false;
		
		Texture spriteA = processA.texture;
		Texture spriteB = processB.texture;
		
		// TODO: implement own rectangle intersection
		if ((spriteA != null) && (spriteB != null))
		{
			int xA = (int)processA.x;
			int yA = (int)processA.y;
			
			int xB = (int)processB.x;
			int yB = (int)processB.y;
			
			Rect rectA = new Rect(xA, yA, spriteA.width + xA, spriteA.height + yA);
			Rect rectB = new Rect(xB, yB, spriteB.width + xB, spriteB.height + yB);
			
			if (rectA.intersect(rectB))
			{
				Bitmap bitmapA = spriteA.bitmap;
				Bitmap bitmapB = spriteB.bitmap;
				
				int intersectionWidth = rectA.width();
				int intersectionHeight = rectA.height();
				
				for (int x = 0; ((x < intersectionWidth) && (!result)); x++)
				{
					for (int y = 0; (y < intersectionHeight) && (!result); y++)
					{
						int realX = x + rectA.left;
						int realY = y + rectA.top;
						
						int alphaA = Color.alpha(bitmapA.getPixel(realX - xA, realY - yA));
						int alphaB = Color.alpha(bitmapB.getPixel(realX - xB, realY - yB));
						
						result = (alphaA > 0) && (alphaB > 0);
					}
				}
			}
		}
		
		return result;
	}
}