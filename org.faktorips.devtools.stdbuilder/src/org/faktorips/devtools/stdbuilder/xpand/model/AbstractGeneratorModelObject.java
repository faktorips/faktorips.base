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

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.ComplianceCheck;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IJavaNamingConvention;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.XpandBuilder;
import org.faktorips.util.LocalizedStringsSet;

public abstract class AbstractGeneratorModelObject {

    private final XpandBuilder builder;

    private final LocalizedStringsSet localizedStringSet = new LocalizedStringsSet(getClass());

    private final IIpsObjectPartContainer ipsObjectPartContainer;

    public AbstractGeneratorModelObject(IIpsObjectPartContainer ipsObjectPartContainer, XpandBuilder builder) {
        this.ipsObjectPartContainer = ipsObjectPartContainer;
        this.builder = builder;
    }

    /**
     * @return Returns the ipsObjectPartContainer.
     */
    public IIpsObjectPartContainer getIpsObjectPartContainer() {
        return ipsObjectPartContainer;
    }

    /**
     * @return Returns the builder
     */
    public XpandBuilder getBuilder() {
        return builder;
    }

    /**
     * @return Returns the builderSet.
     */
    public StandardBuilderSet getBuilderSet() {
        return builder.getBuilderSet();
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
        builder.addImport(clazz.getName());
        return clazz.getSimpleName();
    }

    /**
     * Add the qualified name to the list of import statements and return the unqualified name
     * 
     * @param qName The qualified name of the type
     * @return the unqualified name of the type
     */
    public String addImport(String qName) {
        builder.addImport(qName);
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
            if (ImportDeclaration.isPackageImport(importStatement)) {
                // simply add additional .ALL because the package import already contains the
                // complete statement
                builder.addImport(importStatement + ".ALL");
            } else {
                builder.addImport(importStatement);
            }
        }
    }

    public boolean removeImport(String importStatement) {
        return builder.removeImport(importStatement);
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

    public String localizedJDoc(String key) {
        return localizedJDoc(key, new Object[0]);
    }

    public String localizedJDoc(String key, Object replacement) {
        return localizedJDoc(key, new Object[] { replacement });
    }

    public String localizedJDoc(String key, Object replacement1, Object replacement2) {
        return localizedJDoc(key, new Object[] { replacement1, replacement2 });
    }

    public String localizedJDoc(String key, Object... replacements) {
        String text = getLocalizedText(key + "_JAVADOC", replacements); //$NON-NLS-1$
        return text;
    }

    /**
     * Returns the language in that variables, methods are named and and Java docs are written in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public Locale getLanguageUsedInGeneratedSourceCode() {
        return getBuilder().getBuilderSet().getLanguageUsedInGeneratedSourceCode();
    }

    public IJavaNamingConvention getJavaNamingConvention() {
        return getIpsObjectPartContainer().getIpsProject().getJavaNamingConvention();
    }

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

    /**
     * Collects all <tt>IJavaElement</tt>s generated for the implementation by this generator into
     * the provided list.
     * <p>
     * Subclasses must add the <tt>IJavaElement</tt>s they generate for the given
     * <tt>IIpsElement</tt> to the provided list (collecting parameter pattern).
     * <p>
     * Only <tt>IJavaElement</tt>s generated for the implementation shall be added to the list.
     * 
     * @see #getGeneratedJavaElementsForPublishedInterface(List, IType, IIpsElement)
     * 
     * @param generatedJavaType The Java type that the calling builder is generating.
     * @param ipsElement The <tt>IIpsElement</tt> for that the client requested the generated
     *            <tt>IJavaElement</tt>s.
     * @return The list containing the generated <tt>IJavaElement</tt>s to.
     */
    public abstract List<IJavaElement> getGeneratedJavaElementsForImplementation(org.eclipse.jdt.core.IType generatedJavaType,
            IIpsElement ipsElement);

}