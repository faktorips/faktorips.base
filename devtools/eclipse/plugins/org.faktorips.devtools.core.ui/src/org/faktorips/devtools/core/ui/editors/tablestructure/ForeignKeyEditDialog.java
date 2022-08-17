/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;
import org.faktorips.devtools.model.tablestructure.IForeignKey;

/**
 * A dialog to edit foreign key.
 */
public class ForeignKeyEditDialog extends KeyEditDialog {

    private TableStructureRefControl refControl;
    private ForeignKeyPMO pmo;

    /** completion processor for a table structure's indices. */
    private TextButtonField tableStructureRefField;
    private TextField uniqueKeyRefField;
    private Text ukRefControl;

    private UniqueKeysProposalProvider contentProposalProvider;

    public ForeignKeyEditDialog(IForeignKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogForeignKey_titleText);
        pmo = new ForeignKeyPMO(key);
    }

    @Override
    protected void addPageTopControls(Composite pageComposite) {
        createStructureAndKeyReferenceControls(pageComposite);
        bind();
    }

    protected void createStructureAndKeyReferenceControls(Composite pageComposite) {
        Composite refTableComposite = getToolkit().createLabelEditColumnComposite(pageComposite);
        GridLayout layout = (GridLayout)refTableComposite.getLayout();
        layout.marginHeight = 12;
        createLabelReferenceStructure(refTableComposite);
        createLabelUniqueKey(refTableComposite);
    }

    private void createLabelReferenceStructure(Composite refTableComposite) {
        getToolkit().createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceStructure);
        refControl = getToolkit().createTableStructureRefControl(getIpsPart().getIpsProject(), refTableComposite);
        refControl.setFocus();
        tableStructureRefField = new TextButtonField(refControl);
    }

    private void createLabelUniqueKey(Composite refTableComposite) {
        getToolkit().createFormLabel(refTableComposite, Messages.KeyEditDialog_labelReferenceUniqueKey);
        ukRefControl = getToolkit().createText(refTableComposite);
        uniqueKeyRefField = new TextField(ukRefControl);
        setupContentAssist(ukRefControl);
    }

    protected void setupContentAssist(Text ukRefControl) {
        contentProposalProvider = new UniqueKeysProposalProvider(pmo);
        UIToolkit.attachContentProposalAdapter(ukRefControl, contentProposalProvider,
                ContentProposalAdapter.PROPOSAL_REPLACE, null);
    }

    private void bind() {
        getBindingContext().bindContent(tableStructureRefField, pmo, ForeignKeyPMO.REFERENCE_TABLE);
        getBindingContext().bindContent(uniqueKeyRefField, pmo, ForeignKeyPMO.UNIQUE_KEY);
    }
}
