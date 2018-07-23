/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import org.faktorips.devtools.core.internal.model.ipsproject.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.message.Message;

public abstract class AbstractBaseClassBuilderSetPropertyDef extends IpsBuilderSetPropertyDef {

    public static final String MSGCODE_CANT_LOAD_JAVA_CLASS = MSGCODE_PREFIX + "CantLoadJavaClass"; //$NON-NLS-1$
    public static final String MSGCODE_NOT_SUBCLASS = MSGCODE_PREFIX + "NotSubclass"; //$NON-NLS-1$

    protected abstract Class<?> getRequiredSuperClass();

    @Override
    public Message validateValue(IIpsProject ipsProject, String value) {
        Message validationMessage = super.validateValue(ipsProject, value);
        if (validationMessage == null && IpsStringUtils.isNotEmpty(value)) {
            try {
                Class<?> superClass = getRequiredSuperClass();
                Class<?> clazz = ipsProject.getClassLoaderForJavaProject(superClass.getClassLoader()).loadClass(value);
                if (!superClass.isAssignableFrom(clazz)) {
                    return Message.newError(MSGCODE_NOT_SUBCLASS,
                            Messages.bind(Messages.AbstractBaseClassBuilderSetPropertyDef_NotSubclass, value,
                                    getLabel(), superClass.getName()));
                }
            } catch (ClassNotFoundException e) {
                return Message.newError(MSGCODE_CANT_LOAD_JAVA_CLASS,
                        Messages.bind(Messages.AbstractBaseClassBuilderSetPropertyDef_CantLoadJavaClass, value,
                                getLabel(), e.getLocalizedMessage()));
            }
        }
        return validationMessage;
    }

    @Override
    public String getDefaultValue(IIpsProject ipsProject) {
        String defaultValue = super.getDefaultValue(ipsProject);
        if (defaultValue == null) {
            return IpsStringUtils.EMPTY;
        } else {
            return defaultValue;
        }
    }

}
