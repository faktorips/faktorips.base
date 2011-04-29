/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.PolicyCmtTypeDescriptionPage;

/**
 * The editor to edit policy component types.
 */
public class PolicyCmptTypeEditor extends TypeEditor implements IModelDescriptionSupport {

    public PolicyCmptTypeEditor() {
        super();
    }

    IPolicyCmptType getPolicyCmptType() {
        try {
            return (IPolicyCmptType)getIpsObject();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.PctEditor_title + getPolicyCmptType().getName();
    }

    @Override
    protected void addAllInOneSinglePage() throws PartInitException {
        addPage(new PolicyCmptTypeStructurePage(this, false));
        if (getIpsProject().isPersistenceSupportEnabled()) {
            addPage(new PersistencePage(this));
        }
    }

    @Override
    protected void addSplittedInMorePages() throws PartInitException {
        addPage(new PolicyCmptTypeStructurePage(this, true));
        addPage(new PolicyCmptTypeBehaviourPage(this));
        if (getIpsProject().isPersistenceSupportEnabled()) {
            addPage(new PersistencePage(this));
        }
    }

    @Override
    public IPage createModelDescriptionPage() throws CoreException {
        return new PolicyCmtTypeDescriptionPage(getPolicyCmptType());
    }

}
