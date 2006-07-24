/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ValueSet;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * A factory to create controls and edit fields that allow to edit values for one or more
 * value datatypes.
 * 
 * @author Joerg Ortmann
 */
public abstract class ValueDatatypeControlFactory {

	public ValueDatatypeControlFactory() {
	}

	/** 
	 * Returns <code>true</code> if this factory can create controls for the 
	 * given datatype, otherwise <code>false</code>.
	 * 
	 * @throws NullPointerException if datatype is <code>null</code>.
	 */
	public abstract boolean isFactoryFor(ValueDatatype datatype);

	/**
	 * Creates a control and edit field that allows to edit a value of one of the value datatypes
	 * this is a factory for.
	 * 
	 * @param toolkit The toolkit used to create the control.
	 * @param parent The parent composite to which the control is added.
	 * @param datatype The value datatype a control should be created for.
	 * @param valueSet An optional valueset. 
	 */
	public abstract EditField createEditField(UIToolkit toolkit, Composite parent, ValueDatatype datatype, ValueSet valueSet);
	
	/**
	 * Creates a control that allows to edit a value of the value datatype
	 * this is a factory for.
	 * 
	 * @param toolki The toolkit used to create the control.
	 * @param parent The parent composite to which the control is added.
	 * @param datatype The value datatype a control should be created for.
	 * @param valueSet An optional valueset. 
	 */
	public abstract Control createControl(UIToolkit toolkit, Composite parent, ValueDatatype datatype, ValueSet valueSet);
}
