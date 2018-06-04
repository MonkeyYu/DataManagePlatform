/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 事件服务，单例
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
public class EventService {
    // members
    private final  com.google.common.eventbus.AsyncEventBus bus;
    private static EventService                        instance;
    private final AtomicBoolean eventActive = new AtomicBoolean(false);
    private final AtomicBoolean logActive   = new AtomicBoolean(true);

    // static block
    public enum Type {
        AUDIT, LOG;
    }

    // constructors
    private EventService() {
        super();
        bus = new com.google.common.eventbus.AsyncEventBus("asyncEventBus", Executors.newCachedThreadPool());
    }

    public static synchronized EventService getInstance() {
        if (instance == null) {
            instance = new EventService();
        }
        return instance;
    }
    // properties


    public AtomicBoolean getEventActive() {
        return eventActive;
    }

    public AtomicBoolean getLogActive() {
        return logActive;
    }

    // public methods
    public void register(Object listener) {
        bus.register(listener);
    }

    public void unregister(Object listener) {
        bus.unregister(listener);
    }

    public void post(Object event) {
        bus.post(event);
    }
    // protected methods

    // friendly methods

    // private methods

    // inner class

    // test main
}
