__kernel void collisionKernel(
__global const float *rect,
__global const float *others,
__global float *result) {
    int gid = get_global_id(0);

    float tx = rect[0];
    float ty = rect[1];
    float tw = rect[2];
    float th = rect[3];
    
    float rx = others[gid * 4];
    float ry = others[gid * 4 + 1];
    float rw = others[gid * 4 + 2];
    float rh = others[gid * 4 + 3];

    if (tw != rw || th != rh || tx != rx || ty != ry) {
        
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;

        if ((rw < rx || rw > tx) && (rh < ry || rh > ty) 
            && (tw < tx || tw > rx) && (th < ty || th > ry)) {
            result[0] = 1;
            result[1] = gid;
        }
    }
}