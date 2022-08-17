/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

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
     * @see #TAB_WIDTH
     */
    @Override
    protected void configureTextViewer(TextViewer textViewer) {
        CompareViewerLineStyleListener listener = new CompareViewerLineStyleListener();
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
