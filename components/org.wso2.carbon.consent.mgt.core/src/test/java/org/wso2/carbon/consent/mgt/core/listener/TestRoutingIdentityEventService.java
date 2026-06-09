/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.consent.mgt.core.listener;

import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.event.services.IdentityEventService;

import java.util.Arrays;
import java.util.List;

/**
 * Minimal {@link IdentityEventService} for tests that dispatches each event synchronously to the supplied handlers,
 * mirroring how the real event framework invokes registered {@link AbstractEventHandler}s. Synchronous dispatch is
 * what lets a handler mutate event properties (such as {@code CONSENT_STATUS}) and have the publisher read them back.
 */
public class TestRoutingIdentityEventService implements IdentityEventService {

    private final List<AbstractEventHandler> handlers;

    public TestRoutingIdentityEventService(AbstractEventHandler... handlers) {

        this.handlers = Arrays.asList(handlers);
    }

    @Override
    public void handleEvent(Event event) throws IdentityEventException {

        for (AbstractEventHandler handler : handlers) {
            handler.handleEvent(event);
        }
    }
}
