package physsquares.kernels;

public class GKDummy {

    public float[] execute(int totalOther, float[] masses, float[] speeds, float[] points, float distanceModifier) {
        float[] results = new float[speeds.length];
        for (int gid = 0; gid < totalOther; gid++) {
            // KERNAL BODY
            // own speeds
            float thisXSpeed = speeds[gid * 2];
            float thisYSpeed = speeds[gid * 2 + 1];
            // own point
            float thisX = points[gid * 2];
            float thisY = points[gid * 2 + 1];
            // own mass
            float thisMass = masses[gid];
            // Compare with every other object
            for (int i = 0; i < totalOther; i++) {
                if (i != gid) {
                    // others's point
                    float otherX = points[i * 2];
                    float otherY = points[i * 2 + 1];
                    // other's mass
                    float otherMass = masses[i];
                    // Calculate distances
                    float distance = (float) Math.sqrt(Math.pow((thisX - otherX), 2) + Math.pow(thisY - otherY, 2));
                    if (distance < (1.5 * distanceModifier)) {
                        continue;
                    }
                    // Calculate force
                    float force = (6.674e-11F * thisMass * otherMass) / (distance * distance);
                    // Calculate accelaration
                    float acc = force / thisMass;
                    // Calculate accelaration for each axis
                    if (distance != 0 && acc != 0) {
                        thisXSpeed -= (thisX - otherX) / distance * acc;
                        thisYSpeed -= (thisY - otherY) / distance * acc;
                    }
                }
            }
            results[gid * 2] = thisXSpeed;
            results[gid * 2 + 1] = thisYSpeed;
            // END KERNAL
        }
        return results;
    }

}
