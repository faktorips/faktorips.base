package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public abstract class AbstractLayouter implements ILayouter {

    protected StringBuilder builder = new StringBuilder();
    public String charset = "UTF-8";

    public AbstractLayouter() {
        super();
    }

    public byte[] generate() {
        if (Charset.isSupported(charset))
            try {
                return builder.toString().getBytes(charset);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        return builder.toString().getBytes();
    }

    public void clean() {
        builder = new StringBuilder();
    }

    protected void append(String value) {
        builder.append(value);
    }
}