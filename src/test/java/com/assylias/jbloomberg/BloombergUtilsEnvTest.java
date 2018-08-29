/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * We need a separate test class here because the environment variable is set once when the BloombergUtils class is first loaded.
 */
public class BloombergUtilsEnvTest {

  @Test (groups = "unit") public void test_bbcomm_location_other(@Mocked ProcessBuilder pb) {
    new Expectations(Files.class, ShellUtils.class, System.class) {{
      System.getenv(anyString); result = "C:/custom";
      ShellUtils.isProcessRunning(anyString); result = false;
      Files.exists(Paths.get("C:/custom/bbcomm.exe")); result = true;
    }};
    BloombergUtils.startBloombergProcessIfNecessary();
    new Verifications() {{
      pb.directory(new File("C:/custom"));
    }};
  }
}
