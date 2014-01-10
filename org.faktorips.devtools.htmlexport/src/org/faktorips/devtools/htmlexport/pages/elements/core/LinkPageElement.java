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

    public LinkPageElement(String path, TargetType target, IPageElement... pageElements) {
        this(path, target);
        addPageElements(pageElements);
    }

    public LinkPageElement(String path, TargetType target, String text) {
        this(path, target, new TextPageElement(text));
    }

    private LinkPageElement(String path, TargetType target) {
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
    public void build() {
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
