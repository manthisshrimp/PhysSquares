__kernel void intersectionKernel(
__global const float *rects,
__global const float *total,
__global float *results) {
    int gid = get_global_id(0);
    int t = *total;

    float tx = rects[gid * 4];
    float ty = rects[gid * 4 + 1];
    float tw = rects[gid * 4 + 2];
    float th = rects[gid * 4 + 3];
    
    float j = 0;
    for (int index = 0; index < t; index++) {
        j += 10;
        if (index != gid) {
            float rx = rects[index * 4];
            float ry = rects[index * 4 + 1];
            float rw = rects[index * 4 + 2];
            float rh = rects[index * 4 + 3];

            if (((rw + rx) < rx || (rw + rx) > tx) && ((rh + ry) < ry || (rh + ry) > ty) 
                    && ((tw + tx) < tx || (tw + tx) > rx) && ((th + ty) < ty || (th + ty) > ry)) {

                // Can't use i as it always returns *total even though the collision detection works fine.

                results[gid] = j / 10;
                return;
            }
        }
    }
    results[gid] = -1;
}
