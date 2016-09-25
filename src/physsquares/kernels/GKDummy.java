package physsquares.kernels;

public class GKDummy {

    public float[] execute(float[] masses, float[] speeds, float[] points, float distMod) {
        int totalObjects = masses.length;
        float[] results = new float[speeds.length];
        for (int gid = 0; gid < totalObjects; gid++) {
            // KERNAL BODY
            // own speeds
            float txs = speeds[gid * 2];
            float tys = speeds[gid * 2 + 1];
            // own point
            float tx = points[gid * 2];
            float ty = points[gid * 2 + 1];
            // own mass
            float tm = masses[gid];
            // Compare with every other object
            for (int i = 0; i < totalObjects; i++) {
                if (i != gid) {
                    // others's point
                    float ox = points[i * 2];
                    float oy = points[i * 2 + 1];
                    // other's mass
                    float om = masses[i];
                    // Calculate distances
                    float dist = (float) Math.sqrt(Math.pow((tx - ox), 2) + Math.pow(ty - oy, 2));
                    if (dist < (1.5 * distMod)) {
                        continue;
                    }
                    // Calculate force
                    float force = (6.674e-11F * tm * om) / (dist * dist);
                    // Calculate accelaration
                    float acc = force / tm;
                    // Calculate accelaration for each axis
                    if (dist != 0 && acc != 0) {
                        txs -= (tx - ox) / dist * acc;
                        tys -= (ty - oy) / dist * acc;
                    }
                }
            }
            results[gid * 2] = txs;
            results[gid * 2 + 1] = tys;
            // END KERNAL
        }
        return results;
    }

}
