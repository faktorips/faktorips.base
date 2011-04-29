/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractLayouter extends AbstractTextGenerator implements ILayouter {

    /**
     * content of a page
     */
    private StringBuilder content = new StringBuilder();

    private Set<LayoutResource> layoutResources = new HashSet<LayoutResource>();

    public AbstractLayouter() {
        super();
    }

    @Override
    public String generateText() {
        return content.toString().trim();
    }

    @Override
    public void clear() {
        content = new StringBuilder();
    }

    /**
     * adds text to the content.
     */
    public void append(String value) {
        content.append(value);
    }

    @Override
    public Set<LayoutResource> getLayoutResources() {
        return layoutResources;
    }

    public void addLayoutResource(LayoutResource layoutResource) {
        layoutResources.add(layoutResource);
    }
}
