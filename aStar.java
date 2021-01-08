package test;
import java.util.ArrayList;
import java.util.Arrays;

public class aStar {
	
	public enum Direction {NORTH, SOUTH, EAST, WEST, NORTH_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST};
	
	public static boolean isDestination(int row, int col, int xEnd, int yEnd) {
		if ((row == xEnd) && (col == yEnd)) 
			return true;
		else
			return false;
	}
	
	// distance from coords to start
	public static int calcGValue(double graph[][], int row, int col, int xStart, int yStart) {
		double rowSum = 0;
		double colSum = 0;
		
		if (row < xStart) {
			for (int i = row; i < xStart; i++) 
				rowSum += graph[i][col];
		}
		else {
			for (int i = xStart; i < row; i++) 
				rowSum += graph[i][col];
		}
		
		if (col < yStart) {
			for (int i = col; i < yStart; i++) 
				colSum += graph[row][i];
		}
		else {
			for (int i = yStart; i < col; i++) 
				colSum += graph[row][i];
		}
		
		return (int) (10 * Math.sqrt(((rowSum)*(rowSum)+ (colSum)* (colSum))));
	}
	
	// distance from start to end
	public static int calcHValue(double graph[][], int row, int col, int xEnd, int yEnd) {
		double rowSum = 0;
		double colSum = 0;
		
		if (row < xEnd) {
			for (int i = row; i < xEnd; i++) 
				rowSum += graph[i][col];
		}
		else {
			for (int i = xEnd; i < row; i++) 
				rowSum += graph[i][col];
		}
		
		if (col < yEnd) {
			for (int i = col; i < yEnd; i++) 
				colSum += graph[row][i];
		}
		else {
			for (int i = yEnd; i < col; i++) 
				colSum += graph[row][i];
		}
		
		return (int) (10 * Math.sqrt(((rowSum)*(rowSum)+ (colSum)* (colSum))));
	}
	
	public static Direction AStar(double graph[][], int xStart, int yStart, int xEnd, int yEnd) {
		int x = xStart;
		int y = yStart;
		int maxHCost = calcHValue(graph, x-1, y, xEnd, yEnd); // NORTH
		
		ArrayList<Direction> dir = new ArrayList<Direction>(); 
		ArrayList<Double> maxCost = new ArrayList<Double>();
		
		int lenRows = graph.length;
		int lenCols = graph[0].length;
		
		
		double fCost = 0; 
		int i = 0;
		while (!isDestination(x, y, xEnd, yEnd)) {
			maxCost.add((double) 0);
			dir.add(Direction.NORTH);
			
			// NORTH
			if ((x-1) >= 0) {
				double north = graph[x-1][y];
				if (x == 0) 
					fCost = 0;
				else  {
					
					fCost = north * (calcHValue(graph, x-1, y, xEnd, yEnd) + calcGValue(graph, x-1, y, xStart, yStart));
				}
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x-1, y, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x-1, y, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH);
				}
			}
			
			
			// SOUTH
			if ((x+1) < lenRows) {
				double south = graph[x+1][y];
				if (x == lenRows)
					fCost = 0;
				else
					fCost = south * (calcHValue(graph, x+1, y, xEnd, yEnd) + calcGValue(graph, x+1, y, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x+1, y, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x+1, y, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH);
				}
			}
			
			
			// EAST
			if ((y+1) < lenCols) {
				double east = graph[x][y+1];
				if (y == lenCols - 1)
					fCost = 0;
				else
					fCost = east * (calcHValue(graph, x, y+1, xEnd, yEnd) + calcGValue(graph, x, y+1, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x, y+1, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x, y+1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.EAST);
				}
			}
			
			// WEST
			if ((y-1) >= 0) {
				double west = graph[x][y-1];
				if (y == 0)
					fCost = 0;
				else
					fCost = west * (calcHValue(graph, x, y-1, xEnd, yEnd) + calcGValue(graph, x, y-1, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x, y-1, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x, y-1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.WEST);
				}
			}
			
			// NORTH-EAST
			if (((x-1) >= 0) && (y+1 < lenCols)) {
				double northEast = graph[x-1][y+1];
				if (x == 0 || y == lenCols - 1)
					fCost = 0;
				else
					fCost = northEast * (calcHValue(graph, x-1, y+1, xEnd, yEnd) + calcGValue(graph, x-1, y+1, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x-1, y+1, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x-1, y+1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH_EAST);
				}
			}
			
			// NORTH-WEST
			if (((x-1) >= 0) && ((y-1) >= 0)) {
				double northWest = graph[x-1][y-1];
				if (x == 0 || y == 0)
					fCost = 0;
				else
					fCost = northWest * (calcHValue(graph, x-1, y-1, xEnd, yEnd) + calcGValue(graph, x-1, y-1, xStart, yStart));
				if (fCost >= maxCost.get(i) && calcHValue(graph, x-1, y-1, xEnd, yEnd) <= maxHCost) {
					maxHCost = calcHValue(graph, x-1, y-1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH_WEST);
				}
			}
			
			// SOUTH-EAST
			if (((x+1) < lenRows) && (y+1 < lenCols)) {
				double southEast = graph[x+1][y+1];
				if (x == lenRows-1 || y == lenCols-1)
					fCost = 0;
				else
					fCost = southEast * (calcHValue(graph, x+1, y+1, xEnd, yEnd) + calcGValue(graph, x+1, y+1, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x+1, y+1, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x+1, y+1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH_EAST);
				}
			}
			
			// SOUTH-WEST
			if (((x+1) < lenRows) && (y-1 >= 0)) {
				double southWest = graph[x+1][y-1];
				if (x == lenRows-1 || y == 0)
					fCost = 0;
				else
					fCost = southWest * (calcHValue(graph, x+1, y-1, xEnd, yEnd) + calcGValue(graph, x+1, y-1, xStart, yStart));
				if (fCost >= maxCost.get(i) && (calcHValue(graph, x+1, y-1, xEnd, yEnd) <= maxHCost)) {
					maxHCost = calcHValue(graph, x+1, y-1, xEnd, yEnd);
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH_WEST);
				}
			}
			
			//System.out.println(dir.get(i));
			switch(dir.get(i)) {
				case NORTH:
					x--;
					break;
				case SOUTH:
					x++;
					break;
				case EAST:
					y++;
					break;
				case WEST:
					y--;
					break;
				case NORTH_EAST:
					x--;
					y++;
					break;
				case NORTH_WEST:
					x--;
					y--;
					break;
				case SOUTH_EAST:
					x++;
					y++;
					break;
				case SOUTH_WEST:
					x++;
					y--;
				default:
					break;
			}
			
			System.out.println(x + " " + y);
			System.out.println(dir.get(i));
			
			maxHCost = 0;
			i++;
			
		}

		
		return dir.get(0);
	}

	public static void main(String[] args) {
		
		double[][] graph = new double[8][8];
        graph[0] = new double[]{0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[1] = new double[]{0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.5};
        graph[2] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[3] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[4] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[5] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[6] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        graph[7] = new double[]{0.5, 1.0, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
		
	}
}
