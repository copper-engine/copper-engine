package org.copperengine.performancetest.main;

public class Main {

    public static void main(String[] args) {
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

        System.out.println("Engine is stopped!");
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static void usage() {
        System.out.println("Usage: ....");
    }
}
