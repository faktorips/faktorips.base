/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Base class for Java source code generators for an ips object part container (ips object or ips part).
 * 
 * @author Jan Ortmann
 */
public abstract class JavaGeneratorForIpsPart {

    // the ips elements this generator generates sourcecode for 
    private IIpsObjectPartContainer ipsPart;
    
    // and the ips project the element belongs to.
    protected IIpsProject ipsProject;
    
    private JavaSourceFileBuilder javaSourceFileBuilder;
    
    private LocalizedTextHelper localizedTextHelper; 
    
    public JavaGeneratorForIpsPart(IIpsObjectPartContainer part, JavaSourceFileBuilder builder, LocalizedStringsSet stringsSet) {
        super();
        this.ipsPart = part;
        this.ipsProject = ipsPart.getIpsProject();
        this.javaSourceFileBuilder = builder;
        this.localizedTextHelper = new LocalizedTextHelper(stringsSet, builder.getLanguageUsedInGeneratedSourceCode(ipsPart), 
                builder.getJavaOptionsSplitLength(), builder.getJavaOptionsTabSize());
    }
    
    public IIpsObjectPartContainer getIpsPart() {
        return ipsPart;
    }
    
    /**
     * Returns the ips project the generator generates source code for.
     */
    public IIpsProject getIpsProject() {
        return javaSourceFileBuilder.getIpsProject();
    }
    
    /**
     * Returns the Java source file builder that uses this generator.
     */
    public JavaSourceFileBuilder getJavaSourceFileBuilder() {
        return javaSourceFileBuilder;
    }
    
    /**
     * Like {@link #appendLocalizedJavaDoc(String, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder) {
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, builder);
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
        
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, modelDescription, builder);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object, String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            JavaCodeFragmentBuilder builder) {
        
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacement, builder);
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
        
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder);
    }

    /**
     * Like
     * {@link #appendLocalizedJavaDoc(String, Object[], String, IIpsElement, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    protected void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            JavaCodeFragmentBuilder builder) {
        
        localizedTextHelper.appendLocalizedJavaDoc(keyPrefix, replacements, builder);
    }
    
    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @return the requested text
     */
    protected String getLocalizedText(String key) {
        return localizedTextHelper.getLocalizedText(key);
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
        return localizedTextHelper.getLocalizedText(key, replacement);
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
        return localizedTextHelper.getLocalizedText(key, replacements);
    }

    protected String getJavaDocCommentForOverriddenMethod() {
        return javaSourceFileBuilder.getJavaDocCommentForOverriddenMethod();
    }

    public String getLocalizedToDo(String keyPrefix, Object replacement) {
        return javaSourceFileBuilder.getLocalizedToDo(ipsPart, keyPrefix, replacement);
    }

    
    protected JavaNamingConvention getJavaNamingConvention() {
        return javaSourceFileBuilder.getJavaNamingConvention();
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
