package physsquares.update;

import physsquares.entity.PhysEntity;
import j2dgl.update.Updater;
import java.io.IOException;
import java.util.ArrayList;
import physsquares.OpenCLInterface;
import physsquares.kernals.GKDummy;

public class EntityUpdater extends Updater<PhysEntity> {

    private OpenCLInterface openCLInterface;
    private GKDummy gKDummy = new GKDummy();

    private float[] rectangles;
    private PhysEntity tempEntity;
    private PhysEntity tempEntity2;

    private final ArrayList<PhysEntity> collisionPairs = new ArrayList<>();
    private float[] speeds;
    private float[] masses;
    private float[] points;
    private float[] newSpeeds;
    private float[] collisionResults;
    private final float DIST_MOD = 1.0e-4F;

    public long checkTime = 0;
    public long compareRunsGPU = 0;
    public long updateRunsGPU = 0;
    public double totalEntityUpdateTime = 0;
    public double totalCompareLoopTime = 0;
    public double totalMomentumLoopTime = 0;
    public boolean gpuMode = true;

    public EntityUpdater() {
        try {
            this.openCLInterface = new OpenCLInterface();
        } catch (IOException ex) {
            System.err.println(ex.toString());
            System.exit(1);
        }
    }

    @Override
    protected void executeUpdate(PhysEntity updatingEntity) {
        checkTime = System.nanoTime();
        super.executeUpdate(updatingEntity);
        if (!gpuMode) {
            updatables.stream().filter((otherEntity) -> (!updatingEntity.equals(otherEntity)))
                    .filter((otherEntity) -> (updatingEntity.aboutToIntersect(otherEntity)))
                    .forEach((otherEntity) -> {
                        updatingEntity.processCollision(otherEntity);
                    });
        }
        totalEntityUpdateTime += System.nanoTime() - checkTime;
    }

    @Override
    protected void postUpdate() {
        if (gpuMode) {
            if (rectangles == null) {
                rectangles = new float[updatables.size() * 4];
            }
            // ========== Check for colisions ==================================
            checkTime = System.nanoTime();
            for (int i = 0; i < updatables.size(); i++) {
                tempEntity = updatables.get(i);
                rectangles[i * 4] = (float) ((float) tempEntity.x + tempEntity.xIncrement);
                rectangles[i * 4 + 1] = (float) ((float) tempEntity.y + tempEntity.yIncrement);
                rectangles[i * 4 + 2] = tempEntity.width;
                rectangles[i * 4 + 3] = tempEntity.height;
            }
            try {
                collisionResults = openCLInterface.executeIntersectionKernal(rectangles);
                for (int i = 0; i < collisionResults.length; ++i) {
                    if (collisionResults[i] != -1) {
                        collisionPairs.add(updatables.get(i));
                        collisionPairs.add(updatables.get((int) collisionResults[i] - 1));
                    }
                }
            } catch (IOException ex) {
                // Do Nothing
            }
            totalCompareLoopTime += System.nanoTime() - checkTime;
            compareRunsGPU++;
            // ========== Apply momentum logic based on colisions ==============
            if (collisionPairs.size() > 0) {
                checkTime = System.nanoTime();
                speeds = new float[collisionPairs.size() * 2];
                masses = new float[collisionPairs.size() * 2];

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
                newSpeeds = new float[collisionPairs.size() * 2];
                try {
                    newSpeeds = openCLInterface.executeMomentumKernal(speeds, masses);
                } catch (IOException ex) {
                    return;
                }
                for (int i = 0; i < collisionPairs.size(); i += 2) {
                    tempEntity = collisionPairs.get(i);
                    tempEntity2 = collisionPairs.get(i + 1);
                    tempEntity.xIncrement = newSpeeds[i * 2];
                    tempEntity2.xIncrement = newSpeeds[i * 2 + 1];
                    tempEntity.yIncrement = newSpeeds[i * 2 + 2];
                    tempEntity2.yIncrement = newSpeeds[i * 2 + 3];
                }
                collisionPairs.clear();
                totalMomentumLoopTime += System.nanoTime() - checkTime;
                checkTime = System.nanoTime();
                updateRunsGPU++;
            }
//             ========== Apply Gravity ========================================
            points = new float[updatables.size() * 2];
            speeds = new float[updatables.size() * 2];
            masses = new float[updatables.size()];
            newSpeeds = new float[speeds.length];
            for (int i = 0; i < updatables.size(); i++) {
                tempEntity = updatables.get(i);
                masses[i] = (float) tempEntity.mass;
                /////////////////////////////////////////////////////////
                points[i * 2] = (float) tempEntity.x * DIST_MOD;
                points[i * 2 + 1] = (float) tempEntity.y * DIST_MOD;
                /////////////////////////////////////////////////////////
                speeds[i * 2] = (float) tempEntity.xIncrement;
                speeds[i * 2 + 1] = (float) tempEntity.yIncrement;
            }
            newSpeeds = gKDummy.execute(masses, speeds, points, DIST_MOD);
            for (int i = 0; i < updatables.size(); i++) {
                tempEntity = updatables.get(i);
                tempEntity.xIncrement = newSpeeds[i * 2];
                tempEntity.yIncrement = newSpeeds[i * 2 + 1];
            }
        }
    }

    @Override
    public void clear() {
        speeds = null;
        masses = null;
        newSpeeds = null;
        rectangles = null;
        updatables.clear();
    }

    public void resetStats() {
        totalCompareLoopTime = 0;
        totalMomentumLoopTime = 0;
        totalEntityUpdateTime = 0;
        compareRunsGPU = 0;
        updateRunsGPU = 0;
    }
}
