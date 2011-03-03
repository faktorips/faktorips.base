/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.JavaGenerationHelper;
import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.LocalizedTextHelper;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for Java source code generators for an IPS object part container (IPS object or IPS
 * object part).
 * 
 * @author Jan Ortmann
 */
public abstract class JavaGeneratorForIpsPart {

    /** The <tt>IIpsObjectPartContainer</tt> this generator generates source code for. */
    private IIpsObjectPartContainer ipsPart;

    private LocalizedTextHelper localizedTextHelper;

    public JavaGeneratorForIpsPart(IIpsObjectPartContainer part, LocalizedStringsSet localizedStringsSet) {
        super();
        ArgumentCheck.notNull(part);
        ArgumentCheck.notNull(localizedStringsSet);
        ipsPart = part;
        localizedTextHelper = new LocalizedTextHelper(localizedStringsSet);
    }

    public IIpsObjectPartContainer getIpsPart() {
        return ipsPart;
    }

    /**
     * Adds an <code>Override</code> annotation to the java code fragment if the java compliance
     * level is greater than 1.5. It takes into account the fine differences regarding the
     * <code>Override</code> annotation for compliance level 1.5 and higher.
     * 
     * @param fragmentBuilder the annotation is added to this {@link JavaCodeFragmentBuilder}
     * @param interfaceMethodImplementation to be able to decide if an Override annotation needs to
     *            be generated it must be known if the the generated method is an implementation of
     *            an interface method or an override of a super class method.
     */
    public void appendOverrideAnnotation(JavaCodeFragmentBuilder fragmentBuilder,
            IIpsProject iIpsProject,
            boolean interfaceMethodImplementation) {

        JavaGenerationHelper.appendOverrideAnnotation(fragmentBuilder, iIpsProject, interfaceMethodImplementation);
    }

    /**
     * Returns the language in that variables, methods are named and Java documentations are written
     * in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public abstract Locale getLanguageUsedInGeneratedSourceCode();

    /**
     * Returns the description of the given {@link IIpsObjectPart} in the language of the code
     * generator.
     * <p>
     * If there is no description in that locale, the description of the default language will be
     * returned.
     * <p>
     * Returns an empty string if there is no default description as well or the given
     * {@link IIpsObjectPart} does not support descriptions.
     * 
     * @param ipsObjectPart The {@link IIpsObjectPart} to obtain the description of.
     * 
     * @throws NullPointerException If <tt>ipsObjectPart</tt> is <tt>null</tt>.
     */
    protected final String getDescriptionInGeneratorLanguage(IIpsObjectPart ipsObjectPart) {
        ArgumentCheck.notNull(ipsObjectPart);
        String description = ""; //$NON-NLS-1$
        if (ipsObjectPart instanceof IDescribedElement) {
            IDescribedElement describedElement = (IDescribedElement)ipsObjectPart;
            IDescription generatorDescription = describedElement.getDescription(getLanguageUsedInGeneratedSourceCode());
            if (generatorDescription != null) {
                description = generatorDescription.getText();
            } else {
                description = IpsPlugin.getMultiLanguageSupport().getDefaultDescription(describedElement);
            }
        }
        return description;
    }

    protected void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, builder, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacement Object that replaces the place holder {0} in the property file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {

        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, modelDescription, builder,
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, Object, String, JavaCodeFragmentBuilder)} without
     * a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix, Object replacement, JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, builder,
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * <p>
     * Calling this method is only allowed during the build cycle. If it is called outside the build
     * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet has been set to this
     * builder a RuntimeException is thrown.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacements Objects that replaces the place holder {0}, {1} etc. in the property file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {

        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder,
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, Object[], String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix, Object[] replacements, JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder,
                getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @return the requested text
     */
    protected String getLocalizedText(String key) {
        return localizedTextHelper.getLocalizedText(key, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the localized text for the provided key. Calling this method is only allowed during
     * the build cycle.
     * 
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     */
    protected String getLocalizedText(String key, Object replacement) {
        return localizedTextHelper.getLocalizedText(key, replacement, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @param replacements indicated regions within the text are replaced by the string
     *            representations of these values.
     */
    protected String getLocalizedText(String key, Object[] replacements) {
        return localizedTextHelper.getLocalizedText(key, replacements, getLanguageUsedInGeneratedSourceCode());
    }

    public String getJavaDocCommentForOverriddenMethod() {
        return JavaGenerationHelper.getJavaDocCommentForOverriddenMethod();
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacement Any object to replace wild cards in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object replacement) {
        return localizedTextHelper.getLocalizedToDo(keyPrefix, replacement, getLanguageUsedInGeneratedSourceCode());
    }

    public JavaNamingConvention getJavaNamingConvention() {
        return JavaGenerationHelper.getJavaNamingConvention();
    }

    /**
     * Returns the getter method to access a property/attribute value.
     */
    protected String getMethodNameGetPropertyValue(String propName, Datatype datatype) {
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }

    /**
     * Returns the setter method to access a property/attribute value.
     * 
     * @param datatype The data type of the property.
     * 
     * @deprecated Use {@link #getMethodNameSetPropertyValue(String)} instead as the data type is of
     *             no relevance.
     */
    @Deprecated
    // Deprecated since 3.0
    protected String getMethodNametSetPropertyValue(String propName, Datatype datatype) {
        return getMethodNameSetPropertyValue(propName);
    }

    /**
     * Returns the method name of the setter method enabling to access the given property.
     */
    protected String getMethodNameSetPropertyValue(String propertyName) {
        return getJavaNamingConvention().getSetterMethodName(propertyName);
    }

    /**
     * Collects all <tt>IJavaElement</tt>s generated for the published interface by this generator
     * into the provided list.
     * <p>
     * Subclasses must add the <tt>IJavaElement</tt>s they generate for the given
     * <tt>IIpsElement</tt> to the provided list (collecting parameter pattern).
     * <p>
     * Only <tt>IJavaElement</tt>s generated for the published interface shall be added to the list.
     * 
     * @see #getGeneratedJavaElementsForImplementation(List, IType, IIpsElement)
     * 
     * @param javaElements The list to add generated <tt>IJavaElement</tt>s to.
     * @param generatedJavaType The Java type that the calling builder is generating.
     * @param ipsElement The <tt>IIpsElement</tt> for that the client requested the generated
     *            <tt>IJavaElement</tt>s.
     */
    public abstract void getGeneratedJavaElementsForPublishedInterface(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement);

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
     * @param javaElements The list to add generated <tt>IJavaElement</tt>s to.
     * @param generatedJavaType The Java type that the calling builder is generating.
     * @param ipsElement The <tt>IIpsElement</tt> for that the client requested the generated
     *            <tt>IJavaElement</tt>s.
     */
    public abstract void getGeneratedJavaElementsForImplementation(List<IJavaElement> javaElements,
            IType generatedJavaType,
            IIpsElement ipsElement);

    /**
     * Returns the Java type signature for the given {@link Datatype}.
     * 
     * @param datatype The {@link Datatype} to get the parameter type signature for
     * @param resolveToPublishedInterface Flag indicating whether the datatype should be resolved to
     *            the published interface type
     * 
     * @see Signature
     */
    protected final String getJavaTypeSignature(Datatype datatype, boolean resolveToPublishedInterface) {
        if (resolveToPublishedInterface) {
            // StdBuilderHelper. TODO AW
        }
        return Signature.createTypeSignature(datatype.getName(), false);
    }

    @Override
    public String toString() {
        return "Generator for " + ipsPart.toString(); //$NON-NLS-1$
    }

}
