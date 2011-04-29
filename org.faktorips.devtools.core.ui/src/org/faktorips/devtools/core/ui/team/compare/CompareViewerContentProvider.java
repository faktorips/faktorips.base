/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

/**
 * Contentprovider for <code>ProductCmptCompareViewer</code>. Returns images for
 * <code>ProductCmptCompareItem</code>s by quering the contained <code>IIpsElement</code>. The
 * getXXXContent() methods simply return the left, right respectiveley ancestor input object
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
