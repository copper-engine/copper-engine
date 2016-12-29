package org.copperengine.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StackTraceCreator {

    private static final String NEWLINE = System.getProperty("line.separator");
    private static final Logger logger = LoggerFactory.getLogger(StackTraceCreator.class);

    public static String createStackTrace() {
        try {
            int state = 0;
            final StringBuilder stackTrace = new StringBuilder(128);
            final StackTraceElement[] excStackTrace = new Exception().getStackTrace();
            for (int i=0; i<excStackTrace.length-2; i++) {
                StackTraceElement ste = excStackTrace[i];
                if (state == 0 && "org.copperengine.core.Workflow".equals(ste.getClassName()) && (ste.getMethodName().equals("wait") || ste.getMethodName().equals("resubmit"))) {
                    state = 1;
                }
                else if (state == 1) {
                    if (stackTrace.length() != 0)
                        stackTrace.append(NEWLINE);
                    stackTrace.append(ste.toString());
                }
            }
            return stackTrace.toString();
        }
        catch(Exception e) {
            logger.warn("Unable to create stack trace", e);
            return null;
        }
    }

}
