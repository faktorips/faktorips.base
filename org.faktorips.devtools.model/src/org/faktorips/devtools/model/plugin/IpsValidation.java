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

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * Encapsulates a Faktor-IPS validation.
 * <p>
 * Validations are written by implementing and adding {@link IpsValidationTask}s for each individual
 * part of the validation. This class will ensure that the validation stops after the execution of a
 * task that returns a message with severity {@link Message#ERROR} while at the same time returning
 * false for {@link IpsValidationTask#isContinueOnError()}.
 * <p>
 * <strong>Important:</strong> This class is experimental and it has not yet been agreed about
 * whether this shall be the way to implement validations in Faktor-IPS. Therefore this class should
 * not be used at the moment, see FIPS-483.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public final class IpsValidation {

    private final List<IpsValidationTask> tasks = new ArrayList<>(1);

    /**
     * Executes this Faktor-IPS validation by executing all validation tasks that have been added
     * using {@link #addTask(IpsValidationTask)}.
     * <p>
     * Returns a {@link MessageList} that contains the result of the validation.
     * <p>
     * <strong>Important:</strong> Note that the validation will stop immediately if a task returns
     * a message with severity {@link Message#ERROR} and
     * {@link IpsValidationTask#isContinueOnError()} returns false.
     * 
     * @param ipsProject Will be used for finder-methods that are used within the implementation
     * 
     * @throws IpsException If any error occurs during the validation
     * @throws NullPointerException If the given {@link IIpsProject} is null
     */
    public MessageList validate(IIpsProject ipsProject) {
        ArgumentCheck.notNull(ipsProject);

        MessageList messageList = new MessageList();
        for (IpsValidationTask task : tasks) {
            Message taskResult = task.execute(ipsProject);
            if (taskResult == null) {
                continue;
            }
            messageList.add(taskResult);
            if (taskResult.getSeverity() == Message.ERROR && !task.isContinueOnError()) {
                break;
            }
        }

        return messageList;
    }

    /**
     * Adds a task to this Faktor-IPS validation.
     * 
     * @param task The {@link IpsValidationTask} to add to this Faktor-IPS validation.
     * 
     * @throws NullPointerException If the task is null
     */
    public void addTask(IpsValidationTask task) {
        ArgumentCheck.notNull(task);
        tasks.add(task);
    }

}
