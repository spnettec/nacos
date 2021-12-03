/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.istio.common;

import com.alibaba.nacos.istio.misc.Loggers;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author special.fy
 */
@Component
public class EventProcessor {


    public BlockingQueue<Event> getEvents() {
        return events;
    }

    private final BlockingQueue<Event> events;

    public EventProcessor() {
        events = new ArrayBlockingQueue<>(20);
    }

    public void notify(Event event) {
        try {
            events.put(event);
        } catch (InterruptedException e) {
            Loggers.MAIN.warn("There are too many events, this event {} will be ignored.", event.getType());
        }
    }


}
