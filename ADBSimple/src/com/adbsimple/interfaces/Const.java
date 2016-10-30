package com.adbsimple.interfaces;

import java.io.File;

public interface Const {
	String ADB_PATH = System.getenv("ANDROID_HOME") + File.separator
			+ "platform-tools" + File.separator;
	String NO_DEVICE = "No Device Selected";

	String SELECT_DEVICE = "Please select device";

	String RECTANGLE = "rectangle";
	String LINE = "line";
	String TEXT = "text";
}
