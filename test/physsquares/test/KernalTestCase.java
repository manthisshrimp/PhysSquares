package physsquares.test;

import java.io.IOException;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import physsquares.OpenCLInterface;
import physsquares.kernals.GKDummy;

public class KernalTestCase {

    private static OpenCLInterface openCLInterface;

//    @Test
    public void testIntersectionKernal() {
        float[] rects = {0, 0, 5, 5, 2, 2, 5, 5, 20, 20, 1, 1};
        float[] results = null;
        float[] expected = {1, 0, -1};

        try {
            results = openCLInterface.executeIntersectionKernal(rects);
        } catch (IOException ex) {
            // Do nothing.
        }

        Assert.assertNotNull(results);
        System.out.println("Input:    " + Arrays.toString(rects));
        System.out.println("Result:   " + Arrays.toString(results));
        System.out.println("Expected: " + Arrays.toString(expected));
        Assert.assertEquals(results, expected);
    }

//    @Test
    public void testMomentumKernal() {
        // TODO: Write test case for openCLInterface.executeMomentumKernal()
    }

    @Test
    public void testGravityKernal() {
        float[] masses = {1000000000, 1000000000};
        float[] speeds = {1, 1, 1, 1};
        float[] points = {1, 1, 2, 2};
        float distMod = 1.0e-5F;
        float[] result;

        result = new GKDummy().execute(masses, speeds, points, distMod);

        Assert.assertNotNull(result);
        System.out.println("Input:    " + Arrays.toString(speeds));
        System.out.println("Result:   " + Arrays.toString(result));
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        openCLInterface = new OpenCLInterface();
    }
}
