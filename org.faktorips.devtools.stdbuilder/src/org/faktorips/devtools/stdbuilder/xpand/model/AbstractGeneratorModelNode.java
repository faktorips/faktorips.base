/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.internal.model.ipsobject.IVersionControlledElement;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet.FormulaCompiling;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for every Xpand generator model object. This class provides some useful methods e.g.
 * for localization and import statements and handles the common needs for every generator model
 * object.
 * <p>
 * The class is instantiated by the {@link ModelService}. You need to specify an constructor with
 * the arguments like
 * {@link #AbstractGeneratorModelNode(IIpsObjectPartContainer, GeneratorModelContext, ModelService)}
 * to be isntantiated by the model service.
 * <p>
 * It is also very important that the model service may reuse the same instance for more than one
 * build. Hence the generator model node needs strictly to be stateless! Every information should be
 * called directly from the Faktor-IPS meta model objects. If you need to cache any information due
 * to performance issues you could use the {@link GeneratorModelContext} which could hold
 * information for the lifetime of a build cycle.
 * 
 * @author dirmeier, widmaier
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
     * @throws NullPointerException If <tt>ipsObjectPart</tt> is <tt>null</tt>.
     */
    public String getDescription() {
        String description = ""; //$NON-NLS-1$
        if (getIpsObjectPartContainer() instanceof IDescribedElement) {
            IDescribedElement describedElement = (IDescribedElement)getIpsObjectPartContainer();
            IDescription generatorDescription = describedElement.getDescription(getLanguageUsedInGeneratedSourceCode());
            if (generatorDescription != null) {
                description = generatorDescription.getText();
            } else {
                description = IpsPlugin.getMultiLanguageSupport().getDefaultDescription(describedElement);
            }
        }
        return description;
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
            return ((IVersionControlledElement)getIpsObjectPartContainer()).getSinceVersion().asString();
        }
        return null;
    }

    protected <T extends AbstractGeneratorModelNode> boolean isCached(Class<T> type) {
        return modelContext.getGeneratorModelCache().isCached(this, type);
    }

    protected <T extends AbstractGeneratorModelNode> Set<T> getCachedObjects(Class<T> type) {
        return new LinkedHashSet<T>(modelContext.getGeneratorModelCache().getCachedNodes(this, type));
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
     *            as an {@link org.faktorips.devtools.core.model.type.IType IType}.
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
     *            as an {@link org.faktorips.devtools.core.model.type.IType IType}.
     * @param resolveGenerationNameIfApplicable In case the given datatype is an
     *            {@link IProductCmptType} this flag controls whether the generation class name
     *            instead of the product class name is returned. The generation class name however
     *            will only be returned if the {@link IProductCmptType} is changing over time.
     * @param forceImplementation Used to force the implementation class of
     *            {@link org.faktorips.devtools.core.model.type.IType ITypes}. <code>true</code> to
     *            force the the class name resolver to always return the implementation class name.
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
        return getModelNode((IPolicyCmptType)datatype, XPolicyCmptClass.class).getSimpleName(
                BuilderAspect.getValue(!forceImplementation));
    }

    private String getJavaClassNameForProductCmptType(Datatype datatype,
            boolean useGeneration,
            boolean forceImplementation) {
        if (useGeneration && ((IProductCmptType)datatype).isChangingOverTime()) {
            return getModelNode((IProductCmptType)datatype, XProductCmptGenerationClass.class).getSimpleName(
                    BuilderAspect.getValue(!forceImplementation));
        } else {
            return getModelNode((IProductCmptType)datatype, XProductCmptClass.class).getSimpleName(
                    BuilderAspect.getValue(!forceImplementation));
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

    public String addImport(org.eclipse.xtend.typesystem.Type type) {
        return type.getName();
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name.
     * <p>
     * To support qualified names provided by a template, this method is also able to handle xpand
     * namespace syntax. For example the string <code>java::util::Map</code> is converted to
     * <code>java.util.Map</code>
     * <p>
     * To avoid the import of classes in the default package and the import of primitive data types
     * ("int", "boolean") qualified class names without package (or without "." respectively) will
     * not be added as an imported. The given unqualified name will still be returned, however.
     * 
     * @param qName The qualified name of the type
     * @return the unqualified name of the type
     */
    public String addImport(String qName) {
        String javaQName;
        if (qName.indexOf(SyntaxConstants.NS_DELIM) != -1) {
            javaQName = qName.replaceAll(SyntaxConstants.NS_DELIM, ".");
        } else {
            javaQName = qName;
        }
        String className = getContext().addImport(javaQName);
        return className;
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
     * is provided by the corresponding localized string set. Additionally you add one replacement
     * parameter.
     * 
     * @see #localizedJDoc(String, Object...)
     * @param key The key to search the localized java doc statement
     * @param replacement The replacement parameter inserted in the localized text
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key, Object replacement) {
        return localizedJDoc(key, new Object[] { replacement });
    }

    /**
     * Returns the localized java doc for the given key. The key is added by the suffix _JAVADOC and
     * is provided by the corresponding localized string set. Additionally you add two replacement
     * parameter.
     * 
     * @see #localizedJDoc(String, Object...)
     * @param key The key to search the localized java doc statement
     * @param replacement1 The first replacement parameter inserted in the localized text
     * @param replacement2 The second replacement parameter inserted in the localized text
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key, Object replacement1, Object replacement2) {
        return localizedJDoc(key, new Object[] { replacement1, replacement2 });
    }

    /**
     * Returns the localized java doc for the given key. The key is added by the suffix _JAVADOC and
     * is provided by the corresponding localized string set. Additionally you add two replacement
     * parameter.
     * 
     * @see #localizedJDoc(String, Object...)
     * @param key The key to search the localized java doc statement
     * @param replacement1 The first replacement parameter inserted in the localized text
     * @param replacement2 The second replacement parameter inserted in the localized text
     * @param replacement3 The third replacement parameter inserted in the localized text
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key, Object replacement1, Object replacement2, Object replacement3) {
        return localizedJDoc(key, new Object[] { replacement1, replacement2, replacement3 });
    }

    /**
     * Returns the localized java doc for the given key. The key is added by the suffix _JAVADOC and
     * is provided by the corresponding localized string set. Additionally you add replacement
     * parameters.
     * <p>
     * Because XPAND cannot call methods with varargs we need the other methods with single
     * arguments
     * 
     * @see #localizedJDoc(String)
     * @param key The key to search the localized java doc statement
     * @return the localized java doc statement
     */
    public String localizedJDoc(String key, Object... replacements) {
        String text = getLocalizedText(key + "_JAVADOC", replacements); //$NON-NLS-1$
        return text;
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
        return getContext().getLanguageUsedInGeneratedSourceCode();
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
     * Returns the kind of formula compiling.
     * 
     * @see FormulaCompiling
     */
    public FormulaCompiling getFormulaCompiling() {
        return getContext().getFormulaCompiling();
    }

    /**
     * Returns whether or not methods for delta-support should be added to generated classes.
     */
    public boolean isGenerateDeltaSupport() {
        return getContext().isGenerateDeltaSupport();
    }

    /**
     * Returns whether or not methods for copy-support should be added to generated classes.
     */
    public boolean isGenerateCopySupport() {
        return getContext().isGenerateCopySupport();
    }

    /**
     * Returns whether or not methods for the visitor-support should be added to generated classes.
     */
    public boolean isGenerateVisitorSupport() {
        return getContext().isGenerateVisitorSupport();
    }

    /**
     * Returns whether or not published interfaces should be generated.
     */
    public boolean isGeneratePublishedInterfaces() {
        return getContext().isGeneratePublishedInterfaces(getIpsObjectPartContainer().getIpsProject());
    }

    /**
     * Returns whether or not change support should be generated.
     */
    public boolean isGenerateChangeSupport() {
        return getContext().isGenerateChangeSupport();
    }

    /**
     * Returns whether or not serializable should be generated.
     */
    public boolean isGenerateSerializablePolicyCmptsSupport() {
        return getContext().isGenerateSerializablePolicyCmptSupport();
    }

    /**
     * Returns whether or not getter methods of {@link ProductCmptType} attributes in the according
     * {@link IPolicyCmptType} class should be generated.
     */
    public boolean isGenerateConvenienceGetters() {
        return getContext().isGenerateConvenienceGetters();
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
        return getContext().isGenerateSeparatedCamelCase();
    }

    /**
     */
    public boolean isGenerateToXmlSupport() {
        return getContext().isGenerateToXmlSupport();
    }

    /**
     * 
     */
    public boolean isGeneratePolicyBuilder() {
        return getContext().isGeneratePolicyBuilder();
    }

    /**
     * 
     */
    public boolean isGenerateProductBuilder() {
        return getContext().isGenerateProductBuilder();
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
    protected final <T extends AbstractGeneratorModelNode> Set<T> initNodesForParts(Collection<? extends IIpsObjectPart> parts,
            Class<T> nodeClass) {
        Set<T> nodes = new LinkedHashSet<T>();
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
        StringBuilder result = new StringBuilder("");
        for (IAnnotationGenerator generator : generators) {
            if (!generator.isGenerateAnnotationFor(this)) {
                continue;
            }
            JavaCodeFragment annotationFragment = generator.createAnnotation(this);
            addImport(annotationFragment.getImportDeclaration());
            result.append(annotationFragment.getSourcecode()).append("\n");
        }
        return result.toString();
    }

    /**
     * Returns all annotations like {@link #getAnnotations(AnnotatedJavaElementType)}, but only if
     * currently an interface is generated ({@code isGeneratingInterface} is {@code true} or
     * interfaces aren't generated at all( {@link #isGeneratePublishedInterfaces()} is {@code false}
     * ).
     */
    public String getInterfaceAnnotations(AnnotatedJavaElementType type, boolean isGeneratingInterface) {
        if (isGeneratingInterface || !isGeneratePublishedInterfaces()) {
            return getAnnotations(type);
        } else {
            return "";
        }
    }

    public List<IJavaElement> getGeneratedJavaElements(IType javaType) {
        List<IJavaElement> result = new ArrayList<IJavaElement>();
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
     * Adds a method with no parameter to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * 
     * @param methodName The name of the generated method
     * @return The methods definition. For example for a method name <em>getFoo</em> the method
     *         definition is <em>getFoo()</em>
     */
    public String method(String methodName) {
        return methodInternal(methodName, new MethodParameter[0]);
    }

    /**
     * Adds a method with one parameter to the list of generated method signatures. The method
     * parameter is defined by the parameter type and the parameter name.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * 
     * @param methodName The name of the generated method
     * @param parameterType The (Java-)type of the method parameter
     * @param parameterName The name of the method parameter
     * @return The methods definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is
     *         <em>SetFoo(String bar)<em>
     */
    public String method(String methodName, String parameterType, String parameterName) {
        return methodInternal(methodName, new MethodParameter(parameterType, parameterName));
    }

    /**
     * Adds a method with two parameters to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * <p>
     * We cannot simply use varagrs because they are not supported ba XPAND.
     * 
     * @param methodName The name of the generated method
     * @param parameterType1 The type of the first method parameter
     * @param parameterName1 The name of the first method parameter
     * @param parameterType2 The type of the second method parameter
     * @param parameterName2 The name of the second method parameter
     * @return The methods definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is
     *         <em>SetFoo(String bar)<em>
     */
    public String method(String methodName,
            String parameterType1,
            String parameterName1,
            String parameterType2,
            String parameterName2) {
        return methodInternal(methodName, new MethodParameter(parameterType1, parameterName1), new MethodParameter(
                parameterType2, parameterName2));
    }

    /**
     * Adds a method with two parameters to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * <p>
     * We cannot simply use varagrs because they are not supported ba XPAND.
     * 
     * @param methodName The name of the generated method
     * @param parameterType1 The type of the first method parameter
     * @param parameterName1 The name of the first method parameter
     * @param parameterType2 The type of the second method parameter
     * @param parameterName2 The name of the second method parameter
     * @param parameterType3 The type of the third method parameter
     * @param parameterName3 The name of the third method parameter
     * @return The methods definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is
     *         <em>SetFoo(String bar)<em>
     */
    public String method(String methodName,
            String parameterType1,
            String parameterName1,
            String parameterType2,
            String parameterName2,
            String parameterType3,
            String parameterName3) {
        return methodInternal(methodName, new MethodParameter(parameterType1, parameterName1), new MethodParameter(
                parameterType2, parameterName2), new MethodParameter(parameterType3, parameterName3));
    }

    /**
     * Adds a method with two parameters to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * <p>
     * We cannot simply use varagrs because they are not supported ba XPAND.
     * 
     * @param methodName The name of the generated method
     * @param parameterType1 The type of the first method parameter
     * @param parameterName1 The name of the first method parameter
     * @param parameterType2 The type of the second method parameter
     * @param parameterName2 The name of the second method parameter
     * @param parameterType3 The type of the third method parameter
     * @param parameterName3 The name of the third method parameter
     * @param parameterType4 The type of the fourth method parameter
     * @param parameterName4 The name of the fourth method parameter
     * @return The methods definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is
     *         <em>SetFoo(String bar)<em>
     */
    // CSOFF: ParameterNumberCheck
    public String method(String methodName,
            String parameterType1,
            String parameterName1,
            String parameterType2,
            String parameterName2,
            String parameterType3,
            String parameterName3,
            String parameterType4,
            String parameterName4) {
        return methodInternal(methodName, new MethodParameter(parameterType1, parameterName1), new MethodParameter(
                parameterType2, parameterName2), new MethodParameter(parameterType3, parameterName3),
                new MethodParameter(parameterType4, parameterName4));
    }

    /**
     * Adds a method with two parameters to the list of generated method signatures.
     * <p>
     * Use to register a method that should be included in the refactoring support and the
     * jump-to-source-code feature.
     * <p>
     * We cannot simply use varagrs because they are not supported ba XPAND.
     * 
     * @param methodName The name of the generated method
     * @param parameterType1 The type of the first method parameter
     * @param parameterName1 The name of the first method parameter
     * @param parameterType2 The type of the second method parameter
     * @param parameterName2 The name of the second method parameter
     * @param parameterType3 The type of the third method parameter
     * @param parameterName3 The name of the third method parameter
     * @param parameterType4 The type of the fourth method parameter
     * @param parameterName4 The name of the fourth method parameter
     * @return The methods definition. For example for a method name <em>setFoo</em> with parameter
     *         <em>bar</em> of type <em>String</em> the method definition is
     *         <em>SetFoo(String bar)<em>
     */
    // CSOFF: ParameterNumberCheck
    public String method(String methodName,
            String parameterType1,
            String parameterName1,
            String parameterType2,
            String parameterName2,
            String parameterType3,
            String parameterName3,
            String parameterType4,
            String parameterName4,
            String parameterType5,
            String parameterName5) {
        return methodInternal(methodName, new MethodParameter(parameterType1, parameterName1), new MethodParameter(
                parameterType2, parameterName2), new MethodParameter(parameterType3, parameterName3),
                new MethodParameter(parameterType4, parameterName4),
                new MethodParameter(parameterType5, parameterName5));
    }

    // CSON: ParameterNumberCheck

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + ipsObjectPartContainer + "]";
    }

}
