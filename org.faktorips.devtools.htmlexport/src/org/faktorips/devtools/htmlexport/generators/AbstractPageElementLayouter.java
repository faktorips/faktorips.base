/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;

public abstract class AbstractPageElementLayouter<T extends IPageElement> implements IPageElementLayouter<T> {
    protected final T pageElement;

    protected AbstractPageElementLayouter(T pageElement) {
        this.pageElement = pageElement;
    }

    @Override
    public final void layout() {
        if (StringUtils.isNotBlank(pageElement.getAnchor())) {
            setAnchor();
        }
        layoutInternal();
    }

    protected abstract void setAnchor();

    protected abstract void layoutInternal();

    protected T getPageElement() {
        return pageElement;
    }
}
