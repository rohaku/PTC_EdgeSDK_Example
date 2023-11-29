package com.thingworx.sdk.steam;

import com.thingworx.communications.client.IPasswordCallback;

/**
 * Sample password callback to retrieve an app key from a specified environment variable.
 * 
 * Provided for demonstration purposes only. 
 * 
 * Not suitable for production environments.
 *
 */
public class SamplePasswordCallback implements IPasswordCallback {

	private String appKey = null;

	public SamplePasswordCallback(String appKey) {
		this.appKey = appKey;
	}

	@Override
	public char[] getSecret() {
		
		return appKey.toCharArray();
	}
}
