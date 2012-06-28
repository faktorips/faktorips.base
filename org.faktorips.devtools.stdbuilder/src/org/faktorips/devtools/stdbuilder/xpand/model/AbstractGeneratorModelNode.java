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

package org.faktorips.devtools.stdbuilder.xpand.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.stdbuilder.AnnotatedJavaElementType;
import org.faktorips.devtools.stdbuilder.IAnnotationGenerator;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for every Xpand generator model object. This class provides some useful methods e.g.
 * for localization and import statements and handles the common needs for every generator model
 * object.
 * 
 * @author dirmeier, widmaier
 */
public abstract class AbstractGeneratorModelNode {

    private final LocalizedStringsSet localizedStringSet = new LocalizedStringsSet(getClass());

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
     */
    public AbstractGeneratorModelNode(IIpsObjectPartContainer ipsObjectPartContainer, GeneratorModelContext context,
            ModelService modelService) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        this.modelContext = context;
        this.modelService = modelService;
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

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param clazz The class you want to add to the import statements
     * @return the unqualified name of the type
     */
    public String addImport(Class<?> clazz) {
        getModelContext().addImport(clazz.getName());
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
     * not be added as an imported. The given qualified name will still be returned, however.
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
        ImportStatement importStatement = getModelContext().addImport(javaQName);
        return importStatement.getUnqualifiedName();
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param importDeclaration The import declaration contains a set of imports
     */
    public void addImport(ImportDeclaration importDeclaration) {
        for (String importStatement : importDeclaration.getImports()) {
            getModelContext().addImport(importStatement);
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
        return getModelContext().removeImport(importStatement);
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
     * Returns the language that variables, methods are named and and JDoc are written in.
     * <p>
     * Do not overwrite this method because it may be used in constructors
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public final Locale getLanguageUsedInGeneratedSourceCode() {
        return getModelContext().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Checks whether or not an override annotation is needed respect to the different compliance
     * levels. Up to Java5 there was no override annotation at all (this case is not longer
     * supported). In Java5 there were only override annotations for real overrides not for
     * interface implementations. For compliance levels greater Java5 we always generate an override
     * annotation if either an interface or an other implementation is overridden. Whether or not an
     * override annotation is needed at all you have to consider in your code template.
     * 
     * @param interfaceMethodImplementation True if the only an interface method is implemented
     *            false if it overrides an other implementation
     * @return true when override is needed
     */
    public boolean needOverrideAnnotation(boolean interfaceMethodImplementation) {
        if (ComplianceCheck.isComplianceLevel5(getIpsObjectPartContainer().getIpsProject())
                && !interfaceMethodImplementation) {
            return true;
        }
        if (ComplianceCheck.isComplianceLevelGreaterJava5(getIpsObjectPartContainer().getIpsProject())) {
            return true;
        }
        return false;
    }

    public GeneratorModelContext getModelContext() {
        return modelContext;
    }

    public ModelService getModelService() {
        return modelService;
    }

    public <T extends AbstractGeneratorModelNode> T getModelNode(IIpsObjectPartContainer ipsObjectPartContainer,
            Class<T> nodeClass) {
        return getModelService().getModelNode(ipsObjectPartContainer, nodeClass, getModelContext());
    }

    protected IJavaNamingConvention getJavaNamingConvention() {
        return getIpsProject().getJavaNamingConvention();
    }

    /**
     * Returns whether or not methods for delta-support should be added to generated classes.
     */
    public boolean isGenerateDeltaSupport() {
        return true;
    }

    /**
     * Returns whether or not published interfaces should be generated.
     */
    public boolean isGeneratingPublishedInterfaces() {
        // TODO FIPS-1059
        return true;
    }

    /**
     * Returns a {@link BuilderAspect} depending on whether or not published interfaces shall be
     * generated. If published interfaces shall be generated this method return
     * {@link BuilderAspect#INTERFACE}, else {@link BuilderAspect#IMPLEMENTATION}.
     */
    protected BuilderAspect getBuilderAspectDependingOnSettings() {
        return isGeneratingPublishedInterfaces() ? BuilderAspect.INTERFACE : BuilderAspect.IMPLEMENTATION;
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
    protected final <T extends AbstractGeneratorModelNode> Set<T> initNodesForParts(Set<? extends IIpsObjectPart> parts,
            Class<T> nodeClass) {
        Set<T> nodes = new LinkedHashSet<T>();
        for (IIpsObjectPart part : parts) {
            nodes.add(getModelNode(part, nodeClass));
        }
        return nodes;
    }

    /**
     * Returns a string containing all annotations to the given {@link AnnotatedJavaElementType} and
     * IpsElement using the given builder.
     * 
     * @param type Determines the type of annotation to generate. See
     *            {@link AnnotatedJavaElementType} for a list of possible types.
     * @param ipsElement The IPS element to create the annotations for. <br/>
     *            <code>Null</code> is permitted for certain AnnotatedJavaElementTypes which do not
     *            need further information. This is the case if <code>type</code> is
     *            POLICY_CMPT_IMPL_CLASS_TRANSIENT_FIELD.
     * @return the string containing the annotations
     * 
     */
    public String getAnnotations(AnnotatedJavaElementType type, IIpsElement ipsElement) {
        List<IAnnotationGenerator> generators = getModelContext().getAnnotationGenerator(type);
        String result = "";
        for (IAnnotationGenerator generator : generators) {
            if (!generator.isGenerateAnnotationFor(ipsElement)) {
                continue;
            }
            // TODO add import
            JavaCodeFragment annotationFragment = generator.createAnnotation(ipsElement);
            addImport(annotationFragment.getImportDeclaration());
            result += annotationFragment.getSourcecode() + "\n";
        }
        return result;
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
        return getAnnotations(type, getIpsObjectPartContainer());
    }

}
