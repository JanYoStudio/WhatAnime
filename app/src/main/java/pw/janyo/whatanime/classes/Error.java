package pw.janyo.whatanime.classes;

import java.io.Serializable;

/**
 * Created by myste.
 */

public class Error implements Serializable
{
	public String time;
	public String appVersionName;
	public int appVersionCode;
	public String AndroidVersion;
	public int sdk;
	public String vendor;
	public String model;
	public Throwable throwable;

	public Error()
	{
	}

	public Error(String time, String appVersionName, int appVersionCode, String androidVersion, int sdk, String vendor, String model, Throwable throwable)
	{
		this.time = time;
		this.appVersionName = appVersionName;
		this.appVersionCode = appVersionCode;
		AndroidVersion = androidVersion;
		this.sdk = sdk;
		this.vendor = vendor;
		this.model = model;
		this.throwable = throwable;
	}
}
