package com.netty.event;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class Event extends ApplicationEvent {

	public Event(Object source) {
		super(source);
	}

}