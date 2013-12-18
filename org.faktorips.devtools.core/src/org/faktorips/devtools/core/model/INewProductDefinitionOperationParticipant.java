/*******************************************************************************
 * Copyright (c) 2005-2013 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A {@link INewProductDefinitionOperationParticipant} will be called by a
 * {@code NewProductDefinitionOperation} to allow additional changes to the created objects by
 * plugins.
 * <p>
 * <strong>This feature is experimental and the interface may change in future releases.</strong>
 * 
 * @since 3.11
 */
public interface INewProductDefinitionOperationParticipant {
    String CONFIG_ELEMENT_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
    String CONFIG_ELEMENT_ID_PARTICIPANT = "Participant"; //$NON-NLS-1$
    String EXTENSION_POINT_ID_NEW_PRODUCT_DEFINITION_OPERATION = "newProductDefinitionOperation"; //$NON-NLS-1$

    /**
     * This method is called by a {@code NewProductDefinitionOperation} after it has finished
     * creating an {@link IIpsSrcFile}, so all default values have been set. Participants can then
     * modify the file before it is saved by the operation.
     * 
     * @param ipsSrcFile The {@link IIpsSrcFile} you want to manipulate
     * @param monitor A progress monitor to show your progress
     */
    void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor);
}
