/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.pctype;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.runtime.Message;
import org.faktorips.util.ArgumentCheck;

public class MessageSeverity extends DefaultEnumValue {

    public final static MessageSeverity ERROR;

    public final static MessageSeverity WARNING;

    public final static MessageSeverity INFO;

    private final static DefaultEnumType enumType;

    static {
        enumType = new DefaultEnumType("MessageSeverity", MessageSeverity.class); //$NON-NLS-1$
        JavaCodeFragment errorFragment = new JavaCodeFragment();
        errorFragment.appendClassName(Message.class);
        errorFragment.append(".ERROR"); //$NON-NLS-1$
        ERROR = new MessageSeverity(enumType, "error", errorFragment); //$NON-NLS-1$
        JavaCodeFragment warningFragment = new JavaCodeFragment();
        warningFragment.appendClassName(Message.class);
        warningFragment.append(".WARNING"); //$NON-NLS-1$
        WARNING = new MessageSeverity(enumType, "warning", warningFragment); //$NON-NLS-1$
        JavaCodeFragment infoFragment = new JavaCodeFragment();
        infoFragment.appendClassName(Message.class);
        infoFragment.append(".INFO"); //$NON-NLS-1$
        INFO = new MessageSeverity(enumType, "info", infoFragment); //$NON-NLS-1$
    }

    public final static EnumType getEnumType() {
        return enumType;
    }

    public final static MessageSeverity getMessageSeverity(String id) {
        return (MessageSeverity)enumType.getEnumValue(id);
    }

    /**
     * Returns a message severity of the JFace
     * <code>org.eclipse.jface.dialogs.IMessageProvider</code> for a MessageSeverity.
     */
    public final static int getJFaceMessageType(MessageSeverity severity) {
        if (MessageSeverity.ERROR.equals(severity)) {
            return IMessageProvider.ERROR;
        }
        if (MessageSeverity.INFO.equals(severity)) {
            return IMessageProvider.INFORMATION;
        }
        if (MessageSeverity.WARNING.equals(severity)) {
            return IMessageProvider.WARNING;
        }

        return IMessageProvider.NONE;
    }

    /** The Java sourcecode fragment that identifies the message severity. */
    private JavaCodeFragment codeFragment;

    /**
     * Returns the Java sourcecode fragment that identifies the message severity, e.g.
     * <code>Message.ERROR</code> for error (import is org.faktorips.util.message.Message).
     */
    public JavaCodeFragment getJavaSourcecode() {
        return new JavaCodeFragment(codeFragment);
    }

    private MessageSeverity(DefaultEnumType type, String id, JavaCodeFragment fragment) {
        super(type, id);
        ArgumentCheck.notNull(fragment);
        this.codeFragment = fragment;
    }

}
