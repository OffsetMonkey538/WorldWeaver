package org.betterx.wover.events.impl;

import org.betterx.wover.events.api.Event;
import org.betterx.wover.events.api.types.EventType;
import org.betterx.wover.util.api.SortedLinkedList;

import java.util.Objects;

public class AbstractEvent<T extends EventType> implements Event<T> {
    protected static class Subscriber<T extends EventType> {
        public final T task;
        private final int priority;

        public Subscriber(T task, int priority) {
            this.task = task;
            this.priority = priority;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Subscriber<?> that) {
                return Objects.equals(task, that.task);
            } else if (o != null) {
                return o.equals(task);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(task);
        }

        @Override
        public String toString() {
            return task.toString() + " - " + priority;
        }
    }

    protected final String eventName;
    protected final SortedLinkedList<Subscriber<T>> handlers;

    public AbstractEvent(String eventName) {
        this.eventName = eventName;
        this.handlers = new SortedLinkedList<>((a, b) -> b.priority - a.priority);
    }

    public final boolean subscribe(T handler) {
        return this.subscribe(handler, 1000);
    }

    public final boolean subscribe(T handler, int priority) {
        if (!handlers.contains(handler)) {
            handlers.add(new Subscriber<>(handler, priority));
            return true;
        }

        return false;
    }
}