package org.copperengine.core;

import java.sql.Connection;

public abstract class AbstractWaitHook implements WaitHook {

    @Override
    public void onWait(Workflow<?> wf, Connection con) throws Exception {

    }

}
