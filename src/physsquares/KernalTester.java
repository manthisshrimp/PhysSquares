package physsquares;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class KernalTester {

    private static final DecimalFormat df = new DecimalFormat("##.###");
    private final OpenCLInterface ocli;

    public KernalTester() throws IOException {
        ocli = new OpenCLInterface();
    }

    public static void main(String[] args) throws Exception {
        KernalTester tester = new KernalTester();

        long startTime = System.nanoTime();
        tester.testCollisionKernal(1000);
        long endTime = System.nanoTime();

        double totalTimeMS = ((double) endTime - startTime) / 1000000;
        System.out.println("CK AVG: " + df.format((totalTimeMS) / 1000) + "ms");

        startTime = System.nanoTime();
        tester.testMomentumKernal(1000);
        endTime = System.nanoTime();

        totalTimeMS = ((double) endTime - startTime) / 1000000;
        System.out.println("MK AVG: " + df.format((totalTimeMS) / 1000) + "ms");
    }

    private void testCollisionKernal(int tests) throws IOException {
        float[] rects = {0, 0, 1, 1, 5, 5, 1, 1, 5, 5, 1, 1, 0, 0, 1, 1, 10, 10, 1, 1};
        float[] results = null;

        for (int i = 0; i < tests; i++) {
            results = ocli.executeIntersectionKernal(rects);
        }
        
        System.out.println(Arrays.toString(results));
    }

    private void testMomentumKernal(int tests) throws IOException {
        float[] speeds = {1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4, 1, 2, 3, 4};
        float[] masses = {1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2};
        float[] results = null;

        for (int i = 0; i < tests; i++) {
            results = ocli.executeMomentumKernal(speeds, masses);
        }
        
        System.out.println(Arrays.toString(results));
    }
}
