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

import java.util.Locale;

import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
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

    private final GeneratorModelContext context;

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
        this.context = context;
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

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param qName The qualified name of the type
     * @return the unqualified name of the type
     */
    public String addImport(String qName) {
        getModelContext().addImport(qName);
        String[] segments = qName.split("\\.");
        return segments[segments.length - 1];
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
     * Returns the language that variables, methods are named and and Java docs are written in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return getModelContext().getLanguageUsedInGeneratedSourceCode();
    }

    /**
     * Returns the naming java convention configured in this project
     * 
     * @return The configured java naming convention
     */
    public IJavaNamingConvention getJavaNamingConvention() {
        return getIpsObjectPartContainer().getIpsProject().getJavaNamingConvention();
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
        return context;
    }

    public ModelService getModelService() {
        return modelService;
    }

}