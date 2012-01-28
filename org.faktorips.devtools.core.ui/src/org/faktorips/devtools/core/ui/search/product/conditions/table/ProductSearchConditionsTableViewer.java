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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.product.Messages;
import org.faktorips.devtools.core.ui.search.product.ProductSearchPresentationModel;
import org.faktorips.devtools.core.ui.table.LinkedColumnsTraversalStrategy;

/**
 * The ProductSearchConditionsTableViewer is a {@link TableViewer} for the conditions of the
 * Faktor-IPS Product Search
 * 
 * @author dicker
 */
public class ProductSearchConditionsTableViewer extends TableViewer {

    private ConditionTypeEditingSupport conditionTypeEditingSupport = new ConditionTypeEditingSupport(this);
    private ElementEditingSupport elementEditingSupport = new ElementEditingSupport(this);
    private OperatorEditingSupport operatorEditingSupport = new OperatorEditingSupport(this);
    private ArgumentEditingSupport argumentEditingSupport = new ArgumentEditingSupport(this);

    public ProductSearchConditionsTableViewer(ProductSearchPresentationModel model, Composite parent) {
        super(new UIToolkit(null).createTable(parent, SWT.NONE));

        setContentProvider(new ArrayContentProvider());
        setInput(model.getProductSearchConditionPresentationModels());

        createTableViewerColumn();
        createTraversalStrategies();

        layoutViewer();
    }

    private void createTableViewerColumn() {

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_conditionType, 150,
                new ConditionTypeLabelProvider(), conditionTypeEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_element, 180,
                new ElementLabelProvider(), elementEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_operator, 150,
                new OperatorLabelProvider(), operatorEditingSupport);

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_argument, 170,
                new ArgumentLabelProvider(), argumentEditingSupport);

    }

    private void createTraversalStrategies() {
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
    }

    private void createTableViewerColumn(String string,
            int width,
            CellLabelProvider labelProvider,
            EditingSupport editingSupport) {
        TableViewerColumn tableViewerColumn = new TableViewerColumn(this, SWT.NONE);
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

        getControl().setLayoutData(gridData);

        getTable().setHeaderVisible(true);
        getTable().setLinesVisible(true);
    }

    @Override
    protected Item getItemAt(Point p) {
        Item itemAt = super.getItemAt(p);

        if (itemAt == null) {
            Rectangle tableBounds = getTable().getBounds();

            if (tableBounds.width < p.x) {
                return null;
            }

            TableItem[] items = getTable().getItems();
            for (TableItem tableItem : items) {
                Rectangle bounds = tableItem.getBounds();

                if (bounds.y <= p.y && bounds.y + bounds.height >= p.y) {
                    return tableItem;
                }
            }

        }
        return itemAt;
    }

}
