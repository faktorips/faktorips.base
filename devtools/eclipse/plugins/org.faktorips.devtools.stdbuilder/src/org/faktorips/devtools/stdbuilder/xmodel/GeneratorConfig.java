/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel;

import java.util.Locale;

import org.faktorips.devtools.model.builder.AbstractBuilderSet;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.JaxbAnnGenFactory.IpsXmlAdapters;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet.FormulaCompiling;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.internal.AbstractModelObject;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.runtime.internal.ProductComponent;

/**
 * Central repository of all generator settings. Provides typesafe accessor methods for
 * {@link StandardBuilderSet} properties.
 */
public class GeneratorConfig {

    private final IIpsArtefactBuilderSetConfig config;
    private final IIpsProject ipsProject;

    public GeneratorConfig(IIpsArtefactBuilderSetConfig config, IIpsProject ipsProject) {
        this.config = config;
        this.ipsProject = ipsProject;
    }

    /**
     * Returns the {@link GeneratorConfig} from the {@link StandardBuilderSet} associated with the
     * given object's {@link IIpsPackageFragmentRoot}.
     * 
     * @see GeneratorModelContext#getGeneratorConfig(IIpsObject)
     */
    public static GeneratorConfig forIpsObject(IIpsObject ipsObject) {
        GeneratorModelContext generatorModelContext = GeneratorModelContext.forElement(ipsObject);
        return generatorModelContext == null ? null : generatorModelContext.getGeneratorConfig(ipsObject);
    }

    /**
     * Returns the {@link GeneratorConfig} from the {@link StandardBuilderSet} associated with the
     * given file's {@link IIpsPackageFragmentRoot}.
     * 
     * @see GeneratorModelContext#getGeneratorConfig(IIpsSrcFile)
     */
    public static GeneratorConfig forIpsSrcFile(IIpsSrcFile ipsSrcFile) {
        GeneratorModelContext generatorModelContext = GeneratorModelContext.forElement(ipsSrcFile);
        return generatorModelContext == null ? null : generatorModelContext.getGeneratorConfig(ipsSrcFile);
    }

    public boolean isGenerateChangeSupport() {
        return config.getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CHANGELISTENER)
                .booleanValue();
    }

    public FormulaCompiling getFormulaCompiling() {
        String kind = config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_FORMULA_COMPILING);
        try {
            return FormulaCompiling.valueOf(kind);
        } catch (IllegalArgumentException e) {
            // if the value is not set correctly we use Both as default value
            return FormulaCompiling.Both;
        }
    }

    /**
     * Returns whether the method names should be unified ({@link ValueSetMethods#Unified}), should
     * reflect the {@link ValueSetType} in their name ({@link ValueSetMethods#ByValueSetType}) or
     * both ({@link ValueSetMethods#Both}).
     * 
     * @see StandardBuilderSet#CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS
     */
    public ValueSetMethods getValueSetMethods() {
        String kind = config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS);
        try {
            return ValueSetMethods.valueOf(kind);
        } catch (IllegalArgumentException e) {
            // when in doubt use both
            return ValueSetMethods.Both;
        }
    }

    public boolean isGenerateBothMethodsForAllowedValues() {
        return getValueSetMethods().isBoth();
    }

    /**
     * Returns whether to generate camel case constant names with underscore separator or without.
     * For example if this property is true, the constant for the property
     * checkAnythingAndDoSomething would be generated as CHECK_ANYTHING_AND_DO_SOMETHING, if the
     * property is false the constant name would be CHECKANYTHINGANDDOSOMETHING.
     * 
     * @see StandardBuilderSet#CONFIG_PROPERTY_CAMELCASE_SEPARATED
     */
    public boolean isGenerateSeparatedCamelCase() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_CAMELCASE_SEPARATED);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean.booleanValue();
    }

    public boolean isGenerateDeltaSupport() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateCopySupport() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_COPY_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateVisitorSupport() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateToXmlSupport() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_TO_XML_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    /**
     * Returns <code>true</code> if the given project is configured to generate published
     * interfaces, <code>false</code> else.
     * 
     * This method uses the context's own {@link IIpsArtefactBuilderSetConfig}. This is important as
     * the project's {@link IIpsArtefactBuilderSetConfig config} may not be available during
     * initialization of the builder set.
     * 
     * @param ipsProject The project in which the property is configured
     * @return <code>true</code> if the project is configured to generate published interfaces,
     *             <code>false</code> if not.
     */
    public boolean isGeneratePublishedInterfaces(IIpsProject ipsProject) {
        return isGeneratePublishedInterfaces(config);
    }

    private boolean isGeneratePublishedInterfaces(IIpsArtefactBuilderSetConfig config) {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES);
        return propertyValueAsBoolean == null ? true : propertyValueAsBoolean.booleanValue();
    }

    public boolean isGenerateSerializablePolicyCmptSupport() {
        Boolean propertyValueAsBoolean = config.getPropertyValueAsBoolean(
                StandardBuilderSet.CONFIG_PROPERTY_GENERATE_SERIALIZABLE_POLICY_CMPTS_SUPPORT);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public boolean isGenerateConvenienceGetters() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS);
        return propertyValueAsBoolean == null ? true : propertyValueAsBoolean;
    }

    public boolean isGeneratePolicyBuilder() {
        String propertyValue = config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR);
        return (StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_ALL.equals(propertyValue)
                || StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_POLICY.equals(propertyValue));
    }

    public boolean isGenerateProductBuilder() {
        String propertyValue = config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR);
        return (StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_ALL.equals(propertyValue)
                || StandardBuilderSet.CONFIG_PROPERTY_BUILDER_GENERATOR_PRODUCT.equals(propertyValue));
    }

    public boolean isGenerateGetEffectiveFromAsCalendar() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_GET_EFFECTIVE_FROM_AS_CALENDAR);
        return propertyValueAsBoolean == null ? true : propertyValueAsBoolean.booleanValue();
    }

    public String getBaseClassPolicyCmptType() {
        String baseClass = config
                .getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE);
        return IpsStringUtils.isBlank(baseClass)
                ? JaxbSupportVariant.None == getJaxbSupport()
                ? AbstractModelObject.class.getName()
                        : IpsXmlAdapters.AbstractJaxbModelObject.qualifiedNameFrom(getJaxbSupport())
                        : baseClass;
    }

    public String getBaseClassProductCmptType() {
        String baseClass = config
                .getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_BASE_CLASS_PRODUCT_CMPT_TYPE);
        return IpsStringUtils.isBlank(baseClass) ? ProductComponent.class.getName() : baseClass;
    }

    public IChangesOverTimeNamingConvention getChangesOverTimeNamingConvention() {
        String changesOverTimeNamingConventionId = config
                .getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION);
        return ipsProject.getIpsModel().getChangesOverTimeNamingConvention(changesOverTimeNamingConventionId);
    }

    /**
     * Returns whether JAXB support is to be generated by this builder.
     *
     * @deprecated for removal since 23.6; use {@link #getJaxbSupport()} instead
     */
    @Deprecated(since = "23.6", forRemoval = true)
    public boolean isGenerateJaxbSupport() {
        return getJaxbSupport() != JaxbSupportVariant.None;
    }

    /**
     * Returns the version of JAXB support to be generated by this builder or
     * {@link JaxbSupportVariant#None} if no JAXB support shall be generated.
     *
     * @since 23.6
     */
    public JaxbSupportVariant getJaxbSupport() {
        return JaxbSupportVariant
                .of(config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT));
    }

    /**
     * Minimal Javadoc includes functional Javadoc tags like {@code @generated}, as well as
     * documentation defined in the model.
     */
    public boolean isGenerateMinimalJavadoc() {
        Boolean propertyValueAsBoolean = config
                .getPropertyValueAsBoolean(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_MINIMAL_JAVADOC);
        return propertyValueAsBoolean == null ? false : propertyValueAsBoolean;
    }

    public Locale getLanguageUsedInGeneratedSourceCode() {
        String localeString = config.getPropertyValueAsString(StandardBuilderSet.CONFIG_PROPERTY_GENERATOR_LOCALE);
        if (localeString == null) {
            return Locale.ENGLISH;
        }
        return AbstractBuilderSet.getLocale(localeString);
    }

    public String getConfiguredAdditionalAnnotations() {
        return getStringProperty(StandardBuilderSet.CONFIG_PROPERTY_ADDITIONAL_ANNOTATIONS, IpsStringUtils.EMPTY);
    }

    public String getConfiguredRetainedAnnotations() {
        return getStringProperty(StandardBuilderSet.CONFIG_PROPERTY_RETAIN_ANNOTATIONS, IpsStringUtils.EMPTY);
    }

    private String getStringProperty(String propertyKey, String defaultValue) {
        String propertyValueAsString = config.getPropertyValueAsString(propertyKey);
        return propertyValueAsString == null ? defaultValue : propertyValueAsString;
    }
}
