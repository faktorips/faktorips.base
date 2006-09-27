/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

/**
 * A viewer for comparing productcomponents. Displays the structural differences between the productcomponents in a tree.
 * Displays detailed differences based on the text representation of both productcomponents similarly to the JavaMergeViewer.
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareViewer extends TextMergeViewer {
    
    /**
     * Creates a ProductCmptCompareViewer using a ProductCmptCompareContentProvider as a 
     * content provider.
     * @param parent
     * @param cc
     */
    protected ProductCmptCompareViewer(Composite parent, CompareConfiguration cc) {
        super(parent, cc);
        setContentProvider(new ProductCmptCompareContentProvider(cc));

        /* Sets the textfont to be used in this compare viewer. 
         * Cannot be called in configureTextViewer() as the TextMergeViewer sets the font in its 
         * constructor, after configureTextViewer() is called .
         */
        Font defaultFont = JFaceResources.getDefaultFont();
        FontData[] data = defaultFont.getFontData();
        for (int i = 0; i < data.length; i++) {
            data[i].setStyle(SWT.NORMAL);
            data[i].setHeight(10);
        }               
        JFaceResources.getFontRegistry().put(ProductCmptCompareViewer.class.getName(), data);
    }
    
    /**
     * Adds a <code>ProductCmptLineStyleListener</code> to the given <code>TextViewer</code> for
     * token highlighting.
     * {@inheritDoc}
     */
    protected void configureTextViewer(TextViewer textViewer) {
        ProductCmptLineStyleListener listener= new ProductCmptLineStyleListener((SourceViewer) textViewer);
        textViewer.getTextWidget().addLineStyleListener(listener);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        return Messages.ProductCmptCompareViewer_CompareViewer_title;
    }
}
