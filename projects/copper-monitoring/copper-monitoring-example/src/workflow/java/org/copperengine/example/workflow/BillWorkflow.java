/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.example.workflow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowDescription;
import org.copperengine.core.audit.AuditTrail;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.monitoring.example.adapter.Bill;
import org.copperengine.monitoring.example.adapter.BillAdapter;
import org.copperengine.monitoring.example.adapter.BillableService;

@WorkflowDescription(alias = "BillWorkflow", majorVersion = 1, minorVersion = 0, patchLevelVersion = 0)
public class BillWorkflow extends PersistentWorkflow<String> {
    private static final long serialVersionUID = 1L;

    private transient BillAdapter billAdapter;
    private transient AuditTrail auditTrail;

    private ArrayList<BillableService> billableServices = new ArrayList<BillableService>();

    @AutoWire
    public void setBillAdapter(BillAdapter billAdapter) {
        this.billAdapter = billAdapter;
    }

    @AutoWire
    public void setAuditTrail(AuditTrail auditTrail) {
        this.auditTrail = auditTrail;
    }

    @Override
    public void main() throws Interrupt {
        while (true) {
            auditTrail.asynchLog(2, new Date(), "1", "1", "", "", "", "wait for Data", "Text");
            callWait();
            auditTrail.asynchLog(1, new Date(), "2", "2", "", "", "", "data found", "Text");

            ArrayList<Response<?>> all = new ArrayList<Response<?>>(getAndRemoveResponses(BillAdapter.BILL_TIME));
            all.addAll(getAndRemoveResponses(BillAdapter.BILLABLE_SERVICE));

            Response<String> rsponse = new Response<String>("cor", "message", null);
            rsponse.getResponse();

            for (Response<?> response : all) {
                if (response.getResponse() instanceof BillableService) {
                    billableServices.add(((BillableService) response.getResponse()));
                }
            }
            for (Response<?> response : all) {
                if (response.getResponse() instanceof Bill) {
                    Bill bill = ((Bill) response.getResponse());
                    BigDecimal sum = new BigDecimal(0);
                    for (BillableService billableService : billableServices) {
                        sum = sum.add(billableService.getAmount());
                    }
                    bill.setTotalAmount(sum);
                    billAdapter.publishBill(bill);
                    billableServices.clear();
                }
            }

        }

    }

    private void callWait() throws Interrupt {
        wait(WaitMode.ALL, Workflow.NO_TIMEOUT, BillAdapter.BILL_TIME, BillAdapter.BILLABLE_SERVICE);
    }

}
