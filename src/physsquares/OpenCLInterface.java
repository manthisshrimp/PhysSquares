package physsquares;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.util.IOUtils;
import java.io.IOException;
import java.nio.ByteOrder;
import org.bridj.Pointer;

public class OpenCLInterface {

    private final CLContext context;
    private final CLQueue queue;
    private final ByteOrder byteOrder;

    private final MomentumKernelExecuter momentumKernelExecuter = new MomentumKernelExecuter();
    private final IntersectionKernelExecuter collisionKernelExecuter = new IntersectionKernelExecuter();
    private final GravityKernelExecuter gravityKernelExecuter = new GravityKernelExecuter();

    public OpenCLInterface() throws IOException {
        context = JavaCL.createBestContext();
        for (CLDevice d : context.getDevices()) {
            System.out.println("Using device: " + d.getName());
        }
        queue = context.createDefaultQueue();
        byteOrder = context.getByteOrder();
    }

    public float[] executeMomentumKernel(float[] inArray1, float[] inArray2) throws IOException {
        return momentumKernelExecuter.execute(inArray1, inArray2);
    }

    public float[] executeIntersectionKernel(float[] rects) throws IOException {
        return collisionKernelExecuter.execute(rects);
    }

    public float[] executeGravityKernel(int totalOther, float[] masses, float[] speeds, float[] points, float distanceModifier) throws IOException {
        return gravityKernelExecuter.execute(totalOther, masses, speeds, points, distanceModifier);
    }

    private class IntersectionKernelExecuter {

        private CLKernel collisionKernel;
        private CLEvent collisionEvt;

        private Pointer<Float> rectsPointer;
        private Pointer<Float> totalPointer;
        private Pointer<Float> resultPointer;

        private CLBuffer<Float> rectsBuffer;
        private CLBuffer<Float> totalBuffer;
        private CLBuffer<Float> resultBuffer;

        private float[] execute(float[] rects) throws IOException {
            if (collisionKernel == null) {
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernels/intersectionKernel.cl"));
                CLProgram collisionProgram = context.createProgram(src);
                collisionKernel = collisionProgram.createKernel("intersectionKernel");
            }

            rectsPointer = Pointer.allocateFloats(rects.length).order(byteOrder);
            totalPointer = Pointer.allocateFloat().order(byteOrder);
            resultPointer = Pointer.allocateFloats(rects.length / 4).order(byteOrder);

            rectsPointer.setFloats(rects);
            totalPointer.setFloat(rects.length / 4);

            rectsBuffer = context.createBuffer(Usage.Input, rectsPointer);
            totalBuffer = context.createBuffer(Usage.Input, totalPointer);
            resultBuffer = context.createBuffer(Usage.Output, resultPointer);

            collisionKernel.setArgs(rectsBuffer, totalBuffer, resultBuffer);
            collisionEvt = collisionKernel.enqueueNDRange(queue, new int[]{rects.length / 4});

            resultPointer = resultBuffer.read(queue, collisionEvt);

            rectsBuffer.release();
            totalBuffer.release();
            resultBuffer.release();

            return resultPointer.getFloats();
        }
    }

    private class MomentumKernelExecuter {

        private CLKernel momentumKernel;
        private CLEvent momentumEvt;

        private Pointer<Float> inArray1Pointer;
        private Pointer<Float> inArray2Pointer;
        private Pointer<Float> outArrayPointer;

        private CLBuffer<Float> inBuffer1;
        private CLBuffer<Float> inBuffer2;
        private CLBuffer<Float> outBuffer;

        private float[] execute(float[] inArray1, float[] inArray2) throws IOException {
            if (momentumKernel == null) {
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernels/momentumKernel.cl"));
                CLProgram momentumProgram = context.createProgram(src);
                momentumKernel = momentumProgram.createKernel("momentumKernel");
            }

            int intputSize = inArray1.length;

            inArray1Pointer = Pointer.allocateFloats(intputSize).order(byteOrder);
            inArray2Pointer = Pointer.allocateFloats(intputSize).order(byteOrder);
            outArrayPointer = Pointer.allocateFloats(intputSize).order(byteOrder);

            inArray1Pointer.setFloats(inArray1);
            inArray2Pointer.setFloats(inArray2);

            inBuffer1 = context.createBuffer(Usage.Input, inArray1Pointer);
            inBuffer2 = context.createBuffer(Usage.Input, inArray2Pointer);
            outBuffer = context.createBuffer(Usage.Output, outArrayPointer);

            momentumKernel.setArgs(inBuffer1, inBuffer2, outBuffer);
            momentumEvt = momentumKernel.enqueueNDRange(queue, new int[]{intputSize});

            outArrayPointer = outBuffer.read(queue, momentumEvt);

            inBuffer1.release();
            inBuffer2.release();
            outBuffer.release();

            return outArrayPointer.getFloats();
        }
    }

    private class GravityKernelExecuter {

        private CLKernel gravityKernel;
        private CLEvent completionEvent;

        private Pointer<Float> massesArrayPointer;
        private Pointer<Float> speedsArrayPointer;
        private Pointer<Float> pointsArrayPointer;
        private Pointer<Float> resultsArrayPointer;

        private CLBuffer<Float> massesBuffer;
        private CLBuffer<Float> speedsBuffer;
        private CLBuffer<Float> pointsBuffer;
        private CLBuffer<Float> resultsBuffer;

        public float[] execute(int total, float[] masses, float[] speeds, float[] points, float distanceModifier) throws IOException {
            if (gravityKernel == null) {
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernels/gravityKernel.cl"));
                CLProgram momentumProgram = context.createProgram(src);
                gravityKernel = momentumProgram.createKernel("gravityKernel");
            }

            massesArrayPointer = Pointer.allocateFloats(masses.length).order(byteOrder);
            speedsArrayPointer = Pointer.allocateFloats(speeds.length).order(byteOrder);
            pointsArrayPointer = Pointer.allocateFloats(points.length).order(byteOrder);

            massesArrayPointer.setFloats(masses);
            speedsArrayPointer.setFloats(speeds);
            pointsArrayPointer.setFloats(points);

            resultsArrayPointer = Pointer.allocateFloats(total).order(byteOrder);

            massesBuffer = context.createBuffer(Usage.Input, massesArrayPointer);
            speedsBuffer = context.createBuffer(Usage.Input, speedsArrayPointer);
            pointsBuffer = context.createBuffer(Usage.Input, pointsArrayPointer);
            resultsBuffer = context.createBuffer(Usage.Output, resultsArrayPointer);

            gravityKernel.setArgs(total, distanceModifier, massesBuffer, speedsBuffer, pointsBuffer, resultsBuffer);
            completionEvent = gravityKernel.enqueueNDRange(queue, new int[]{total});

            resultsArrayPointer = resultsBuffer.read(queue, completionEvent);
            float[] results = resultsArrayPointer.getFloats();

            massesArrayPointer.release();
            speedsArrayPointer.release();
            pointsArrayPointer.release();
            resultsArrayPointer.release();

            massesBuffer.release();
            speedsBuffer.release();
            pointsBuffer.release();
            resultsBuffer.release();
            
            completionEvent.release();

            return results;
        }

    }
}
