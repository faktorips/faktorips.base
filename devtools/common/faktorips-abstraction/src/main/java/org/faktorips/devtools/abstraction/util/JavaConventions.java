/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.util;

import java.text.MessageFormat;
import java.util.Set;
import java.util.regex.Pattern;

import javax.lang.model.SourceVersion;

import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.MessageLists;

public class JavaConventions {
    private static final Pattern PACKAGE_NAME = Pattern.compile("([a-zA-Z_]\\w*)(\\.[a-zA-Z_]\\w*)*"); //$NON-NLS-1$
    private static final Pattern NICE_PACKAGE_NAME = Pattern.compile("([a-z]\\w*)(\\.[a-z]\\w*)*"); //$NON-NLS-1$
    private static final String MSG_CODE_PREFIX = JavaConventions.class.getSimpleName() + '_';
    private static final Set<String> FORBIDDEN_NAMES = Set.of("abstract", "continue", "for", "new", "switch", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "assert", "default", "if", "package", "synchronized", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "boolean", "do", "goto", "private", "this", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "break", "double", "implements", "protected", "throw", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "byte", "else", "import", "public", "throws", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "case", "enum", "instanceof", "return", "transient", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "catch", "extends", "int", "short", "try", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "char", "final", "interface", "static", "void", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "class", "finally", "long", "strictfp", "volatile", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "const", "float", "native", "super", "while", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "_"); //$NON-NLS-1$
    // see https://docs.oracle.com/javase/specs/jls/se11/html/jls-3.html#jls-3.9

    public static final String MSG_CODE_INVALID_PACKAGE_NAME = MSG_CODE_PREFIX + "InvalidPackageName"; //$NON-NLS-1$
    public static final String MSG_CODE_DISCOURAGED_PACKAGE_NAME = MSG_CODE_PREFIX + "DiscouragedPackageName"; //$NON-NLS-1$
    public static final String MSG_CODE_INVALID_TYPE_NAME = MSG_CODE_PREFIX + "InvalidTypeName"; //$NON-NLS-1$
    public static final String MSG_CODE_DISCOURAGED_TYPE_NAME = MSG_CODE_PREFIX + "DiscouragedTypeName"; //$NON-NLS-1$

    private JavaConventions() {
        // util
    }

    public static MessageList validatePackageName(String name) {
        if (name == null) {
            return new MessageList(Message.newError(MSG_CODE_INVALID_PACKAGE_NAME, "A package name must not be null")); //$NON-NLS-1$
        }
        if (!PACKAGE_NAME.matcher(name).matches()) {
            return new MessageList(Message.newError(MSG_CODE_INVALID_PACKAGE_NAME,
                    MessageFormat.format("''{0}'' is not allowed as a Java package name", name))); //$NON-NLS-1$
        }
        if (!NICE_PACKAGE_NAME.matcher(name).matches()) {
            return new MessageList(Message.newWarning(MSG_CODE_DISCOURAGED_PACKAGE_NAME,
                    MessageFormat.format("''{0}'' is discouraged as a Java package name", name))); //$NON-NLS-1$
        }
        for (String packageName : name.split("\\.")) { //$NON-NLS-1$
            if (FORBIDDEN_NAMES.contains(packageName)) {
                return new MessageList(Message.newError(MSG_CODE_INVALID_PACKAGE_NAME,
                        MessageFormat.format("''{0}'' is not allowed as a Java package name", packageName))); //$NON-NLS-1$
            }
        }
        return MessageLists.emptyMessageList();
    }

    public static MessageList validateTypeName(String name) {
        int lastIndexOfSeparator = name.lastIndexOf('.');
        if (lastIndexOfSeparator > 0) {
            String packageName = name.substring(0, lastIndexOfSeparator);
            var messageList = validatePackageName(packageName);
            String unqualifiedName = name.substring(lastIndexOfSeparator + 1);
            messageList.add(validateUnqualifiedTypeName(unqualifiedName));
            return messageList;
        } else {
            return validateUnqualifiedTypeName(name);
        }
    }

    public static MessageList validateUnqualifiedTypeName(String name) {
        MessageList messageList = new MessageList();
        char firstChar = name.charAt(0);
        if (!Character.isJavaIdentifierStart(firstChar)) {
            messageList.add(Message.newError(MSG_CODE_INVALID_TYPE_NAME,
                    MessageFormat.format(
                            "''{0}'' is not allowed as a Java type name, because {1} is not a 'Java Letter'", name, //$NON-NLS-1$
                            firstChar)));
        } else {
            if (Character.isLowerCase(firstChar)) {
                messageList.add(Message.newWarning(MSG_CODE_DISCOURAGED_TYPE_NAME,
                        MessageFormat.format(
                                "''{0}'' is discouraged as a Java type name, because it starts with a lower case character", //$NON-NLS-1$
                                name)));
            }
            if ('$' == firstChar) {
                messageList.add(Message.newWarning(MSG_CODE_DISCOURAGED_TYPE_NAME,
                        MessageFormat.format(
                                "''{0}'' is discouraged as a Java type name, because it starts with '$'", //$NON-NLS-1$
                                name)));
            }
        }
        name.codePoints().skip(1).forEach(c -> {
            if (!Character.isJavaIdentifierPart(c)) {
                messageList.add(Message.newError(MSG_CODE_INVALID_TYPE_NAME,
                        MessageFormat.format(
                                "''{0}'' is not allowed as a Java type name, because {1} is not a 'Java Letter or Digit'", //$NON-NLS-1$
                                name, Character.toString(c))));
            }
        });
        return messageList;
    }

    /**
     * Validate the given field or method name for the given source level.
     */
    public static boolean validateName(String name, Runtime.Version sourceLevel) {
        return !name.contains(".") && SourceVersion.isName(name, convertToSourceVersion(sourceLevel));
    }

    /**
     * Validate the given field or method name.
     */
    public static boolean validateName(String name) {
        return !name.contains(".") && SourceVersion.isName(name);
    }

    private static SourceVersion convertToSourceVersion(Runtime.Version rv) {
        return SourceVersion.valueOf("RELEASE_" + rv.feature());
    }
}
