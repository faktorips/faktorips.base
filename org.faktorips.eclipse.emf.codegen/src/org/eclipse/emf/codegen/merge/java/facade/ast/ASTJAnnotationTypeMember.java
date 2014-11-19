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
 * $Id: ASTJAnnotationTypeMember.java,v 1.3 2006/12/31 02:32:47 marcelop Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade.ast;

import org.eclipse.emf.codegen.merge.java.facade.JAnnotationTypeMember;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;

public class ASTJAnnotationTypeMember extends ASTJMember<AnnotationTypeMemberDeclaration> implements
        JAnnotationTypeMember {
    /**
     * Cached default value
     * 
     * @see #getDefaultValue()
     * @see #setDefaultValue(String)
     */
    protected String defaultValue = UNITIALIZED_STRING;

    /**
     * Cached type
     * 
     * @see #getType()
     * @see #setType(String)
     */
    protected String type = UNITIALIZED_STRING;

    /**
     * @param annotationTypeMemberDeclaration
     */
    public ASTJAnnotationTypeMember(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
        super(annotationTypeMemberDeclaration);
    }

    @Override
    public void dispose() {
        defaultValue = null;
        type = null;
        super.dispose();
    }

    @Override
    public String getDefaultValue() {
        if (defaultValue == UNITIALIZED_STRING) {
            defaultValue = getFacadeHelper().toString(getASTNode().getDefault());
            if (defaultValue == null) {
                defaultValue = "";
            }
        }
        return defaultValue;
    }

    @Override
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        setTrackedNodeProperty(getASTNode(), defaultValue, AnnotationTypeMemberDeclaration.DEFAULT_PROPERTY,
                ASTNode.SIMPLE_NAME);
    }

    @Override
    public String getType() {
        if (type == UNITIALIZED_STRING) {
            type = getFacadeHelper().toString(getASTNode().getType());
        }
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
        setTrackedNodeProperty(getASTNode(), type, AnnotationTypeMemberDeclaration.TYPE_PROPERTY, ASTNode.SIMPLE_TYPE);
    }

    @Override
    public String getName() {
        if (name == UNITIALIZED_STRING) {
            name = ASTFacadeHelper.toString(getASTNode().getName());
        }
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        setNodeProperty(getASTNode(), name, AnnotationTypeMemberDeclaration.NAME_PROPERTY, ASTNode.SIMPLE_NAME);
    }
}
