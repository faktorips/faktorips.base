/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractLayouter implements ILayouter {

    /*
     * content of a page
     */
    protected StringBuilder content = new StringBuilder();

    /*
     * Set of LayoutResources like Images, Stylesheets
     */
    private Set<LayoutResource> layoutResources = new HashSet<LayoutResource>();

    public AbstractLayouter() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.generators.IGenerator#generate()
     */
    @Override
    public byte[] generate() {
        if (Charset.isSupported(CHARSET)) {
            try {
                return content.toString().getBytes(CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return content.toString().getBytes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.faktorips.devtools.htmlexport.generators.ILayouter#clear()
     */
    @Override
    public void clear() {
        content = new StringBuilder();
    }

    /**
     * appends String to the content
     * 
     * @param value
     */
    protected void append(String value) {
        content.append(value);
    }

    @Override
    public Set<LayoutResource> getLayoutResources() {
        return layoutResources;
    }

    protected void addLayoutResource(LayoutResource layoutResource) {
        layoutResources.add(layoutResource);
    }
}
