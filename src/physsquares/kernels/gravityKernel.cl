__kernel void gravityKernel(
int totalOther,
float distanceModifier,
__global const float *masses,
__global const float *speeds,
__global const float *points,
__global float *results)
{
    int gid = get_group_id(0) * get_local_size(0) + get_local_id(0);

    float thisXSpeed = speeds[gid * 2];
    float thisYSpeed = speeds[gid * 2 + 1];

    float thisX = points[gid * 2];
    float thisY = points[gid * 2 + 1];

    float thisMass = masses[gid];

    for (int i = 0; i < totalOther; i++) {
        if (i != gid) {

            float otherX = points[i * 2];
            float otherY = points[i * 2 + 1];

            float otherMass = masses[i];

            float distance = native_sqrt(native_powr((thisX - otherX), 2) + native_powr(thisY - otherY, 2));

            if (distance < (1.5 * distanceModifier)) {
                continue;
            }

            float force = (6.674e-11F * thisMass * otherMass) / (distance * distance);
            float accelaration = force / thisMass;

            if (distance != 0 && accelaration != 0) {
                thisXSpeed -= (thisX - otherX) / distance * accelaration;
                thisYSpeed -= (thisY - otherY) / distance * accelaration;
            }
        }
    }
            
    results[gid * 2] = thisXSpeed;
    results[gid * 2 + 1] = thisYSpeed;
}