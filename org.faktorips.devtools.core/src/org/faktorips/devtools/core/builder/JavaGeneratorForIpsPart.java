/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import java.util.Locale;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for Java source code generators for an ips object part container (ips object or ips part).
 * 
 * @author Jan Ortmann
 */
public abstract class JavaGeneratorForIpsPart {

    // the ips elements this generator generates sourcecode for 
    private IIpsObjectPartContainer ipsPart;
    
    private LocalizedTextHelper localizedTextHelper; 
    
    public JavaGeneratorForIpsPart(IIpsObjectPartContainer part, LocalizedStringsSet localizedStringsSet) {
        super();
        ArgumentCheck.notNull(part);
        ArgumentCheck.notNull(localizedStringsSet);
        this.ipsPart = part;
        this.localizedTextHelper = new LocalizedTextHelper(localizedStringsSet);
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
    //TODO duplicate code with JavaSourceFileBuilder
    public void appendOverrideAnnotation(JavaCodeFragmentBuilder fragmentBuilder, boolean interfaceMethodImplementation){
        if(ComplianceCheck.isComplianceLevel5(getIpsPart().getIpsProject()) && !interfaceMethodImplementation){
            fragmentBuilder.annotationLn(JavaSourceFileBuilder.ANNOTATION_OVERRIDE);
        }
        if(ComplianceCheck.isComplianceLevelGreaterJava5(getIpsPart().getIpsProject())){
            fragmentBuilder.annotationLn(JavaSourceFileBuilder.ANNOTATION_OVERRIDE);
        }
    }

    /**
     * Returns the language in that variables, methods are named and and Java docs are written in.
     * 
     * @see IIpsArtefactBuilderSet#getLanguageUsedInGeneratedSourceCode()
     */
    public abstract Locale getLanguageUsedInGeneratedSourceCode();

    /**
     * Like {@link #appendLocalizedJavaDoc(String, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, builder, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * 
     * @param key prefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefic. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
     * @param replacement Object that replaces the placeholder {0} in the property file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     * @return the requested text
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, modelDescription, builder, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object, String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, builder, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * <p>
     * Calling this method is only allowed during the build cycle. If it is called outside the build
     * cycle a RuntimeException is thrown. In addition if no LocalizedStringSet has been set to this
     * builder a RuntimeException is thrown.
     * 
     * @param key prefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefic. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
     * @param replacements Objects that replaces the placeholdersw {0}, {1} etc. in the property
     *            file
     * @param element the ips element used to access the ips project where the language to use is
     *            defined.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     * @return the requested text
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder, getLanguageUsedInGeneratedSourceCode());
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object[], String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder, getLanguageUsedInGeneratedSourceCode());
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
     * @return the requested text
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
     * @return the requested text
     */
    protected String getLocalizedText(String key, Object[] replacements) {
        return localizedTextHelper.getLocalizedText(key, replacements, getLanguageUsedInGeneratedSourceCode());
    }

    //TODO duplicate in JavaSourceFileBuilder
    public String getJavaDocCommentForOverriddenMethod() {
        return "{@inheritDoc}"; //$NON-NLS-1$
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * <pre>// TODO Implement the rule xyz.</pre>
     * 
     * @param element Any ips element used to access the ips project and determine the language for the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the prefix
     * @param replacements Any objects to replace wildcards in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object replacement) {
        return localizedTextHelper.getLocalizedToDo(keyPrefix, replacement, getLanguageUsedInGeneratedSourceCode());
    }

//  TODO duplicate in JavaSourceFileBuilder
    public JavaNamingConvention getJavaNamingConvention() {
        return JavaNamingConvention.ECLIPSE_STANDARD;
    }
    
    /**
     * Returns the getter method to access a property/attribute value.
     */
    protected String getMethodNameGetPropertyValue(String propName, Datatype datatype){
        return getJavaNamingConvention().getGetterMethodName(propName, datatype);
    }
    
    /**
     * Returns the setter method to access a property/attribute value.
     */
    protected String getMethodNametSetPropertyValue(String propName, Datatype datatype){
        return getJavaNamingConvention().getSetterMethodName(propName, datatype);
    }
    
    public String toString() {
        return "Generator for " + ipsPart.toString();
    }
    
}
