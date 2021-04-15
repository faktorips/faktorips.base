/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorSite;
import org.faktorips.devtools.core.ui.editors.tablecontents.Messages;

/**
 * Writes Messages about the Selection in the Eclipse Status Bar. The Row if a single Row is
 * selected. Or the Area in witch the Rows are selected and the amount of selected Rows.
 */
public class SelectionStatusBarPublisher {
    private static final int MAX_ROWS_MARKED_NO_AREA = 5;

    private final List<Integer> selectedRows = new ArrayList<>();

    private final IStatusLineManager statusLineManager;

    public SelectionStatusBarPublisher(IEditorSite editorSite) {
        statusLineManager = getStatusLineManager(editorSite);
    }

    public SelectionStatusBarPublisher(IStatusLineManager statusLineManager) {
        this.statusLineManager = statusLineManager;
    }

    private IStatusLineManager getStatusLineManager(IEditorSite editorSite) {
        IActionBars bars = editorSite.getActionBars();
        if (bars == null) {
            return null;
        } else {
            return bars.getStatusLineManager();
        }
    }

    public void updateMarkedRows(List<Integer> selection) {
        updateMarkedRowsArray(selection);
        updateMarkedRowsLabel();
    }

    private void updateMarkedRowsArray(List<Integer> rows) {
        selectedRows.clear();
        for (int row : rows) {
            selectedRows.add(row + 1);
            Collections.sort(selectedRows);
        }
    }

    private void updateMarkedRowsLabel() {
        String labelContent;
        if (selectedRows.isEmpty()) {
            labelContent = StringUtils.EMPTY;

        } else {
            if (selectedRows.size() == 1) {
                labelContent = NLS.bind(Messages.SelectionStatusBarPublisher_singleMarkedRow, selectedRows.get(0));
            } else if (selectedRows.size() <= MAX_ROWS_MARKED_NO_AREA) {
                labelContent = NLS.bind(Messages.SelectionStatusBarPublisher_multipleMarkedRows, selectedRows);
            } else {
                Object[] selArg = { selectedRows.size(), selectedRows.get(0),
                        selectedRows.get(selectedRows.size() - 1) };
                labelContent = NLS.bind(Messages.SelectionStatusBarPublisher_manyMarkedRows, selArg);
            }
        }
        setLeftBottomStatusBar(labelContent);
    }

    private void setLeftBottomStatusBar(final String text) {
        statusLineManager.setMessage(text);
    }

}
