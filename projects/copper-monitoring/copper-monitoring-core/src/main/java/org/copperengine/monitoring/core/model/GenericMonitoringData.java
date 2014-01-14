/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class GenericMonitoringData implements Serializable, MonitoringData {
    private static final long serialVersionUID = -8043002015065128548L;

    private Date timeStamp;
    private String content;
    private ContentType contentType;
    private String creatorId;

    public GenericMonitoringData(Date timeStamp, String content, ContentType contentType, String creatorId) {
        super();
        this.timeStamp = timeStamp;
        this.content = content;
        this.contentType = contentType;
        this.creatorId = creatorId;
    }

    public GenericMonitoringData() {
        super();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public Date getTimeStamp() {
        return timeStamp;
    }

    public static enum ContentType {
        HTML
    }
}
