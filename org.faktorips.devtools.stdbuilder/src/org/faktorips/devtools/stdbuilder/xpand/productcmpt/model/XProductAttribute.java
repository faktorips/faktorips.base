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
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.ListOfValueDatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XAttribute;
import org.faktorips.values.ListUtil;

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
            return getNewMultiValueInstanceWithDefaultValue();
        } else {
            return super.getDefaultValueCode();
        }
    }

    public boolean isDefaultValueNull() {
        return getAttribute().getDefaultValue() == null && !getAttribute().isMultiValueAttribute();
    }

    /**
     * {@inheritDoc}
     * <p>
     * For single value product attributes we never generate a safe copy.
     * 
     */
    @Override
    public String getReferenceOrSafeCopyIfNecessary(String memberVarName) {
        if (!isMultiValue()) {
            return memberVarName;
        } else {
            return super.getReferenceOrSafeCopyIfNecessary(memberVarName);
        }
    }

    private JavaCodeFragment newListInitializer(String defaultValue) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendClassName(ListUtil.class.getName());
        builder.append(".");
        builder.append("newList");
        builder.appendParameters(new String[] { defaultValue });
        return builder.getFragment();

    }

    public String getNewMultiValueInstanceWithDefaultValue() {
        if (getAttribute().getDefaultValue() == null) {
            JavaCodeFragment newInstance = getDatatypeHelper().newInstance("");
            addImport(newInstance.getImportDeclaration());
            return newInstance.getSourcecode();
        } else {
            JavaCodeFragment defaultValueCode = super.getDatatypeHelper().newInstance(getAttribute().getDefaultValue());
            addImport(defaultValueCode.getImportDeclaration());
            JavaCodeFragment fragment = newListInitializer(defaultValueCode.getSourcecode());
            addImport(fragment.getImportDeclaration());
            return fragment.getSourcecode();
        }
    }

    public String getNewMultiValueInstance() {
        JavaCodeFragment newInstance = getDatatypeHelper().newInstance("");
        addImport(newInstance.getImportDeclaration());
        return newInstance.getSourcecode();
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
