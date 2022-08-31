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
 * $Id: AbstractJNode.java,v 1.6 2006/12/29 20:50:43 marcelop Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade;

import java.util.Collections;
import java.util.List;

/**
 * @since 2.2.0
 */
public abstract class AbstractJNode implements JNode {
    protected static final String[] EMPTY_STRING_ARRAY = {};

    protected String qualifiedName;

    abstract public void dispose();

    abstract public boolean isDisposed();

    abstract protected Object getWrappedObject();

    abstract public void setFacadeHelper(FacadeHelper facadeHelper);

    abstract public FacadeHelper getFacadeHelper();

    @Override
    public String getQualifiedName() {
        if (qualifiedName == null) {
            qualifiedName = computeQualifiedName();
        }
        return qualifiedName;
    }

    protected String computeQualifiedName() {
        JNode parent = getParent();
        return parent == null || parent instanceof JCompilationUnit ? getName()
                : parent.getQualifiedName() + "."
                        + getName();
    }

    protected String getName(JInitializer initializer) {
        JNode parent = initializer.getParent();
        if (parent != null) {
            int index = getFacadeHelper().getChildren(parent, JInitializer.class).indexOf(initializer);
            return parent.getName() + "." + index;
        }
        return null;
    }

    protected String computeQualifiedName(JInitializer initializer) {
        JNode parent = initializer.getParent();
        if (parent != null) {
            int index = getFacadeHelper().getChildren(parent, JInitializer.class).indexOf(initializer);
            return parent.getQualifiedName() + "." + index;
        }
        return null;
    }

    protected String computeQualifiedName(JAbstractType abstractType) {
        JNode parent = abstractType.getParent();
        if (parent instanceof JAbstractType) {
            return parent.getQualifiedName() + "." + abstractType.getName();
        }

        JPackage jPackage = getFacadeHelper().getPackage(this);
        return jPackage != null ? jPackage.getName() + "." + abstractType.getName() : abstractType.getName();
    }

    protected String computeQualifiedName(JMethod method) {
        StringBuilder result = new StringBuilder(getParent().getQualifiedName());
        result.append(".");
        if (method.isConstructor()) {
            result.append(getParent().getName());
        } else {
            result.append(getName());
        }
        result.append("(");
        String[] parameters = method.getParameterTypes();
        if (parameters != null) {
            for (int i = 0; i < parameters.length; ++i) {
                if (i != 0) {
                    result.append(", ");
                }
                result.append(parameters[i]);
            }
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public List<JNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int getFlags() {
        return FacadeFlags.DEFAULT;
    }
}
