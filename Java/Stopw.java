/**
 * Stopw.java
 *  written by blanclux
 *  This software is distributed on an "AS IS" basis WITHOUT WARRANTY OF ANY KIND.
 */
package Blanclux.util;

import java.lang.reflect.Method;

public class Stopw {
	private static int mode; // Timer 0: milli, 1: nano
	private static long MSEC_CONV;

	private double[] sTime; // start time
	private double[] eTime; // end time
	private double[] aTime; // accumulated time

    static {
        if (checkNanoTimeMethod() != null) {
        	mode = 1;
        	MSEC_CONV = 1000 * 1000;
        } else {
        	mode = 0;
        	MSEC_CONV = 1;
        }
    }
	public Stopw() {
		sTime = new double[1];
		aTime = new double[1];
		eTime = new double[1];
	}

	public Stopw(int num) {
		sTime = new double[num];
		aTime = new double[num];
		eTime = new double[num];
	}

	public void reset() {
		sTime[0] = 0;
		aTime[0] = 0;
		eTime[0] = 0;
	}

	public void reset(int index) {
		sTime[index] = 0;
		aTime[index] = 0;
		eTime[index] = 0;
	}

	public void setMode(int flag) {
		mode = flag;
	}

	private double getCurTime() {
		double ret;

		if (mode == 0) {
			ret = System.currentTimeMillis();
		} else {
			ret = System.nanoTime();
		}
		return ret;
	}

	public void start() {
		sTime[0] = getCurTime();
		while (sTime[0] == getCurTime()) {
		}
		sTime[0] = getCurTime();
	}

	public void stop() {
		eTime[0] = getCurTime();
		aTime[0] += eTime[0] - sTime[0];
	}

	public void start(int index) {
		sTime[index] = getCurTime();
		while (sTime[index] == getCurTime()) {
		}
		sTime[index] = getCurTime();
	}

	public void stop(int index) {
		eTime[index] = getCurTime();
		aTime[index] += eTime[index] - sTime[index];
	}

	public double getTime() {
		return aTime[0] / MSEC_CONV;
	}

	public double getTime(int index) {
		return aTime[index] /MSEC_CONV;
	}

	/**
	 * Average Time : msec / count
	 */
	public double getAverage(long count) {
		double t = aTime[0] / MSEC_CONV;
		return t / count;
	}

	public double getAverage(int index, long count) {
		double t = aTime[index] / MSEC_CONV;
		return t / count;
	}

	/**
	 * Rate : count / sec
	 */
	public double getRate(long count) {
		double t = aTime[0] / MSEC_CONV;
		return (1000 * count) / t;
	}

	public double getRate(int index, long count) {
		double t = aTime[index] / MSEC_CONV;
		return (1000 * count) / t;
	}

	private static final Method checkNanoTimeMethod() {
		final String className = "java.lang.System";
		final String methodName = "nanoTime";
		final Class<?>[] methodArgsTypes = new Class[] {};
		final Object[] NULL_ARGS = new Object[] {};

		try {
			/* get nanoTime */
			final Class<?> clazz = Class.forName(className);
			final Method method = clazz.getMethod(methodName, methodArgsTypes);

			final Object result = method.invoke(null, NULL_ARGS);
			if (result == null) {
				return null;
			}
			return method;
		} catch (Exception e) {
			return null;
		}
	}

}
