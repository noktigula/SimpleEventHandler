package com.noktigula.eventhandler.handler;

import com.noktigula.eventhandler.SimpleEvent;

/**
 * Created by noktigula on 09.02.16.
 */
public interface EventHandler {
	/***
	 * Post some event. Event will be processed when event processor will be available.
	 * This method is non-blocking (returns immediately).
	 * @param r - event to be processed.
	 */
	void post(SimpleEvent r);
	/**
	 * Post some event. Event will be processed when event processor will be available, and callback will be called.
	 * This method is non-blocking (returns immediately).
	 * @param r event to be processed.
	 * @param callback Callback which will be called after event processing.
	 */
	void post(SimpleEvent r, EventCallback callback);
	/**
	 * Send a stop signal to event processor. Events in queue wouldn't be processed. Processing of current events will be finished.
	 * This method is non-blocking (returns immediately).
	 */
	void stop();

	/**
	 * Make caller thread wait until all processors finish their work.
	 * This method is blocking.
	 */
	void join() throws InterruptedException;

	interface EventCallback {
		void onSuccess();
		void onFail();
	}
}
