package org.faktorips.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * A collection of util methods for Strings.
 *  
 * @author Jan Ortmann
 */
public class StringUtil
{
    public final static String CHARSET_UTF8 = "UTF-8";
    
    public final static String CHARSET_ISO_8859_1 = "ISO-8859-1";
    
    
    /**
     * Reads the available bytes from the input stream and returns them as a string
     * using the given {@link java.nio.charset.Charset </code>charset<code>}. The method does not close the stream.
     * 
     * @throws IOException
     */
    public final static String readFromInputStream(InputStream is, Charset charset) throws IOException {
        return readFromInputStream(is, charset.name());
    }
	
    /**
     * Reads the available bytes from the input stream and returns them as a string
     * using the given {@link java.nio.charset.Charset </code>charset<code>}.
     * <p> 
     * This method closes the input stream before returning!
     * 
     * @throws IOException
     */
    public final static String readFromInputStream(InputStream is, String charsetName) throws IOException {
        try {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            return new String(bytes, charsetName);
        } finally {
            is.close();
        }
    }
    
    /**
     * Returns the passed String enclosed in double quotes.
     */
    public static String quote(String s) {
        if (s==null)
        {
            return null;
        }
        return "\"" + s + "\"";
    }
    
    /**
     * Takes a name like a class name and removes the package information from the beginning.
     */
    public final static String unqualifiedName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index == -1)
        {
            return qualifiedName;
        }
        return qualifiedName.substring(index+1);
    }
    
    /**
     * Returns the package name for a given class name. Returns an empty String
     * if the class name does not contain a package name.
     * 
     * @throws NullPointerException if the qualifiedClassName is null.
     */
    public final static String getPackageName(String qualifiedClassName)
    {
        int index = qualifiedClassName.lastIndexOf(".");
        if (index == -1)
        {
            return "";
        }
        return qualifiedClassName.substring(0, index);
    }
    
	/**
	 * Returns the filename without extension.
	 */
	public static String getFilenameWithoutExtension(String filename) {
		int index = filename.lastIndexOf('.');
		if (index==-1)
		{
			return filename;
		}
		return filename.substring(0, index);
	}
	
	/**
	 * Returns the lines of the given text as array. Each array item represents
	 * one line. The lines don't contains the line separator.
	 */
    public final static String[] getLines(String text, String lineSeparator) {
        List lines = new ArrayList();
        int start = 0;
        int end = text.indexOf(lineSeparator);
        while (end>0) {
            lines.add(text.substring(start, end));
            start = end + lineSeparator.length();
            end = text.indexOf(lineSeparator, start);
        }
        lines.add(text.substring(start));
        return (String[])lines.toArray(new String[lines.size()]);
    }
    
    /**
     * Returns the line in the text that starts at the given position and
     * ends at the next line separator. The line separator itself is not returned
     * as part of the line. 
     */
    public final static String getLine(
            String text, 
            int startPos, 
            String lineSeparator) {
        int pos = text.indexOf(lineSeparator, startPos);
        if (pos==-1) {
            return text.substring(startPos);
        }
        return text.substring(startPos, pos);
    }
    
    /**
     * Returns the line separator provided by System.getProperty("line.separator").
     */
    public static String getSystemLineSeparator(){
        return System.getProperty("line.separator");
    }
    
}
