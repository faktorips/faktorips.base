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

package org.faktorips.devtools.core.ui.editors.enums;

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.tableedit.DatatypeEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;

public class EnumEditingSupport extends DatatypeEditingSupport<IEnumValue> {

    private final Condition condition;

    public EnumEditingSupport(UIToolkit toolkit, TableViewer tableViewer, IIpsProject ipsProject,
            ValueDatatype datatype, IElementModifier<IEnumValue, String> elementModifier, Condition condition) {
        super(toolkit, tableViewer, ipsProject, datatype, elementModifier);
        this.condition = condition;
    }

    @Override
    protected boolean canEdit(Object element) {
        return condition.isEditable();
    }

    public interface Condition {

        boolean isEditable();

    }

}
