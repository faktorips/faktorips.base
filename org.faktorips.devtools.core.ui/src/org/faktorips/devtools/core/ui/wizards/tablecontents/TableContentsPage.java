/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.IpsProjectRefField;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.productdefinition.TypeSelectionComposite;
import org.faktorips.devtools.core.ui.wizards.productdefinition.UiUpdater;
import org.faktorips.util.message.MessageList;

public class TableContentsPage extends WizardPage {

    private final ResourceManager resourManager;

    private final NewTableContentsPMO pmo;

    private BindingContext bindingContext;

    private TypeSelectionComposite structureSelectionComposite;

    private TableContentsPageUiUpdater uiUpdater;

    private Text nameText;

    private Text runtimeId;

    private IpsProjectRefControl ipsProjectRefControl;

    public TableContentsPage(NewTableContentsPMO pmo) {
        super(Messages.TableContentsPage_title);
        this.pmo = pmo;
        resourManager = new LocalResourceManager(JFaceResources.getResources());
        bindingContext = new BindingContext();
    }

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.verticalSpacing = 10;

        Composite twoColumnComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(twoColumnComposite, "Project:");
        ipsProjectRefControl = toolkit.createIpsProjectRefControl(twoColumnComposite);

        toolkit.createHorizonzalLine(composite);

        structureSelectionComposite = new TypeSelectionComposite(composite, toolkit);
        structureSelectionComposite.setTitle(Messages.TableContentsPage_labelStructure);

        toolkit.createHorizonzalLine(composite);

        Composite nameAndIdComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(nameAndIdComposite, "Name:");

        nameText = toolkit.createText(nameAndIdComposite);

        toolkit.createLabel(nameAndIdComposite, "Runtime ID:");
        runtimeId = toolkit.createText(nameAndIdComposite);
        runtimeId.setEnabled(pmo.isCanEditRuntimeId());

        setControl(composite);

        bindControls(structureSelectionComposite);

        uiUpdater.updateUI();
        bindingContext.updateUI();
    }

    void bindControls(final TypeSelectionComposite typeSelectionComposite) {
        uiUpdater = new TableContentsPageUiUpdater(this, pmo);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateStructuresList();

        IpsProjectRefField ipsProjectRefField = new IpsProjectRefField(ipsProjectRefControl);
        bindingContext.bindContent(ipsProjectRefField, pmo, NewTableContentsPMO.PROPERTY_IPS_PROJECT);
        bindingContext.bindContent(typeSelectionComposite.getListViewerField(), pmo,
                NewTableContentsPMO.PROPERTY_SELECTED_STRUCTURE);

        bindingContext.bindContent(nameText, pmo, NewTableContentsPMO.PROPERTY_NAME);
        bindingContext.bindContent(runtimeId, pmo, NewTableContentsPMO.PROPERTY_RUNTIME_ID);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        fixPreviousPage();
        // setting the actual message if there is any but do not show as error
        setMessage(getMessage());
        nameText.selectAll();
        nameText.setFocus();
    }

    /**
     * Previous page may be wrong if dialog was started by new menu.
     */
    private void fixPreviousPage() {
        IWizardPage page0 = getWizard().getPages()[0];
        if (getPreviousPage() != page0) {
            page0.setPreviousPage(getPreviousPage());
            setPreviousPage(page0);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        resourManager.dispose();
        bindingContext.dispose();
        if (uiUpdater != null) {
            pmo.removePropertyChangeListener(uiUpdater);
        }
    }

    private static class TableContentsPageUiUpdater extends UiUpdater {

        private final NewTableContentsPMO pmo;

        public TableContentsPageUiUpdater(TableContentsPage productCmptPage, NewTableContentsPMO pmo) {
            super(productCmptPage);
            this.pmo = pmo;
        }

        /**
         * @return Returns the pmo.
         */
        public NewTableContentsPMO getPmo() {
            return pmo;
        }

        /**
         * @return Returns the productCmptPage.
         */
        @Override
        public TableContentsPage getPage() {
            return (TableContentsPage)super.getPage();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NewTableContentsPMO.PROPERTY_IPS_PROJECT)) {
                updateStructuresList();
            }
            super.propertyChange(evt);
        }

        public void updateStructuresList() {
            getPage().structureSelectionComposite.setListInput(getPmo().getStructuresList());
        }

        @Override
        public void updateUI() {
            super.updateUI();
            updateStructuresList();
        }

        @Override
        protected MessageList validatePage() {
            // TODO
            MessageList messageList = new MessageList();
            return messageList;
        }
    }

}
