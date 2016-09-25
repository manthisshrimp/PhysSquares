__kernel void momentumKernel(
__global const float *speeds,
__global const float *masses,
__global float *newSpeeds)
{
    int gid = get_global_id(0);
    if (gid % 2 > 0) {
	newSpeeds[gid] = (((masses[gid] - masses[gid - 1]) / (masses[gid] + masses[gid - 1])) * speeds[gid])
            + (((masses[gid - 1] * 2) / (masses[gid] + masses[gid - 1])) * speeds[gid - 1]);
    } else {
	newSpeeds[gid] = (((masses[gid] - masses[gid + 1]) / (masses[gid] + masses[gid + 1])) * speeds[gid])
            + (((masses[gid + 1] * 2) / (masses[gid] + masses[gid + 1])) * speeds[gid + 1]);
    }
}