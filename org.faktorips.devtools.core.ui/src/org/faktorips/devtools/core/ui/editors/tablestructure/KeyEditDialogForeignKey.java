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

import java.beans.PropertyChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.TableStructureRefControl;

/**
 * A dialog to edit foreign key.
 */
public class KeyEditDialogForeignKey extends KeyEditDialog {

    private IKey key;

    private TableStructureRefControl refControl;

    private KeyEditForeignKeyPMO pmo;

    /** completion processor for a table structure's indices. */
    private TextButtonField tableStructureRefField;
    private TextField uniqueKeyRefField;
    private Text ukRefControl;

    private KeyContentProposalProvider contentProposalProvider;

    public KeyEditDialogForeignKey(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogForeignKey_titleText);
        this.key = key;
        this.pmo = new KeyEditForeignKeyPMO(this);
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
        refControl = getToolkit().createTableStructureRefControl(key.getIpsProject(), refTableComposite);
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
        contentProposalProvider = new KeyContentProposalProvider(new IIndex[0]);

        new UIToolkit(null).attachContentProposalAdapter(ukRefControl, contentProposalProvider,
                ContentProposalAdapter.PROPOSAL_REPLACE, null);

    }

    private void bind() {
        getBindingContext().bindContent(tableStructureRefField, key, IForeignKey.PROPERTY_REF_TABLE_STRUCTURE);
        getBindingContext().bindContent(uniqueKeyRefField, key, IForeignKey.PROPERTY_REF_UNIQUE_KEY);

        getBindingContext().bindContent(tableStructureRefField, pmo, KeyEditForeignKeyPMO.REFERENCE_TABLE);
        getBindingContext().bindContent(uniqueKeyRefField, pmo, KeyEditForeignKeyPMO.UNIQUE_KEY);

    }

    public static class KeyEditForeignKeyPMO extends PresentationModelObject {
        private static final String UNIQUE_KEY = "uniqueKey"; //$NON-NLS-1$
        private static final String REFERENCE_TABLE = "referenceTable"; //$NON-NLS-1$
        private String uniqueKey = StringUtils.EMPTY;
        private String referenceTable = StringUtils.EMPTY;
        private KeyEditDialogForeignKey dialog;

        public KeyEditForeignKeyPMO(KeyEditDialogForeignKey dialog) {
            this.dialog = dialog;
        }

        public void setUniqueKey(String uniqueKey) {
            String oldValue = getUniqueKey();
            this.uniqueKey = uniqueKey;
            this.notifyListeners(new PropertyChangeEvent(this, UNIQUE_KEY, oldValue, uniqueKey));

        }

        public String getUniqueKey() {
            IIndex[] allowedContent = getAllowedContent(getReferenceTable());
            dialog.contentProposalProvider.setUniquekeys(allowedContent);
            return uniqueKey;
        }

        public void setReferenceTable(String referenceTable) {
            String oldValue = getReferenceTable();
            this.referenceTable = referenceTable;
            this.notifyListeners(new PropertyChangeEvent(this, REFERENCE_TABLE, oldValue, referenceTable));
        }

        public String getReferenceTable() {
            return referenceTable;
        }

        private IIndex[] getAllowedContent(String refTableInput) {
            IIpsObject ipsObject;
            try {
                ipsObject = dialog.key.getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, refTableInput);
                if (ipsObject instanceof TableStructure) {
                    TableStructure tableStructure = (TableStructure)ipsObject;
                    return tableStructure.getUniqueKeys();
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }

}
