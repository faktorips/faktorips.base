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

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.util.LocalizedStringsSet;

/**
 * 
 * @author Jan Ortmann
 */
public class LocalizedTextHelper {

    private LocalizedStringsSet localizedStringsSet;
    private Locale locale;
    private Integer javaOptionsSplitLength;
    private Integer javaOptionsTabSize;
    
    //TODO remove Integer javaOptionsSplitLength, Integer javaOptionsTabSize 
    public LocalizedTextHelper(LocalizedStringsSet localizedStringsSet, Locale locale, Integer javaOptionsSplitLength, Integer javaOptionsTabSize) {
        super();
        this.localizedStringsSet = localizedStringsSet;
        this.locale = locale;
        initJavaOptions();
    }
    
    //TODO duplicate code in JavaSourceFileBuilder
    private void initJavaOptions() {
        try {
            javaOptionsSplitLength = Integer.valueOf(JavaCore
                    .getOption(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT));
            javaOptionsTabSize = Integer.valueOf(JavaCore.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE));
        } catch (Exception e) {
            IpsPlugin.log(new IpsStatus("Unable to apply the java formatter options.", e)); //$NON-NLS-1$
        }
    }

    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @return the requested text
     */
    public String getLocalizedText(String key) {
        return localizedStringsSet.getString(key, locale);
    }
    
    /**
     * Returns a single line comment containing a TO DO, e.g.
     * <pre>// TODO Implement this rule.</pre>
     * 
     * @param element Any ips element used to access the ips project and determine the langauge for the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the prefix
     */
    public String getLocalizedToDo(String keyPrefix, JavaCodeFragmentBuilder builder) {
        return getLocalizedToDo(keyPrefix, new Object[0]);
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * <pre>// TODO Implement the rule xyz.</pre>
     * 
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the prefix
     * @param replacement An object to replace the wildcard in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object replacement) {
        return getLocalizedToDo(keyPrefix, new Object[]{replacement});
    }

    /**
     * Returns a single line comment containing a TO DO, e.g.
     * <pre>// TODO Implement the rule xyz.</pre>
     * 
     * @param element Any ips element used to access the ips project and determine the langauge for the generated code.
     * @param keyPrefix A key prefix for the resource bundle, this method adds a "_TODO" to the prefix
     * @param replacements Any objects to replace wildcards in the message text.
     */
    public String getLocalizedToDo(String keyPrefix, Object[] replacements) {
        return "// TODO " + getLocalizedText(keyPrefix + "_TODO", replacements); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Inserts the localized Javadoc inclusing the annotations into the given
     * JavaCodeFragmentBuikder.
     * 
     * @param key prefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefic. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     * @return the requested text
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        String text = getLocalizedText(keyPrefix + "_JAVADOC"); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(wrapText(buf.toString()), annotations);
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix, JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, (String)null, builder);
    }

    /**
     * Inserts the localized Javadoc inclusing the annotations into the given
     * JavaCodeFragmentBuikder.
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
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        String text = getLocalizedText(keyPrefix + "_JAVADOC", replacement); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(wrapText(buf.toString()), annotations);
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, Object, String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object replacement,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, replacement, null, builder);
    }

    /**
     * Inserts the localized Javadoc including the annotations into the given JavaCodeFragmentBuilder.
     * 
     * @param key prefix the key prefix that identifies the requested javadoc and annotation. The
     *            javadoc is looked up in the localized text by adding _JAVADOC to the prefic. The
     *            annotation is looked up in the localized text by adding _ANNOTATION to the prefic.
     * @param replacements Objects that replaces the placeholdersw {0}, {1} etc. in the property
     *            file
     * @param modelDescription a description of the model object can be provided here so that it can
     *            be added to the description provided by the generator configuration
     * @param builder the builder the Javadoc is appended to.
     * @return the requested text
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            String modelDescription,
            JavaCodeFragmentBuilder builder) {
        
        String text = getLocalizedText(keyPrefix + "_JAVADOC", replacements); //$NON-NLS-1$
        String[] annotations = new String[] { getLocalizedText(keyPrefix + "_ANNOTATION") }; //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append(text);
        if (modelDescription != null) {
            buf.append(SystemUtils.LINE_SEPARATOR).append(modelDescription);
        }
        builder.javaDoc(wrapText(buf.toString()), annotations);
    }

    /**
     * Like {@link #appendLocalizedJavaDoc(String, Object[], String, JavaCodeFragmentBuilder)}
     * without a description that is expected to be provided by the model.
     */
    public void appendLocalizedJavaDoc(String keyPrefix,
            Object[] replacements,
            JavaCodeFragmentBuilder builder) {
        appendLocalizedJavaDoc(keyPrefix, replacements, null, builder);
    }
    
    /**
     * Returns the localized text for the provided key.
     * 
     * @param key the key that identifies the requested text
     * @param replacement an indicated region within the text is replaced by the string
     *            representation of this value
     * @return the requested text
     */
    public String getLocalizedText(String key, Object replacement) {
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
     * @return the requested text
     */
    public String getLocalizedText(String key, Object[] replacements) {
        return localizedStringsSet.getString(key, locale, replacements);
    }

    private String wrapText(String text) {

        if (StringUtils.isEmpty(text) || javaOptionsSplitLength == null || javaOptionsTabSize == null) {
            return text;
        }
        int maxLengthInt = javaOptionsSplitLength.intValue();
        int tabSizeInt = javaOptionsTabSize.intValue();
        int length = maxLengthInt - tabSizeInt - 3;
        String[] lines = StringUtils.split(text, SystemUtils.LINE_SEPARATOR);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];
            while (lineText.length() > length) {
                int index = lineText.indexOf(' ', length);
                if (index != -1) {
                    buf.append(lineText.substring(0, index));
                    buf.append(SystemUtils.LINE_SEPARATOR);
                    if (lineText.length() > index + 1) {
                        lineText = lineText.substring(index + 1, lineText.length() - 1);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            buf.append(lineText);
            buf.append(SystemUtils.LINE_SEPARATOR);
        }
        return buf.toString();
    }

    
}
