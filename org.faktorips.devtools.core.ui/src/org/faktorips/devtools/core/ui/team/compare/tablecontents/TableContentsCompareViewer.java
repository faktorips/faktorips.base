/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

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
 * Viewer for displaying differences in <code>ITableContents</code>.
 * 
 * @author Stefan Widmaier
 */
public class TableContentsCompareViewer extends TextMergeViewer {
    /**
     * The tab width (number of characters a tabulator is displayed as) of the textviewers used in
     * this compareviewer. Value is 4.
     */
    public static final int TAB_WIDTH = 4;

    /**
     * Creates a <code>TableContentsCompareViewer</code> using a
     * <code>CompareViewerCompareContentProvider</code> as a content provider.
     * 
     * @param parent
     * @param cc
     */
    public TableContentsCompareViewer(Composite parent, CompareConfiguration configuration) {
        super(parent, configuration);
        /*
         * Configure the textcompare-engine in TextMergeViewer to ignore whitespace for
         * TableContents. This is necessary since rows may contain additional whitespace characters
         * due to differing column widths. The text compare must ignore those characters to be able
         * to recognize rows of equal content.
         */
        configuration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.TRUE);
        setContentProvider(new CompareViewerContentProvider(configuration));

        /*
         * Sets the textfont to be used in this compare viewer. Cannot be set in
         * configureTextViewer() as the TextMergeViewer sets the font in its constructor, after
         * configureTextViewer() is called .
         */
        Font fixedFont = JFaceResources.getTextFont();
        FontData[] data = fixedFont.getFontData();
        for (FontData element : data) {
            element.setStyle(SWT.NORMAL);
            element.setHeight(10);
        }
        JFaceResources.getFontRegistry().put(getClass().getName(), data);
    }

    /**
     * Adds a <code>ProductCmptLineStyleListener</code> to the given <code>TextViewer</code> for
     * token highlighting. Sets the tabwidth of the given textViewer to a specific value.
     * 
     * @see #TAB_WIDTH {@inheritDoc}
     */
    @Override
    protected void configureTextViewer(TextViewer textViewer) {
        CompareViewerLineStyleListener listener = new CompareViewerLineStyleListener((SourceViewer)textViewer);
        textViewer.getTextWidget().addLineStyleListener(listener);
        textViewer.getTextWidget().setTabs(TAB_WIDTH);
    }

    /**
     * Returns this TableContentsCompareViewer's title. {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Messages.TableContentsCompareViewer_TableContentsCompare;
    }

}
