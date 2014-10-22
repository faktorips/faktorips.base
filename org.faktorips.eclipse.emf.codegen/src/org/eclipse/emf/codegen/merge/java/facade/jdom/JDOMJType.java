/**
 * <copyright>
 *
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: JDOMJType.java,v 1.4 2007/06/12 20:56:06 emerks Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade.jdom;

import org.eclipse.emf.codegen.merge.java.facade.JPackage;
import org.eclipse.emf.codegen.merge.java.facade.JType;
import org.eclipse.jdt.core.jdom.IDOMType;

/**
 * @since 2.2.0
 */
@SuppressWarnings({ "deprecation" })
public class JDOMJType extends JDOMJMember implements JType {
    protected JPackage jPackage;

    public JDOMJType(IDOMType type) {
        super(type);
    }

    @Override
    protected IDOMType getWrappedObject() {
        return (IDOMType)super.getWrappedObject();
    }

    @Override
    public String getSuperclass() {
        return getWrappedObject().getSuperclass();
    }

    @Override
    public void setSuperclass(String superclassName) {
        getWrappedObject().setSuperclass(superclassName);
    }

    @Override
    public String[] getSuperInterfaces() {
        String[] ret = getWrappedObject().getSuperInterfaces();
        return ret == null ? EMPTY_STRING_ARRAY : ret;
    }

    @Override
    public void setSuperInterfaces(String[] interfaceNames) {
        getWrappedObject().setSuperInterfaces(interfaceNames);
    }

    @Override
    public void addSuperInterface(String interfaceName) {
        getWrappedObject().addSuperInterface(interfaceName);
    }

    @Override
    public String[] getTypeParameters() {
        String[] ret = getWrappedObject().getTypeParameters();
        return ret == null ? EMPTY_STRING_ARRAY : ret;
    }

    @Override
    protected String computeQualifiedName() {
        return computeQualifiedName(this);
    }

    @Override
    public void setTypeParameters(String[] typeParameters) {
        // not supported in JDOM
    }
}
