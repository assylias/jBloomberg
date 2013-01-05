/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author yannletallec
 */
public class BloombergUtilsTest {

    @Test(groups = "unit")
    public void test_ProcessRunning() {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock(invocations = 1)
            public boolean isProcessRunning(String processName) {
                return true;
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_NoRetryOnceRunning() {
        setBbcommStartedFlag(true);
        new MockUp<ShellUtils>() {
            @Mock(invocations = 0)
            public boolean isProcessRunning(String processName) {
                return true;
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_ProcessNotRunning_StartBbCommSucceeds(@Mocked final ProcessBuilder pb, @Mocked final Process p) throws IOException {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock
            public boolean isProcessRunning(String processName) {
                return false;
            }
        };
        new NonStrictExpectations() {
            {
                pb.start();
                result = p;
                p.getInputStream();
                result = new ByteArrayInputStream("started".getBytes());
            }
        };
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    @Test(groups = "unit")
    public void test_ProcessNotRunning_StartBbCommFails(@Mocked final ProcessBuilder pb, @Mocked final Process p) throws IOException {
        setBbcommStartedFlag(false);
        new MockUp<ShellUtils>() {
            @Mock
            public boolean isProcessRunning(String processName) {
                return false;
            }
        };
        new NonStrictExpectations() {
            {
                pb.start();
                result = p;
                p.getInputStream();
                result = new ByteArrayInputStream("whatever".getBytes());
            }
        };
        assertFalse(BloombergUtils.startBloombergProcessIfNecessary());
    }


    @Test(groups = "requires-bloomberg")
    public void testBbStart() throws Exception {
        setBbcommStartedFlag(false);
        assertTrue(BloombergUtils.startBloombergProcessIfNecessary());
    }

    private static void setBbcommStartedFlag(boolean flag) {
        try {
            Field f = BloombergUtils.class.getDeclaredField("isBbcommStarted");
            f.setAccessible(true);
            f.set(null, flag);
        } catch (Exception e) {
            fail();
        }
    }
}
