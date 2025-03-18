/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.codegen.AdditionalAnnotationsLocation;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.dthelpers.GenericValueDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.GenericValueDatatype;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.builder.DefaultBuilderSet;
import org.faktorips.devtools.model.builder.IJavaBuilderSet;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.builder.java.util.EnumTypeDatatypeHelper;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.builder.xmodel.ModelService;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.faktorips.devtools.model.builder.xmodel.enumtype.XEnumType;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.PersistenceSupportNames;
import org.faktorips.runtime.ICopySupport;
import org.faktorips.runtime.IDeltaSupport;
import org.faktorips.runtime.model.type.PolicyCmptType;
import org.faktorips.runtime.model.type.ProductCmptType;

/** @since 24.7 */
public abstract class JavaBuilderSet extends DefaultBuilderSet implements IJavaBuilderSet {

    /**
     * Configuration property that enables/disables the generation of a copy method.
     *
     * @see ICopySupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_COPY_SUPPORT = "generateCopySupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of delta computation.
     *
     * @see IDeltaSupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_DELTA_SUPPORT = "generateDeltaSupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of the visitor support.
     *
     * @see IDeltaSupport
     */
    public static final String CONFIG_PROPERTY_GENERATE_VISITOR_SUPPORT = "generateVisitorSupport"; //$NON-NLS-1$

    /**
     * Configuration property that is supposed to be used to read a configuration value from the
     * IIpsArtefactBuilderSetConfig object provided by the initialize method of an
     * IIpsArtefactBuilderSet instance.
     */
    public static final String CONFIG_PROPERTY_GENERATE_CHANGELISTENER = "generateChangeListener"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of JAXB support.
     */
    public static final String CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT = JaxbSupportVariant.STD_BUILDER_PROPERTY_GENERATE_JAXB_SUPPORT;

    /**
     * Configuration property contains the persistence provider implementation.
     * <p>
     * All persistence support IDs are defined in {@link PersistenceSupportNames}.
     */
    public static final String CONFIG_PROPERTY_PERSISTENCE_PROVIDER = PersistenceSupportNames.STD_BUILDER_PROPERTY_PERSISTENCE_PROVIDER;

    /**
     * Configuration property contains the kind of formula compiling.
     */
    public static final String CONFIG_PROPERTY_FORMULA_COMPILING = "formulaCompiling"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether toXml() methods should be
     * generated.
     */
    public static final String CONFIG_PROPERTY_TO_XML_SUPPORT = "toXMLSupport"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of serializable support on policy
     * components.
     *
     * @see Serializable
     */
    public static final String CONFIG_PROPERTY_GENERATE_SERIALIZABLE_POLICY_CMPTS_SUPPORT = "serializablePolicyCmpts"; //$NON-NLS-1$

    /**
     * Configuration property that enables/disables the generation of getter methods of
     * {@link ProductCmptType} attributes in the according {@link PolicyCmptType} class.
     */
    public static final String CONFIG_PROPERTY_GENERATE_CONVENIENCE_GETTERS = "generateConvenienceGetters"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate camel case constant
     * names with underscore separator or without. For example if this property is true, the
     * constant for the name checkAnythingRule would be generated as CHECK_ANYTHING_RULE, if the
     * property is false the constant name would be CHECKANYTHINGRUL.
     */
    public static final String CONFIG_PROPERTY_CAMELCASE_SEPARATED = "camelCaseSeparated"; //$NON-NLS-1$

    /**
     * Name of the configuration property that indicates whether to generate public interfaces or
     * not.
     * <p>
     * Although this property is defined in this abstraction it needs to be configured in the
     * extension point of every specific builder. If it is not specified as a configuration
     * definition of any builder, the default value is <code>true</code>.
     */
    public static final String CONFIG_PROPERTY_PUBLISHED_INTERFACES = "generatePublishedInterfaces"; //$NON-NLS-1$

    /**
     * Configuration property that defines additional annotations that are generated above all
     * generated methods of {@link IPolicyCmptType}, {@link IProductCmptType}, {@link IEnumType},
     * {@link ITableStructure} and {@link ITableContents}
     */
    public static final String CONFIG_PROPERTY_ADDITIONAL_ANNOTATIONS = "additionalAnnotations"; //$NON-NLS-1$

    /**
     * Configuration property that defines whether restrained modifiable methods should be included
     * when generating additional annotations. The default value is
     * {@link AdditionalAnnotationsLocation#GeneratedAndRestrainedModifiable} and therefore the
     * annotations should be generated above {@code @generated} as well as
     * {@code @restrainedmodifiable} methods.
     */
    public static final String CONFIG_PROPERTY_ADDITIONAL_ANNOTATIONS_LOCATION = "additionalAnnotationsLocation"; //$NON-NLS-1$

    /**
     * Configuration property that defines annotations that are not removed from generated methods
     * of {@link IPolicyCmptType}, {@link IProductCmptType}, {@link IEnumType},
     * {@link ITableStructure} and {@link ITableContents}
     */
    public static final String CONFIG_PROPERTY_RETAIN_ANNOTATIONS = "retainAnnotations"; //$NON-NLS-1$

    /**
     * Configuration property that defines whether and which builder classes should be generated.
     */
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR = "builderClasses";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_NONE = "None";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_ALL = "All";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_POLICY = "Policies only";
    public static final String CONFIG_PROPERTY_BUILDER_GENERATOR_PRODUCT = "Products only";

    /**
     * Configuration property that defines which variant of local date should be used (joda or
     * java8)
     */
    public static final String CONFIG_PROPERTY_LOCAL_DATE_HELPER_VARIANT = "localDateDatatypeHelperVariant"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_BASE_CLASS_POLICY_CMPT_TYPE = "baseClassPolicyComponent"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_BASE_CLASS_PRODUCT_CMPT_TYPE = "baseClassProductComponent"; //$NON-NLS-1$

    public static final String CONFIG_PROPERTY_CHANGES_OVER_TIME_NAMING_CONVENTION = "changesInTimeNamingConvention"; //$NON-NLS-1$

    /**
     * Configuration property that reduces the amount of generated comments.
     */
    public static final String CONFIG_PROPERTY_GENERATE_MINIMAL_JAVADOC = "minimalJavadoc"; //$NON-NLS-1$

    /**
     * Configuration property for the unify value set methods option.
     */
    public static final String CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS = "valueSetMethods";

    /**
     * Configuration property that defines whether the getEffectiveFromAsCalendar() method should
     * always be generated.
     */
    public static final String CONFIG_PROPERTY_GENERATE_GET_EFFECTIVE_FROM_AS_CALENDAR = "generateGetEffectiveFromAsCalendar"; //$NON-NLS-1$

    private ModelService modelService;

    private GeneratorModelContext generatorModelContext;

    /**
     * Registry for looking up helpers for data types.
     * <p>
     * Note that the registry is initialized when the IPS project is set (i.e. when
     * {@link #setIpsProject(IIpsProject)} is invoked).
     */
    private ProjectDatatypeHelperRegistry datatypeHelperRegistry;

    @Override
    public void clean(IProgressMonitor monitor) {
        super.clean(monitor);
        modelService.clear();
    }

    @Override
    public void initialize(IIpsArtefactBuilderSetConfig config) {
        modelService = new ModelService();
        generatorModelContext = new GeneratorModelContext(config, this, getIpsProject());
        super.initialize(config);
    }

    public <T extends AbstractGeneratorModelNode> T getModelNode(IIpsObjectPartContainer object, Class<T> type) {
        return modelService.getModelNode(object, type, generatorModelContext);
    }

    public ModelService getModelService() {
        return modelService;
    }

    public GeneratorModelContext getGeneratorModelContext() {
        return generatorModelContext;
    }

    @Override
    public boolean isGeneratePublishedInterfaces() {
        return generatorModelContext.getBaseGeneratorConfig().isGeneratePublishedInterfaces(getIpsProject());
    }

    @Override
    protected String getConfiguredAdditionalAnnotations() {
        return generatorModelContext.getBaseGeneratorConfig().getConfiguredAdditionalAnnotations();
    }

    @Override
    public String getAdditionalAnnotationsLocation() {
        return generatorModelContext.getBaseGeneratorConfig().getAdditionalAnnotationsLocation();
    }

    @Override
    protected String getConfiguredRetainedAnnotations() {
        return generatorModelContext.getBaseGeneratorConfig().getConfiguredRetainedAnnotations();
    }

    public String getValidationMessageBundleBaseName(IIpsSrcFolderEntry entry) {
        return generatorModelContext.getValidationMessageBundleBaseName(entry);
    }

    /**
     * Returns the qualified class name for the given datatype.
     *
     * @param datatype datatype to retrieve the class name for
     */
    public String getJavaClassName(Datatype datatype) {
        return getJavaClassName(datatype, true);
    }

    @Override
    public String getJavaClassName(Datatype datatype, boolean interfaces) {
        return switch (datatype) {
            case IPolicyCmptType policyCmptType -> getJavaClassNameForPolicyCmptType(policyCmptType, interfaces);
            case IProductCmptType productCmptType -> getJavaClassNameForProductCmptType(productCmptType, interfaces);
            default -> getDatatypeHelper(datatype).getJavaClassName();
        };
    }

    private String getJavaClassNameForPolicyCmptType(IPolicyCmptType type, boolean interfaces) {
        return getJavaClassName(type, interfaces, XPolicyCmptClass.class);
    }

    public String getJavaClassNameForProductCmptTypeIgnoreChangingOverTime(IProductCmptType type, boolean interfaces) {
        return getJavaClassName(type, interfaces, XProductCmptClass.class);
    }

    private String getJavaClassNameForProductCmptType(IProductCmptType type, boolean interfaces) {
        if (type.isChangingOverTime()) {
            return getJavaClassName(type, interfaces, XProductCmptGenerationClass.class);
        } else {
            return getJavaClassName(type, interfaces, XProductCmptClass.class);
        }
    }

    private <T extends XType> String getJavaClassName(IType type, boolean interfaces, Class<T> modelNodeClass) {
        return modelService.getModelNode(type, modelNodeClass, generatorModelContext)
                .getQualifiedName(BuilderAspect.getValue(interfaces));

    }

    @Override
    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        if (datatype instanceof EnumTypeDatatypeAdapter enumtypeadapter) {
            XEnumType enumType = getModelNode(enumtypeadapter.getEnumType(), XEnumType.class);
            return new EnumTypeDatatypeHelper(enumType, (EnumTypeDatatypeAdapter)datatype);
        }

        return datatypeHelperRegistry.getDatatypeHelper(datatype);
    }

    @Override
    public void setIpsProject(IIpsProject ipsProject) {
        super.setIpsProject(ipsProject);
        synchronized (ipsProject) {
            datatypeHelperRegistry = new ProjectDatatypeHelperRegistry(getIpsProject());
        }
    }

    public enum FormulaCompiling {

        Subclass,
        XML,
        Both;

        public boolean isCompileToSubclass() {
            return this == Subclass || this == Both;
        }

        public boolean isCompileToXml() {
            return this == XML || this == Both;
        }
    }

    /** Registry for looking up the {@link DatatypeHelper} for a {@link Datatype}. */
    protected static class ProjectDatatypeHelperRegistry {

        private Map<Datatype, DatatypeHelper> helperMap = new HashMap<>();

        public ProjectDatatypeHelperRegistry(IIpsProject ipsProject) {
            super();
            initialize(ipsProject);
        }

        /**
         * Returns the helper registered for the given data type or {@code null} if no helper is
         * registered for that type.
         */
        public DatatypeHelper getDatatypeHelper(Datatype datatype) {
            return helperMap.get(datatype);
        }

        /**
         * Initializes the registered helpers using (all) the helpers provided via the
         * {@link IIpsModelExtensions#getDatatypeHelperRegistry()} and the data types defined in the
         * given project.
         */
        private void initialize(IIpsProject ipsProject) {
            helperMap.putAll(IIpsModelExtensions.get().getDatatypeHelperRegistry().get());

            List<Datatype> definedDatatypes = ipsProject.getProperties().getDefinedDatatypes();
            for (Datatype datatype : definedDatatypes) {
                if (datatype instanceof GenericValueDatatype valueDatatype) {
                    registerHelper(valueDatatype, new GenericValueDatatypeHelper(valueDatatype));
                }
            }
        }

        private void registerHelper(Datatype datatype, DatatypeHelper helper) {
            helperMap.put(datatype, helper);
        }
    }

}
