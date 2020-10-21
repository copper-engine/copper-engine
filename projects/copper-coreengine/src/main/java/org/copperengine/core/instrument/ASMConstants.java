package org.copperengine.core.instrument;

import org.objectweb.asm.Opcodes;

public class ASMConstants {
    public static int API_VERSION = getApiVersion();

    private static int getApiVersion() {
        String version = System.getProperty("org.copperengine.asm.api.version", "9").trim();
        switch (version) {
            case "5": return Opcodes.ASM5;
            case "6": return Opcodes.ASM6;
            case "7": return Opcodes.ASM7;
            case "8": return Opcodes.ASM8;
            case "9": return Opcodes.ASM9;
            default: throw new IllegalArgumentException("Unsupported ASM API version: " + version);
        }
    }
}
