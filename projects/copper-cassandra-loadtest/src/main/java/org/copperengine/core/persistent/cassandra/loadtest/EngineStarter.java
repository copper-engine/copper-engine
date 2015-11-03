package org.copperengine.core.persistent.cassandra.loadtest;

public class EngineStarter {

    public static void main(final String[] args) {
        final CassandraEngineFactory factory = new CassandraEngineFactory();
        try {
            factory.createEngine(false);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    factory.destroyEngine();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
