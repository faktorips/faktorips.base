/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * Content provider for <code>ProductCmptCompareViewer</code>. Returns images for
 * <code>ProductCmptCompareItem</code>s by querying the contained <code>IIpsElement</code>. The
 * getXXXContent() methods simply return the left, right respectively ancestor input object
 * referenced by the given input.
 * 
 * @author Stefan Widmaier
 */
public class CompareViewerContentProvider implements IMergeViewerContentProvider {

    private CompareConfiguration config;

    public CompareViewerContentProvider(CompareConfiguration cc) {
        config = cc;
    }

    @Override
    public String getAncestorLabel(Object input) {
        return config.getAncestorLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the ancestor-
     * <code>ProductCmptCompareItem</code>. {@inheritDoc}
     */
    @Override
    public Image getAncestorImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getAncestor();
            if (el instanceof AbstractCompareItem) {
                return ((AbstractCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the ancestor-<code>ProductCmptCompareItem</code> itself. The
     * <code>TextMergeViewer</code> can use <code>ProductCmptCompareItem</code>s as
     * <code>IDocumentRange</code>s. {@inheritDoc}
     */
    @Override
    public Object getAncestorContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getAncestor();
            if (el instanceof AbstractCompareItem) {
                return el;
            }
        }
        return null;
    }

    @Override
    public boolean showAncestor(Object input) {
        return false;
    }

    @Override
    public String getLeftLabel(Object input) {
        return config.getLeftLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the left
     * <code>ProductCmptCompareItem</code>. {@inheritDoc}
     */
    @Override
    public Image getLeftImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getLeft();
            if (el instanceof AbstractCompareItem) {
                return ((AbstractCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the left <code>ProductCmptCompareItem</code> itself. The <code>TextMergeViewer</code>
     * can use <code>ProductCmptCompareItem</code>s as <code>IDocumentRange</code>s. {@inheritDoc}
     */
    @Override
    public Object getLeftContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getLeft();
            if (el instanceof AbstractCompareItem) {
                return el;
            }
        }
        return null;
    }

    @Override
    public boolean isLeftEditable(Object input) {
        return false;
    }

    @Override
    public void saveLeftContent(Object input, byte[] bytes) {
        // Empty implementation, nothing to save
    }

    @Override
    public String getRightLabel(Object input) {
        return config.getRightLabel(input);
    }

    /**
     * Returns the image of the <code>IIpsElement</code> that is referenced by the right
     * <code>ProductCmptCompareItem</code>. {@inheritDoc}
     */
    @Override
    public Image getRightImage(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getRight();
            if (el instanceof AbstractCompareItem) {
                return ((AbstractCompareItem)el).getImage();
            }
        }
        return null;
    }

    /**
     * Returns the right <code>ProductCmptCompareItem</code> itself. The
     * <code>TextMergeViewer</code> can use <code>ProductCmptCompareItem</code>s as
     * <code>IDocumentRange</code>s. {@inheritDoc}
     */
    @Override
    public Object getRightContent(Object input) {
        if (input instanceof ICompareInput) {
            ITypedElement el = ((ICompareInput)input).getRight();
            if (el instanceof AbstractCompareItem) {
                return el;
            }
        }
        return null;
    }

    @Override
    public boolean isRightEditable(Object input) {
        return false;
    }

    @Override
    public void saveRightContent(Object input, byte[] bytes) {
        // Empty implementation, nothing to save
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }

}
