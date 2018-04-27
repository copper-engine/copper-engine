module org.copperengine.ext {
    requires org.copperengine.core;

    requires java.sql;

    requires slf4j.api;
    requires org.objectweb.asm;
    requires commons.io;
    requires com.google.common;
    requires snakeyaml;

    exports org.copperengine.ext.persistent;
    exports org.copperengine.ext.util;
    exports org.copperengine.ext.wfrepo.classpath;
}
