/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

import org.faktorips.devtools.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.util.QNameUtil;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract class that can be used as base class for a custom validation.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractCustomValidation<T extends IIpsObjectPartContainer> implements ICustomValidation<T> {

    private Class<T> extendedClass;

    public AbstractCustomValidation(Class<T> extendedClass) {
        ArgumentCheck.notNull(extendedClass);
        this.extendedClass = extendedClass;
    }

    @Override
    public Class<T> getExtendedClass() {
        return extendedClass;
    }

    @Override
    public String toString() {
        return "CustomValidation for " + QNameUtil.getUnqualifiedName(getExtendedClass().getName()); //$NON-NLS-1$
    }
}
