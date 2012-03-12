/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.htmlexport.helper;

import java.util.Set;

import junit.framework.Assert;

import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.generators.LayoutResource;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractCompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.AbstractRootPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ICompositePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ImagePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.ListPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;

public abstract class AbstractTestLayouter extends Assert implements ILayouter {

    @Override
    public void clear() {
        // default implementation

    }

    @Override
    public Set<LayoutResource> getLayoutResources() {
        // default implementation
        return null;
    }

    @Override
    public void layoutImagePageElement(ImagePageElement pageElement) {
        // default implementation

    }

    @Override
    public void layoutLinkPageElement(LinkPageElement pageElement) {
        visitSubElements(pageElement);
    }

    @Override
    public void layoutListPageElement(ListPageElement pageElement) {
        visitSubElements(pageElement);
    }

    @Override
    public void layoutRootPageElement(AbstractRootPageElement pageElement) {
        visitSubElements(pageElement);
    }

    @Override
    public void layoutTablePageElement(TablePageElement pageElement) {
        visitSubElements(pageElement);
    }

    @Override
    public void layoutTextPageElement(TextPageElement pageElement) {
        // default implementation
    }

    @Override
    public void layoutWrapperPageElement(AbstractCompositePageElement pageElement) {
        visitSubElements(pageElement);
    }

    @Override
    public byte[] generate() {
        // default implementation
        return null;
    }

    protected void visitSubElements(ICompositePageElement compositePageElement) {
        compositePageElement.visitSubElements(this);
    }

    public abstract void assertTest();
}
