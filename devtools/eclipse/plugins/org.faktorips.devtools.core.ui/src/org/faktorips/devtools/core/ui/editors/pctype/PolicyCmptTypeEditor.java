/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.PolicyCmtTypeDescriptionPage;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;

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
    public IPage createModelDescriptionPage() {
        return new PolicyCmtTypeDescriptionPage(getPolicyCmptType());
    }

}
