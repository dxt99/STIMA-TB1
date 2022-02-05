package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

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

        //basic fix logic
        if (myCar.damage >= 3) {
            return new FixCommand();
        }

        //wall avoidance logic:
        //1. continue if lane doesn't contain wall
        //2. turn left/right immediately if lane is empty
        //3. use lizard (avoids wall)
        if(middle.contains(Terrain.WALL)){
            if(!(right.contains(Terrain.WALL)||left.contains(Terrain.WALL))){
                //checks for power up
                if(right.contains(Terrain.BOOST))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.BOOST))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.EMP))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.EMP))return new ChangeLaneCommand(0);
                if(right.contains(Terrain.LIZARD))return new ChangeLaneCommand(1);
                if(left.contains(Terrain.LIZARD))return new ChangeLaneCommand(0);
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

        if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return new BoostCommand();
        }
        if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
            return new EmpCommand();
        }
        if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
            return new LizardCommand();
        }
        if (hasPowerUp(PowerUps.OIL, myCar.powerups)){
            return new OilCommand();
        }
        if (hasPowerUp(PowerUps.TWEET, myCar.powerups)){
            return new TweetCommand(opponent.position.lane,opponent.position.block+1);
        }

        return new AccelerateCommand(); //default accel
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     * Returns wall if lane is not valid
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

}
