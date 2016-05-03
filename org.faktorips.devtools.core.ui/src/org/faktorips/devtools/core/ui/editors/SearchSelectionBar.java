/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.editors;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.enumcontent.EnumSearchFilter;
import org.faktorips.devtools.core.ui.editors.enums.EnumValuesContentProvider;
import org.faktorips.devtools.core.ui.editors.tablecontents.TableContentsContentProvider;
import org.faktorips.devtools.core.ui.editors.tablecontents.TableSearchFilter;

public class SearchSelectionBar {

    private TableViewer tableViewer;
    private final Text searchField;
    private boolean noProgress = true;
    private SearchFilter searchFilter;

    public SearchSelectionBar(Composite formBody, UIToolkit toolkit) {
        // initialize tableViewer via init(...)
        Composite searchPanel = toolkit.createLabelEditColumnComposite(formBody);
        toolkit.createFormLabel(searchPanel, Messages.SearchSelectionBar_searchBarTitle);
        this.searchField = toolkit.createText(searchPanel);
        searchField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                if (noProgress) {
                    noProgress = false;
                    searchFilter.setSearchText(searchField.getText());
                    tableViewer.setFilters(new ViewerFilter[] { searchFilter });
                    noProgress = true;
                } else {
                    return;
                }
            }
        });

    }

    public void init(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
        IContentProvider contentProvider = tableViewer.getContentProvider();
        if (contentProvider instanceof EnumValuesContentProvider) {
            this.searchFilter = new EnumSearchFilter();
        } else if (contentProvider instanceof TableContentsContentProvider) {
            this.searchFilter = new TableSearchFilter();
        } else {
            this.searchFilter = new SearchFilter();
        }
    }
}
