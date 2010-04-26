/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.team.compare.CompareViewerContentProvider;
import org.faktorips.devtools.core.ui.team.compare.CompareViewerLineStyleListener;

/**
 * A viewer for comparing productcomponents. Displays the structural differences between the
 * productcomponents in a tree. Displays detailed differences based on the text representation of
 * both productcomponents similarly to the JavaMergeViewer.
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareViewer extends TextMergeViewer {

    /**
     * Creates a <code>ProductCmptCompareViewer</code> using the given parent as parent composite
     * and the given <code>CompareConfiguration</code>. A <code>CompareViewerContentProvider</code>
     * is used as a content provider.
     * 
     * @param parent
     * @param cc
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
        CompareViewerLineStyleListener listener = new CompareViewerLineStyleListener((SourceViewer)textViewer);
        textViewer.getTextWidget().addLineStyleListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Messages.ProductCmptCompareViewer_CompareViewer_title;
    }
}
