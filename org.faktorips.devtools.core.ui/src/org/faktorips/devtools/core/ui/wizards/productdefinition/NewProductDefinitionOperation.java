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
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.faktorips.devtools.core.ExtensionPoints;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.INewProductDefinitionOperationParticipant;

public class NewProductDefinitionOperation extends WorkspaceModifyOperation {

    private final NewProductDefinitionPMO pmo;
    private List<INewProductDefinitionOperationParticipant> participants;

    public NewProductDefinitionOperation(NewProductDefinitionPMO pmo) {
        this.pmo = pmo;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
            InterruptedException {

    }

    /**
     * Returns the {@link INewProductDefinitionOperationParticipant
     * INewProductDefinitionOperationParticipants} defined in the extension point
     * {@code org.faktorips.devtools.core.newProductDefinitionOperation}.
     * 
     * @return the {@link INewProductDefinitionOperationParticipant
     *         INewProductDefinitionOperationParticipants}
     */
    private List<INewProductDefinitionOperationParticipant> getParticipants() {
        if (participants == null) {
            ExtensionPoints extensionPoints = new ExtensionPoints(IpsPlugin.getDefault().getExtensionRegistry(),
                    IpsPlugin.PLUGIN_ID);
            IExtension[] extensions = extensionPoints
                    .getExtension(INewProductDefinitionOperationParticipant.EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION);
            participants = new ArrayList<INewProductDefinitionOperationParticipant>();
            for (IExtension extension : extensions) {
                participants.addAll(ExtensionPoints.createExecutableExtensions(extension,
                        INewProductDefinitionOperationParticipant.CONFIG_ELEMENT_ID_PARTICIPANT,
                        INewProductDefinitionOperationParticipant.CONFIG_ELEMENT_ATTRIBUTE_CLASS,
                        INewProductDefinitionOperationParticipant.class));
            }
        }
        return participants;
    }

}
