/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;

/**
 * A field for datatype references.
 */
public class DatatypeField extends TextButtonField {

    public DatatypeField(TextButtonControl control) {
        super(control);
    }

    /**
     * Returns the datatype if possible, or null if the current value in the control does not
     * specifiy a datatype.
     */
    public Datatype getDatatype(IIpsProject project) throws CoreException {
        return project.findDatatype(getText());
    }

}
