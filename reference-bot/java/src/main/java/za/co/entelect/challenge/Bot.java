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
        List<Object> middleBoost = getBlocksInFront(myCar.position.lane, myCar.position.block, 15);
        List<Object> middleAcc = getBlocksInFront(myCar.position.lane, myCar.position.block, NextSpeed());
        List<Object> middleDec = getBlocksInFront(myCar.position.lane, myCar.position.block, PrevSpeed());
        List<Object> right  = getBlocksInFront(myCar.position.lane+1, myCar.position.block-1, myCar.speed);
        List<Object> left  = getBlocksInFront(myCar.position.lane-1, myCar.position.block-1, myCar.speed);

        // boosting logic
        if (myCar.boostCounter > 0){
            if (!LaneClean(middle)) {
                if(LaneClean(left)&&LaneClean(right))return PowerGreed(left,right);
                else if (LaneClean(right)) {
                    return new ChangeLaneCommand(1);
                } else if (LaneClean(left)) {
                    return new ChangeLaneCommand(0);
                } else if (hasPowerUp(PowerUps.LIZARD)) {
                    return new LizardCommand();
                }
            }
        }

        //lizard logic if tailing opponent
        if(IsCrashing()&&hasPowerUp(PowerUps.LIZARD))return new LizardCommand();

        //basic fix logic (ada temporary addon, sepertinya tambah cepat)
        if(myCar.damage >= 3){
            return new FixCommand();
        }
        if(myCar.damage>=1&&hasPowerUp(PowerUps.BOOST)) {
            return new FixCommand();
        }

        //default power ups usage, delete later

        if(myCar.damage==0&&LaneClean(middleBoost)&&hasPowerUp(PowerUps.BOOST)&& myCar.boostCounter==0){
            return new BoostCommand();
        }

        //if car will crash when accel, but won't if not, try power ups
        if(LaneClean(middle)&&!LaneClean(middleAcc)&&myCar.speed>0){
            PowerUps P = TryPower();
            if(P!=null)return UsePower(P);
        }

        /*
        obstacle logic (wall handled above)
        1. if speed=0, just continue
        2. continue if lane is clean and not crashing to opponent
        3. if exists clean lane (left/right), steer
        4. use lizard if no clean lane, if none just accel
        */
        if(!LaneClean(middleAcc)&&myCar.speed>0){
            if(LaneClean(left)&&LaneClean(right)){
                //checks for power up
                return PowerGreed(left,right);
            }
            //if "do nothing" works, check for power up or do nothing
            if(LaneClean(middle)){
                PowerUps P=TryPower();
                if(P!=null)return UsePower(P);
                return new DoNothingCommand();
            }
            //otherwise, try steering
            if(LaneClean(left))return new ChangeLaneCommand(0);
            if(LaneClean(right))return new ChangeLaneCommand(1);
            //try "decelerate"
            if(myCar.speed>6&&LaneClean(middleDec))return new DecelerateCommand();
            //all hopes lost, use lizard
            if(hasPowerUp(PowerUps.LIZARD))return new LizardCommand();


            //otherwise, avoid wall
            if(Tankable(middle)&&myCar.speed<6&&myCar.damage<2&&hasPowerUp(PowerUps.BOOST))return new BoostCommand();
            if(!Tankable(middle)&&Tankable(left)&&Tankable(right))return PowerGreed(left,right);
            if(!Tankable(middle)&&Tankable(right))return new ChangeLaneCommand(1);
            if(!Tankable(middle)&&Tankable(left))return new ChangeLaneCommand(0);
        }

        PowerUps P=TryPower();
        if(P!=null)return UsePower(P);

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


        //Returns null if speed is 0
        if(speed<=0)return blocks;
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
    private boolean hasPowerUp(PowerUps powerUpToCheck) {
        for (PowerUps powerUp: myCar.powerups) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    // Checks if lane is clear
    private boolean LaneClean(List<Object> LaneBlocks){
        if(LaneBlocks.contains(Terrain.WALL))return false;
        if(LaneBlocks.contains(Terrain.MUD))return false;
        if(LaneBlocks.contains(Terrain.OIL_SPILL))return false;
        return true;
    }

    private boolean Tankable(List<Object> LaneBlocks){
        if(LaneBlocks.contains(Terrain.WALL))return false;
        return MudCount(LaneBlocks) < 2;
    }

    private int MudCount(List<Object> LaneBlocks){
        int ret=0;
        for(Object L:LaneBlocks)if(L.equals(Terrain.MUD)||L.equals(Terrain.OIL_SPILL))ret++;
        return ret;
    }

    // Checks if crashing to opponent
    private boolean IsCrashing(){
        int dist = max(0,myCar.position.block-opponent.position.block);
        boolean same = myCar.position.lane==opponent.position.lane;
        return same&&(dist<=myCar.speed-opponent.speed);
    }

    private Command PowerGreed(List<Object> left, List<Object> right){
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

    private PowerUps TryPower(){
        if (myCar.position.block<opponent.position.block&&hasPowerUp(PowerUps.EMP)&&Math.abs(myCar.position.lane-opponent.position.lane)<=1){
            return PowerUps.EMP;
        }
        //Tweet Logic
        if (hasPowerUp(PowerUps.TWEET) && myCar.speed>=8){
            return PowerUps.TWEET;
        }

        //Oil Logic
        //1. Max speed and has powerup
        if((myCar.speed==9||myCar.speed==15)&&hasPowerUp(PowerUps.OIL)){
            return PowerUps.OIL;
        }
        if(myCar.speed>=6&&hasPowerUp(PowerUps.OIL) && opponent.position.lane == myCar.position.lane && opponent.position.block + opponent.speed >= myCar.position.block - 1){
            return PowerUps.OIL;
        }
        return null;
    }

    private Command UsePower(PowerUps P){
        if(P.equals(PowerUps.EMP))
            return new EmpCommand();
        if(P.equals(PowerUps.TWEET)) {
            //prevent crashing into your tweet now
            if (opponent.position.lane != myCar.position.lane)
                return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 3);
            int tar = opponent.position.block + 3;
            //just tweet to his side if gonna crash
            if (tar - myCar.position.block <= myCar.speed && tar >= myCar.position.block)
                if (opponent.position.lane <= 2)
                    return new TweetCommand(opponent.position.lane + 1, opponent.position.block+opponent.speed);
            return new TweetCommand(opponent.position.lane - 1, opponent.position.block+opponent.speed);
        }
        return new OilCommand();
    }

    private int NextSpeed(){
        int s=myCar.speed;
        if(s==5)return 6;
        int[] speeds= new int[]{0, 3, 6, 8, 9, 15};
        int ret=0;
        for(int i=0;i<4;i++)if(s==speeds[i])ret=speeds[i+1];
        if(s==9)ret=9;
        ret=java.lang.Math.min(speeds[5- myCar.damage],ret);
        return ret;
    }

    private int PrevSpeed(){
        int s=myCar.speed;
        if(s==5)return 3;
        if(s==0)return 0;
        int[] speeds= new int[]{0, 3, 6, 8, 9, 15};
        for(int i=1;i<6;i++)if(s==speeds[i])return speeds[i-1];
        return 0;
    }

}
