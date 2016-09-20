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

    private final MomentumKernalExecuter momentumKernalExecuter = new MomentumKernalExecuter();
    private final IntersectionKernalExecuter collisionKernalExecuter = new IntersectionKernalExecuter();

    public OpenCLInterface() throws IOException {
        context = JavaCL.createBestContext();
        for (CLDevice d : context.getDevices()) {
            System.out.println("Using device: " + d.getName());
        }
        queue = context.createDefaultQueue();
        byteOrder = context.getByteOrder();
    }

    public float[] executeMomentumKernal(float[] inArray1, float[] inArray2) throws IOException {
        return momentumKernalExecuter.execute(inArray1, inArray2);
    }

    public float[] executeIntersectionKernal(float[] rects) throws IOException {
        return collisionKernalExecuter.execute(rects);
    }

    private class IntersectionKernalExecuter {

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
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernals/intersectionKernal.cl"));
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

    private class MomentumKernalExecuter {

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
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernals/momentumKernel.cl"));
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
}
