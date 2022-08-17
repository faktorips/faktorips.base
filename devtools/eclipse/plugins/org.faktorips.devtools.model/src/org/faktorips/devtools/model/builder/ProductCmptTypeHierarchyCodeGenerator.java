/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.TypeHierarchyVisitor;

/**
 * A visitor makes it easy to implement a code generation function for all types in a supertype
 * hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class ProductCmptTypeHierarchyCodeGenerator extends TypeHierarchyVisitor<IProductCmptType> {

    private JavaCodeFragmentBuilder fieldsBuilder;
    private JavaCodeFragmentBuilder methodsBuilder;

    public ProductCmptTypeHierarchyCodeGenerator(IIpsProject ipsProject, JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) {

        super(ipsProject);
        this.fieldsBuilder = fieldsBuilder;
        this.methodsBuilder = methodsBuilder;
    }

    public JavaCodeFragmentBuilder getMethodsBuilder() {
        return methodsBuilder;
    }

    public JavaCodeFragmentBuilder getFieldsBuilder() {
        return fieldsBuilder;
    }

}
