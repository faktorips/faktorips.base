/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.InternationalStringDatatypeHelper;
import org.faktorips.codegen.dthelpers.ListOfValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
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
            if (isMultilingual()) {
                return new ListOfValueDatatypeHelper(new InternationalStringDatatypeHelper(false));
            } else {
                return new ListOfValueDatatypeHelper(super.getDatatypeHelper());
            }
        } else {
            if (isMultilingual()) {
                return new InternationalStringDatatypeHelper(false);
            } else {
                return super.getDatatypeHelper();
            }
        }
    }

    @Override
    public String getJavaClassName() {
        return super.getJavaClassName();
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
        if (isMultiValue()) {
            return super.getReferenceOrSafeCopyIfNecessary(memberVarName);
        } else {
            return memberVarName;
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

    /**
     * Returns <code>true</code> if the value extracted from MXL could be handled directly into the
     * generated field.
     * <p>
     * For the datatype {@link Datatype#STRING} there is no need of conversion of every single
     * element in the XML.
     */
    public boolean isMultiValueDirectXmlHandling() {
        return getAttribute().getDatatype().equals(Datatype.STRING.getQualifiedName());
    }

    public boolean isMultilingual() {
        return getAttribute().isMultilingual();
    }

    public XSingleValueOfMultiValueAttribute getSingleValueOfMultiValueAttribute() {
        return getModelNode(getAttribute(), XSingleValueOfMultiValueAttribute.class);
    }

    @Override
    public XProductAttribute getOverwrittenAttribute() {
        return (XProductAttribute)super.getOverwrittenAttribute();
    }

    /**
     * Checks whether the attribute is abstract or not. The attribute is abstract if its datatype is
     * an abstract datatype.
     */
    public boolean isAbstract() {
        return getDatatype().isAbstract();
    }

    public boolean isGenerateAbstractGetter() {
        return !isGeneratePublishedInterfaces() && !isGenerateContentCode() && !isOverwrite();
    }

    public boolean isGenerateInterfaceGetter() {
        return isGenerateContentCode() || !isOverwrite();
    }

    /**
     * The default value is set under following circumstances:
     * 
     * <li>For abstract attributes we never call setDefaultValue</li> <li>If the default value is
     * not <code>null</code> then call setDefaultValue</li> <li>If the attribute was configured in a
     * super type we always call setDefaultValue. To get this we could check if it is not abstract
     * and no content code is generated.</li>
     * 
     */
    public boolean isCallSetDefaultValue() {
        return !isAbstract() && (!isDefaultValueNull() || !isGenerateContentCode());
    }

    /**
     * For abstract attributes we only generate an abstract getter but no further elements hence
     * this method returns <code>false</code>.
     * 
     * If the attribute is overwriting an other attribute we only generate code if the super
     * attribute was abstract.
     */
    protected boolean isGenerateContentCode() {
        return !isAbstract() && (!isOverwrite() || getOverwrittenAttribute().isAbstract());
    }

}
