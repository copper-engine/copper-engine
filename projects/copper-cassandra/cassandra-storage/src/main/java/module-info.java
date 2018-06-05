module org.copperengine.cassandra.storage {
    requires org.copperengine.core;
    requires org.copperengine.management;
    requires org.copperengine.ext;

    requires java.sql;

    requires org.slf4j;
    requires commons.lang;
    requires cassandra.driver.core;
    requires com.google.common;
    requires jackson.core;
    requires jackson.databind;

    exports org.copperengine.core.persistent.cassandra;
    exports org.copperengine.core.persistent.hybrid;
}