/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.productcmpt.Messages;

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
 * It is also possible to override {@link #createIpsSrcFile(IProgressMonitor)} if it should be
 * necessary to hook into creation of the source file.
 * 
 * @param <PMO> type of the {@link PresentationModelObject} that configures this operation
 */
public abstract class NewProductDefinitionOperation<PMO extends NewProductDefinitionPMO> extends
        WorkspaceModifyOperation {

    private final PMO pmo;

    private final List<INewProductDefinitionOperationParticipant> participants = new ArrayList<INewProductDefinitionOperationParticipant>();

    protected NewProductDefinitionOperation(PMO pmo) {
        this.pmo = pmo;
        loadParticipantsFromExtensions();
    }

    private void loadParticipantsFromExtensions() {
        ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.getDefault().getExtensionRegistry(),
                IpsPlugin.PLUGIN_ID);
        IExtension[] extensions = extensionPoints
                .getExtension(INewProductDefinitionOperationParticipant.EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION);
        for (IExtension extension : extensions) {
            participants.addAll(ExtensionPoints.createExecutableExtensions(extension,
                    INewProductDefinitionOperationParticipant.CONFIG_ELEMENT_ID_PARTICIPANT,
                    INewProductDefinitionOperationParticipant.CONFIG_ELEMENT_ATTRIBUTE_CLASS,
                    INewProductDefinitionOperationParticipant.class));
        }
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask(Messages.NewProductCmptWizard_title, 5);

        try {
            createIpsPackageFragmentIfNonExistent(monitor);

            IIpsSrcFile ipsSrcFile = createIpsSrcFile(monitor);
            finishIpsSrcFile(ipsSrcFile, monitor);
            callParticipants(ipsSrcFile, monitor);
            saveIpsSrcFile(ipsSrcFile, monitor);
            postProcess(ipsSrcFile, monitor);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } finally {
            monitor.done();
        }
    }

    private void createIpsPackageFragmentIfNonExistent(IProgressMonitor monitor) throws CoreException {
        IIpsPackageFragment ipsPackage = pmo.getIpsPackage();
        if (!ipsPackage.exists()) {
            pmo.getPackageRoot().createPackageFragment(ipsPackage.getName(), true, new SubProgressMonitor(monitor, 1));
        }
    }

    private void saveIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        ipsSrcFile.save(true, new SubProgressMonitor(monitor, 1));
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
     * @throws CoreException in case of exceptions during file creation
     */
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException {
        // @formatter:off
        return pmo.getIpsPackage().createIpsFile(
                pmo.getIpsObjectType(),
                pmo.getName(),
                true,
                new SubProgressMonitor(monitor, 1));
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
     * @throws CoreException thrown in case of any error
     */
    protected abstract void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException;

    /**
     * <strong>Subclassing:</strong><br>
     * This method may put the new {@link IIpsSrcFile} in context to other objects. It is called
     * after the source file has been created and saved.
     * 
     * @param ipsSrcFile the newly created source file
     * @param monitor progress monitor to show progress with
     */
    protected abstract void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor);

    private void callParticipants(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        for (INewProductDefinitionOperationParticipant participant : participants) {
            participant.finishIpsSrcFile(ipsSrcFile, monitor);
        }
    }

    /**
     * Returns the {@link PresentationModelObject} that configures this operation.
     */
    protected final PMO getPmo() {
        return pmo;
    }

}
