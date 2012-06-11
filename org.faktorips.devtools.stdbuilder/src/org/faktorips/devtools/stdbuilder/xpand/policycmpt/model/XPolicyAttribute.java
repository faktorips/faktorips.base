/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;

public class XPolicyAttribute extends XAttribute {

    private DatatypeHelper datatypeHelper;
    private DatatypeHelper valuesetDatatypeHelper;

    public XPolicyAttribute(IPolicyCmptTypeAttribute attribute, GeneratorModelContext model) {
        super(attribute, model);
        try {
            datatypeHelper = attribute.getIpsProject().findDatatypeHelper(attribute.getDatatype());
            valuesetDatatypeHelper = StdBuilderHelper.getDatatypeHelperForValueSet(attribute.getIpsProject(),
                    datatypeHelper);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public IPolicyCmptTypeAttribute getIpsObjectPartContainer() {
        return (IPolicyCmptTypeAttribute)super.getIpsObjectPartContainer();
    }

    /**
     * @return Returns the attribute.
     */
    public IPolicyCmptTypeAttribute getAttribute() {
        return getIpsObjectPartContainer();
    }

    public final DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }

    public final ValueDatatype getDatatype() {
        return (ValueDatatype)getDatatypeHelper().getDatatype();
    }

    public String getJavaClassName() {
        return addImport(getDatatypeHelper().getJavaClassName());
    }

    public final boolean isOverwrite() {
        return getAttribute().isOverwrite();
    }

    public boolean isGenerateField() {
        return getAttribute().getAttributeType() != AttributeType.DERIVED_ON_THE_FLY;
    }

    public boolean isGenerateGetter() {
        return true;
    }

    public boolean isGenerateSetter() {
        return !getAttribute().isDerived();
    }

    public boolean isGenerateDefaultInitialize() {
        return isOverwrite() && getAttribute().isChangeable();
    }

    public boolean isGenerateDefaultForDerivedAttribute() {
        try {
            IProductCmptTypeMethod formulaSignature = (getAttribute()).findComputationMethod(getIpsProject());
            return (!(getAttribute()).isProductRelevant() || formulaSignature == null || formulaSignature.validate(
                    getIpsProject()).containsErrorMsg());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public String getDefaultValueCode() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        JavaCodeFragment fragment = getDatatypeHelper().referenceOrSafeCopyIfNeccessary(memberVarName);
        addImport(fragment.getImportDeclaration());
        return fragment.getSourcecode();
    }

}
