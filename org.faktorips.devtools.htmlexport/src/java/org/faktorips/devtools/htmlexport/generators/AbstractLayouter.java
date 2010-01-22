package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractLayouter implements ILayouter {

	protected StringBuilder builder = new StringBuilder();
	public String charset = "UTF-8";
	private Set<LayoutResource> layoutResources = new HashSet<LayoutResource>();;

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

	public Set<LayoutResource> getLayoutResources() {
		return layoutResources;
	}

	protected void addLayoutResource(LayoutResource layoutResource) {
		layoutResources.add(layoutResource);
	}
}