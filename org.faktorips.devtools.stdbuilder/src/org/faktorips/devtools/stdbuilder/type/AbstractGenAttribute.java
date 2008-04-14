/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.stdbuilder.type;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.BasePolicyCmptTypeBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptInterfaceBuilder;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptGenInterfaceBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractGenAttribute extends DefaultJavaGeneratorForIpsPart {

    protected IAttribute attribute;
    protected String attributeName;
    protected DatatypeHelper datatypeHelper;
    protected String staticConstantPropertyName;
    protected String memberVarName;
    private boolean generatePolicySide;

    private PolicyCmptInterfaceBuilder policyCmptInterfaceBuilder;
    private ProductCmptGenInterfaceBuilder productCmptGenInterfaceBuilder;

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];

    public AbstractGenAttribute(IAttribute a, DefaultJavaSourceFileBuilder builder, LocalizedStringsSet stringsSet,
            boolean generateImplementation) throws CoreException {
        super(a, builder, stringsSet, generateImplementation);
        this.attribute = a;
        attributeName = a.getName();
        datatypeHelper = builder.getIpsProject().findDatatypeHelper(a.getDatatype());
        if (datatypeHelper == null) {
            throw new NullPointerException("No datatype helper found for " + a);
        }
        staticConstantPropertyName = getLocalizedText("FIELD_PROPERTY_NAME", StringUtils.upperCase(a.getName()));
        memberVarName = getJavaNamingConvention().getMemberVarName(attributeName);
        generatePolicySide = builder instanceof BasePolicyCmptTypeBuilder;
    }

    public boolean isGeneratingPolicySide() {
        return generatePolicySide;
    }

    public boolean isGeneratingProductSide() {
        return !generatePolicySide;
    }

    protected PolicyCmptInterfaceBuilder getPolicyCmptInterfaceBuilder() {
        if (null == policyCmptInterfaceBuilder) {
            IIpsArtefactBuilder[] builders = getIpsProject().getIpsArtefactBuilderSet().getArtefactBuilders();
            for (int i = 0; i < builders.length; i++) {
                IIpsArtefactBuilder builder = builders[i];
                if (builder instanceof PolicyCmptInterfaceBuilder) {
                    policyCmptInterfaceBuilder = (PolicyCmptInterfaceBuilder)builder;
                }
            }
        }
        return policyCmptInterfaceBuilder;
    }

}
