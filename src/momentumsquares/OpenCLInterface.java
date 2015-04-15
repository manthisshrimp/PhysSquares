package momentumsquares;

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

    private final MomentumKernal momentumKernal = new MomentumKernal();
    private final CollisionKernal collisionKernal = new CollisionKernal();

    public OpenCLInterface() throws IOException {
        context = JavaCL.createBestContext();
        for (CLDevice d : context.getDevices()) {
            System.out.println(d.getName());
        }
        queue = context.createDefaultQueue();
        byteOrder = context.getByteOrder();
    }

    public float[] executeMomentumKernal(float[] inArray1, float[] inArray2) throws IOException {
        return momentumKernal.execute(inArray1, inArray2);
    }

    public float[] executeCollisionKernal(float[] mainRect, float[] otherRects) throws IOException {
        return collisionKernal.execute(mainRect, otherRects);
    }

    private class CollisionKernal {

        private CLKernel collisionKernel;
        private CLEvent collisionEvt;

        private Pointer<Float> mainRectPointer;
        private Pointer<Float> otherRectsPointer;
        private Pointer<Float> resultPointer;

        private CLBuffer<Float> mainRectBuffer;
        private CLBuffer<Float> otherRectsBuffer;
        private CLBuffer<Float> resultBuffer;

        private float[] execute(float[] mainRect, float[] otherRects) throws IOException {
            if (collisionKernel == null) {
                String src = IOUtils.readText(OpenCLInterface.class.getResource("kernals/collisionKernal.cl"));
                CLProgram collisionProgram = context.createProgram(src);
                collisionKernel = collisionProgram.createKernel("collisionKernel");
            }

            mainRectPointer = Pointer.allocateFloats(mainRect.length).order(byteOrder);
            otherRectsPointer = Pointer.allocateFloats(otherRects.length).order(byteOrder);
            resultPointer = Pointer.allocateFloats(2).order(byteOrder);

            mainRectPointer.setFloats(mainRect);
            otherRectsPointer.setFloats(otherRects);

            mainRectBuffer = context.createBuffer(Usage.Input, mainRectPointer);
            otherRectsBuffer = context.createBuffer(Usage.Input, otherRectsPointer);
            resultBuffer = context.createBuffer(Usage.Output, resultPointer);

            collisionKernel.setArgs(mainRectBuffer, otherRectsBuffer, resultBuffer);
            collisionEvt = collisionKernel.enqueueNDRange(queue, new int[]{otherRects.length / 4});

            resultPointer = resultBuffer.read(queue, collisionEvt);
            
            mainRectBuffer.release();
            otherRectsBuffer.release();
            resultBuffer.release();

            return resultPointer.getFloats();
        }
    }

    private class MomentumKernal {

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
