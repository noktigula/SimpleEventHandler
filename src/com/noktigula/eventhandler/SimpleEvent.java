package com.noktigula.eventhandler;

/**
 * Created by noktigula on 10.02.16.
 */
public class SimpleEvent implements Runnable {
	private static int sId;
	public final int mId = sId++;

	@Override
	public void run()
	{
		System.out.println("Executing event: " + mId);
		int seconds = 2;
		try
		{
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("Event " + mId + " processed!");
	}
}
