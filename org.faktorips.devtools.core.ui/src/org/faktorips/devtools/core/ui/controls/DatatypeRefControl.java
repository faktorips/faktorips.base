/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.DatatypeSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A control that allows to edit a reference to a datatype.
 */
public class DatatypeRefControl extends TextButtonControl {

    private IIpsProject ipsProject;

    private DatatypeContentProposalProvider proposalProvider;

    private UIToolkit toolkit;

    public DatatypeRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.DatatypeRefControl_title);

        ipsProject = project;

        this.toolkit = toolkit;

        proposalProvider = new DatatypeContentProposalProvider(ipsProject);
        UIToolkit.attachContentProposalAdapter(getTextControl(), proposalProvider,
                new DatatypeContentProposalLabelProvider());
    }

    public UIToolkit getToolkit() {
        return toolkit;
    }

    public void setVoidAllowed(boolean includeVoid) {
        proposalProvider.setIncludeVoid(includeVoid);
    }

    public void setPrimitivesAllowed(boolean includePrimitives) {
        proposalProvider.setIncludePrimitives(includePrimitives);
    }

    public boolean getPrimitivesAllowed() {
        return proposalProvider.isIncludePrimitives();
    }

    public boolean isVoidAllowed() {
        return proposalProvider.isIncludeVoid();
    }

    public void setOnlyValueDatatypesAllowed(boolean valuetypesOnly) {
        proposalProvider.setValueDatatypesOnly(valuetypesOnly);
    }

    public boolean isOnlyValueDatatypesAllowed() {
        return proposalProvider.isValueDatatypesOnly();
    }

    public void setAbstractAllowed(boolean abstractAllowed) {
        proposalProvider.setIncludeAbstract(abstractAllowed);
    }

    public boolean isAbstractAllowed() {
        return proposalProvider.isIncludeAbstract();
    }

    public void setIpsProject(IIpsProject project) {
        this.ipsProject = project;
        proposalProvider.setIpsProject(project);
    }

    public void setDisallowedDatatypes(List<Datatype> disallowedDatatypes) {
        proposalProvider.setExcludedDatatypes(disallowedDatatypes);
    }

    public List<Datatype> getDisallowedDatatypes() {
        return proposalProvider.getExcludedDatatypes();
    }

    @Override
    protected void buttonClicked() {
        DatatypeSelectionDialog dialog = new DatatypeSelectionDialog(getShell());
        dialog.setElements(ipsProject.findDatatypes(isOnlyValueDatatypesAllowed(), isVoidAllowed(),
                getPrimitivesAllowed(), getDisallowedDatatypes(), isAbstractAllowed()));
        if (dialog.open() == Window.OK) {
            String textToSet = ""; //$NON-NLS-1$
            if (dialog.getResult().length > 0) {
                Datatype datatype = (Datatype)dialog.getResult()[0];
                textToSet = datatype.getQualifiedName();
            }
            try {
                setImmediatelyNotifyListener(true);
                getTextControl().setText(textToSet);
            } finally {
                setImmediatelyNotifyListener(false);
            }
        }
    }
}
