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

/**
 * Abstract base class for all Faktor-IPS "Pull Up" refactoring processors.
 * 
 * @param <T> The type of the container to which parts are pulled up to
 * 
 * @since 3.4
 * 
 * @author Alexander Weickmann
 */
public abstract class IpsPullUpProcessor<T extends IIpsObjectPartContainer> extends IpsRefactoringProcessor {

    private T target;

    /**
     * @param ipsObjectPart {@link IIpsObjectPart} to be refactored
     */
    protected IpsPullUpProcessor(IIpsObjectPart ipsObjectPart) {
        super(ipsObjectPart);
    }

    @Override
    public final RefactoringParticipant[] loadParticipants(RefactoringStatus status,
            SharableParticipants sharedParticipants) throws CoreException {

        // TODO AW 03-06-2011: Move constants to some central, published place
        List<RefactoringParticipant> participants = new ExtensionPoints(IpsPlugin.PLUGIN_ID)
                .createExecutableExtensions(ExtensionPoints.PULL_UP_PARTICIPANTS,
                        "pullUpParticipant", "class", RefactoringParticipant.class); //$NON-NLS-1$ //$NON-NLS-2$
        for (RefactoringParticipant participant : participants) {
            participant.initialize(this, getIpsElement(), new IpsPullUpArguments());
        }
        return participants.toArray(new RefactoringParticipant[participants.size()]);
    }

    /**
     * This implementation validates the target container and returns a {@link RefactoringStatus} as
     * result of the validation.
     * <p>
     * It checks that the target container is specified and does not equal the current container.
     */
    @Override
    public RefactoringStatus validateUserInput(IProgressMonitor pm) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();

        if (target == null) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetNotSpecified,
                    getLocalizedContainerCaption()));
            return status;
        }

        if (target.equals(getIpsObjectPart().getIpsObject())) {
            status.addFatalError(NLS.bind(Messages.IpsPullUpProcessor_msgTargetEqualsCurrentContainer,
                    getLocalizedContainerCaption()));
            return status;
        }

        validateUserInputThis(status, pm);
        return status;
    }

    private String getLocalizedContainerCaption() {
        return IpsPlugin.getMultiLanguageSupport().getLocalizedCaption(getIpsObjectPart().getIpsObject());
    }

    /**
     * This operation is called by {@link #validateUserInput(IProgressMonitor)}. Subclasses must
     * implement special user input validations here.
     * 
     * @param status {@link RefactoringStatus} to report messages to
     * @param pm {@link IProgressMonitor} to report progress to
     * 
     * @throws CoreException May be thrown at any time
     */
    protected abstract void validateUserInputThis(RefactoringStatus status, IProgressMonitor pm) throws CoreException;

    /**
     * Sets the target container the {@link IIpsObjectPart} to be refactored shall be moved up to.
     * 
     * @param targetIpsObjectPartContainer The target container to pull up to
     * 
     * @throws NullPointerException If the parameter is null
     */
    public final void setTarget(T targetIpsObjectPartContainer) {
        ArgumentCheck.notNull(targetIpsObjectPartContainer);
        this.target = targetIpsObjectPartContainer;
    }

    /**
     * Returns the target container to which the {@link IIpsObjectPart} to be refactored will be
     * pulled up to.
     */
    public final T getTarget() {
        return target;
    }

    /**
     * Returns the {@link IIpsObjectPart} to be refactored.
     */
    protected final IIpsObjectPart getIpsObjectPart() {
        return (IIpsObjectPart)getIpsElement();
    }

}
