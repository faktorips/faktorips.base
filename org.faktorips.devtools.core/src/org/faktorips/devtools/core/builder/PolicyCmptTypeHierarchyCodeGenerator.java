/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;

/**
 * A visitor makes it easy to implement a code generation function for all types in a supertype
 * hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class PolicyCmptTypeHierarchyCodeGenerator extends PolicyCmptTypeHierarchyVisitor {

    protected JavaCodeFragmentBuilder fieldsBuilder;
    protected JavaCodeFragmentBuilder methodsBuilder;

    public PolicyCmptTypeHierarchyCodeGenerator(JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) {

        this.fieldsBuilder = fieldsBuilder;
        this.methodsBuilder = methodsBuilder;
    }

}
