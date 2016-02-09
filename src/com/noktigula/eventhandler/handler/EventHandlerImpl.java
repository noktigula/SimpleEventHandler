package com.noktigula.eventhandler.handler;

import com.noktigula.eventhandler.SimpleEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ThreadPool pattern implementation for Eyeo interview.
 * This class handle incoming events (which are Runnables), put them into queue and handles when handler be available.
 * If event count is bigger then capacity, then new events are skipped until there will be free space.
 */
public class EventHandlerImpl implements EventHandler {
	private static final int DEFAULT_EXECUTOR_COUNT = 5;
	private static final int DEFAULT_EVENT_QUEUE_CAPACITY = 100;

	private BlockingQueue<EventWrapper> mWorkQueue;
	private WorkerThread[] mThreadPool;
	private boolean mStopped = false;

	public EventHandlerImpl() {
		this( DEFAULT_EXECUTOR_COUNT, DEFAULT_EVENT_QUEUE_CAPACITY );
	}

	public EventHandlerImpl(int threadCount, int eventHolderCapacity) {
		if (threadCount < 1) {
			throw new IllegalArgumentException( "Unexpected value for threadCount: " + threadCount );
		}

		if (eventHolderCapacity < 1) {
			throw new IllegalArgumentException( "Unexpected value for eventCapacity: " + eventHolderCapacity );
		}

		mWorkQueue = new LinkedBlockingQueue<>( eventHolderCapacity );
		mThreadPool = new WorkerThread[threadCount];
		for (int i = 0; i < threadCount; ++i) {
			mThreadPool[i] = new WorkerThread();
			mThreadPool[i].start();
		}
	}

	@Override
	public void post( SimpleEvent event ) {
		if (mStopped) {
			throw new IllegalStateException( "Attempt to post event into already stopped EventHandler" );
		}
		System.out.println("Posting event: " + event.mId);
		boolean result = mWorkQueue.offer( new EventWrapper( event, null ) );
		if (!result) {
			System.out.println("Can't handle event: too much events");
		}
	}

	@Override
	public void post( SimpleEvent event, EventCallback callback ) {
		if (mStopped) {
			throw new IllegalStateException( "Attempt to post event into already stopped EventHandler" );
		}
		System.out.println("Posting event: " + event.mId);
		boolean result = mWorkQueue.offer( new EventWrapper( event, callback ) );
		if (!result) {
			System.out.println("Can't handle event: too much events");
		}
	}

	@Override
	public void stop() {
		if (mStopped) {
			return;
		}
		for(WorkerThread thread : mThreadPool) {
			thread.setInterrupted();
		}
		mStopped = true;
	}

	@Override
	public void join() throws InterruptedException
	{
		if (!mStopped) {
			throw new IllegalStateException("Try to join EventHandler which hasn't been stopped");
		}

		for (Thread thread : mThreadPool) {
			if (thread.isAlive()) {
				thread.join();
			}
		}
	}

	private class WorkerThread extends Thread {
		private volatile boolean mInterrupted = false;

		@Override
		public void run() {
			while (!mInterrupted) {
				EventWrapper eventWrapper = null;
				try
				{
					eventWrapper = mWorkQueue.take();
				} catch (InterruptedException e)
				{
					e.printStackTrace();
					System.out.println("Interrupted while waiting for new event");
				}

				if (eventWrapper == null) {
					continue;
				}

				try {
					eventWrapper.mEvent.run();
					if (eventWrapper.mCallback != null) {
						eventWrapper.mCallback.onSuccess();
					}
				} catch( RuntimeException err ) {
					err.printStackTrace();
					if (eventWrapper.mCallback != null) {
						eventWrapper.mCallback.onFail();
					}
				}
			}
		}

		public void setInterrupted() {
			mInterrupted = true;
		}
	}

	private static class EventWrapper {
		public final Runnable mEvent;
		public final EventCallback mCallback;

		public EventWrapper( Runnable event, EventCallback callback ) {
			mEvent = event;
			mCallback = callback;
		}
	}
}
