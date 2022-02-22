/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.runtime.MessageList;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for all Faktor-IPS "Pull Up" refactoring processors.
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsPullUpProcessor extends IpsRefactoringProcessor {

    private IIpsObjectPartContainer target;

    /**
     * @param ipsObjectPart {@link IIpsObjectPart} to be refactored
     */
    protected IpsPullUpProcessor(IIpsObjectPart ipsObjectPart) {
        super(ipsObjectPart);
    }

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) {

        // TODO AW 03-06-2011: Move extension point properties to some central, published place
        List<RefactoringParticipant> participants = new ExtensionPoints(IpsPlugin.PLUGIN_ID)
                .createExecutableExtensions(ExtensionPoints.PULL_UP_PARTICIPANTS,
                        "pullUpParticipant", "class", RefactoringParticipant.class); //$NON-NLS-1$ //$NON-NLS-2$
        List<RefactoringParticipant> initializedParticipants = new ArrayList<>(
                participants.size());
        for (RefactoringParticipant participant : participants) {
            boolean initialized = participant.initialize(this, getIpsElement(), new IpsPullUpArguments(target));
            if (initialized) {
                initializedParticipants.add(participant);
            }
        }
        return initializedParticipants.toArray(new RefactoringParticipant[initializedParticipants.size()]);
    }

    @Override
    protected void validateIpsModel(MessageList validationMessageList) {
        validationMessageList.add(getIpsObjectPart().validate(getIpsProject()));
        validationMessageList.add(getTarget().validate(getIpsProject()));
    }

    /**
     * This implementation validates the target container. It checks that the target container is
     * specified and does not equal the current container.
     */
    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) {
        if (target == null) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetNotSpecified,
                    getLocalizedContainerCaption()));
            return;
        }

        if (target.equals(getIpsObjectPart().getIpsObject())) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetEqualsCurrentContainer,
                    getLocalizedContainerCaption()));
        }
    }

    private String getLocalizedContainerCaption() {
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(getIpsObjectPart().getIpsObject());
    }

    /**
     * Sets the target container the {@link IIpsObjectPart} to be refactored shall be moved up to.
     * 
     * @param target The target container to pull up to
     * 
     * @throws IllegalArgumentException If the type of the target container is not allowed by this
     *             pull up processor
     * @throws NullPointerException If the parameter is null
     */
    public final void setTarget(IIpsObjectPartContainer target) {
        ArgumentCheck.notNull(target);
        ArgumentCheck.isTrue(isTargetTypeAllowed(target));
        this.target = target;
    }

    /**
     * Subclasses must return whether the type of the given target is allowed.
     * 
     * @param target The target container where the part to be refactored will be pulled up to
     */
    protected abstract boolean isTargetTypeAllowed(IIpsObjectPartContainer target);

    /**
     * Returns the target container to which the {@link IIpsObjectPart} to be refactored will be
     * pulled up to.
     */
    public final IIpsObjectPartContainer getTarget() {
        return target;
    }

    /**
     * Returns the {@link IIpsObjectPart} to be refactored.
     */
    protected final IIpsObjectPart getIpsObjectPart() {
        return (IIpsObjectPart)getIpsElement();
    }

}
