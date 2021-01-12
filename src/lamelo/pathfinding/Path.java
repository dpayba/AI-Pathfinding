package lamelo.pathfinding;
import battlecode.common.*;
import static lamelo.common.Vars.*;

public class Path {

    public static double calcHValue(MapLocation square, MapLocation target) {
        double hValue = 0;
        hValue = Math.max(Math.abs(target.x - square.x), Math.abs(target.y - square.y));
        return hValue;

    }

	public static double passabilityThreshold = 0.7;
    static Direction bugDirection = null;

    public static Direction basicBug(MapLocation target) throws GameActionException {
        Direction minBugDirection = null;
        double minFCost = 1000;
        double fCost = 0;

        Direction d = rc().getLocation().directionTo(target); // calculates direction to face target
        if (rc().isReady()) {
            // if can move and greater than threshold
            if (rc().canMove(d) && rc().sensePassability(rc().getLocation().add(d)) >= passabilityThreshold) {
                Direction bugDirectionTemp = d; // set return to direction facing target
                bugDirection = null; // resets bugDirection
                return bugDirectionTemp;
            }
            else {
                if (bugDirection == null) { // if can't move, reset direction to point to target
                    bugDirection = d;
                }
                for (int i = 0; i < 8; ++i) { // calculate fCost of 8 directions, find min fCost
                    if (rc().canMove(bugDirection) && rc().sensePassability(rc().getLocation().add(bugDirection)) >= passabilityThreshold) {
                        fCost = (1 / rc().sensePassability(rc().getLocation().add(bugDirection))) + calcHValue(rc().getLocation().add(bugDirection), target);
                        if (fCost < minFCost) {
                            minBugDirection = bugDirection;
                            minFCost = fCost;
                        }
                    }
                    bugDirection = bugDirection.rotateLeft();
                }
                bugDirection = bugDirection.rotateRight();
                return minBugDirection;
            }
        }
        return null;
    }
}
