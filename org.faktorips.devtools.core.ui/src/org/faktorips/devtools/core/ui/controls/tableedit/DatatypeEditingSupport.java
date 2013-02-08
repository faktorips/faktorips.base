/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * {@link FormattedCellEditingSupport} that creates its cell editors based on a specific
 * {@link ValueDatatype}.
 * 
 * @author Stefan Widmaier
 */
public class DatatypeEditingSupport<T> extends FormattedCellEditingSupport<T, String> {

    private final UIToolkit toolkit;
    private final TableViewer tableViewer;
    private final ValueDatatype datatype;
    private ValueDatatypeControlFactory controlFactory;
    private final IIpsProject ipsProject;

    public DatatypeEditingSupport(UIToolkit toolkit, TableViewer tableViewer, IIpsProject ipsProject,
            ValueDatatype datatype, IElementModifier<T, String> elementModifier) {
        super(tableViewer, elementModifier);
        this.toolkit = toolkit;
        this.tableViewer = tableViewer;
        this.ipsProject = ipsProject;
        this.datatype = datatype;
        controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(T element) {
        return controlFactory.createTableCellEditor(toolkit, datatype, null, tableViewer, 0, ipsProject);
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    /**
     * Returns the string representing the given element. The string is formatted depending on the
     * datatype and the current locale.
     * 
     * @param element the element to return a formatted string for
     */
    @Override
    public String getFormattedValue(T element) {
        return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, getValue(element));
    }

}
