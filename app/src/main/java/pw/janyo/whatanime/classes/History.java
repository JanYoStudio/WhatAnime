package pw.janyo.whatanime.classes;

import org.litepal.crud.DataSupport;

/**
 * Created by myste.
 */

public class History extends DataSupport
{
	private String imaPath;
	private String title;
	private String saveFilePath;

	public String getImaPath()
	{
		return imaPath;
	}

	public void setImaPath(String imaPath)
	{
		this.imaPath = imaPath;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getSaveFilePath()
	{
		return saveFilePath;
	}

	public void setSaveFilePath(String saveFilePath)
	{
		this.saveFilePath = saveFilePath;
	}
}
