/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.builder;

import java.util.Locale;

import org.apache.commons.lang.SystemUtils;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class LocalizedTextHelper {

    private LocalizedStringsSet localizedStringsSet;

    public LocalizedTextHelper(LocalizedStringsSet localizedStringsSet) {
        super();
        this.localizedStringsSet = localizedStringsSet;
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     */
    public String getLocalizedText(String key, Locale locale) {
        return localizedStringsSet.getString(key, locale);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * 
     * @deprecated Use {@link #getLocalizedText(String, Locale)} instead since the builder is of no
     *             relevance.
     */
    @Deprecated
    // Deprecated since 3.0
    @SuppressWarnings("unused")
    // Suppressing warning that the parameter builder is not used since the method is deprecated
    // because of that
    public String getLocalizedToDo(String keyPrefix, JavaCodeFragmentBuilder builder, Locale locale) {
        return getLocalizedText(keyPrefix, locale);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     */
    public String getLocalizedToDo(String keyPrefix, Locale locale) {
        return getLocalizedToDo(keyPrefix, new Object[0], locale);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacement An object to replace the wild card in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object replacement, Locale locale) {
        return getLocalizedToDo(keyPrefix, new Object[] { replacement }, locale);
    }

    /**
     * Returns a single line comment containing a TO DO.
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the
     *            prefix
     * @param replacements Any objects to replace wild cards in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object[] replacements, Locale locale) {
        return "// TODO " + getLocalizedText(keyPrefix + "_TODO", replacements, locale); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuikder.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            String modelDescription,
            JavaCodeFragmentBuilder builder,
            Locale locale) {

        String text = getLocalizedText(keyPrefix + "_JAVADOC", locale); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION", locale) }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    public void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder, Locale locale) {
        appendLocalizedJavaDoc(keyPrefix, (String)null, builder, locale);
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuikder.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacement Object that replaces the place holder {0} in the property file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            JavaCodeFragmentBuilder builder,
            Locale locale) {

        String text = getLocalizedText(keyPrefix + "_JAVADOC", replacement, locale); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION", locale) }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            JavaCodeFragmentBuilder builder,
            Locale locale) {

        appendLocalizedJavaDoc(keyPrefix, replacement, null, builder, locale);
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given
     * JavaCodeFragmentBuilder.
     * 
     * @param keyPrefix the key prefix that identifies the requested Javadoc and annotation. The
     *            Javadoc is looked up in the localized text by adding _JAVADOC to the prefix. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefix.
     * @param replacements Objects that replaces the place holder {0}, {1} etc. in the property file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            String modelDescription,
            JavaCodeFragmentBuilder builder,
            Locale locale) {

        String text = getLocalizedText(keyPrefix + "_JAVADOC", replacements, locale); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION", locale) }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(buf.toString(), annotations);
    }

    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            JavaCodeFragmentBuilder builder,
            Locale locale) {

        appendLocalizedJavaDoc(keyPrefix, replacements, null, builder, locale);
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     */
    public String getLocalizedText(String key, Object replacement, Locale locale) {
        if (localizedStringsSet == null) {
            throw new RuntimeException(
                    "A LocalizedStringSet has to be set to this builder to be able to call this method."); //$NON-NLS-1$
        }
        return localizedStringsSet.getString(key, locale, replacement);
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @param replacements indicated regions within the text are replaced by the string
     *            representations of these values.
     */
    public String getLocalizedText(String key, Object[] replacements, Locale locale) {
        return localizedStringsSet.getString(key, locale, replacements);
    }

}
