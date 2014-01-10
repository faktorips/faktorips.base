/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * @author dicker
 * 
 */
public abstract class AbstractPageElement implements IPageElement {

    protected Set<Style> styles = new LinkedHashSet<Style>();

    private String id;
    private String anchor;

    public AbstractPageElement() {
        super();
    }

    public AbstractPageElement(Set<Style> styles) {
        super();
        this.styles = styles;
    }

    @Override
    public Set<Style> getStyles() {
        if (styles == null) {
            return Collections.emptySet();
        }
        return new HashSet<Style>(styles);
    }

    @Override
    public IPageElement addStyles(Style... style) {
        styles.addAll(Arrays.asList(style));
        return this;
    }

    @Override
    public void removeStyles(Style... style) {
        styles.removeAll(Arrays.asList(style));
    }

    @Override
    public boolean hasStyle(Style style) {
        return getStyles().contains(style);
    }

    @Override
    public void build() {
        // override in subclass
    }

    @Override
    public abstract void acceptLayouter(ILayouter layouter);

    @Override
    public void makeBlock() {
        addStyles(Style.BLOCK);
    }

    @Override
    public String getId() {
        if (id == null) {
            createId();
        }
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * creates the Id of a page e.g. for internal links
     * 
     */
    protected void createId() {
        // could be overridden
    }

    public boolean hasId() {
        return StringUtils.isNotBlank(getId());
    }

    @Override
    public String getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
}
