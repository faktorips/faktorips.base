package org.faktorips.devtools.core.util;

import java.io.File;

import org.apache.commons.cli.CommandLine;


public class CommandLineHelper {

	public final static File getFileOption(CommandLine line, char option)
	{
	    String file = line.getOptionValue(option);
	    if (file == null)
	    {
	        return null;
	    }
	    return new File(file.trim());
	}

	public final static String getStringOption(CommandLine line, char option)
	{
	    String value = line.getOptionValue(option);
	    if (value == null)
	    {
	        return null;
	    }
	    return value.trim();
	}

}
