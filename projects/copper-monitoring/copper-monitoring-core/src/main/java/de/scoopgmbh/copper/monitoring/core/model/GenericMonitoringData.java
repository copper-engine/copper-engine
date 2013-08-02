package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class GenericMonitoringData implements Serializable, MonitoringData{
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
	public static enum ContentType{
		HTML
	}
}
