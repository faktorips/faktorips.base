/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.pctype;

import java.util.Arrays;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.runtime.Severity;
import org.faktorips.util.ArgumentCheck;

public enum MessageSeverity {

    ERROR("error", new JavaCodeFragment().appendClassName(Severity.class).append(".ERROR")), //$NON-NLS-1$//$NON-NLS-2$
    WARNING("warning", new JavaCodeFragment().appendClassName(Severity.class).append(".WARNING")), //$NON-NLS-1$//$NON-NLS-2$
    INFO("info", new JavaCodeFragment().appendClassName(Severity.class).append(".INFO")); //$NON-NLS-1$//$NON-NLS-2$

    private String id;

    /** The Java sourcecode fragment that identifies the message severity. */
    private JavaCodeFragment codeFragment;

    MessageSeverity(String id, JavaCodeFragment fragment) {
        ArgumentCheck.notNull(id);
        this.id = id;
        ArgumentCheck.notNull(fragment);
        codeFragment = fragment;
    }

    public static final MessageSeverity getMessageSeverity(String id) {
        return Arrays.stream(MessageSeverity.values()).filter(s -> s.id.equals(id)).findAny().orElse(null);
    }

    public String getId() {
        return id;
    }

    /**
     * Returns the Java sourcecode fragment that identifies the message severity, e.g.
     * <code>Message.ERROR</code> for error (import is org.faktorips.util.message.Message).
     */
    public JavaCodeFragment getJavaSourcecode() {
        return new JavaCodeFragment(codeFragment);
    }

    /**
     * Returns a message severity of the JFace
     * <code>org.eclipse.jface.dialogs.IMessageProvider</code> for a MessageSeverity.
     */
    public static final int getJFaceMessageType(MessageSeverity severity) {
        if (MessageSeverity.ERROR.equals(severity)) {
            return 3;
        }
        if (MessageSeverity.INFO.equals(severity)) {
            return 1;
        }
        if (MessageSeverity.WARNING.equals(severity)) {
            return 2;
        }

        return 0;
    }

}
