package com.logreposit.logrepositapi.communication.messaging.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message
{
    private String          id;
    private Date            date;
    private String          type;
    private MessageMetaData metaData;
    private String          payload;

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getDate()
    {
        return this.date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public MessageMetaData getMetaData()
    {
        return this.metaData;
    }

    public void setMetaData(MessageMetaData metaData)
    {
        this.metaData = metaData;
    }

    public String getPayload()
    {
        return this.payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }
}
