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

package org.faktorips.devtools.core.ui.search.product;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.search.product.conditions.ISearchOperatorType;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

public class ProductSearchConditionsTableViewerProvider {
    private static final class ArgumentLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();

            ValueDatatype valueDatatype = getValueDatatype(model);

            if (valueDatatype != null) {
                cell.setText(IpsUIPlugin.getDefault().getDatatypeFormatter()
                        .formatValue(valueDatatype, model.getArgument()));
            } else {
                cell.setText(model.getArgument() == null ? StringUtils.EMPTY : model.getArgument());
            }
        }

        private ValueDatatype getValueDatatype(ProductSearchConditionPresentationModel model) {
            if (model.getSearchedElement() == null) {
                return null;
            }
            return model.getCondition().getValueDatatype(model.getSearchedElement());
        }
    }

    private static final class ArgumentEditingSupport extends EditingSupport {

        public ArgumentEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            ValueDatatype valueDatatype = model.getCondition().getValueDatatype(model.getSearchedElement());

            ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                    valueDatatype);

            IValueSet valueSet = model.getCondition().getValueSet(model.getSearchedElement());

            IpsCellEditor tableCellEditor = controlFactory.createTableCellEditor(new UIToolkit(null), valueDatatype,
                    valueSet, (TableViewer)getViewer(), 3, model.getSearchedElement().getIpsProject());

            return tableCellEditor;
        }

        @Override
        protected boolean canEdit(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            return model.isSearchedElementChosen();

        }

        @Override
        protected Object getValue(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            return model.getArgument();
        }

        @Override
        protected void setValue(Object element, Object value) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            model.setArgument((String)value);
            getViewer().refresh();
        }
    }

    private static final class OperatorLabelProvider extends CellLabelProvider {

        @Override
        public void update(ViewerCell cell) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();
            if (model.getOperatorTypeIndex() == null) {
                cell.setText(StringUtils.EMPTY);
            } else {
                ISearchOperatorType operatorType = model.getOperatorType();
                cell.setText(operatorType == null ? StringUtils.EMPTY : operatorType.getLabel());
            }
        }
    }

    private static final class OperatorEditingSupport extends EditingSupport {

        public OperatorEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            List<? extends ISearchOperatorType> operatorTypes = model.getCondition().getSearchOperatorTypes(
                    model.getSearchedElement());
            String[] operatorTypesLabels = new String[operatorTypes.size()];
            for (int i = 0; i < operatorTypesLabels.length; i++) {
                operatorTypesLabels[i] = operatorTypes.get(i).getLabel();
            }

            return new ComboBoxCellEditor(((TableViewer)getViewer()).getTable(), operatorTypesLabels);
        }

        @Override
        protected boolean canEdit(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            return model.isSearchedElementChosen();
        }

        @Override
        protected Object getValue(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            return model.getOperatorTypeIndex() == null ? Integer.valueOf(0) : model.getOperatorTypeIndex();
        }

        @Override
        protected void setValue(Object element, Object value) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            model.setOperatorTypeIndex((Integer)value);
            getViewer().refresh();
        }
    }

    private static final class ElementLabelProvider extends CellLabelProvider {
        @Override
        public void update(ViewerCell cell) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();

            if (model.getSearchedElementIndex() == null) {
                cell.setText(StringUtils.EMPTY);
            } else {
                IIpsElement searchedElement = model.getSearchedElement();
                cell.setText(searchedElement == null ? StringUtils.EMPTY : searchedElement.getName());
            }

        }

    }

    private static final class ElementEditingSupport extends EditingSupport {

        public ElementEditingSupport(TableViewer viewer) {
            super(viewer);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            List<? extends IIpsElement> searchableElements = model.getSearchableElements();
            String[] searchableElementsNames = new String[searchableElements.size()];
            for (int i = 0; i < searchableElementsNames.length; i++) {
                searchableElementsNames[i] = searchableElements.get(i).getName();
            }

            return new ComboBoxCellEditor(((TableViewer)getViewer()).getTable(), searchableElementsNames);
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            return model.getSearchedElementIndex() == null ? Integer.valueOf(0) : model.getSearchedElementIndex();
        }

        @Override
        protected void setValue(Object element, Object value) {
            ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

            model.setSearchedElementIndex((Integer)value);
            getViewer().refresh();
        }
    }

    private static final class ConditionTypeLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(Object element) {
            return ((ProductSearchConditionPresentationModel)element).getCondition().getName();
        }
    }

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

        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_conditionType, 150,
                new ConditionTypeLabelProvider(), null);
        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_element, 180,
                new ElementLabelProvider(), new ElementEditingSupport(tableViewer));
        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_operator, 150,
                new OperatorLabelProvider(), new OperatorEditingSupport(tableViewer));
        createTableViewerColumn(Messages.ProductSearchConditionsTableViewerProvider_argument, 170,
                new ArgumentLabelProvider(), new ArgumentEditingSupport(tableViewer));

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

    /*
     * return conditionModel.getCondition().getName();
     * 
     * case 1: return conditionModel.getSearchedElement().getName();
     * 
     * case 2: return conditionModel.getOperatorType().getLabel();
     * 
     * case 3: return conditionModel.getArgument();
     * 
     * default: return null;
     */

    public TableViewer getTableViewer() {
        return tableViewer;
    }
}
