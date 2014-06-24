/**
 * 
 */
package com.belatrixsf.tishadow.runner;

/**
 *	Interface to allow to RunnerTishadow notify to classes that implement this interface
 *	about running has finished.
 */
public interface IRunnerCallback {
	/** Callback method*/
	public void onRunnerTishadowFinish(Object response);
}
