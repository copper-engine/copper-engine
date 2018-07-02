module org.copperengine.ext {
    requires org.copperengine.core;

    requires java.sql;

    requires org.slf4j;
    requires org.objectweb.asm;
    requires org.apache.commons.io;
    requires com.google.common;
    requires snakeyaml;

    exports org.copperengine.ext.persistent;
    exports org.copperengine.ext.util;
    exports org.copperengine.ext.wfrepo.classpath;
}
