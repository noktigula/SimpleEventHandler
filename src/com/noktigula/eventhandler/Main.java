package com.noktigula.eventhandler;

import com.noktigula.eventhandler.handler.EventHandler;
import com.noktigula.eventhandler.handler.EventHandlerImpl;

/**
 * Created by noktigula on 10.02.16.
 */
public class Main
{
	public static void main(String[] args)
	{
		testSimple();
		testStop();
		testSize();
		testOverflow();
	}

	private static void testSimple() {
		System.out.println("Starting test1");
		EventHandler handler = new EventHandlerImpl();
		CountingCallback countingCallback = new CountingCallback();
		final int COUNT = 100;
		for (int i = 0; i < COUNT; ++i) {
			handler.post(new SimpleEvent(), countingCallback);
		}
		while (countingCallback.getTotalCount() != COUNT) {
			try
			{
				System.out.println("Work isn't done yet: "+countingCallback.getTotalCount()+", wait some time...");
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		handler.stop();
		try
		{
			handler.join();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("Success: " + countingCallback.getSuccesCount() + " fail: " + countingCallback.getFailureCount());
		System.out.println("Test 1 done");
	}

	private static void testStop() {
		System.out.println("Starting testStop");
		EventHandler handler = new EventHandlerImpl();
		CountingCallback countingCallback = new CountingCallback();
		for(int i = 0; i < 100; ++i) {
			handler.post(new SimpleEvent(), countingCallback);
		}

		handler.stop();

		try
		{
			handler.join();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		System.out.println("Was executed " + countingCallback.getTotalCount() + " before stop");
		System.out.println("Test stop done");
	}

	private static void testSize() {
		System.out.println("Starting testSize");
		try {
			System.out.println("Creating handler with 0 count and 0 capacity");
			EventHandler handler = new EventHandlerImpl(0, 0);
		} catch (RuntimeException err) {
			System.out.println("It's impossible, catch exception");
		}

		try {
			System.out.println("Creating handler with 0 count");
			EventHandler handler = new EventHandlerImpl(0, 100500);
		} catch (RuntimeException err) {
			System.out.println("This is impossible too");
		}

		try {
			System.out.println("Creating handler with 0 capacity");
			EventHandler handler = new EventHandlerImpl(100500, 0);
		} catch (RuntimeException err) {
			System.out.println("This is impossible too");
		}

		try {
			System.out.println("Creating handler with 1 count and 1 capacity");
			EventHandler handler = new EventHandlerImpl(1, 1);
			System.out.println("Created successfully");
			CountingCallback countingCallback = new CountingCallback();
			for (int i = 0; i < 5; ++i) {
				handler.post(new SimpleEvent(), countingCallback);
			}
			handler.stop();
			handler.join();
			System.out.println("Was processed: " + countingCallback.getTotalCount() + " events");
		} catch (InterruptedException e)
		{
			System.out.println("Ooops, we've been interrupted");
			e.printStackTrace();
		} catch (RuntimeException err) {
			System.out.println("Oops, this shouldn't been happened");
		}

		System.out.println("Test size done");
	}

	private static void testOverflow() {
		System.out.println("Starting testOverflow");
		EventHandler handler = new EventHandlerImpl();
		CountingCallback callback = new CountingCallback();
		final int COUNT = 200;
		for (int i = 0; i < COUNT; ++i) {
			handler.post(new SimpleEvent(), callback);
		}
		handler.stop();
		try
		{
			handler.join();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("Was processed  " + callback.getTotalCount() + " from " + COUNT + ", other was skipped");
		System.out.println("Test overflow done");
	}

	private static class CountingCallback implements EventHandler.EventCallback {
		private volatile int mSucces;
		private volatile int mFailure;

		public int getSuccesCount() {
			return mSucces;
		}

		public int getFailureCount() {
			return mFailure;
		}

		public synchronized int getTotalCount() {
			return mSucces + mFailure;
		}

		@Override
		public void onSuccess()
		{
			++mSucces;
		}

		@Override
		public void onFail()
		{
			++mFailure;
		}
	}
}
