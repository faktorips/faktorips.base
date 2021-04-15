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

import java.util.HashSet;
import java.util.Set;

/**
 * Base class of {@link ILayouter}s
 * 
 * @author dicker
 */
public abstract class AbstractLayouter extends AbstractTextGenerator implements ILayouter {

    /**
     * content of a page
     */
    private StringBuilder content = new StringBuilder();

    private Set<LayoutResource> layoutResources = new HashSet<>();

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
