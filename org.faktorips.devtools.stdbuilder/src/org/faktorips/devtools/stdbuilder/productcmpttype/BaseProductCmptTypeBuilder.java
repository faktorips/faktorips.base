/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypePart;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.policycmpttype.GenPolicyCmptType;
import org.faktorips.devtools.stdbuilder.policycmpttype.attribute.GenChangeableAttribute;
import org.faktorips.devtools.stdbuilder.productcmpttype.association.GenProdAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.attribute.GenProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.type.GenType;
import org.faktorips.util.LocalizedStringsSet;
import org.w3c.dom.Element;

/**
 * 
 * 
 * @author Jan Ortmann, Daniel Hohenberger
 */
public abstract class BaseProductCmptTypeBuilder extends AbstractProductCmptTypeBuilder {

    public BaseProductCmptTypeBuilder(StandardBuilderSet builderSet, LocalizedStringsSet localizedStringsSet) {
        super(builderSet, localizedStringsSet);
    }

    @Override
    public StandardBuilderSet getBuilderSet() {
        return (StandardBuilderSet)super.getBuilderSet();
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        return getBuilderSet().getGenerator(getProductCmptType()).getGenerator(a);
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
    protected void generateCodeForPolicyCmptTypeAttribute(IPolicyCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder) throws CoreException {

        GenPolicyCmptType genPolicyCmptType = getBuilderSet().getGenerator(attribute.getPolicyCmptType());
        GenChangeableAttribute generator = (GenChangeableAttribute)genPolicyCmptType.getGenerator(attribute);
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
    protected final void generateCodeForProductCmptTypeAttribute(IProductCmptTypeAttribute attribute,
            DatatypeHelper datatypeHelper,
            JavaCodeFragmentBuilder fieldsBuilder,
            JavaCodeFragmentBuilder methodsBuilder,
            JavaCodeFragmentBuilder constantBuilder) throws CoreException {
        if (attribute.isChangingOverTime() == isChangingOverTimeContainer()) {
            GenProductCmptTypeAttribute generator = getBuilderSet().getGenerator(getProductCmptType()).getGenerator(
                    attribute);
            if (generator != null) {
                generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
            }
        }
    }

    /**
     * Returns true if this builder is responsible for a {@link IPropertyValueContainer} that
     * changes over time. Concrete: For PropertyComponentGenerations this should return true, for
     * ProductComponents returns false;
     * 
     * @return true if the generated container does change over time.
     */
    protected abstract boolean isChangingOverTimeContainer();

    protected boolean isUseTypesafeCollections() {
        return getBuilderSet().isUseTypesafeCollections();
    }

    protected final GenType getGenType(IType type) {
        try {
            return getBuilderSet().getGenerator(type);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitPropertiesFromXml(Map configMap) {
     *      super.doInitPropertiesFromXml(configMap);
     *      Element configElement = null;
     *      String value = null;
     *      configElement = (Element)configMap.get(&quot;testTypeDecimal&quot;);
     *      if (configElement != null) {
     *          value = ValueToXmlHelper.getValueFromElement(configElement, &quot;Value&quot;);
     *          this.testTypeDecimal = Decimal.valueOf(value);
     *      }
     *  }
     * </pre>
     * 
     * Java 5 code sample
     * 
     * <pre>
     *  [javadoc]
     *  protected void doInitPropertiesFromXml(Map&lt;String, Element&gt; configMap) {
     *      super.doInitPropertiesFromXml(configMap);
     *      Element configElement = null;
     *      String value = null;
     *      configElement = configMap.get(&quot;testTypeDecimal&quot;);
     *      if (configElement != null) {
     *          value = ValueToXmlHelper.getValueFromElement(configElement, &quot;Value&quot;);
     *          this.testTypeDecimal = Decimal.valueOf(value);
     *      }
     *  }
     * </pre>
     */
    protected void generateMethodDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(Modifier.PROTECTED, "void", "doInitPropertiesFromXml", new String[] { "configMap" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new String[] { isUseTypesafeCollections() ? Map.class.getName() + "<" + String.class.getName() + ", " //$NON-NLS-1$//$NON-NLS-2$
                        + Element.class.getName() + ">" : Map.class.getName() }); //$NON-NLS-1$

        builder.appendln("super.doInitPropertiesFromXml(configMap);"); //$NON-NLS-1$

        boolean reusableLocalVariablesGenerated = false;
        GenProductCmptType typeGenerator = getBuilderSet().getGenerator(getProductCmptType());
        for (GenProductCmptTypeAttribute attributeGenerator : typeGenerator.getGenProdAttributes()) {
            if (attributeGenerator.getAttribute().isChangingOverTime() == isChangingOverTimeContainer()) {
                if (reusableLocalVariablesGenerated == false) {
                    generateDefineLocalVariablesForXmlExtraction(builder);
                    reusableLocalVariablesGenerated = true;
                }
                attributeGenerator.generateDoInitPropertiesFromXml(builder);
            }
        }
        generateAdditionalDoInitPropertiesFromXml(builder, reusableLocalVariablesGenerated);
        builder.methodEnd();
    }

    protected void generateDefineLocalVariablesForXmlExtraction(JavaCodeFragmentBuilder builder) {
        builder.appendClassName(Element.class);
        builder.appendln(" configElement = null;"); //$NON-NLS-1$
        builder.appendClassName(String.class);
        builder.appendln(" value = null;"); //$NON-NLS-1$
    }

    /**
     * Method to generate additional initialization of other properties like config elements.
     * 
     * @param builder The builder to add the source code to
     * @param localVariablesAlreadyGenerated a flag that indicates weather there local variables for
     *            configElement and value are already generated
     * @throws CoreException in case of a core exception
     */
    protected void generateAdditionalDoInitPropertiesFromXml(JavaCodeFragmentBuilder builder,
            boolean localVariablesAlreadyGenerated) throws CoreException {
        // default do nothing
    }

    protected void generateMethodWritePropertiesToXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.javaDoc(getJavaDocCommentForOverriddenMethod(), ANNOTATION_GENERATED);
        appendOverrideAnnotation(builder, false);
        builder.methodBegin(Modifier.PROTECTED, "void", "writePropertiesToXml", new String[] { "element" }, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new String[] { Element.class.getName() });

        /*
         * Only call super, if the {@link ProductCmptType} has a supertype. See
         * ProductComponent#toXml() for clarification.
         */
        if (superCallRequired()) {
            builder.appendln("super.writePropertiesToXml(element);"); //$NON-NLS-1$            
        } else {
            builder.appendln("//Do not call super. See overridden method for clarification.");
        }

        GenProductCmptType typeGenerator = getBuilderSet().getGenerator(getProductCmptType());
        boolean reusableLocalVariablesGenerated = false;
        for (GenProductCmptTypeAttribute attributeGenerator : typeGenerator.getGenProdAttributes()) {
            if (attributeGenerator.getAttribute().isChangingOverTime() == isChangingOverTimeContainer()) {
                if (!reusableLocalVariablesGenerated) {
                    reusableLocalVariablesGenerated = true;
                    builder.appendClassName(Element.class);
                    builder.append(" attributeElement= null;");
                }
                attributeGenerator.generateWritePropertyToXml(builder);
            }
        }
        generateAdditionalWritePropertiesToXml(builder);
        builder.methodEnd();
    }

    /**
     * @return true if this builders {@link ProductCmptType} has a supertype.
     */
    private boolean superCallRequired() {
        return getProductCmptType().hasSupertype();
    }

    /**
     * 
     * @param builder the builder to append the code to
     * @throws CoreException in case an error occurs
     */
    protected void generateAdditionalWritePropertiesToXml(JavaCodeFragmentBuilder builder) throws CoreException {
        // default do nothing
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        IType type = null;
        if (ipsObjectPartContainer instanceof IType) {
            type = (IType)ipsObjectPartContainer;
        } else if (ipsObjectPartContainer instanceof IProductCmptProperty) {
            IProductCmptProperty productCmptProperty = (IProductCmptProperty)ipsObjectPartContainer;
            if (productCmptProperty.isChangingOverTime() == isChangingOverTimeContainer()) {
                type = productCmptProperty.getType();
            } else {
                return;
            }
        } else if (ipsObjectPartContainer instanceof ITypePart) {
            type = ((ITypePart)ipsObjectPartContainer).getType();
        } else if (ipsObjectPartContainer instanceof ITableStructureUsage) {
            type = ((ITableStructureUsage)ipsObjectPartContainer).getProductCmptType();
        } else {
            return;
        }

        org.eclipse.jdt.core.IType javaType = getGeneratedJavaTypes(type).get(0);
        if (generatesInterface()) {
            getGenType(type).getGeneratedJavaElementsForPublishedInterface(javaElements, javaType,
                    ipsObjectPartContainer);
        } else {
            getGenType(type).getGeneratedJavaElementsForImplementation(javaElements, javaType, ipsObjectPartContainer);
        }
    }

}
