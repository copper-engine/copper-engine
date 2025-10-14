module org.copperengine.core {
    requires transitive org.copperengine.management;

    requires transitive java.sql;
    requires transitive java.management;
    requires transitive org.slf4j;
    requires transitive org.objectweb.asm;
    requires transitive org.objectweb.asm.tree;
    requires org.objectweb.asm.util;
    requires java.desktop;
    requires java.compiler;

    uses javax.tools.JavaCompiler;

    exports org.copperengine.core;
    exports org.copperengine.core.audit;
    exports org.copperengine.core.batcher;
    exports org.copperengine.core.batcher.impl;
    exports org.copperengine.core.common;
    exports org.copperengine.core.db.utility;
    exports org.copperengine.core.db.utility.oracle;
    exports org.copperengine.core.instrument;
    exports org.copperengine.core.internal;
    exports org.copperengine.core.lockmgr;
    exports org.copperengine.core.lockmgr.tranzient;
    exports org.copperengine.core.monitoring;
    exports org.copperengine.core.persistent;
    exports org.copperengine.core.persistent.adapter;
    exports org.copperengine.core.persistent.alpha.generator;
    exports org.copperengine.core.persistent.lock;
    exports org.copperengine.core.persistent.txn;
    exports org.copperengine.core.tranzient;
    exports org.copperengine.core.util;
    exports org.copperengine.core.wfrepo;
}
