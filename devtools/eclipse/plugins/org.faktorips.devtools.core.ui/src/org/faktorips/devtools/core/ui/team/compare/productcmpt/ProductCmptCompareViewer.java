/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.team.compare.CompareViewerContentProvider;
import org.faktorips.devtools.core.ui.team.compare.CompareViewerLineStyleListener;

/**
 * A viewer for comparing product components and templates. Displays the structural differences
 * between the product components in a tree. Displays detailed differences based on the text
 * representation of both product components similarly to the JavaMergeViewer.
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareViewer extends TextMergeViewer {

    /**
     * Creates a <code>ProductCmptCompareViewer</code> using the given parent as parent composite
     * and the given <code>CompareConfiguration</code>. A <code>CompareViewerContentProvider</code>
     * is used as a content provider.
     */
    protected ProductCmptCompareViewer(Composite parent, CompareConfiguration cc) {
        super(parent, cc);
        setContentProvider(new CompareViewerContentProvider(cc));

        /*
         * Sets the textfont to be used in this compare viewer. Cannot be called in
         * configureTextViewer() as the TextMergeViewer sets the font in its constructor, after
         * configureTextViewer() is called .
         */
        Font defaultFont = JFaceResources.getDefaultFont();
        FontData[] data = defaultFont.getFontData();
        for (FontData element : data) {
            element.setStyle(SWT.NORMAL);
            element.setHeight(10);
        }
        JFaceResources.getFontRegistry().put(getClass().getName(), data);
    }

    /**
     * Adds a <code>CompareViewerLineStyleListener</code> to the given <code>TextViewer</code> for
     * token highlighting. {@inheritDoc}
     */
    @Override
    protected void configureTextViewer(TextViewer textViewer) {
        CompareViewerLineStyleListener listener = new CompareViewerLineStyleListener();
        textViewer.getTextWidget().addLineStyleListener(listener);
    }

    @Override
    public String getTitle() {
        return Messages.ProductCmptCompareViewer_CompareViewer_title;
    }

}
