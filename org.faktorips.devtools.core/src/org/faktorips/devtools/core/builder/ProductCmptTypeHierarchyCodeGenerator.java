/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;

/**
 * A visitor makes it easy to implement a code generation function for all types in a supertype hierarchy.
 * 
 * @author Jan Ortmann
 */
public abstract class ProductCmptTypeHierarchyCodeGenerator extends ProductCmptTypeHierarchyVisitor {

    protected JavaCodeFragmentBuilder fieldsBuilder;
    protected JavaCodeFragmentBuilder methodsBuilder;
    
    public ProductCmptTypeHierarchyCodeGenerator(
            IIpsProject ipsProject,
            JavaCodeFragmentBuilder fieldsBuilder, 
            JavaCodeFragmentBuilder methodsBuilder) {
        
        super(ipsProject);
        this.fieldsBuilder = fieldsBuilder;
        this.methodsBuilder = methodsBuilder;
    }

}
