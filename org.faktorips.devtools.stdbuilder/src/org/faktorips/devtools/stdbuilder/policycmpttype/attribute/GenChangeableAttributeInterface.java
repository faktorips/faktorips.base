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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class GenChangeableAttributeInterface extends GenChangeableAttribute {

    public GenChangeableAttributeInterface(IPolicyCmptTypeAttribute a, PolicyCmptInterfaceBuilder builder, LocalizedStringsSet stringsSet) throws CoreException {
        super(a, builder, stringsSet, false);
    }

    /**
     * 
     * Returns the policy component interface builder.
     */
    private PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        return (PolicyCmptInterfaceBuilder)getJavaSourceFileBuilder();
    }
    
    /**
     * {@inheritDoc}
     */
    protected ProductCmptGenInterfaceBuilder getProductCmptGenInterfaceBuilder() {
        return getInterfaceBuilder().getProductCmptGenInterfaceBuilder();
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMemberVariables(JavaCodeFragmentBuilder builder) throws CoreException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    protected void generateMethods(JavaCodeFragmentBuilder builder) throws CoreException {
        if (isOverwritten()) {
            return;
        }
        generateGetterInterface(builder);
        generateSetterInterface(builder);
        if (isRangeValueSet()) {
            getProductCmptGenInterfaceBuilder().generateMethodGetRangeFor(attribute, wrapperDatatypeHelper, builder);
        } else if (isEnumValueSet()) {
            getProductCmptGenInterfaceBuilder().generateMethodGetAllowedValuesFor(attribute, wrapperDatatypeHelper.getDatatype(), builder);
        }
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public void setPremium(Money newValue);
     * </pre>
     */
    protected void generateSetterInterface(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        String[] replacements = new String[]{attributeName, description};
        appendLocalizedJavaDoc("METHOD_SETVALUE", replacements, attributeName, methodsBuilder);
        generateSetterSignature(methodsBuilder);
        methodsBuilder.appendln(";");
    }
    
}
