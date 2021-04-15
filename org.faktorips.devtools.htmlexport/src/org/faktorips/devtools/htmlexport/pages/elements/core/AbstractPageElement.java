/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.model.plugin.IpsStatus;

/**
 * @author dicker
 * 
 */
public abstract class AbstractPageElement implements IPageElement {

    private final DocumentationContext context;
    private Set<Style> styles = new LinkedHashSet<>();
    private String id;
    private String anchor;

    public AbstractPageElement(DocumentationContext context) {
        this.context = context;
    }

    public AbstractPageElement(Set<Style> styles, DocumentationContext context) {
        super();
        this.styles = styles;
        this.context = context;
    }

    public DocumentationContext getContext() {
        return context;
    }

    @Override
    public Set<Style> getStyles() {
        return styles;
    }

    public Set<Style> getStylesCopy() {
        if (styles == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(styles);
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
        return styles.contains(style);
    }

    @Override
    public void build() {
        try {
            buildInternal();
            // CSOFF: IllegalCatch
        } catch (RuntimeException e) {
            IpsStatus ipsStatus = new IpsStatus(IStatus.ERROR, "A problem occured while procesing an object", e); //$NON-NLS-1$
            context.addStatus(ipsStatus);
        }
        // CSON: IllegalCatch
    }

    protected void buildInternal() {
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
