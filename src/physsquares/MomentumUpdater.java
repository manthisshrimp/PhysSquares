package physsquares;

import j2dgl.update.Updater;
import java.io.IOException;
import java.util.ArrayList;

public class MomentumUpdater extends Updater<MomentumEntity> {

    private OpenCLInterface openCLInterface;

    private float[] rectangles;
    private MomentumEntity tempEntity;
    private MomentumEntity tempEntity2;

    private final ArrayList<MomentumEntity> collisionPairs = new ArrayList<>();
    private float[] speeds;
    private float[] masses;
    private float[] newSpeeds;
    private float[] collisionResults;

    long checkTime = 0;
    long compareRunsGPU = 0;
    long updateRunsGPU = 0;
    double totalEntityUpdateTime = 0;
    double totalCompareLoopTime = 0;
    double totalMomentumLoopTime = 0;
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
                for (int i = 0; i < collisionResults.length; i++) {
                    if (collisionResults[i] != -1) {
                        collisionPairs.add(updatables.get(i));
                        collisionPairs.add(updatables.get((int) collisionResults[i]));
                    }
                }
            } catch (IOException ex) {
                // Do Nothing
            }
            totalCompareLoopTime += System.nanoTime() - checkTime;
            compareRunsGPU++;

            if (collisionPairs.size() > 0) {
                checkTime = System.nanoTime();

                speeds = new float[collisionPairs.size() * 2];
                masses = new float[collisionPairs.size() * 2];
                newSpeeds = new float[collisionPairs.size() * 2];

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
        }
    }

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
