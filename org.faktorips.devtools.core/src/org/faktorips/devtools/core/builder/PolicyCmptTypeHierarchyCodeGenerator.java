/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;

/**
 * A visitor makes it easy to implement a code generation function for all types in a supertype
 * hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class PolicyCmptTypeHierarchyCodeGenerator extends TypeHierarchyVisitor<IPolicyCmptType> {

    private JavaCodeFragmentBuilder fieldsBuilder;
    private JavaCodeFragmentBuilder methodsBuilder;

    public PolicyCmptTypeHierarchyCodeGenerator(IIpsProject ipsProject, JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) {

        super(ipsProject);
        this.fieldsBuilder = fieldsBuilder;
        this.methodsBuilder = methodsBuilder;
    }

    public JavaCodeFragmentBuilder getFieldsBuilder() {
        return fieldsBuilder;
    }

    public JavaCodeFragmentBuilder getMethodsBuilder() {
        return methodsBuilder;
    }
}
