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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.builder.AbstractProductCmptTypeBuilder;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IType;
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

    public BaseProductCmptTypeBuilder(StandardBuilderSet builderSet, String kindId,
            LocalizedStringsSet localizedStringsSet) {
        super(builderSet, kindId, localizedStringsSet);
    }

    @Override
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException {
        super.beforeBuild(ipsSrcFile, status);
    }

    public GenProdAssociation getGenerator(IProductCmptTypeAssociation a) throws CoreException {
        return getStandardBuilderSet().getGenerator(getProductCmptType()).getGenerator(a);
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

        GenPolicyCmptType genPolicyCmptType = getStandardBuilderSet().getGenerator(attribute.getPolicyCmptType());
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
        if (needGenerateCodeForAttribute(attribute)) {
            GenProductCmptTypeAttribute generator = getStandardBuilderSet().getGenerator(getProductCmptType())
                    .getGenerator(attribute);
            if (generator != null) {
                generator.generate(generatesInterface(), getIpsProject(), getMainTypeSection());
            }
        }
    }

    protected abstract boolean needGenerateCodeForAttribute(IProductCmptTypeAttribute attribute);

    protected boolean isUseTypesafeCollections() {
        return getStandardBuilderSet().isUseTypesafeCollections();
    }

    public StandardBuilderSet getStandardBuilderSet() {
        return (StandardBuilderSet)getBuilderSet();
    }

    protected final GenType getGenType(IType type) {
        try {
            return getStandardBuilderSet().getGenerator(type);
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
     *          testTypeDecimal = Decimal.valueOf(value);
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
     *          testTypeDecimal = Decimal.valueOf(value);
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

        boolean attributeFound = false;
        GenProductCmptType typeGenerator = getStandardBuilderSet().getGenerator(getProductCmptType());
        for (GenProductCmptTypeAttribute attributeGenerator : typeGenerator.getGenProdAttributes()) {
            if (needGenerateCodeForAttribute(attributeGenerator.getAttribute())) {
                if (attributeFound == false) {
                    generateDefineLocalVariablesForXmlExtraction(builder);
                    attributeFound = true;
                }
                attributeGenerator.generateDoInitPropertiesFromXml(builder);
            }
        }
        generateAdditionalDoInitPropertiesFromXml(builder, attributeFound);
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

}
