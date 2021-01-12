package lamelo.robots;

import battlecode.common.*;
import lamelo.common.*;
import lamelo.pathfinding.BasicBug;
import lamelo.pathfinding.PathFinder;
import lamelo.statemachine.State;

import java.util.*;

import static lamelo.common.Vars.*;
import static lamelo.common.Vars.rc;

public class Muckraker {
    // Current State
    private static State state = State.DO_NOTHING;
    private static RobotInfo[] allNearbyRobots;
    // represents the current desired wall formation, if in BuildWall, Mobilize, or Charge state
    private static int formation = 0;
    private static Optional<RobotInfo> enlightenmentCenterInfo = Optional.empty();

    // Derived Fields
    private static int enlightenmentCenterFlag; // guaranteed to be non-null iff enlightenmentCenterInfo.isPresent()
    private static int enlightenmentCenterID; // guaranteed to be non-null iff enlightenmentCenterInfo.isPresent()
    private static MapLocation enlightenmentCenterLocation; // guaranteed to be non-null iff enlightenmentCenterInfo.isPresent()

    private static class DoNothing {
        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() {
            return Tuple.of(Optional.empty(), Optional.empty());
        }

        public static State transition() throws GameActionException {
            // If enlightenment center died before we could get a role, enter survival mode
            if (!enlightenmentCenterInfo.isPresent()) {
                return State.SURVIVE;
            }

            // Extract the enlightenment center state
            State enlightenmentCenterState = Communication.enlightenmentCenterStateDecoding(enlightenmentCenterFlag);

            // Enter explore mode if enlightenment center is deploying scouts, or getting information
            // otherwise follow the enlightenment center state
            switch (enlightenmentCenterState) {
                case DEPLOY_SCOUTS:
                case MOBILIZE: return State.EXPLORE;
                default: return enlightenmentCenterState;
            }
        }
    }

    private static class Explore {
        private static Explorer explorer = null;

        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            // Report any possible locations of interest found to the EC
            Tuple<Optional<MapLocation>, Optional<MapLocation>> locationsToReport = getLocationsToReport();
            Optional<MapLocation> ecLocation = locationsToReport.x();
            Optional<MapLocation> highDensityLocation = locationsToReport.y();

            if (ecLocation.isPresent()) {
                reportEnlightenmentCenter(ecLocation.get());
            } else if (highDensityLocation.isPresent()) {
                reportHighDensityEnemyLocation(highDensityLocation.get());
            } else {
                rc().setFlag(0);
            }

            // Find a slanderer to expose, if any
            Optional<RobotInfo> slandererToExpose = getSlandererToExpose(allNearbyRobots);

            if (slandererToExpose.isPresent()) {
                MapLocation slandererLocation = slandererToExpose.get().getLocation();
                return Tuple.of(Optional.empty(), Optional.of(slandererLocation));
            }

            if (explorer == null) {
                Direction initialDirection = enlightenmentCenterLocation.directionTo(rc().getLocation());
                explorer = new Explorer(initialDirection, RobotType.MUCKRAKER);
            }

            Optional<Direction> directionToMove = explorer.getDirectionToMove();

            return Tuple.of(directionToMove, Optional.empty());
        }

        private static State transition() {
            // TODO
            return state;
        }

        private static void reportEnlightenmentCenter(MapLocation location) throws GameActionException {
            rc().setFlag(Communication.explorerMessageEncoding(location, enlightenmentCenterLocation, Explorer.ReportMessageType.EC_LOCATION));
        }

        private static void reportHighDensityEnemyLocation(MapLocation location) throws GameActionException {
            rc().setFlag(Communication.explorerMessageEncoding(location, enlightenmentCenterLocation, Explorer.ReportMessageType.HIGH_DENSITY_LOCATION));
        }

        private static Tuple<Optional<MapLocation>, Optional<MapLocation>> getLocationsToReport() throws GameActionException {
            List<MapLocation> enlightenmentCenterLocations = new LinkedList<>();
            int numEnemiesNearby = 0;
            for (RobotInfo robotInfo : allNearbyRobots) {
                if (robotInfo.getTeam().equals(myTeam())) {
                    continue; // ignore friendly robots
                }

                if (robotInfo.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    if (!robotInfo.getTeam().isPlayer() && robotInfo.getConviction() > 200 && rc().getRoundNum() < 600) {
                        continue; // don't report strong neutral ec's early on -- focus on the enemy
                    }

                    enlightenmentCenterLocations.add(robotInfo.getLocation());
                    continue; // add any enemy enlightenment centers
                }

                numEnemiesNearby++;
            }

            // If an enlightenment center not owned by us exists, return the location of the closest one
            if (enlightenmentCenterLocations.size() != 0) {
                MapLocation closestEcLocation = null;
                for (MapLocation ecLocation: enlightenmentCenterLocations) {
                    if (closestEcLocation == null || enlightenmentCenterLocation.distanceSquaredTo(ecLocation) < enlightenmentCenterLocation.distanceSquaredTo(closestEcLocation)) {
                        closestEcLocation = ecLocation;
                    }
                }

                return Tuple.of(Optional.of(closestEcLocation), Optional.empty());
            }

            return numEnemiesNearby > 15 ?
                    Tuple.of(Optional.empty(), Optional.of(rc().getLocation())) :
                    Tuple.of(Optional.empty(), Optional.empty());
        }
    }

    private static class Camp {
        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            // TODO
            return Tuple.of(Optional.empty(), Optional.empty());
        }

        private static State transition() {
            // TODO
            return state;
        }
    }

    private static class BuildWall {
        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            // Find a slanderer to expose, if any
            Optional<RobotInfo> slandererToExpose = getSlandererToExpose(allNearbyRobots);

            if (slandererToExpose.isPresent()) {
                MapLocation slandererLocation = slandererToExpose.get().getLocation();
                return Tuple.of(Optional.empty(), Optional.of(slandererLocation));
            }

            // Update current formation
            formation = Communication.enlightenmentCenterFormationDecoding(enlightenmentCenterFlag);

            // Decide which direction to move in, based on the formation
            Optional<Direction> directionToMove = Utils.getDirectionToMoveForWallUnit(formation, enlightenmentCenterLocation);

            return Tuple.of(directionToMove, Optional.empty());
        }

        private static State transition() {
            // TODO
            return state;
        }
    }

    private static class Mobilize {
        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            MapLocation myLocation = rc().getLocation();

            // If we are buffed, get away from politicians
            if (rc().getEmpowerFactor(myTeam(), 0) > 1) {
                Set<MapLocation> politicianLocations = new HashSet<>();

                for (Direction direction : Utils.NON_DIAGONAL_DIRECTIONS) {
                    MapLocation possiblePoliticianLocation = myLocation.add(direction);
                    if (!rc().onTheMap(possiblePoliticianLocation)) {
                        continue; // ignore locations off the map
                    }

                    RobotInfo possiblePolitician = rc().senseRobotAtLocation(possiblePoliticianLocation);
                    if (possiblePolitician == null) {
                        continue; // ignore empty squares
                    }

                    if (possiblePolitician.getType() != RobotType.POLITICIAN || possiblePolitician.getTeam() != myTeam()) {
                        continue; // ignore units that are not friendly politicians
                    }

                    politicianLocations.add(possiblePoliticianLocation);
                }

                // Try to move away from all politicians
                if (politicianLocations.size() > 0) {
                    for (Direction direction : Utils.DIRECTIONS) {
                        if (!rc().canMove(direction)) {
                            continue; // ignore directions that we can't move to
                        }

                        MapLocation newLocation = myLocation.add(direction);
                        for (MapLocation politicianLocation : politicianLocations) {
                            if (newLocation.isWithinDistanceSquared(politicianLocation, 1)) {
                                continue; // ignore new locations that put us next to an exploding politician
                            }
                        }

                        return Tuple.of(Optional.of(direction), Optional.empty());
                    }
                }

                return Tuple.of(Optional.empty(), Optional.empty());
            }

            // Find a slanderer to expose, if any
            Optional<RobotInfo> slandererToExpose = getSlandererToExpose(allNearbyRobots);

            if (slandererToExpose.isPresent()) {
                MapLocation slandererLocation = slandererToExpose.get().getLocation();
                return Tuple.of(Optional.empty(), Optional.of(slandererLocation));
            }

            // TODO change later, but move one step away from EC
            if (myLocation.isAdjacentTo(enlightenmentCenterLocation)) {
                Direction direction = enlightenmentCenterLocation.directionTo(myLocation);
                if (rc().canMove(direction)) {
                    return Tuple.of(Optional.of(direction), Optional.empty());
                }
            }

            return Tuple.of(Optional.empty(), Optional.empty());
        }

        private static State transition() {
            // TODO
            return state;
        }
    }

    private static class Charge {
        private static MapLocation target = null;

        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            // Update our target location, based on what our EC says
            target = getUpdatedTarget();

            if (target == null) {
                throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "target is null, when it shouldn't be");
            }

            // Find a slanderer to expose, if any
            Optional<RobotInfo> slandererToExpose = getSlandererToExpose(allNearbyRobots);

            if (slandererToExpose.isPresent()) {
                MapLocation slandererLocation = slandererToExpose.get().getLocation();
                return Tuple.of(Optional.empty(), Optional.of(slandererLocation));
            }

            Optional<Direction> directionToMove = PathFinder.directionTo(target);

            return Tuple.of(directionToMove, Optional.empty());
        }

        private static State transition() {
            // TODO
            return state;
        }

        private static MapLocation getUpdatedTarget() throws GameActionException {
            MapLocation myLocation = rc().getLocation();

            // If our EC has died, try to get it back
            if (!rc().canGetFlag(enlightenmentCenterID)) {

                // If we are successful bringing back our EC, get its updated ID
                if (myLocation.isWithinDistanceSquared(enlightenmentCenterLocation, RobotType.MUCKRAKER.sensorRadiusSquared)) {
                    RobotInfo ecInfo = rc().senseRobotAtLocation(enlightenmentCenterLocation);
                    if (ecInfo.getTeam().equals(myTeam())) {
                        enlightenmentCenterID = ecInfo.getID();
                    }
                }

                return enlightenmentCenterLocation;
            }

            int ecFlag = rc().getFlag(enlightenmentCenterID);
            State ecState = Communication.enlightenmentCenterStateDecoding(ecFlag);

            // If EC is still in the charging state, update our target with the latest information
            //  If not, then our EC is building the wall, and we should continue to attack the target
            //      If we manage to successfully convert the target, change enlightenment center to the target
            if (ecState.equals(State.CHARGE)) {
                return Communication.enlightenmentCenterLocationDecoding(ecFlag, enlightenmentCenterLocation);
            } else {
                if (myLocation.isWithinDistanceSquared(target, RobotType.MUCKRAKER.sensorRadiusSquared)) {
                    RobotInfo ecInfo = rc().senseRobotAtLocation(target);

                    if (ecInfo == null || !ecInfo.getType().equals(RobotType.ENLIGHTENMENT_CENTER)) {
                        return target; // we are attacking a high-density location, continue to do so
                    }

                    // If we managed to convert the target, it becomes our new enlightenment center
                    if (ecInfo.getTeam().equals(myTeam())) {
                        enlightenmentCenterID = ecInfo.getID();
                        enlightenmentCenterInfo = Optional.of(ecInfo);
                        enlightenmentCenterLocation = ecInfo.getLocation();
                        enlightenmentCenterFlag = rc().getFlag(enlightenmentCenterID);
                    }
                }

                return target;
            }
        }
    }

    private static class Survive {
        public static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
            // TODO
            return Tuple.of(Optional.empty(), Optional.empty());
        }

        public static State transition() {
            // TODO
            return state;
        }
    }

    public static void run() throws GameActionException {
        // Gain information from surroundings and merge it with current state
        gatherInfoAndUpdateCurrentState();

        // Decide if it is appropriate to take any action (move or expose) this turn
        Tuple<Optional<Direction>, Optional<MapLocation>> action = getAction();
        Optional<Direction> directionToMove = action.x();
        Optional<MapLocation> exposeLocation = action.y();

        // Expose, if appropriate
        if (exposeLocation.isPresent()) {
            rc().expose(exposeLocation.get());
            return;
        }

        // Move, if appropriate
        if (directionToMove.isPresent()) {
            rc().move(directionToMove.get());
            return;
        }
    }

    private static Optional<RobotInfo> getSlandererToExpose(RobotInfo[] robotInfos) throws GameActionException {
        for (RobotInfo robotInfo : robotInfos) {
            if (!robotInfo.getType().canBeExposed()) {
                continue;
            }

            if (!robotInfo.getTeam().equals(Vars.enemyTeam())) {
                continue;
            }

            if (!rc().canExpose(robotInfo.getLocation())) {
                continue;
            }

            return Optional.of(robotInfo);
        }

        return Optional.empty();
    }

    /**
     * Determines if this robot should move on this turn, expose on this turn, or do nothing. Returns a tuple representing
     * <ul>
     *     <li> an optional direction to move to </li>
     *     <li> an optional expose map location </li>
     * </ul>
     *
     * At most one of the optionals returned will be non-empty. Returns a tuple of two empty optionals if this robot
     * should do nothing on this turn. If the optional direction is non-empty, the direction D in the optional is
     * guaranteed to be move-able (i.e. rc().canMove(D) == true). If the optional expose map location is non-empty,
     * the map location L in the optional is guaranteed be expose-able (i.e. rc().canExpose(L) == true)
     * @throws GameActionException if rc has not yet been set
     */
    private static Tuple<Optional<Direction>, Optional<MapLocation>> getAction() throws GameActionException {
        switch (state) {
            case DO_NOTHING:    return DoNothing.getAction();
            case EXPLORE:       return Explore.getAction();
            case CAMP:          return Camp.getAction();
            case BUILD_WALL:    return BuildWall.getAction();
            case MOBILIZE:      return Mobilize.getAction();
            case CHARGE:        return Charge.getAction();
            case SURVIVE:       return Survive.getAction();
        }

        throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Unhandled switch case: " + Muckraker.state);
    }

    /**
     * Obtains the next (state machine) state that this robot should enter, based on current state and the information
     * it received from its environment
     */
    private static State getStateMachineState() throws GameActionException {
        switch (state) {
            case DO_NOTHING:    return DoNothing.transition();
            case EXPLORE:       return Explore.transition();
            case CAMP:          return Camp.transition();
            case BUILD_WALL:    return BuildWall.transition();
            case MOBILIZE:      return Mobilize.transition();
            case CHARGE:        return Charge.transition();
            case SURVIVE:       return Survive.transition();
        }

        throw new GameActionException(GameActionExceptionType.INTERNAL_ERROR, "Unhandled switch case: " + Muckraker.state);
    }

    /**
     * Senses this robot's surrounding environment, and uses this new information to update any relevant state fields
     * @throws GameActionException if rc has not yet been set
     */
    private static void gatherInfoAndUpdateCurrentState() throws GameActionException {
        allNearbyRobots = rc().senseNearbyRobots(RobotType.MUCKRAKER.sensorRadiusSquared);
        enlightenmentCenterInfo = Utils.getEnlightenmentCenterInfo(allNearbyRobots);

        if (enlightenmentCenterInfo.isPresent()) {
            RobotInfo ecInfo = enlightenmentCenterInfo.get();
            // don't overwrite these if they are already set
            enlightenmentCenterID = enlightenmentCenterID == 0 ? ecInfo.getID() : enlightenmentCenterID;
            enlightenmentCenterLocation = enlightenmentCenterLocation == null ? ecInfo.getLocation() : enlightenmentCenterLocation;

            if (rc().canGetFlag(enlightenmentCenterID)) {
                enlightenmentCenterFlag = rc().getFlag(enlightenmentCenterID);
            }
        }

        // update state machine state last, so that other state fields can be used to make proper transitions
        state = getStateMachineState();
    }
}
