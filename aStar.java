package test;
import java.util.ArrayList;
import java.util.Arrays;

public class aStar {
	
	public static double horizontal = 10;
	public static double diagonal = 14;
	
	public enum Direction {NORTH, SOUTH, EAST, WEST, NORTH_WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST};
	
	public static boolean isDestination(int row, int col, int xEnd, int yEnd) {
		if ((row == xEnd) && (col == yEnd)) 
			return true;
		else
			return false;
	}
	
	public static int calcGValue(int row, int col, int xStart, int xEnd) {
		int gCost = 0;
		while ((row != xStart) && (col != xEnd)) {
			if (row != col) {
				if (row > col) {
					row--;
					gCost += horizontal;
				}
				else {
					col--;
					gCost += horizontal;
				}
			}
			else {
				row--;
				col--;
				gCost += diagonal;
			}
		}
		return gCost;
		
	}
	
	// distance from start to end
	public static int calcHValue(int row, int col, int xEnd, int yEnd) {
		return (int) (10 * Math.sqrt(((xEnd - row)*(xEnd - row)+ (yEnd - col)* (yEnd - col))));
		
	}
	
	public static Direction AStar(double graph[][], int xStart, int yStart, int xEnd, int yEnd) {
		int x = xStart;
		int y = yStart;
		
		ArrayList<Direction> dir = new ArrayList<Direction>(); 
		ArrayList<Double> maxCost = new ArrayList<Double>();
		
		int lenRows = graph.length;
		int lenCols = graph[0].length;
		
		
		double fCost = 0; 
		int i = 0;
		maxCost.add((double) 0);
		dir.add(Direction.NORTH);
		while (!isDestination(x, y, xEnd, yEnd)) {
		
			
			// NORTH
			if ((x-1) >= 0) {
				double north = graph[x-1][y];
				fCost = north * (calcHValue(x-1, y, xEnd, yEnd) + calcGValue(x-1, y, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH);
				}
			}
			
			// SOUTH
			if ((x+1) < lenRows) {
				double south = graph[x+1][y];
				fCost = south * (calcHValue(x+1, y, xEnd, yEnd) + calcGValue(x+1, y, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH);
				}
			}
			
			// EAST
			if ((y+1) < lenCols) {
				double east = graph[x][y+1];
				fCost = east * (calcHValue(x, y+1, xEnd, yEnd) + calcGValue(x, y+1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.EAST);
				}
			}
			
			// WEST
			if ((y-1) >= 0) {
				double west = graph[x][y-1];
				fCost = west * (calcHValue(x, y-1, xEnd, yEnd) + calcGValue(x, y-1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.WEST);
				}
			}
			
			// NORTH-EAST
			if (((x-1) >= 0) && (y+1 < lenCols)) {
				double northEast = graph[x-1][y+1];
				fCost = northEast * (calcHValue(x-1, y+1, xEnd, yEnd) + calcGValue(x-1, y+1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH_EAST);
				}
			}
			
			// NORTH-WEST
			if (((x-1) >= 0) && ((y-1) >= 0)) {
				double northWest = graph[x-1][y-1];
				fCost = northWest * (calcHValue(x-1, y-1, xEnd, yEnd) + calcGValue(x-1, y-1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.NORTH_WEST);
				}
			}
			
			// SOUTH-EAST
			if (((x+1) < lenRows) && (y+1 < lenCols)) {
				double southEast = graph[x+1][y+1];
				fCost = southEast * (calcHValue(x+1, y+1, xEnd, yEnd) + calcGValue(x+1, y+1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH_EAST);
				}
			}
			
			// SOUTH-WEST
			if (((x+1) < lenRows) && (y-1 >= 0)) {
				double southWest = graph[x+1][y-1];
				fCost = southWest * (calcHValue(x+1, y-1, xEnd, yEnd) + calcGValue(x+1, y-1, xStart, yStart));
				if (fCost > maxCost.get(i)) {
					maxCost.set(i, fCost);
					dir.set(i, Direction.SOUTH_WEST);
				}
			}
			
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
	
        Direction d = AStar(graph, 7, 1, 0, 7);
        Direction d1 = AStar(graph, 6, 1, 0, 7); // north
        Direction d2 = AStar(graph, 1, 1, 0, 7); // east
        Direction d3 = AStar(graph, 2, 1, 0, 7); // northeast
        
   }
}
