/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.helper;

import java.util.Set;

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
import org.junit.Assert;

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
