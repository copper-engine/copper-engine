module org.copperengine.spring {
    requires transitive org.copperengine.core;

    requires java.sql;

    requires org.slf4j;

    requires spring.core;
    requires transitive spring.jdbc;
    requires spring.batch.infrastructure;
    requires transitive spring.beans;
    requires transitive spring.context;
    requires transitive spring.tx;

    exports org.copperengine.spring.audit;
}
