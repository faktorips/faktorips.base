/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.internal.IpsStringUtils;

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
                if (!isJavaTypeInProject(ipsProject, value)) {
                    return Message.newError(MSGCODE_CANT_LOAD_JAVA_CLASS,
                            Messages.bind(Messages.AbstractBaseClassBuilderSetPropertyDef_CantLoadJavaClass, value,
                                    getLabel(), e.getLocalizedMessage()));
                }
            }
        }
        return validationMessage;
    }

    /**
     * If the referenced base class is located in the same project we get an error on clean-build
     * because the class file is not yet compiled (Faktor-IPS builder runs before java compiler).
     * That means if the class file was not found on the project's classpath it could still be
     * valid. Therefore we search for the corresponding type in the java project.
     */
    private boolean isJavaTypeInProject(IIpsProject ipsProject, String value) {
        try {
            IJavaProject javaProject = ipsProject.getJavaProject().unwrap();
            IType type = javaProject.findType(value);
            return type != null && type.exists();
        } catch (JavaModelException e) {
            // not found
            return false;
        }
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
