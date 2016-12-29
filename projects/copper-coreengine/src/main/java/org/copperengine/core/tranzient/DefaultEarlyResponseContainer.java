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
package org.copperengine.core.tranzient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.copperengine.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the {@link EarlyResponseContainer} interface.
 * Early response are stored for a configurable time interval. Later on they may be removed.
 *
 * @author austermann
 */
public class DefaultEarlyResponseContainer implements EarlyResponseContainer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEarlyResponseContainer.class);

    static final class EarlyResponse {
        final long ts;
        final Response<?> response;

        public EarlyResponse(final Response<?> response, final long minHoldBackTime) {
            this.response = response;
            long ts = System.currentTimeMillis() + minHoldBackTime;
            if (ts <= 0) {
                ts = Long.MAX_VALUE;
            }
            this.ts = ts;
        }
    }

    private int lowerBorderResponseMapSize = 25000;
    private int upperBorderResponseMapSize = 26000;
    private LinkedHashMap<String, List<EarlyResponse>> responseMap = new LinkedHashMap<String, List<EarlyResponse>>(5000);

    private long minHoldBackTime = 30000;
    private Thread thread;
    private boolean shutdown = false;
    private int checkInterval = 250;

    public DefaultEarlyResponseContainer() {
    }

    @Override
    public void put(final Response<?> response) {
        if (response == null)
            throw new NullPointerException();

        synchronized (responseMap) {
            List<EarlyResponse> list = responseMap.get(response.getCorrelationId());
            if (list == null) {
                list = new ArrayList<DefaultEarlyResponseContainer.EarlyResponse>(3);
                responseMap.put(response.getCorrelationId(), list);
            }
            list.add(new EarlyResponse(response, response.getInternalProcessingTimeout() == null ? minHoldBackTime : response.getInternalProcessingTimeout()));

            if (responseMap.size() > upperBorderResponseMapSize) {
                // TODO Hier wird die Groesse der Map beruecksichtigt, jedoch nicht die Anzahl EarlyResponses (pro
                // Eintrag in der Map koennen das mehrere sein)
                // Koennte man noch verbessern
                Iterator<String> iterator = responseMap.keySet().iterator();
                while (responseMap.size() > lowerBorderResponseMapSize) {
                    iterator.next();
                    iterator.remove();
                }
            }
        }

    }

    @Override
    public List<Response<?>> get(final String correlationId) {
        if (correlationId == null)
            throw new NullPointerException();
        if (correlationId.length() == 0)
            throw new IllegalArgumentException();

        synchronized (responseMap) {
            List<EarlyResponse> erList = responseMap.remove(correlationId);
            if (erList == null || erList.isEmpty()) {
                return Collections.emptyList();
            }
            List<Response<?>> rv = new ArrayList<Response<?>>(erList.size());
            for (EarlyResponse earlyResponse : erList) {
                rv.add(earlyResponse.response);
            }
            return rv;
        }
    }

    @Override
    public synchronized void startup() {
        if (thread != null)
            throw new IllegalStateException();
        thread = new Thread("EarlyResponseManager") {
            @Override
            public void run() {
                doHousekeeping();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        shutdown = true;
        thread.interrupt();
        thread = null;
    }

    public void setUpperBorderResponseMapSize(int upperBorderResponseMapSize) {
        if (upperBorderResponseMapSize <= lowerBorderResponseMapSize)
            throw new IllegalArgumentException();
        this.upperBorderResponseMapSize = upperBorderResponseMapSize;
    }

    public void setLowerBorderResponseMapSize(int lowerBorderResponseMapSize) {
        if (lowerBorderResponseMapSize <= 0)
            throw new IllegalArgumentException();
        this.lowerBorderResponseMapSize = lowerBorderResponseMapSize;
    }

    public int getLowerBorderResponseMapSize() {
        return lowerBorderResponseMapSize;
    }

    public int getUpperBorderResponseMapSize() {
        return upperBorderResponseMapSize;
    }

    public void setMinHoldBackTime(long minHoldBackTime) {
        if (minHoldBackTime <= 0)
            throw new IllegalArgumentException();
        this.minHoldBackTime = minHoldBackTime;
    }

    public long getMinHoldBackTime() {
        return minHoldBackTime;
    }

    public void setCheckInterval(int checkInterval) {
        if (checkInterval <= 0)
            throw new IllegalArgumentException();
        this.checkInterval = checkInterval;
    }

    public int getCheckInterval() {
        return checkInterval;
    }

    // TODO Frueher (vor den ResponseListen) konnte man die Ueberpruefung abbrechen, sobald man kein weiteres element in
    // der Liste gefunden hat.
    // Jetzt gibt es eine Map von Listen und man muss immer alles komplett Ueberpruefen - ggf. optimieren
    private void doHousekeeping() {
        logger.info("started");
        while (!shutdown) {
            try {
                List<EarlyResponse> removedEarlyResponses = new ArrayList<>();
                synchronized (responseMap) {
                    Iterator<List<EarlyResponse>> responseMapIterator = responseMap.values().iterator();
                    while (responseMapIterator.hasNext()) {
                        List<EarlyResponse> erList = responseMapIterator.next();
                        Iterator<EarlyResponse> erListIterator = erList.iterator();
                        while (erListIterator.hasNext()) {
                            EarlyResponse earlyResponse = erListIterator.next();
                            if (earlyResponse.ts < System.currentTimeMillis()) {
                                responseMapIterator.remove();
                                removedEarlyResponses.add(earlyResponse);
                            }
                        }
                        if (erList.isEmpty()) {
                            responseMapIterator.remove();
                        }
                    }
                }
                for (EarlyResponse er : removedEarlyResponses) {
                    logger.info("Removed early response with correlationId {} and responseId {}", er.response.getCorrelationId(), er.response.getResponseId());
                }
                removedEarlyResponses = null; // let the GC do its job :-)
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        logger.info("stopped");
    }
}
