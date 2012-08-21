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

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.ListOfValueDatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;

/**
 * This is the generator model node for {@link IProductCmptTypeAttribute}.
 * <p>
 * In addition to {@link XAttribute} this model node handles the special cases for multi value
 * attributes. For example it does return an special data type so you could generate most of the
 * code just like for single value data type. If you need the data type of the single value just
 * take the {@link XSingleValueOfMultiValueAttribute} model node.
 * 
 * @author dirmeier
 */
public class XProductAttribute extends XAttribute {

    /**
     * Default constructor as expected by {@link ModelService}
     */
    public XProductAttribute(IProductCmptTypeAttribute attribute, GeneratorModelContext context,
            ModelService modelService) {
        super(attribute, context, modelService);
    }

    @Override
    public IProductCmptTypeAttribute getIpsObjectPartContainer() {
        return (IProductCmptTypeAttribute)super.getIpsObjectPartContainer();
    }

    @Override
    public IProductCmptTypeAttribute getAttribute() {
        return (IProductCmptTypeAttribute)super.getAttribute();
    }

    @Override
    public DatatypeHelper getDatatypeHelper() {
        if (isMultiValue()) {
            return new ListOfValueDatatypeHelper((ValueDatatype)super.getDatatypeHelper().getDatatype());
        } else {
            return super.getDatatypeHelper();
        }
    }

    @Override
    public String getJavaClassName() {
        if (isMultiValue()) {
            JavaCodeFragment declarationJavaTypeFragment = ((ListOfValueDatatypeHelper)getDatatypeHelper())
                    .getDeclarationJavaTypeFragment();
            addImport(declarationJavaTypeFragment.getImportDeclaration());
            return declarationJavaTypeFragment.getSourcecode();
        } else {
            return super.getJavaClassName();
        }
    }

    @Override
    public String getDefaultValueCode() {
        if (isMultiValue()) {
            return getNewInstance();
        } else {
            return super.getDefaultValueCode();
        }
    }

    public boolean isMultiValue() {
        return getAttribute().isMultiValueAttribute();
    }

    public XSingleValueOfMultiValueAttribute getSingleValueOfMultiValueAttribute() {
        return getModelNode(getAttribute(), XSingleValueOfMultiValueAttribute.class);
    }

    public boolean isChangingOverTime() {
        return getAttribute().isChangingOverTime();
    }
}
