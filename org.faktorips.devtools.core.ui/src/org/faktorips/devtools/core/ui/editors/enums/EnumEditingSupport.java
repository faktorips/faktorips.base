/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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
