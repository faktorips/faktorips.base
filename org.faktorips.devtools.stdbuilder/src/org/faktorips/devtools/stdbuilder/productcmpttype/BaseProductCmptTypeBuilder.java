/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * 
 * @author Jan Ortmann, Daniel Hohenberger
 */
public abstract class BaseProductCmptTypeBuilder extends AbstractProductCmptTypeBuilder {

    public BaseProductCmptTypeBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        return ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType()).getGenerator(a);
    }

    /**
     * This method is called from the abstract builder if the policy component attribute is valid
     * and therefore code can be generated.
     * 
     * @param attribute The attribute source code should be generated for.
     * @param datatypeHelper The data type code generation helper for the attribute's data type.
     * @param fieldsBuilder The code fragment builder to build the member variables section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    @Override
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute a,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenPolicyCmptType genPolicyCmptType = ((StandardBuilderSet)getBuilderSet()).getGenerator(a.getPolicyCmptType());
        GenChangeableAttribute generator = (GenChangeableAttribute)genPolicyCmptType.getGenerator(a);
        if (generator != null) {
            generator.generateCodeForProductCmptType(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    /**
     * This method is called from the abstract builder if the product component attribute is valid
     * and therefore code can be generated.
     * 
     * @param attribute The attribute source code should be generated for.
     * @param datatypeHelper The data type code generation helper for the attribute's data type.
     * @param fieldsBuilder The code fragment builder to build the member variables section.
     * @param methodsBuilder The code fragment builder to build the method section.
     */
    @Override
    protected void generateCodeForProductCmptTypeAttribute(org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {

        GenProductCmptTypeAttribute generator = ((StandardBuilderSet)getBuilderSet()).getGenerator(getProductCmptType())
                .getGenerator(attribute);
        if (generator != null) {
            generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
        }
    }

    protected boolean isUseTypesafeCollections() {
        return ((StandardBuilderSet)getBuilderSet()).isUseTypesafeCollections();
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

    protected final GenProductCmptType getGenProductCmptType(IProductCmptType productCmptType) {
        try {
            return ((StandardBuilderSet)getBuilderSet()).getGenerator(productCmptType);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
