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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.MoneyField;
import org.faktorips.devtools.core.ui.controls.TextComboControl;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

public class MoneyControlFactory extends ValueDatatypeControlFactory {

    public MoneyControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.MONEY.equals(datatype);
    }

    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        TextComboControl control = (TextComboControl)createControl(toolkit, parent, datatype, valueSet, ipsProject);
        return new MoneyField(control);
    }

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        TextComboControl control = new TextComboControl(parent, toolkit);
        return control;
    }

    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpsCellEditor createGridTableCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            GridTableViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpsCellEditor createGridTreeCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            GridTreeViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        // TODO Auto-generated method stub
        return null;
    }

}
