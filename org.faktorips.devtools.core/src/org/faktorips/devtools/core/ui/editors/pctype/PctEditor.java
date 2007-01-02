/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.DescriptionPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;


/**
 * The editor to edit policy component types.
 */
public class PctEditor extends IpsObjectEditor {
    
    public PctEditor() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    protected void addPagesForParsableSrcFile() throws PartInitException, CoreException {
        addPage(new StructurePage(this));
        addPage(new BehaviourPage(this));
        addPage(new DescriptionPage(this));
    }

    IPolicyCmptType getPolicyCmptType() {
        try {
            return (IPolicyCmptType)getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    /** 
     * {@inheritDoc}
     */
    protected String getUniformPageTitle() {
        return Messages.PctEditor_title + getPolicyCmptType().getName();
    }
}
