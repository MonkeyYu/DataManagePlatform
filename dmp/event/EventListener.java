/*
 * Copyright (c) 2016. Guangdong Etone Technology Co.,Ltd.
 * All rights reserved.
 */
package com.etone.universe.dmp.event;

import com.google.common.eventbus.Subscribe;

/**
 * 一句简洁的说明
 *
 * @author <a href="mailto:maxid@qq.com">ZengHan</a>
 * @since $$Id$$
 */
public interface EventListener {
    @Subscribe
    void saveLog(SqlEvent event);

    @Subscribe
    void saveLog(FtpEvent event);

    @Subscribe
    void saveLog(ExportEvent event);

    @Subscribe
    void saveLog(ChmodEvent event);
    
    @Subscribe
    void saveLog(ProblemEvent event);

    @Subscribe
    void updateStatus(BaseEvent event);
}
