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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.valueset.ValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.table.TableCellEditor;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A default factory that creates a simple text control for any value datatype if 
 * the given value set is not an enum value set. If the given value set is an enum value set
 * it create a combo box.
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
	public boolean isFactoryFor(ValueDatatype datatype) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public EditField createEditField(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, IValueSet valueSet, IIpsProject ipsProject) {
		
		if (datatype != null && valueSet instanceof IEnumValueSet) {
            Combo combo = toolkit.createCombo(parent);
			return new EnumValueSetField(combo, (IEnumValueSet)valueSet, datatype);
		}
		return new TextField(toolkit.createText(parent));
	}

	/**
	 * {@inheritDoc}
	 */
	public Control createControl(UIToolkit toolkit, Composite parent,
			ValueDatatype datatype, IValueSet valueSet, IIpsProject ipsProject) {

		return createEditField(toolkit, parent, datatype, valueSet, ipsProject).getControl();
	}

    /**
     * Creates a <code>TextCellEditor</code> with the given <code>TableViewer</code>, the given 
     * columnIndex and a <code>Text</code> control. 
     * {@inheritDoc}
     */
    public TableCellEditor createCellEditor(UIToolkit toolkit, ValueDatatype dataType, ValueSet valueSet, TableViewer tableViewer, int columnIndex, IIpsProject ipsProject) {
        Text textControl= toolkit.createText(tableViewer.getTable(), SWT.SINGLE);
        return new TextCellEditor(tableViewer, columnIndex, textControl);
    }
    
}
