/**
 * <copyright>
 *
 * Copyright (c) 2006-2007 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: ASTJMethod.java,v 1.6 2008/01/05 14:30:58 emerks Exp $
 */
package org.eclipse.emf.codegen.merge.java.facade.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.codegen.merge.java.facade.JMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;

/**
 * Wraps {@link MethodDeclaration} object.
 * 
 * @since 2.2.0
 */
public class ASTJMethod extends ASTJMember<MethodDeclaration> implements JMethod {
    protected String returnType = UNITIALIZED_STRING;
    protected String[] typeParameters = EMPTY_STRING_ARRAY;
    protected String[] parameterNames = EMPTY_STRING_ARRAY;
    protected String[] parameters = EMPTY_STRING_ARRAY;

    /**
     * Array of cached exceptions. Current exceptions is a combination of this array and
     * {@link #addedExceptions}.
     */
    protected String[] exceptions = EMPTY_STRING_ARRAY;

    /**
     * List of added exceptions by calling {@link #addException(String)}. This list does not include
     * existing exceptions, nor exceptions set by {@link #setExceptions(String[])}
     */
    protected List<String> addedExceptions = null;

    protected String body = UNITIALIZED_STRING;

    /**
     * @param methodDeclaration
     */
    public ASTJMethod(MethodDeclaration methodDeclaration) {
        super(methodDeclaration);
    }

    @Override
    public void dispose() {
        returnType = null;
        typeParameters = null;
        parameterNames = null;
        parameters = null;
        exceptions = null;
        body = null;
        if (addedExceptions != null) {
            addedExceptions.clear();
            addedExceptions = null;
        }

        super.dispose();
    }

    @Override
    public boolean isConstructor() {
        // always returns original value
        return getASTNode().isConstructor();
    }

    @Override
    public String getName() {
        if (name == UNITIALIZED_STRING) {
            name = (isConstructor() ? null : ASTFacadeHelper.toString(getASTNode().getName()));
        }
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        setNodeProperty(getASTNode(), name, MethodDeclaration.NAME_PROPERTY, ASTNode.SIMPLE_NAME);
    }

    @Override
    public String getReturnType() {
        if (returnType == UNITIALIZED_STRING) {
            returnType = getFacadeHelper().toString(getASTNode().getReturnType2());
            if (returnType != null) {
                // append extra dimensions since they are not stored in Type object
                for (int i = 0; i < getASTNode().getExtraDimensions(); i++) {
                    returnType += "[]";
                }
            }
        }
        return returnType;
    }

    @Override
    public void setReturnType(String type) {
        returnType = type;
        removeExtraDimensions();
        setTrackedNodeProperty(getASTNode(), type, MethodDeclaration.RETURN_TYPE2_PROPERTY, ASTNode.SIMPLE_TYPE);
    }

    /**
     * Extra dimension is the optional array declaration after the method name. For example in
     * 
     * <pre>
     * private String a()[] {
     *     // ...
     * }
     * </pre>
     * 
     * We do not want to have these extra dimensions at all. If there are some extra dimensions we
     * simply remove them.
     */
    private void removeExtraDimensions() {
        @SuppressWarnings("unchecked")
        List<Dimension> extraDimensions = getASTNode().extraDimensions();
        for (Dimension d : extraDimensions) {
            removeNodeFromListProperty(getASTNode(), d, MethodDeclaration.EXTRA_DIMENSIONS2_PROPERTY);
        }
    }

    @Override
    public String[] getTypeParameters() {
        if (typeParameters == EMPTY_STRING_ARRAY) {
            @SuppressWarnings("unchecked")
            List<TypeParameter> typeParametersList = getASTNode().typeParameters();
            typeParameters = convertASTNodeListToStringArray(typeParametersList);
        }
        return typeParameters;
    }

    @Override
    public void setTypeParameters(String[] typeParameters) {
        this.typeParameters = typeParameters;
        setListNodeProperty(getASTNode(), typeParameters, MethodDeclaration.TYPE_PARAMETERS_PROPERTY,
                ASTNode.TYPE_PARAMETER);
    }

    /**
     * Returns parameter names.
     * <p>
     * Note that if parameters have been changed by {@link #setParameters(String[])} method, this
     * method will <strong>not</strong> parse parameters, and will <strong>not</strong> return the
     * new parameter names. This method will return either parameter names set by
     * {@link #setParameterNames(String[])} method or original parameter names.
     * 
     * @see org.eclipse.emf.codegen.merge.java.facade.JMethod#getParameterNames()
     */
    @Override
    public String[] getParameterNames() {
        if (parameterNames == EMPTY_STRING_ARRAY) {

            @SuppressWarnings("unchecked")
            List<SingleVariableDeclaration> parameters = getASTNode().parameters();

            parameterNames = new String[parameters.size()];
            int j = 0;
            for (SingleVariableDeclaration parameter : parameters) {
                parameterNames[j++] = ASTFacadeHelper.toString(parameter.getName());
            }
        }
        return parameterNames;
    }

    /**
     * Returns type erasure of all the types of formal parameters of the original method
     * declaration.
     * <p>
     * This method is used to create a method signature, and match methods by signature.
     * <p>
     * Note that this implementation does not expand types into fully qualified names, nor does it
     * replace type parameters defined for a class or a method.
     * 
     * @see ASTFacadeHelper#getTypeErasure(org.eclipse.jdt.core.dom.Type)
     * @see org.eclipse.emf.codegen.merge.java.facade.JMethod#getParameterTypes()
     */
    @Override
    public String[] getParameterTypes() {

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = getASTNode().parameters();

        String[] ret = new String[parameters.size()];
        int j = 0;
        for (SingleVariableDeclaration parameter : parameters) {
            String type = ASTFacadeHelper.getTypeErasure(parameter.getType());
            // append extra dimensions if any
            for (int i = 0; i < parameter.getExtraDimensions(); i++) {
                type += "[]";
            }

            // append [] if it is variable arity parameter (@see JLS3 8.4.1,
            // http://java.sun.com/docs/books/jls/third_edition/html/classes.html#300870)
            if (parameter.isVarargs()) {
                type += "[]";
            }
            ret[j++] = type;
        }
        return ret;
    }

    /**
     * Returns all the types of formal parameters of the original method declaration. Note that this
     * implementation does not expand types into fully qualified names, nor does it replace type
     * parameters defined for a class or a method.
     * 
     * @see org.eclipse.emf.codegen.merge.java.facade.JMethod#getFullParameterTypes()
     */
    @Override
    public String[] getFullParameterTypes() {

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = getASTNode().parameters();

        String[] ret = new String[parameters.size()];
        int j = 0;
        for (SingleVariableDeclaration parameter : parameters) {
            String type = getFacadeHelper().toString(parameter.getType());
            // append extra dimensions if any
            for (int i = 0; i < parameter.getExtraDimensions(); i++) {
                type += "[]";
            }

            // append [] if it is variable arity parameter (@see JLS3 8.4.1,
            // http://java.sun.com/docs/books/jls/third_edition/html/classes.html#300870)
            if (parameter.isVarargs()) {
                type += "[]";
            }
            ret[j++] = type;
        }
        return ret;
    }

    /**
     * Sets parameter names. New parameter names will not be returned by {@link #getParameters()},
     * but will be returned by {@link #getParameterNames()}.
     * 
     * @see #getParameterNames()
     * @see #getParameters()
     * @see org.eclipse.emf.codegen.merge.java.facade.JMethod#setParameterNames(java.lang.String[])
     */
    @Override
    public void setParameterNames(String[] names) {

        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = getASTNode().parameters();

        if (parameters.size() != names.length) {
            throw new IllegalArgumentException("Length of names must match number of existing parameters.");
        }

        parameterNames = names;
        int i = 0;
        for (SingleVariableDeclaration parameter : parameters) {
            setNodeProperty(parameter, names[i++], SingleVariableDeclaration.NAME_PROPERTY, ASTNode.SIMPLE_NAME);
        }
    }

    @Override
    public String[] getExceptions() {
        if (exceptions == EMPTY_STRING_ARRAY) {

            @SuppressWarnings("unchecked")
            List<Name> exceptionsList = getASTNode().thrownExceptionTypes();

            exceptions = new String[exceptionsList.size()];
            int j = 0;
            for (Name exception : exceptionsList) {
                exceptions[j++] = ASTFacadeHelper.toString(exception);
            }
        }
        exceptions = combineArrayAndList(exceptions, addedExceptions);
        return exceptions;
    }

    @Override
    public void setExceptions(String[] exceptionTypes) {
        exceptions = exceptionTypes;
        addedExceptions = null;
        setListNodeProperty(getASTNode(), exceptionTypes, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY,
                ASTNode.SIMPLE_NAME);
    }

    @Override
    public void addException(String exceptionType) {
        if (addedExceptions == null) {
            addedExceptions = new ArrayList<>();
        }
        addedExceptions.add(exceptionType);
        addValueToListProperty(getASTNode(), exceptionType, MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY,
                ASTNode.SIMPLE_NAME);
    }

    @Override
    public String getBody() {
        if (body == UNITIALIZED_STRING) {
            body = getFacadeHelper().toString(getASTNode().getBody());
        }
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
        setTrackedNodeProperty(getASTNode(), body, MethodDeclaration.BODY_PROPERTY, ASTNode.BLOCK);
    }

    @Override
    protected String computeQualifiedName() {
        return computeQualifiedName(this);
    }

    /**
     * Returned parameters will not include new names set by {@link #setParameterNames(String[])}.
     * 
     * @see org.eclipse.emf.codegen.merge.java.facade.JMethod#getParameters()
     */
    @Override
    public String[] getParameters() {
        if (parameters == EMPTY_STRING_ARRAY) {
            @SuppressWarnings("unchecked")
            List<SingleVariableDeclaration> parametersList = getASTNode().parameters();
            parameters = convertASTNodeListToStringArray(parametersList);
        }
        return parameters;
    }

    @Override
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
        setListNodeProperty(getASTNode(), parameters, MethodDeclaration.PARAMETERS_PROPERTY,
                ASTNode.SINGLE_VARIABLE_DECLARATION);
    }
}
