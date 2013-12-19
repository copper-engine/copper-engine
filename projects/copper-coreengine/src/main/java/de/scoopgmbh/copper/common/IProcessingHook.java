package de.scoopgmbh.copper.common;

import de.scoopgmbh.copper.Workflow;

public interface IProcessingHook {

     void postProcess(Workflow<?> wf);
     void preProcess(Workflow<?> wf);
}
