module org.copperengine.cassandra.storage {
    requires transitive org.copperengine.core;
    requires transitive org.copperengine.ext;
    requires org.copperengine.management;

    requires java.sql;

    requires org.slf4j;
    requires commons.lang;
    requires transitive cassandra.driver.core;
    requires transitive com.google.common;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports org.copperengine.core.persistent.cassandra;
    exports org.copperengine.core.persistent.hybrid;
}