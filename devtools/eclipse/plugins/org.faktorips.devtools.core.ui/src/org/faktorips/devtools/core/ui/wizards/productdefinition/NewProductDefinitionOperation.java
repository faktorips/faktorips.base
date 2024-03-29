/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.productcmpt.Messages;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Operation that is responsible for the creation of a new product definition element. Intended to
 * be used together with {@link NewProductDefinitionWizard}.
 * <p>
 * <strong>Subclassing:</strong><br>
 * Concrete {@link NewProductDefinitionWizard NewProductDefinitionWizards} are supposed to use an
 * according {@link NewProductDefinitionOperation} as well.
 * <p>
 * Subclasses must implement two operations that are called during
 * {@link #execute(IProgressMonitor)}:
 * <ul>
 * <li>{@link #finishIpsSrcFile(IIpsSrcFile, IProgressMonitor)}
 * <li>{@link #postProcess(IIpsSrcFile, IProgressMonitor)}
 * </ul>
 * <p>
 * It is also possible to override {@link #createIpsSrcFile(SubMonitor)} if it should be necessary
 * to hook into creation of the source file.
 * 
 * @param <PMO> type of the {@link PresentationModelObject} that configures this operation
 */
public abstract class NewProductDefinitionOperation<PMO extends NewProductDefinitionPMO> extends
        WorkspaceModifyOperation {

    private final PMO pmo;

    private final List<INewProductDefinitionOperationParticipant> participants = new ArrayList<>();

    protected NewProductDefinitionOperation(PMO pmo) {
        this.pmo = pmo;
        participants
                .addAll(IpsPlugin.getDefault().getIpsCoreExtensions().getNewProductDefinitionOperationParticipants());
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        SubMonitor subMonitor = SubMonitor.convert(monitor,
                NLS.bind(Messages.NewProductCmptWizard_title, getPmo().getIpsObjectType().getDisplayName()),
                5);

        createIpsPackageFragmentIfNonExistent(subMonitor);

        IIpsSrcFile ipsSrcFile = createIpsSrcFile(subMonitor);
        finishIpsSrcFile(ipsSrcFile, subMonitor);
        callParticipants(ipsSrcFile, subMonitor);
        saveIpsSrcFile(ipsSrcFile, subMonitor);
        postProcess(ipsSrcFile, subMonitor);
    }

    private void createIpsPackageFragmentIfNonExistent(SubMonitor monitor) {
        IIpsPackageFragment ipsPackage = pmo.getIpsPackage();
        if (!ipsPackage.exists()) {
            pmo.getPackageRoot().createPackageFragment(ipsPackage.getName(), true, monitor.split(1));
        }
    }

    private void saveIpsSrcFile(IIpsSrcFile ipsSrcFile, SubMonitor monitor) {
        ipsSrcFile.save(monitor.split(1));
    }

    /**
     * Creates a new {@link IIpsSrcFile} using the information provided by the user and returns the
     * newly created file.
     * <p>
     * <strong>Subclassing:</strong><br>
     * The {@link NewProductDefinitionOperation} implementation creates the source file in the
     * package fragment as configured by {@link NewProductDefinitionPMO}.
     * 
     * @param monitor progress monitor to show progress to the user
     * @return the new {@link IIpsSrcFile}
     * 
     * @throws IpsException in case of exceptions during file creation
     */
    protected IIpsSrcFile createIpsSrcFile(SubMonitor monitor) {
        // @formatter:off
        return pmo.getIpsPackage().createIpsFile(
                pmo.getIpsObjectType(),
                pmo.getName(),
                true,
                monitor.split(1));
        // @formatter:on
    }

    /**
     * <strong>Subclassing:</strong><br>
     * Finishing the new {@link IIpsSrcFile} means to fill all information given from the user into
     * the newly created object. Subclasses may copy the content from an old object or something
     * similar. Subclasses do not need to save the file after changing anything as this will be done
     * automatically.
     * 
     * @param ipsSrcFile the newly created source file
     * @param monitor progress monitor to show progress with
     * 
     * @throws IpsException thrown in case of any error
     */
    protected abstract void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor)
            throws IpsException;

    /**
     * This method may put the new {@link IIpsSrcFile} in context to other objects. It is called
     * after the source file has been created and saved. All touched source files including the
     * given one needs to be saved by this operation!
     * 
     * @param ipsSrcFile the newly created source file
     * @param monitor progress monitor to show progress with
     */
    protected abstract void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor);

    private void callParticipants(IIpsSrcFile ipsSrcFile, SubMonitor monitor) {
        for (INewProductDefinitionOperationParticipant participant : participants) {
            participant.finishIpsSrcFile(ipsSrcFile, monitor);
        }
    }

    /**
     * Returns the {@link PresentationModelObject} that configures this operation.
     */
    protected PMO getPmo() {
        return pmo;
    }

}
