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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.StdBuilderHelper;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelObject;

public class GPolicyAttribute extends AbstractGeneratorModelObject {

    private DatatypeHelper datatypeHelper;
    private Object valuesetDatatypeHelper;
    private final GPolicyCmpt gPolicyCmpt;

    public GPolicyAttribute(GPolicyCmpt gPolicyCmpt, IPolicyCmptTypeAttribute attribute, XpandBuilder builder) {
        super(attribute, builder);
        this.gPolicyCmpt = gPolicyCmpt;
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
     * @return Returns the gPolicyCmpt.
     */
    public GPolicyCmpt getGPolicyCmpt() {
        return gPolicyCmpt;
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

    public String getDefaultValueCode() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
    }

    @Override
    public List<IJavaElement> getGeneratedJavaElementsForImplementation(IType generatedJavaType, IIpsElement ipsElement) {
        // TODO Auto-generated method stub
        return null;
    }

}
