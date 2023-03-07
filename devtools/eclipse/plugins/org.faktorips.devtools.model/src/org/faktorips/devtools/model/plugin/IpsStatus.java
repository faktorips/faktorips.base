/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

/**
 * Extension of status that sets the correct plug-in id and provides some convenience constructors.
 * 
 * @author Jan Ortmann
 */
public class IpsStatus extends Status {

    /**
     * Creates a new error status based on the given {@code Throwable}.
     */
    public IpsStatus(Throwable throwable) {
        super(IStatus.ERROR, IpsModelActivator.PLUGIN_ID, 0,
                throwable.getMessage() != null ? throwable.getMessage() : "", throwable); //$NON-NLS-1$
        // use the throwable's message as the error dialog makes no use of the throwable
    }

    /**
     * Creates a new error status based on the given message.
     */
    public IpsStatus(String msg) {
        super(IStatus.ERROR, IpsModelActivator.PLUGIN_ID, 0, msg, null);
    }

    /**
     * Creates a new status with the provided severity and message.
     */
    public IpsStatus(int severity, String msg) {
        super(severity, IpsModelActivator.PLUGIN_ID, 0, msg, null);
    }

    /**
     * Creates a new error status based on the given message and {@code Throwable}.
     */
    public IpsStatus(String msg, Throwable t) {
        super(IStatus.ERROR, IpsModelActivator.PLUGIN_ID, 0, msg, t);
    }

    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, String message, Throwable exception) {
        super(severity, IpsModelActivator.PLUGIN_ID, 0, message, exception);
    }

    /**
     * Creates a new status.
     */
    public IpsStatus(int severity, int code, String message, Throwable exception) {
        super(severity, IpsModelActivator.PLUGIN_ID, code, message, exception);
    }

    /**
     * Creates a new status from the given {@link MessageList}.
     * <ul>
     * <li>If the {@linkplain MessageList#isEmpty() MessageList is empty}, a
     * {@link Status#OK_STATUS} will be returned.</li>
     * <li>If the {@link MessageList} contains exactly one {@link Message}, an {@link IpsStatus} for
     * that Message is returned.</li>
     * <li>Otherwise a {@link MultiStatus} containing an {@link IpsStatus} for every {@link Message}
     * in the {@link MessageList} is returned.</li>
     * </ul>
     *
     * @see #of(Message)
     */
    public static IStatus of(MessageList messages) {
        if (messages == null || messages.isEmpty()) {
            return Status.OK_STATUS;
        }
        if (messages.size() == 1) {
            return IpsStatus.of(messages.getMessage(0));
        }
        return new MultiStatus(IpsModelActivator.PLUGIN_ID, toEclipseSeverity(messages.getSeverity()),
                messages.stream().map(IpsStatus::of).toArray(IStatus[]::new), messages.getText(), null);
    }

    /**
     * <ul>
     * <li>If the {@linkplain Message} is {@code null}, a {@link Status#OK_STATUS} will be
     * returned.</li>
     * <li>Otherwise creates a new {@link IpsStatus} from the given {@link Message}, using its
     * {@link Severity} and text.</li>
     * </ul>
     */
    public static IStatus of(Message message) {
        if (message == null) {
            return Status.OK_STATUS;
        }
        return new IpsStatus(toEclipseSeverity(message.getSeverity()), message.getText());
    }

    private static int toEclipseSeverity(Severity serverity) {
        return switch (serverity) {
            case ERROR -> IStatus.ERROR;
            case WARNING -> IStatus.WARNING;
            case INFO -> IStatus.INFO;
            case NONE -> IStatus.OK;
        };
    }

}
