/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.table.GridTableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A default factory that creates a combo box for none-abstract enum value sets and a simple text
 * control in all other cases.
 * 
 * @author Joerg Ortmann
 */
public class DefaultControlFactory extends ValueDatatypeControlFactory {

    public DefaultControlFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        if (datatype != null && valueSet != null && valueSet.canBeUsedAsSupersetForAnotherEnumValueSet()) {
            Combo combo = toolkit.createCombo(parent);
            return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
        }
        return new TextField(toolkit.createText(parent));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
    }

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given
     * columnIndex and a <code>Text</code> control. {@inheritDoc}
     * 
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given
     * columnIndex and a <code>Text</code> control. {@inheritDoc}
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        Text textControl = toolkit.createText(tableViewer.getTable(), SWT.SINGLE);
        TextCellEditor cellEditor = new TextCellEditor(textControl);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given
     * columnIndex and a <code>Text</code> control. {@inheritDoc}
     */
    @Override
    public IpsCellEditor createGridTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            GridTableViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        Text textControl = toolkit.createText(gridViewer.getGrid(), SWT.SINGLE);
        TextCellEditor cellEditor = new TextCellEditor(textControl);
        cellEditor.setTraversalStrategy(new GridTableViewerTraversalStrategy(cellEditor, gridViewer, columnIndex));
        return cellEditor;
    }

    @Override
    public IpsCellEditor createGridTreeCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            GridTreeViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        Text textControl = toolkit.createText(gridViewer.getGrid(), SWT.SINGLE);
        TextCellEditor cellEditor = new TextCellEditor(textControl);
        return cellEditor;
    }

}
