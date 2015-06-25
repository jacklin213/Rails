/*
 * This file is a component of Powered Rails, this license makes sure any work
 * associated with Powered Rails, must follow the conditions of the license
 * included.
 * 
 * Copyright 2015 PoweredRails
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.poweredrails.impl.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.poweredrails.railsapi.event.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RegisteredListener<T extends Listener> {

	private T listener;
	private Map<Class<? extends Event>, List<Method>> eventMap;

	@SuppressWarnings("unchecked")
	public RegisteredListener(T listener) {
		this.listener = listener;
		this.eventMap = Maps.newHashMap();

		Class<? extends Listener> clazz = listener.getClass();
		for (Method method : clazz.getMethods()) {

			if (!method.isAnnotationPresent(Subscribe.class))
				continue;

			Class<?>[] params = method.getParameterTypes();
			if (params.length != 1)
				continue;

			Class<?> param = params[0];
			if (!Event.class.isAssignableFrom(param))
				continue;

			Class<? extends Event> eventClass = (Class<? extends Event>) param;
			List<Method> methods = eventMap.get(eventClass);
			if (methods == null) {
				methods = Lists.newArrayList();
				eventMap.put(eventClass, methods);
			}
			methods.add(method);
		}

	}

	public <E extends Event> void invoke(E evt) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<Method> methods = eventMap.get(evt.getClass());
		if (methods == null)
			return;
		for (Method method : methods)
			method.invoke(listener, evt);
	}

	public Listener getListener() {
		return listener;
	}

}
