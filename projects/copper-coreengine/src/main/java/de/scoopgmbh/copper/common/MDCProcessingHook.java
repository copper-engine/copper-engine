package de.scoopgmbh.copper.common;


import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.util.MDCConstants;
import org.slf4j.MDC;

public class MDCProcessingHook implements IProcessingHook {

    @Override
    public void postProcess(Workflow<?> wf) {
        MDC.remove(MDCConstants.REQUEST);
    }

    @Override
    public void preProcess(Workflow<?> wf) {
        MDC.put(MDCConstants.REQUEST, wf.getId());
    }
}
