/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;

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

    public ForeignKeyEditDialog(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogForeignKey_titleText);
        this.pmo = new ForeignKeyPMO(key);
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
        contentProposalProvider = new UniqueKeysProposalProvider();
        getToolkit().attachContentProposalAdapter(ukRefControl, contentProposalProvider,
                ContentProposalAdapter.PROPOSAL_REPLACE, null);
        addListenerToRefreshContentProposalProvider();
    }

    private void addListenerToRefreshContentProposalProvider() {
        ukRefControl.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
            }

            @Override
            public void keyPressed(KeyEvent e) {
                IIndex[] allowedContent = pmo.getAllowedContent();
                contentProposalProvider.setUniqueKeys(allowedContent);
            }
        });
    }

    private void bind() {
        getBindingContext().bindContent(tableStructureRefField, getIpsPart(), IForeignKey.PROPERTY_REF_TABLE_STRUCTURE);
        getBindingContext().bindContent(uniqueKeyRefField, getIpsPart(), IForeignKey.PROPERTY_REF_UNIQUE_KEY);

        getBindingContext().bindContent(tableStructureRefField, pmo, ForeignKeyPMO.REFERENCE_TABLE);
        getBindingContext().bindContent(uniqueKeyRefField, pmo, ForeignKeyPMO.UNIQUE_KEY);
    }
}
