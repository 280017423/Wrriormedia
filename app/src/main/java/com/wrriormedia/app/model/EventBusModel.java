package com.wrriormedia.app.model;

import java.io.Serializable;

/**
 * EventBus传递数据的model
 *
 * @author zou.sq
 */
public class EventBusModel implements Serializable {

    private int eventId;
    private String eventBusAction;
    private Object eventBusObject;

    public EventBusModel() {
    }

    public EventBusModel(String eventBusAction, Object eventBusObject) {
        this.eventBusAction = eventBusAction;
        this.eventBusObject = eventBusObject;
    }

    public EventBusModel(String eventBusAction, Object eventBusObject, int eventId) {
        this.eventBusAction = eventBusAction;
        this.eventBusObject = eventBusObject;
        this.eventId = eventId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventBusAction() {
        return eventBusAction;
    }

    public void setEventBusAction(String eventBusAction) {
        this.eventBusAction = eventBusAction;
    }

    public Object getEventBusObject() {
        return eventBusObject;
    }

    public void setEventBusObject(Object eventBusObject) {
        this.eventBusObject = eventBusObject;
    }
}
