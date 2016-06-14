package org.copperengine.performancetest.main;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    public static void main(String[] args) {
        System.setProperty("ts", new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date()));
        try {
            if (args.length == 0) {
                usage();
            }
            else if ("latency".equalsIgnoreCase(args[0])) {
                new LatencyPerformanceTest().run();
            }
            else if ("throughput".equalsIgnoreCase(args[0])) {
                new ThroughputPerformanceTest().run();
            }
            else {
                usage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            usage();
        }
        System.exit(0);
    }

    private static void usage() {
        System.out.println("Usage: java <parameters> -jar copper-performance-test.jar (latency|throughput)");
        System.out.println("  latency    - measures the latency for executing resubmit/savepoints in an otherwise idle system");
        System.out.println("  throughput - executes a large amount of workflow instances, each with 10 wait/notifies, to measure the avg. number of wait/notify cycles per second");
        System.out.println();
        System.out.println("  with <parameters> as follows");
        for (ConfigParameterGroup grp : ConfigParameterGroup.values()) {
            System.out.println("  ** " + grp.getDescription() + " **");
            for (ConfigParameter p : ConfigParameter.all4group(grp)) {
                System.out.println("      -D" + p.getKey() + "=<value>  --> (" + p.getMandatory() + ") " + p.getDescription() + (p.getDefaultValue() != null ? (" - default value is " + p.getDefaultValue()) : ""));
            }
            System.out.println();
        }
    }
}
