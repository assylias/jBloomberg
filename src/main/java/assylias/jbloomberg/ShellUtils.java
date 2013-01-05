/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that should not be very useful for the API users. It is used to check if the bbcomm.exe process is
 * running.
 */
final class ShellUtils {

    private final static Logger logger = LoggerFactory.getLogger(ShellUtils.class);

    private ShellUtils() { //not instantiable
    }

    /**
     * This method returns a list of processes currently running.
     * <p>
     * It is Windows specific and does not handle non-ASCII characters very well. See <a
     * href="http://stackoverflow.com/questions/13348811/get-list-of-processes-on-windows-in-a-charset-safe-way"> this
     * question on Stackoverflow for example.</a>
     * <br>
     * However it is good enough to find if the bbcomm.exe process is running or not.
     *
     * @return a list of processes currently running
     *
     * @throws RuntimeException if the request sent to the OS to get the list of running processes fails
     */
    public static List<String> getRunningProcesses() {
        List<String> processes = new ArrayList<>();

        try {
            Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
            try (BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")))) {
                String line;
                while ((line = input.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String process = line.split(" ")[0];
                        if (process.contains("exe")) {
                            processes.add(process);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not retrieve the list of running processes from the OS", e);
        }

        return processes;
    }

    /**
     *
     * @param processName the name of the process, for example "bbcomm.exe"
     * <p/>
     * @return true if the process is currently running
     * <p/>
     * @throws RuntimeException if the request sent to the OS to get the list of running processes fails
     */
    public static boolean isProcessRunning(String processName) {
        List<String> processes = getRunningProcesses();
        return processes.contains(processName);
    }
}
