/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import java.beans.PropertyChangeEvent;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.IpsProjectRefField;
import org.faktorips.devtools.core.ui.controls.IpsProjectRefControl;
import org.faktorips.devtools.core.ui.wizards.productdefinition.PageUiUpdater;
import org.faktorips.devtools.core.ui.wizards.productdefinition.TypeSelectionComposite;
import org.faktorips.util.message.MessageList;

public class TableContentsPage extends WizardPage {

    private final ResourceManager resourManager;

    private final NewTableContentsPMO pmo;

    private BindingContext bindingContext;

    private TypeSelectionComposite structureSelectionComposite;

    private TableContentsPageUiUpdater uiUpdater;

    private Text nameText;

    private IpsProjectRefControl ipsProjectRefControl;

    public TableContentsPage(NewTableContentsPMO pmo) {
        super(Messages.TableContentsPage_title);
        setTitle(Messages.TableContentsPage_pageTitle);
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
        toolkit.createLabel(twoColumnComposite, Messages.TableContentsPage_label_project);
        ipsProjectRefControl = toolkit.createIpsProjectRefControl(twoColumnComposite);

        toolkit.createHorizonzalLine(composite);

        structureSelectionComposite = new TypeSelectionComposite(composite, toolkit, pmo,
                NewTableContentsPMO.PROPERTY_SELECTED_STRUCTURE);
        structureSelectionComposite.setTitle(Messages.TableContentsPage_labelStructure);

        toolkit.createHorizonzalLine(composite);

        Composite nameAndIdComposite = toolkit.createLabelEditColumnComposite(composite);
        toolkit.createLabel(nameAndIdComposite, Messages.TableContentsPage_label_name);

        nameText = toolkit.createText(nameAndIdComposite);

        setControl(composite);

        bindControls();

        uiUpdater.updateUI();
        bindingContext.updateUI();
    }

    void bindControls() {
        uiUpdater = new TableContentsPageUiUpdater(this, pmo);
        pmo.addPropertyChangeListener(uiUpdater);
        uiUpdater.updateStructuresList();

        IpsProjectRefField ipsProjectRefField = new IpsProjectRefField(ipsProjectRefControl);
        bindingContext.bindContent(ipsProjectRefField, pmo, NewTableContentsPMO.PROPERTY_IPS_PROJECT);

        bindingContext.bindContent(nameText, pmo, NewTableContentsPMO.PROPERTY_NAME);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        // setting the actual message if there is any but do not show as error
        setMessage(getMessage());
        nameText.selectAll();
        nameText.setFocus();
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

    private static class TableContentsPageUiUpdater extends PageUiUpdater {

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
            if (NewTableContentsPMO.PROPERTY_IPS_PROJECT.equals(evt.getPropertyName())) {
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
            MessageList messageList = pmo.getValidator().validateTableContents();
            return messageList;
        }
    }

}
