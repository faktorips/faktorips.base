/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype.rule;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.pctype.Messages;
import org.faktorips.devtools.core.ui.editors.pctype.rule.ValidationRuleMarkerPMO.MarkerViewItem;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class ValidationRuleMarkerUI {
    private static final int TABLE_LINE_HEIGHT = 22;
    private CheckboxTableViewer markerTable;
    private UIToolkit toolkit;

    private int visibleTableLines = SWT.DEFAULT;

    public ValidationRuleMarkerUI(UIToolkit toolkit) {
        this.toolkit = toolkit;
    }

    public void createUI(Composite parent, ValidationRuleMarkerPMO ruleMarkerPMO) {
        Group markerGroup = toolkit.createGroup(parent, Messages.ValidationRuleMarkerUI_TabName_Markers);
        markerGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        if (ruleMarkerPMO.hasAvailableMarkers()) {
            setUpMarkersTable(markerGroup, ruleMarkerPMO);
        } else {
            Label label = new Label(markerGroup, SWT.NONE);
            label.setText(Messages.ValidationRuleMarkerUI_Label_NoMarkerEnumDefined);
        }
    }

    /**
     * Defines how many lines the table should display after it is created, by setting the tables
     * height hint. Must be set <em>before</em>
     * {@link #createUI(Composite, ValidationRuleMarkerPMO)} is called.
     */
    public void setTableVisibleLines(int visibleTableLines) {
        this.visibleTableLines = visibleTableLines;
    }

    private CheckboxTableViewer setUpMarkersTable(Group markerGroup, ValidationRuleMarkerPMO ruleMarkerPMO) {
        markerTable = CheckboxTableViewer.newCheckList(markerGroup, SWT.BORDER);
        Table table = getMarkerTable().getTable();
        GridData tableGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        tableGD.heightHint = visibleTableLines == SWT.DEFAULT ? visibleTableLines
                : TABLE_LINE_HEIGHT
                        * visibleTableLines - 7;
        table.setLayoutData(tableGD);

        getMarkerTable().setContentProvider(new ArrayContentProvider());
        getMarkerTable().setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((MarkerViewItem)element).getLabelAndMarkerEnumName();
            }
        });
        getMarkerTable().setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isGrayed(Object element) {
                return false;
            }

            @Override
            public boolean isChecked(Object element) {
                MarkerViewItem viewItem = (MarkerViewItem)element;
                return viewItem.isChecked();
            }
        });
        getMarkerTable().addCheckStateListener(event -> {
            MarkerViewItem viewItem = (MarkerViewItem)event.getElement();
            viewItem.updateCheckedState();
        });
        getMarkerTable().setInput(ruleMarkerPMO.getItems());
        return getMarkerTable();
    }

    public CheckboxTableViewer getMarkerTable() {
        return markerTable;
    }

    public boolean hasMarkerTable() {
        return getMarkerTable() != null;
    }

    public Table getMarkerTableControl() {
        return getMarkerTable().getTable();
    }

    /**
     * Returns <code>true</code> if marker enums are enabled for the given {@link IIpsProject}. If
     * marker enums is disabled <code>false</code> is returned.
     */
    public boolean isMarkerEnumsEnabled(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().isMarkerEnumsEnabled();
    }

}
