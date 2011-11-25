/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import org.faktorips.devtools.core.ui.table.LinkedColumnsTraversalStrategy;

public class ProductSearchConditionsTableViewerProvider {

    private TableViewer tableViewer;
    private final ProductSearchPresentationModel model;
    private final Composite parent;

    public ProductSearchConditionsTableViewerProvider(ProductSearchPresentationModel model, Composite parent) {
        this.model = model;
        this.parent = parent;

        createTableViewer();
    }

    private void createTableViewer() {
        UIToolkit toolkit = new UIToolkit(null);
        Table table = toolkit.createTable(parent, SWT.NONE);

        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        tableViewer = new TableViewer(table);

        tableViewer.setContentProvider(new ArrayContentProvider());
        tableViewer.setInput(model.getProductSearchConditionPresentationModels());

        ConditionTypeEditingSupport conditionTypeEditingSupport = new ConditionTypeEditingSupport(tableViewer);
        ElementEditingSupport elementEditingSupport = new ElementEditingSupport(tableViewer);
        OperatorEditingSupport operatorEditingSupport = new OperatorEditingSupport(tableViewer);
        ArgumentEditingSupport argumentEditingSupport = new ArgumentEditingSupport(tableViewer);

        LinkedColumnsTraversalStrategy conditionTraversalStrategy = new ConditionsTableTraversalStrategy(
                conditionTypeEditingSupport);
        LinkedColumnsTraversalStrategy elementTraversalStrategy = new ConditionsTableTraversalStrategy(
                elementEditingSupport);
        LinkedColumnsTraversalStrategy operatorTraversalStrategy = new ConditionsTableTraversalStrategy(
                operatorEditingSupport);
        LinkedColumnsTraversalStrategy argumentTraversalStrategy = new ConditionsTableTraversalStrategy(
                argumentEditingSupport);

        conditionTraversalStrategy.setFollower(elementTraversalStrategy);
        elementTraversalStrategy.setFollower(operatorTraversalStrategy);
        operatorTraversalStrategy.setFollower(argumentTraversalStrategy);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_conditionType, 150,
                new ConditionTypeLabelProvider(), conditionTypeEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_element, 180,
                new ElementLabelProvider(), elementEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_operator, 150,
                new OperatorLabelProvider(), operatorEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_argument, 170,
                new ArgumentLabelProvider(), argumentEditingSupport);

        layoutViewer();
    }

    private void createTableViewerColumn(String string,
            int width,
            CellLabelProvider labelProvider,
            EditingSupport editingSupport) {
        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        tableViewerColumn.getColumn().setText(string);
        tableViewerColumn.getColumn().setWidth(width);
        tableViewerColumn.setLabelProvider(labelProvider);
        if (editingSupport != null) {
            tableViewerColumn.setEditingSupport(editingSupport);
        }
    }

    private void layoutViewer() {
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalSpan = 2;
        gridData.horizontalSpan = 1;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.heightHint = 150;
        tableViewer.getControl().setLayoutData(gridData);
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }
}
