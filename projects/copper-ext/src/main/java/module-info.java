module org.copperengine.ext {
    requires transitive org.copperengine.core;

    requires java.sql;

    requires org.slf4j;
    requires org.objectweb.asm;
    requires org.apache.commons.io;
    requires transitive com.google.common;
    requires transitive snakeyaml;

    exports org.copperengine.ext.persistent;
    exports org.copperengine.ext.util;
    exports org.copperengine.ext.wfrepo.classpath;
    exports org.copperengine.ext.wfrepo.git;
}
