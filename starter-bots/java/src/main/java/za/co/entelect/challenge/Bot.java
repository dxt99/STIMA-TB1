package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import javax.swing.event.ChangeEvent;
import java.util.*;

import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;
    private final static Command FIX = new FixCommand();

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {
        List<Object> middle = getBlocksInFront(myCar.position.lane, myCar.position.block, myCar.speed);
        List<Object> right  = getBlocksInFront(myCar.position.lane+1, myCar.position.block, myCar.speed-1);
        List<Object> left  = getBlocksInFront(myCar.position.lane-1, myCar.position.block, myCar.speed-1);

        //lizard logic if tailing opponent
        if(IsCrashing()&&hasPowerUp(PowerUps.LIZARD, myCar.powerups))return new LizardCommand();

        //basic fix logic (ada temporary addon, sepertinya tambah cepat)
        if(myCar.damage >= 3)return new FixCommand();
        if(myCar.damage>=1&&hasPowerUp(PowerUps.BOOST, myCar.powerups))return new FixCommand();

        //wall avoidance logic:
        //1. continue if lane doesn't contain wall
        //2. turn left/right immediately if lane is empty
        //3. use lizard (avoids wall)
        // edge case: all wall
        if(middle.contains(Terrain.WALL)){
            if(!(right.contains(Terrain.WALL)||left.contains(Terrain.WALL))){
                //checks for power up
                if(right.contains(Terrain.BOOST))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.BOOST))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.LIZARD))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.LIZARD))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.EMP))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.EMP))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.TWEET))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.TWEET))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.OIL_POWER))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.OIL_POWER))return new ChangeLaneCommand(0);
                if(myCar.position.lane==1||myCar.position.lane==2)return new ChangeLaneCommand(1);
                return new ChangeLaneCommand(0);
            }
            if(!right.contains(Terrain.WALL))return new ChangeLaneCommand(1);
            if(!left.contains(Terrain.WALL))return new ChangeLaneCommand(0);
            return new LizardCommand();
        }

        //default power ups usage, delete later

        if (myCar.damage==0&&LaneClean(middle)&&hasPowerUp(PowerUps.BOOST, myCar.powerups)){
            return new BoostCommand();
        }
        if (myCar.position.block<opponent.position.block&&hasPowerUp(PowerUps.EMP, myCar.powerups)){
            return new EmpCommand();
        }
        /*
        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
            return new LizardCommand();
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)){
            return new OilCommand();
        }
        */
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane,opponent.position.block+opponent.speed+1);
        }

        //obstacle logic (wall handled above)
        //1. if all three lanes has walls, check for lizard
        //2. continue if lane is clean and not crashing to opponent
        //3. if exists clean lane (left/right), steer
        //4. use lizard if no clean lane, if none just accel
        if(middle.contains(Terrain.WALL)&&left.contains(Terrain.WALL)&&right.contains(Terrain.WALL)&&hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
               return new LizardCommand();
        }else if(!LaneClean(middle)){
            if(LaneClean(left)&&LaneClean(right)&&!IsCrashing()){
                //checks for power up
                if(right.contains(Terrain.BOOST))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.BOOST))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.LIZARD))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.LIZARD))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.EMP))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.EMP))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.TWEET))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.TWEET))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.OIL_POWER))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.OIL_POWER))return new ChangeLaneCommand(0);
                if(myCar.position.lane==1||myCar.position.lane==2)return new ChangeLaneCommand(1);
                return new ChangeLaneCommand(0);
            }
            if(LaneClean(left))return new ChangeLaneCommand(0);
            if(LaneClean(right))return new ChangeLaneCommand(1);
            //all hopes lost, use lizard
            if(hasPowerUp(PowerUps.LIZARD, myCar.powerups))return new LizardCommand();
            //otherwise, just surrender
        }

        //Oil Logic
        //1. Max speed and has powerup
        if((myCar.speed==9||myCar.speed==15)&&hasPowerUp(PowerUps.OIL, myCar.powerups)){
            return new OilCommand();
        }

        //just floor the pedal
        return new AccelerateCommand();
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     * Returns wall if lane is not valid
     * Treats CyberTrucks as walls
     **/
    private List<Object> getBlocksInFront(int lane, int block,int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        //Returns if lane is not valid
        if(lane<1||lane>4){
            blocks.add(Terrain.WALL);
            return blocks;
        }

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            if(laneList[i].isOccupiedByCyberTruck)blocks.add(Terrain.WALL);
            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Checks if lane is clear
    private Boolean LaneClean(List<Object> LaneBlocks){
        if(LaneBlocks.contains(Terrain.WALL))return false;
        if(LaneBlocks.contains(Terrain.MUD))return false;
        if(LaneBlocks.contains(Terrain.OIL_SPILL))return false;
        return true;
    }

    // Checks if crashing to opponent
    private Boolean IsCrashing(){
        int dist = max(0,myCar.position.block-opponent.position.block);
        Boolean same = myCar.position.lane==opponent.position.lane;
        return same&&(dist<=myCar.speed-opponent.speed);
    }

}
