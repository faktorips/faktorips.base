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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IVersion;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.labels.LabelAndDescriptionPropertiesBuilder;
import org.faktorips.devtools.stdbuilder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for every Xtend generator model object. This class provides some useful methods e.g.
 * for localization and import statements and handles the common needs for every generator model
 * object.
 * <p>
 * The class is instantiated by the {@link ModelService}. You need to specify an constructor with
 * the arguments like
 * {@link #AbstractGeneratorModelNode(IIpsObjectPartContainer, GeneratorModelContext, ModelService)}
 * to be instantiated by the model service.
 * <p>
 * It is also very important that the model service may reuse the same instance for more than one
 * build. Hence the generator model node needs strictly to be stateless! Every information should be
 * called directly from the Faktor-IPS meta model objects. If you need to cache any information due
 * to performance issues you could use the {@link GeneratorModelContext} which could hold
 * information for the lifetime of a build cycle.
 */
public abstract class AbstractGeneratorModelNode {

    private final LocalizedStringsSet localizedStringSet;

    private final IIpsObjectPartContainer ipsObjectPartContainer;

    private final GeneratorModelContext modelContext;

    private final ModelService modelService;

    /**
     * This constructor is required in every generator model node. It defines
     * <ul>
     * <li>the {@link IIpsObjectPartContainer} this node is responsible for (or represents
     * respectively)</li>
     * <li>the {@link GeneratorModelContext} to handle additional generator information</li>
     * <li>the {@link ModelService} used to create new model node objects</li>
     * </ul>
     * <p>
     * The instances should be created by {@link ModelService} only. If any subclass does not have
     * this constructor, the {@link ModelService} will not be able to instantiate that class.
     * <p>
     * Model nodes create child nodes, one for each relevant IPS object part, in this constructor
     * (using the model service). So a model node is fully initialized once it is instantiated. An
     * {@link XPolicyCmptClass} for example creates model nodes for each attribute and association
     * and saves them in the respective lists. It will not, however, create dependent
     * {@link XProductCmptClass} nodes as a product component type is not a part (as in IPS object
     * part) of a policy component type.
     * <p>
     * As a result of this model nodes are immutable and no further initialization is required by
     * client code.
     * 
     * @param ipsObjectPartContainer The object this generator model object is responsible for
     * @param context The generator model context used to store context sensitive information
     * @param modelService The model service used to instantiate this model node and used to create
     *            other model nodes
     */
    public AbstractGeneratorModelNode(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        this(ipsObjectPartContainer, context, modelService, null);
    }

    /**
     * Call this constructor if you need to specify another {@link LocalizedStringsSet} as the
     * default one. Do not provide this constructor to your client. Define a constructor with the
     * arguments of
     * {@link #AbstractGeneratorModelNode(IIpsObjectPartContainer, GeneratorModelContext, ModelService)}
     * instead.
     * 
     * @see #AbstractGeneratorModelNode(IIpsObjectPartContainer, GeneratorModelContext,
     *      ModelService)
     * 
     * @param ipsObjectPartContainer The object this generator model object is responsible for
     * @param context The generator model context used to store context sensitive information
     * @param modelService The model service used to instantiate this model node and used to create
     *            other model nodes
     * @param localizedStringsSet The localized string set used to translate strings
     */
    protected AbstractGeneratorModelNode(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService, LocalizedStringsSet localizedStringsSet) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        this.modelContext = context;
        this.modelService = modelService;
        if (localizedStringsSet == null) {
            this.localizedStringSet = new LocalizedStringsSet(getClass());
        } else {
            this.localizedStringSet = localizedStringsSet;
        }
    }

    /**
     * @return Returns the ipsObjectPartContainer.
     */
    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return ipsObjectPartContainer;
    }

    /**
     * Returns the project of the {@link #getIpsObjectPartContainer()}
     * 
     * @return The {@link IIpsProject} of the corresponding {@link IIpsObjectPartContainer}
     */
    public IIpsProject getIpsProject() {
        return getIpsObjectPartContainer().getIpsProject();
    }

    /**
     * Returns the name of the {@link IIpsObjectPartContainer}
     * 
     * @return the name of the {@link IIpsObjectPartContainer}
     */
    public String getName() {
        return getIpsObjectPartContainer().getName();
    }

    /**
     * Returns the description of this {@link IIpsObjectPartContainer} in the language of the code
     * generator.
     * <p>
     * If there is no description in that locale, the description of the default locale will be
     * returned.
     * <p>
     * Returns an empty string if there is no default description as well or the given
     * {@link IIpsObjectPartContainer} does not support descriptions.
     * 
     * @throws NullPointerException If <code>ipsObjectPart</code> is <code>null</code>.
     */
    public String getDescription() {
        String description = ""; //$NON-NLS-1$
        if (getIpsObjectPartContainer() instanceof IDescribedElement) {
            IDescribedElement describedElement = (IDescribedElement)getIpsObjectPartContainer();
            IDescription generatorDescription = describedElement.getDescription(getLanguageUsedInGeneratedSourceCode());
            if (generatorDescription != null) {
                description = generatorDescription.getText();
            } else {
                description = IIpsModel.get().getMultiLanguageSupport().getDefaultDescription(describedElement);
            }
        }
        return description;
    }

    public boolean isDescribed() {
        return StringUtils.isNotBlank(getDescription());
    }

    public String getDescriptionForJDoc() {
        String description = getDescription();
        return StringUtils.isEmpty(description) ? "" : "<p>\n" + description;
    }

    /**
     * Checks whether the corresponding part has a since version or not. If the corresponding part
     * is no {@link IVersionControlledElement} this method always returns false.
     */
    public boolean hasSinceVersion() {
        if (getIpsObjectPartContainer() instanceof IVersionControlledElement) {
            IVersionControlledElement versionControlledElement = (IVersionControlledElement)getIpsObjectPartContainer();
            return versionControlledElement.isValidSinceVersion();
        } else {
            return false;
        }
    }

    /**
     * Returns the since version of the corresponding {@link IIpsObjectPartContainer} if there is
     * any version. This method returns <code>null</code> if {@link #hasSinceVersion()} returns
     * <code>false</code>.
     * 
     * @return The since version as string.
     */
    public String getSinceVersion() {
        if (hasSinceVersion()) {
            IVersion<?> sinceVersion = ((IVersionControlledElement)getIpsObjectPartContainer()).getSinceVersion();
            if (sinceVersion != null) {
                return sinceVersion.asString();
            }
        }
        return null;
    }

    protected <T extends AbstractGeneratorModelNode> boolean isCached(Class<T> type) {
        return modelContext.getGeneratorModelCache().isCached(this, type);
    }

    protected <T extends AbstractGeneratorModelNode> Set<T> getCachedObjects(Class<T> type) {
        return new LinkedHashSet<>(modelContext.getGeneratorModelCache().getCachedNodes(this, type));
    }

    protected <T extends AbstractGeneratorModelNode> void putToCache(T objectToCache) {
        modelContext.getGeneratorModelCache().put(objectToCache, this);
    }

    protected <T extends AbstractGeneratorModelNode> void putToCache(Set<T> objectsToCache) {
        for (T t : objectsToCache) {
            putToCache(t);
        }
    }

    /**
     * Returns the qualified class name for the given datatype.
     * 
     * @param datatype The datatype to retrieve the class name for. May be a value datatype as well
     *            as an {@link org.faktorips.devtools.model.type.IType IType}.
     * @param resolveGenerationNameIfApplicable In case the given datatype is an
     *            {@link IProductCmptType} this flag controls whether the generation class name
     *            instead of the product class name is returned. The generation class name however
     *            will only be returned if the {@link IProductCmptType} is changing over time.
     * @return the qualified class name for the datatype
     */
    protected String getJavaClassName(Datatype datatype, boolean resolveGenerationNameIfApplicable) {
        return getJavaClassName(datatype, resolveGenerationNameIfApplicable, false);
    }

    /**
     * Returns the qualified class name for the given datatype.
     * 
     * @param datatype The datatype to retrieve the class name for. May be a value datatype as well
     *            as an {@link org.faktorips.devtools.model.type.IType IType}.
     * @param resolveGenerationNameIfApplicable In case the given datatype is an
     *            {@link IProductCmptType} this flag controls whether the generation class name
     *            instead of the product class name is returned. The generation class name however
     *            will only be returned if the {@link IProductCmptType} is changing over time.
     * @param forceImplementation Used to force the implementation class of
     *            {@link org.faktorips.devtools.model.type.IType ITypes}. <code>true</code> to force
     *            the the class name resolver to always return the implementation class name.
     *            <code>false</code> to get the interface if it is generated or the implementation
     *            if no interfaces are generated.
     * 
     * @return the qualified class name for the datatype
     */
    protected String getJavaClassName(Datatype datatype,
            boolean resolveGenerationNameIfApplicable,
            boolean forceImplementation) {
        if (datatype instanceof IPolicyCmptType) {
            return getJavaClassNameForPolicyCmptType(datatype, forceImplementation);
        } else if (datatype instanceof IProductCmptType) {
            return getJavaClassNameForProductCmptType(datatype, resolveGenerationNameIfApplicable, forceImplementation);
        } else if (datatype.isVoid()) {
            return "void";
        } else {
            DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);
            return addImport(datatypeHelper.getJavaClassName());
        }
    }

    private String getJavaClassNameForPolicyCmptType(Datatype datatype, boolean forceImplementation) {
        return getModelNode((IPolicyCmptType)datatype, XPolicyCmptClass.class)
                .getSimpleName(BuilderAspect.getValue(!forceImplementation));
    }

    private String getJavaClassNameForProductCmptType(Datatype datatype,
            boolean useGeneration,
            boolean forceImplementation) {
        if (useGeneration && ((IProductCmptType)datatype).isChangingOverTime()) {
            return getModelNode((IProductCmptType)datatype, XProductCmptGenerationClass.class)
                    .getSimpleName(BuilderAspect.getValue(!forceImplementation));
        } else {
            return getModelNode((IProductCmptType)datatype, XProductCmptClass.class)
                    .getSimpleName(BuilderAspect.getValue(!forceImplementation));
        }
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param clazz The class you want to add to the import statements
     * @return the unqualified name of the type
     */
    public String addImport(Class<?> clazz) {
        getContext().addImport(clazz.getCanonicalName());
        return clazz.getSimpleName();
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name.
     * <p>
     * To avoid the import of classes in the default package and the import of primitive data types
     * ("int", "boolean") qualified class names without package (or without "." respectively) will
     * not be added as an imported. The given unqualified name will still be returned, however.
     * 
     * @param qName The qualified name of the type
     * @return the unqualified name of the type
     */
    public String addImport(String qName) {
        String className = getContext().addImport(qName);
        return className;
    }

    /**
     * Add the qualified name and element to the list of static import statements and return the
     * element.
     * 
     * @param qName The qualified name of the type
     * @param element The element in the class you want to import, may be '*'
     * @return the unqualified name of the imported element as given, for convenient use
     */
    public String addStaticImport(String qName, String element) {
        return getContext().addStaticImport(qName, element);
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param importDeclaration The import declaration contains a set of imports
     */
    public void addImport(ImportDeclaration importDeclaration) {
        for (String importStatement : importDeclaration.getImports()) {
            getContext().addImport(importStatement);
        }
    }

    /**
     * To remove an unused import.
     * 
     * @param importStatement the unused import statement
     * 
     * @return true if remove was successful
     */
    public boolean removeImport(String importStatement) {
        return getContext().removeImport(importStatement);
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle.
     * 
     * @param key the key that identifies the requested text
     * @param replacements an indicated region within the text is replaced by the string
     *            representation of this value
     */
    public String getLocalizedText(String key, Object... replacements) {
        return localizedStringSet.getString(key, getLanguageUsedInGeneratedSourceCode(), replacements);
    }

    /**
     * Returns the localized java doc for the given key. The key is added by the suffix _JAVADOC and
     * is provided by the corresponding localized string set.
     * 
     * @param key The key to search the localized java doc statement
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key) {
        return localizedJDoc(key, new Object[0]);
    }

    /**
     * Returns the localized java doc for the given key. The key is added by the suffix _JAVADOC and
     * is provided by the corresponding localized string set. Additionally you add replacement
     * parameters.
     * 
     * @see #localizedJDoc(String)
     * @param key the key to search the localized java doc statement
     * @param replacements the replacement parameters inserted in the localized text
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key, Object... replacements) {
        String text = getGeneratorConfig().isGenerateMinimalJavadoc()
                ? Arrays.toString(replacements).contains(getDescription()) ? getDescription() : ""
                : getLocalizedText(key + "_JAVADOC", replacements);

        return removeEmptyLines(text);
    }

    private String removeEmptyLines(String text) {
        return Arrays.stream(text.split("\\R")).filter(s -> !s.isBlank()).collect(Collectors.joining("\n"));
    }

    /**
     * Returns the localized string for the given key. The resulting string will be returned with
     * the prefix for comments ("//").
     */
    public String localizedComment(String key) {
        String text = getLocalizedText(key);
        return "// " + text;
    }

    /**
     * Returns the localized string for the given key. The resulting string will be returned with
     * the prefix for comments ("//").
     */
    public String localizedComment(String key, String replacement) {
        String text = getLocalizedText(key, replacement);
        return "// " + text;
    }

    /**
     * Returns the localized string for the given key. The resulting string will be returned with
     * the prefix for comments ("//").
     */
    public String localizedText(String key) {
        String text = getLocalizedText(key);
        return text;
    }

    /**
     * Returns the localized string for the given key and replacement parameter.
     */
    public String localizedText(String key, String replacement) {
        String text = getLocalizedText(key, replacement);
        return text;
    }

    /**
     * Returns the localized string for the given key and replacement parameters.
     */
    public String localizedText(String key, String replacement, String replacement2) {
        String text = getLocalizedText(key, replacement, replacement2);
        return text;
    }

    /**
     * Returns the language that variables, methods are named and and JDoc are written in.
     * <p>
     * Do not overwrite this method because it may be used in constructors
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public final Locale getLanguageUsedInGeneratedSourceCode() {
        return getGeneratorConfig().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the {@link GeneratorConfig} for the {@link #getIpsObjectPartContainer()
     * IIpsObjectPartContainer} this model node represents. If the container is not yet set, the
     * {@link GeneratorModelContext#getBaseGeneratorConfig() base configuration from the context} is
     * returned.
     */
    public GeneratorConfig getGeneratorConfig() {
        IIpsObjectPartContainer container = getIpsObjectPartContainer();
        if (container != null) {
            IIpsObject ipsObject = container.getIpsObject();
            if (ipsObject != null) {
                return GeneratorConfig.forIpsObject(ipsObject);
            }
        }
        return getContext().getBaseGeneratorConfig();
    }

    public GeneratorModelContext getContext() {
        return modelContext;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public <T extends AbstractGeneratorModelNode> T getModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            Class<T> nodeClass) {
        return getModelService().getModelNode(ipsObjectPartContainer, nodeClass, getContext());
    }

    public IJavaNamingConvention getJavaNamingConvention() {
        return getIpsProject().getJavaNamingConvention();
    }

    /**
     * 
     * Creates a list containing one {@link AbstractGeneratorModelNode} (of the given class) for
     * every {@link IIpsObjectPart} in the given list.
     * <p>
     * This method must be final because it may be called in constructor.
     * 
     * @param parts the parts to create {@link AbstractGeneratorModelNode nodes} for
     * @param nodeClass the expected concrete generator model class (subclass of
     *            {@link AbstractGeneratorModelNode}) that will be created for each part
     */
    protected final <T extends AbstractGeneratorModelNode> Set<T> initNodesForParts(
            Collection<? extends IIpsObjectPart> parts,
            Class<T> nodeClass) {
        Set<T> nodes = new LinkedHashSet<>();
        for (IIpsObjectPart part : parts) {
            nodes.add(getModelNode(part, nodeClass));
        }
        return nodes;
    }

    /**
     * Returns a string containing all annotations to the given {@link AnnotatedJavaElementType} and
     * the {@link IIpsObjectPartContainer} that is represented by this model node.
     * 
     * @see #getIpsObjectPartContainer()
     * 
     * @param type The type you want to generate
     * @return the string containing the annotations
     */
    public String getAnnotations(AnnotatedJavaElementType type) {
        List<IAnnotationGenerator> generators = getContext().getAnnotationGenerator(type);
        StringBuilder result = new StringBuilder(AnnotatedJavaElementType.ELEMENT_JAVA_DOC == type ? " * " : "");
        for (IAnnotationGenerator generator : generators) {
            if (!generator.isGenerateAnnotationFor(this)) {
                continue;
            }
            JavaCodeFragment annotationFragment = generator.createAnnotation(this);
            addImport(annotationFragment.getImportDeclaration());
            result.append(annotationFragment.getSourcecode());
        }
        if (result.length() <= 3) {
            return IpsStringUtils.EMPTY;
        } else {
            if (AnnotatedJavaElementType.ELEMENT_JAVA_DOC == type) {
                return new StringBuilder().append("*").append(System.lineSeparator()).append(result).toString();
            }
            return result.toString();
        }
    }

    /**
     * Returns all annotations like {@link #getAnnotations(AnnotatedJavaElementType)}, but only if
     * currently an interface is generated ({@code isGeneratingInterface} is {@code true} or
     * interfaces aren't generated at all(
     * {@link GeneratorConfig#isGeneratePublishedInterfaces(IIpsProject)} is {@code false} ).
     */
    public String getAnnotationsForPublishedInterface(AnnotatedJavaElementType type, boolean isGeneratingInterface) {
        if (isGeneratingInterface
                || !getGeneratorConfig().isGeneratePublishedInterfaces(getIpsObjectPartContainer().getIpsProject())) {
            return getAnnotations(type);
        } else {
            switch (type) {
                case POLICY_CMPT_DECL_CLASS:
                case PRODUCT_CMPT_DECL_CLASS:
                    return "";
                default:
                    return getAnnotations(AnnotatedJavaElementType.DEPRECATION);
            }
        }
    }

    public List<IJavaElement> getGeneratedJavaElements(IType javaType) {
        List<IJavaElement> result = new ArrayList<>();
        List<IGeneratedJavaElement> generatedJavaElements = modelContext.getGeneratedJavaElements(this);
        for (IGeneratedJavaElement generatedJavaElement : generatedJavaElements) {
            result.add(generatedJavaElement.getJavaElement(javaType));
        }
        return result;
    }

    // METHODS FOR TEMPLATES TO ADD GENERATED ARTIFACTS
    // These methods needs to be easy accessible from templates.

    /**
     * Adds a field with the given name to the list of generated fields.
     * <p>
     * Use to register a field that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * 
     * @param fieldName The name of the field
     * 
     * @return Returns simply the name to use in the template
     */
    public String field(String fieldName) {
        modelContext.addGeneratedJavaElement(this, new Field(fieldName));
        return fieldName;
    }

    /**
     * Adds a method with parameters to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * <p>
     * 
     * @param methodName the name of the generated method
     * @param parameterTypesAndNames The types and names for the parameters of the generated method.
     *            If you want to create the Method {@link String#indexOf(String, int)}, you would
     *            pass {@code "String", "str", "int", "fromIndex"}.
     * @return The method's definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is <em>setFoo(String
     *         bar)</em>
     */
    public String method(String methodName, String... parameterTypesAndNames) {
        return methodInternal(methodName, MethodParameter.arrayOf(parameterTypesAndNames));
    }

    public String method(String methodName, List<MethodParameter> parameters) {
        return methodInternal(methodName, parameters.toArray(new MethodParameter[parameters.size()]));
    }

    private String methodInternal(String methodName, MethodParameter... parameters) {
        MethodDefinition methodSignature = new MethodDefinition(methodName, parameters);
        modelContext.addGeneratedJavaElement(this, methodSignature);
        return methodSignature.getDefinition();
    }

    public String getMethodNameDoInitFromXml() {
        return "doInit" + StringUtils.capitalize(getName());
    }

    public String getMethodNameWriteToXml() {
        return "write" + StringUtils.capitalize(getName());
    }

    private IIpsSrcFolderEntry getIpsSrcFolderEntry() {
        IIpsObjectPathEntry entry = getIpsObjectPartContainer().getIpsSrcFile().getIpsPackageFragment().getRoot()
                .getIpsObjectPathEntry();
        if (entry instanceof IIpsSrcFolderEntry) {
            return (IIpsSrcFolderEntry)entry;
        }
        return null;
    }

    public String getDocumentationResourceBundleBaseName() {
        IIpsSrcFolderEntry srcEntry = getIpsSrcFolderEntry();
        if (srcEntry == null) {
            return IIpsObjectPathEntry.ERROR_CAST_EXCEPTION_PATH;
        }
        LabelAndDescriptionPropertiesBuilder lAdBuilder = getIpsProject().getIpsArtefactBuilderSet()
                .getBuildersByClass(LabelAndDescriptionPropertiesBuilder.class).get(0);
        return lAdBuilder.getResourceBundleBaseName(srcEntry);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + ipsObjectPartContainer + "]";
    }

    public DatatypeHelper getDatatypeHelper(Datatype datatype) {
        return getIpsProject().getDatatypeHelper(datatype);
    }

    public DatatypeHelper getDatatypeHelper(String qName) {
        return getIpsProject().findDatatypeHelper(qName);
    }
}
