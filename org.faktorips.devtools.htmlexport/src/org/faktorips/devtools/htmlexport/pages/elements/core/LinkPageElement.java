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

import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.ILayouter;
import org.faktorips.devtools.htmlexport.helper.path.TargetType;

/**
 * {@link IPageElement} representing a link
 * 
 * @author dicker
 * 
 */
public class LinkPageElement extends AbstractCompositePageElement {

    /**
     * the link target for e.g. the frame, where the linked file should be loaded
     */
    private TargetType target;
    /**
     * the path of the link
     */
    private String path;

    private String linkAnchor;

    public LinkPageElement(String path, TargetType target, DocumentationContext context, IPageElement... pageElements) {
        this(path, target, context);
        addPageElements(pageElements);
    }

    public LinkPageElement(String path, TargetType target, String text, DocumentationContext context) {
        this(path, target, context, new TextPageElement(text, context));
    }

    private LinkPageElement(String path, TargetType target, DocumentationContext context) {
        super(context);
        this.path = path;
        this.target = target;
    }

    /**
     * @return the target
     */
    public TargetType getTarget() {
        return target;
    }

    /**
     * @return the pathFromRoot
     */
    public String getPathFromRoot() {
        return path;

    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutLinkPageElement(this);
    }

    @Override
    protected void buildInternal() {
        // could be overridden
    }

    /**
     * sets the target
     * 
     */
    public void setTarget(TargetType target) {
        this.target = target;
    }

    public String getLinkAnchor() {
        return linkAnchor;
    }

    public void setLinkAnchor(String linkAnchor) {
        this.linkAnchor = linkAnchor;
    }
}
