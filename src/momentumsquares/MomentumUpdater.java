package momentumsquares;

import j2dgl.update.Updater;
import java.io.IOException;
import java.util.ArrayList;

public class MomentumUpdater extends Updater<MomentumEntity> {

    private OpenCLInterface openCLInterface;

//    private float[] otherRectangles;
//    private final float[] mainRectangle = new float[4];
    private MomentumEntity tempEntity;
    private MomentumEntity tempEntity2;

    private final ArrayList<MomentumEntity> collisionPairs = new ArrayList<>();
    private float[] speeds;
    private float[] masses;
    private float[] newSpeeds;
//    private float[] collisionResults;

    long runs = 0;
    long checkTime = 0;
    double totalCompareLoopTime = 0;
    double totalGetLoopTime = 0;
    double totalMomentumLoopTime = 0;
    double totalSetLoopTime = 0;
    boolean gpuMode = true;

    public MomentumUpdater() {
        try {
            this.openCLInterface = new OpenCLInterface();
        } catch (IOException ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }
    }

    @Override
    protected void executeUpdate(MomentumEntity updatingEntity) {
        super.executeUpdate(updatingEntity);
//        if (otherRectangles == null) {
//            otherRectangles = new float[updatables.size() * 4];
//        }

        checkTime = System.nanoTime();

//         Find out if the current entity is about to collide with another
//        mainRectangle[0] = (float) ((float) updatingEntity.x + updatingEntity.xIncrement);
//        mainRectangle[1] = (float) ((float) updatingEntity.y + updatingEntity.yIncrement);
//        mainRectangle[2] = updatingEntity.width;
//        mainRectangle[3] = updatingEntity.height;
//
//        for (int i = 0; i < updatables.size(); i++) {
//            tempEntity = updatables.get(i);
//            otherRectangles[i * 4] = (float) ((float) tempEntity.x + tempEntity.xIncrement);
//            otherRectangles[i * 4 + 1] = (float) ((float) tempEntity.y + tempEntity.yIncrement);
//            otherRectangles[i * 4 + 2] = tempEntity.width;
//            otherRectangles[i * 4 + 3] = tempEntity.height;
//        }
        for (MomentumEntity otherEntity : updatables) {
            if (!otherEntity.equals(updatingEntity)) {
                if (updatingEntity.aboutToIntersect(otherEntity)) {
                    if (gpuMode) {
                        collisionPairs.add(updatingEntity);
                        collisionPairs.add(otherEntity);
                    } else {
                        updatingEntity.processCollision(otherEntity);
                    }
                    break;
                }
            }
        }

//        try {
//            collisionResults = openCLInterface.executeCollisionKernal(mainRectangle, otherRectangles);
//            if (collisionResults[0] > 0) {
//                collisionPairs.add(updatingEntity);
//                collisionPairs.add(updatables.get((int) collisionResults[1]));
//            }
//        } catch (IOException ex) {
//            // Do Nothing
//        }
        totalCompareLoopTime += System.nanoTime() - checkTime;
    }

    @Override
    protected void postUpdate() {
        if (collisionPairs.size() > 0 && gpuMode) {

            speeds = new float[collisionPairs.size() * 2];
            masses = new float[collisionPairs.size() * 2];
            newSpeeds = new float[collisionPairs.size() * 2];

            checkTime = System.nanoTime();

            for (int i = 0; i < collisionPairs.size(); i += 2) {
                tempEntity = collisionPairs.get(i);
                tempEntity2 = collisionPairs.get(i + 1);
                speeds[i * 2] = (float) tempEntity.xIncrement;
                speeds[i * 2 + 1] = (float) tempEntity2.xIncrement;
                speeds[i * 2 + 2] = (float) tempEntity.yIncrement;
                speeds[i * 2 + 3] = (float) tempEntity2.yIncrement;

                masses[i * 2] = (float) tempEntity.mass;
                masses[i * 2 + 1] = (float) tempEntity2.mass;
                masses[i * 2 + 2] = (float) tempEntity.mass;
                masses[i * 2 + 3] = (float) tempEntity2.mass;
            }

            totalGetLoopTime += System.nanoTime() - checkTime;
            checkTime = System.nanoTime();

            try {
                newSpeeds = openCLInterface.executeMomentumKernal(speeds, masses);
            } catch (IOException ex) {
                return;
            }

            totalMomentumLoopTime += System.nanoTime() - checkTime;
            checkTime = System.nanoTime();

            for (int i = 0; i < collisionPairs.size(); i += 2) {
                tempEntity = collisionPairs.get(i);
                tempEntity2 = collisionPairs.get(i + 1);
                tempEntity.xIncrement = newSpeeds[i * 2];
                tempEntity2.xIncrement = newSpeeds[i * 2 + 1];
                tempEntity.yIncrement = newSpeeds[i * 2 + 2];
                tempEntity2.yIncrement = newSpeeds[i * 2 + 3];
            }
            collisionPairs.clear();

            totalSetLoopTime += System.nanoTime() - checkTime;
        }
        runs++;
    }

    public void clear() {
//        otherRectangles = null;
        speeds = null;
        masses = null;
        newSpeeds = null;
        updatables.clear();
    }

    public void resetStats() {
        runs = 0;
        totalGetLoopTime = 0;
        totalCompareLoopTime = 0;
        totalMomentumLoopTime = 0;
        totalSetLoopTime = 0;
    }
}
