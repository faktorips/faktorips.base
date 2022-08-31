/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.IPageElement;

/**
 * Base class of {@link IPageElementLayouter}
 * 
 * @author dicker
 */
public abstract class AbstractPageElementLayouter<T extends IPageElement> implements IPageElementLayouter<T> {
    protected final T pageElement;

    protected AbstractPageElementLayouter(T pageElement) {
        this.pageElement = pageElement;
    }

    @Override
    public final void layout() {
        if (IpsStringUtils.isNotBlank(pageElement.getAnchor())) {
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
