package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.faktorips.devtools.htmlexport.pages.PageElement;

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

    public void layoutPageElement(PageElement pageElement) {
        layoutPageElement(pageElement, LayouterVisitingMode.COMPLETE);
    }

    public void layoutPageElement(PageElement pageElement, LayouterWrapperType wrapper) {
        layoutPageElement(pageElement, wrapper, LayouterVisitingMode.COMPLETE);
    }

    public void layoutPageElement(PageElement pageElement, LayouterVisitingMode mode) {
        layoutPageElement(pageElement, LayouterWrapperType.NONE, mode);
    }

    public abstract void layoutPageElement(PageElement pageElement, LayouterWrapperType wrapper, LayouterVisitingMode mode);

    public void reset() {
        builder = new StringBuilder();
    }

    protected void append(String value) {
        builder.append(value);
    }
}