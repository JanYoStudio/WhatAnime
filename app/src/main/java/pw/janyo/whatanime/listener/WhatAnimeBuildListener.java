package pw.janyo.whatanime.listener;

import pw.janyo.whatanime.classes.Animation;

/**
 * Created by myste.
 */

public interface WhatAnimeBuildListener
{
	void done();

	void error(Throwable e);
}
