package com.frontend.model;

import java.util.Date;

public class AdvisorRequest {
    private String requestId;
    private String advisorName;
    private Date requestDate;
    private String status;

    public AdvisorRequest(String requestId, String advisorName, Date requestDate, String status) {
        this.requestId = requestId;
        this.advisorName = advisorName;
        this.requestDate = requestDate;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public String getAdvisorName() { return advisorName; }
    public Date getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
}
