/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.ElementIterator;
import com.bloomberglp.blpapi.Schema;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that should not be very useful for the users of the API.
 */
final class BloombergUtils {

    private final static Logger logger = LoggerFactory.getLogger(BloombergUtils.class);
    private static volatile boolean isBbcommStarted = false;
    private final static String BBCOMM_PROCESS = "bbcomm.exe";
    private final static String BBCOMM_FOLDER = "C:/blp/API";

    private BloombergUtils() {
    }

    /**
     * Transforms a Bloomberg Element into the most specific Object (for example: Double, Float, Integer, DateTime,
     * String etc.).<br>
     * Complex types are returned in the form of collections ({@code List<Object>} for arrays and
     * {@code Map<String, Object>}
     * for sequences).
     */
    public static Object getSpecificObjectOf(Element field) {
        if (field.datatype() == Schema.Datatype.FLOAT64) {
            //likeliest data type
            return field.getValueAsFloat64();
        } else if (field.datatype() == Schema.Datatype.FLOAT32) {
            return field.getValueAsFloat32();
        } else if (field.datatype() == Schema.Datatype.BOOL) {
            return field.getValueAsBool();
        } else if (field.datatype() == Schema.Datatype.CHAR) {
            return field.getValueAsChar();
        } else if (field.datatype() == Schema.Datatype.INT32) {
            return field.getValueAsInt32();
        } else if (field.datatype() == Schema.Datatype.INT64) {
            return field.getValueAsInt64();
        } else if (field.datatype() == Schema.Datatype.STRING) {
            return field.getValueAsString();
        } else if (field.datatype() == Schema.Datatype.DATE) {
            Datetime dt = field.getValueAsDate();
            return LocalDate.of(dt.year(), dt.month(), dt.dayOfMonth());
        } else if (field.datatype() == Schema.Datatype.TIME) {
            Datetime dt = field.getValueAsDatetime();
            Calendar cal = dt.calendar(); //returns a calendar with TZ = UTC
            return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), dt.nanosecond());
        } else if (field.datatype() == Schema.Datatype.DATETIME) {
            Datetime dt = field.getValueAsDatetime();
            Calendar cal = dt.calendar(); //returns a calendar with TZ = UTC
            ZonedDateTime zdt = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.of("Z"));
            if (!dt.hasParts(Datetime.DATE)) return zdt.toLocalTime();
            else return zdt;
        } else if (field.isArray()) {
            List<Object> list = new ArrayList<>(field.numValues());
            for (int i = 0; i < field.numValues(); i++) {
                list.add(getSpecificObjectOf(field.getValueAsElement(i)));
            }
            return list;
        } else if (field.datatype() == Schema.Datatype.SEQUENCE
                || field.datatype() == Schema.Datatype.CHOICE) { //has to be after array because arrays are sequences...
            ElementIterator it = field.elementIterator();
            Map<String, Object> map = new LinkedHashMap<>(field.numElements(), 1.0f);
            while (it.hasNext()) {
                Element e = it.next();
                map.put(e.name().toString(), getSpecificObjectOf(e));
            }
            return map;
        } else {
            return field.toString(); //always works
        }
    }

    /**
     * Starts the bbcomm process if necessary, which is required to connect to the Bloomberg data feed.<br>
     * This method will block up to one second if it needs to manually start the process. If the process is not
     * started by the end of the timeout, this method will return false but the process might start later on.
     * <p/>
     * @return true if bbcomm was started successfully within one second, false otherwise.
     */
    public static boolean startBloombergProcessIfNecessary() {
        return isBbcommStarted || isBloombergProcessRunning() || startBloombergProcess();
    }

    /**
     *
     * @return true if the bbcomm process is running
     */
    private static boolean isBloombergProcessRunning() {
        if (ShellUtils.isProcessRunning(BBCOMM_PROCESS)) {
            logger.info("{} is started", BBCOMM_PROCESS);
            return true;
        }
        return false;
    }

    private static boolean startBloombergProcess() {
        Callable<Boolean> startBloombergProcess = BloombergUtils::getStartingCallable;
        isBbcommStarted = getResultWithTimeout(startBloombergProcess, 1, TimeUnit.SECONDS);
        return isBbcommStarted;
    }

    private static Boolean getStartingCallable() throws Exception {
        logger.info("Starting {} manually", BBCOMM_PROCESS);
        ProcessBuilder pb = new ProcessBuilder(BBCOMM_PROCESS);
        pb.directory(new File(BBCOMM_FOLDER));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("UTF-8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("{} > {}", BBCOMM_PROCESS, line);
                if (line.toLowerCase().contains("started")) {
                    logger.info("{} is started", BBCOMM_PROCESS);
                    return true;
                }
            }
            return false;
        }
    }

    private static boolean getResultWithTimeout(Callable<Boolean> startBloombergProcess, int timeout, TimeUnit timeUnit) {
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "Bloomberg - bbcomm starter thread");
                t.setDaemon(true);
                return t;
            }
        });
        Future<Boolean> future = executor.submit(startBloombergProcess);

        try {
            return future.get(timeout, timeUnit);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException | TimeoutException e) {
            logger.error("Could not start bbcomm", e);
            return false;
        } finally {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    logger.warn("bbcomm starter thread still running");
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
