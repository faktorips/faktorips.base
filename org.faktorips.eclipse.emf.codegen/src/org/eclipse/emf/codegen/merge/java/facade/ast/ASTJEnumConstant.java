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
 * $Id: ASTJEnumConstant.java,v 1.3 2006/12/31 02:32:47 marcelop Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade.ast;

import java.util.List;

import org.eclipse.emf.codegen.merge.java.facade.JEnumConstant;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Expression;

public class ASTJEnumConstant extends ASTJMember<EnumConstantDeclaration> implements JEnumConstant {
    /**
     * Cached array of arguments
     * 
     * @see #getArguments()
     * @see #setArguments(String[])
     */
    protected String[] arguments = EMPTY_STRING_ARRAY;

    /**
     * Cached body of the enum constant.
     * 
     * @see #getBody()
     * @see #setBody(String)
     */
    protected String body = UNITIALIZED_STRING;

    /**
     * @param enumConstantDeclaration
     */
    public ASTJEnumConstant(EnumConstantDeclaration enumConstantDeclaration) {
        super(enumConstantDeclaration);
    }

    @Override
    public void dispose() {
        arguments = null;
        body = null;
        super.dispose();
    }

    @Override
    public String[] getArguments() {
        if (arguments == EMPTY_STRING_ARRAY) {
            @SuppressWarnings("unchecked")
            List<Expression> argumentsList = getASTNode().arguments();
            arguments = convertASTNodeListToStringArray(argumentsList);
        }
        return arguments;
    }

    @Override
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
        setListNodeProperty(getASTNode(), arguments, EnumConstantDeclaration.ARGUMENTS_PROPERTY, ASTNode.SIMPLE_NAME);
    }

    @Override
    public String getBody() {
        if (body == UNITIALIZED_STRING) {
            body = getFacadeHelper().toString(getASTNode().getAnonymousClassDeclaration());
        }
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
        setTrackedNodeProperty(getASTNode(), body, EnumConstantDeclaration.ANONYMOUS_CLASS_DECLARATION_PROPERTY,
                ASTNode.ANONYMOUS_CLASS_DECLARATION);
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
        setNodeProperty(getASTNode(), name, EnumConstantDeclaration.NAME_PROPERTY, ASTNode.SIMPLE_NAME);
    }
}
