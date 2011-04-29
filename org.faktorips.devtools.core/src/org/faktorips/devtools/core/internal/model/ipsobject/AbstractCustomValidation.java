/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.faktorips.devtools.core.model.ipsobject.ICustomValidation;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.util.QNameUtil;
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
