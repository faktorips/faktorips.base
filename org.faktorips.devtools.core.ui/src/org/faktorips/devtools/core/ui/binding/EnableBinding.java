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

package org.faktorips.devtools.core.ui.binding;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Binding between the enable property of a SWT control and a boolean property of an abitrary
 * object, usually a domain model object or presentation model object.
 * 
 * @author Jan Ortmann
 */
public class EnableBinding extends ControlPropertyBinding {

    private Object expectedValue;

    public EnableBinding(Control control, Object object, String property, Object expectedValue) {
        super(control, object, property, null);
        this.expectedValue = expectedValue;
    }

    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        try {
            boolean workingModeEdit = IpsPlugin.getDefault().getIpsPreferences().isWorkingModeEdit();
            boolean objectEdiable = isObjectEditable(getObject());
            Object value = getProperty().getReadMethod().invoke(getObject(), new Object[0]);
            boolean enabled = value != null && value.equals(expectedValue) && workingModeEdit && objectEdiable;
            getControl().setEnabled(enabled);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isObjectEditable(Object object) {
        if (object instanceof IIpsObjectPartContainer) {
            IIpsObjectPartContainer partContainer = (IIpsObjectPartContainer)object;
            return IpsUIPlugin.isEditable(partContainer.getIpsSrcFile());
        } else if (object instanceof IpsObjectPartPmo) {
            IIpsObjectPartContainer partContainer = ((IpsObjectPartPmo)object).getIpsObjectPartContainer();
            return isObjectEditable(partContainer);
        } else if (object instanceof IIpsSrcFile) {
            IIpsSrcFile srcFile = (IIpsSrcFile)object;
            return IpsUIPlugin.isEditable(srcFile);
        } else {
            return true;
        }
    }

}
