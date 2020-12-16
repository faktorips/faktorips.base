/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.product.Messages;
import org.faktorips.devtools.core.ui.search.product.ProductSearchPresentationModel;

/**
 * The ProductSearchConditionsTableViewerCreator delivers a {@link TableViewer} for the conditions
 * of the Faktor-IPS Product Search
 * 
 * @author dicker
 */
public class ProductSearchConditionsTableViewerCreator {

    public TableViewer createTableViewer(ProductSearchPresentationModel model, Composite parent) {
        Table table = new UIToolkit(null).createTable(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS | SWT.SINGLE
                | SWT.FULL_SELECTION);

        TableViewer tableViewer = new TableViewer(table);

        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setInput(model.getProductSearchConditionPresentationModels());

        ConditionTypeEditingSupport conditionTypeEditingSupport = new ConditionTypeEditingSupport(tableViewer);
        ElementEditingSupport elementEditingSupport = new ElementEditingSupport(tableViewer);
        OperatorEditingSupport operatorEditingSupport = new OperatorEditingSupport(tableViewer);
        ArgumentEditingSupport argumentEditingSupport = new ArgumentEditingSupport(tableViewer);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_conditionType, 150,
                new ConditionTypeLabelProvider(), conditionTypeEditingSupport, tableViewer);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_element, 180,
                new ElementLabelProvider(), elementEditingSupport, tableViewer);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_operator, 150,
                new OperatorLabelProvider(), operatorEditingSupport, tableViewer);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_argument, 170,
                new ArgumentLabelProvider(), argumentEditingSupport, tableViewer);

        ConditionsTableTraversalStrategy conditionTraversalStrategy = new ConditionsTableTraversalStrategy(
                conditionTypeEditingSupport);
        ConditionsTableTraversalStrategy elementTraversalStrategy = new ConditionsTableTraversalStrategy(
                elementEditingSupport);
        ConditionsTableTraversalStrategy operatorTraversalStrategy = new ConditionsTableTraversalStrategy(
                operatorEditingSupport);
        ConditionsTableTraversalStrategy argumentTraversalStrategy = new ConditionsTableTraversalStrategy(
                argumentEditingSupport);

        conditionTraversalStrategy.setFollower(elementTraversalStrategy);
        elementTraversalStrategy.setFollower(operatorTraversalStrategy);
        operatorTraversalStrategy.setFollower(argumentTraversalStrategy);

        layoutViewer(tableViewer);

        return tableViewer;
    }

    private void createTableViewerColumn(String string,
            int width,
            CellLabelProvider labelProvider,
            EditingSupport editingSupport,
            TableViewer tableViewer) {
        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setText(string);
        tableViewerColumn.getColumn().setWidth(width);
        tableViewerColumn.setLabelProvider(labelProvider);
        if (editingSupport != null) {
            tableViewerColumn.setEditingSupport(editingSupport);
        }
    }

    private void layoutViewer(TableViewer tableViewer) {
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalSpan = 2;
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.heightHint = 150;

        tableViewer.getControl().setLayoutData(gridData);

        tableViewer.getTable().setHeaderVisible(true);
        tableViewer.getTable().setLinesVisible(true);
    }
}
