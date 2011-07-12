/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.refactor.Messages;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.MessageList;

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
            SharableParticipants sharedParticipants) throws CoreException {

        // TODO AW 03-06-2011: Move extension point properties to some central, published place
        List<RefactoringParticipant> participants = new ExtensionPoints(IpsPlugin.PLUGIN_ID)
                .createExecutableExtensions(ExtensionPoints.PULL_UP_PARTICIPANTS,
                        "pullUpParticipant", "class", RefactoringParticipant.class); //$NON-NLS-1$ //$NON-NLS-2$
        List<RefactoringParticipant> initializedParticipants = new ArrayList<RefactoringParticipant>(
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
    protected void validateIpsModel(MessageList validationMessageList) throws CoreException {
        validationMessageList.add(getIpsObjectPart().validate(getIpsProject()));
        validationMessageList.add(getTarget().validate(getIpsProject()));
    }

    /**
     * This implementation validates the target container. It checks that the target container is
     * specified and does not equal the current container.
     */
    @Override
    protected void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException {
        if (target == null) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetNotSpecified,
                    getLocalizedContainerCaption()));
            return;
        }

        if (target.equals(getIpsObjectPart().getIpsObject())) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetEqualsCurrentContainer,
                    getLocalizedContainerCaption()));
            return;
        }
    }

    private String getLocalizedContainerCaption() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getIpsObjectPart().getIpsObject());
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
