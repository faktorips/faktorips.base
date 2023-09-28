/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.codegen.dthelpers.InternationalStringDatatypeHelper;
import org.faktorips.codegen.dthelpers.ListOfValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.XAttribute;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.values.DefaultInternationalString;
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

    public String addImport() {
        return addImport(getQualifiedJavaClassName());
    }

    @Override
    public String getJavaClassName() {
        return super.getJavaClassName();
    }

    public String getReturnType() {
        if (getDatatype() instanceof ListOfTypeDatatype && getDatatype().isAbstract()) {
            return addImport(List.class) + "<? extends " + addImport(
                    getDatatypeHelper(((ListOfTypeDatatype)getDatatype()).getBasicDatatype()).getJavaClassName()) + ">";
        } else {
            return getJavaClassName();
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
        if (isMultiValue()) {
            return super.getReferenceOrSafeCopyIfNecessary(memberVarName);
        } else {
            return memberVarName;
        }
    }

    private JavaCodeFragment newListInitializer(List<String> values) {
        JavaCodeFragmentBuilder builder = new JavaCodeFragmentBuilder();
        builder.appendClassName(ListUtil.class.getName());
        builder.append(".");
        builder.append("newList");
        builder.appendParameters(values.toArray(new String[0]));
        return builder.getFragment();
    }

    public String getNewMultiValueInstanceWithDefaultValue() {
        String[] defaultValues = MultiValueHolder.Factory.getSplitMultiValue(getAttribute().getDefaultValue());
        List<String> defaultValueCodes = new ArrayList<>(defaultValues.length);
        for (String defaultValue : defaultValues) {
            JavaCodeFragment fragment;
            if (defaultValue == null && isMultilingual()) {
                fragment = new JavaCodeFragment(DefaultInternationalString.class.getSimpleName() + ".EMPTY");
            } else {
                fragment = super.getDatatypeHelper().newInstance(defaultValue);
            }
            addImport(fragment.getImportDeclaration());
            defaultValueCodes.add(fragment.getSourcecode());
        }

        JavaCodeFragment result = newListInitializer(defaultValueCodes);
        addImport(result.getImportDeclaration());
        return result.getSourcecode();
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

    public boolean isGenerateInterfaceGetter() {
        return isGenerateContentCode() || !isOverwrite();
    }

    /**
     * The default value is set under following circumstances:
     *
     * <ul>
     * <li>For abstract attributes we never call setDefaultValue</li>
     * <li>If the default value is not <code>null</code> then call setDefaultValue</li>
     * <li>If the attribute was configured in a super type we always call setDefaultValue. To get
     * this we could check if it is not abstract and no content code is generated.</li>
     * </ul>
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
