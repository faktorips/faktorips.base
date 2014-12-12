/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRootRefField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.util.message.MessageList;

/**
 * Wizard page for translated messages for validation rules.
 * 
 * TODO This implementation is not completed yet. Have a look at FIPS-626
 * 
 * @author dirmeier
 */
public class MessagesImportPage extends WizardDataTransferPage {

    IpsPckFragmentRootRefControl target;
    private final IStructuredSelection selection;
    private UIToolkit uiToolkit;
    private BindingContext bindingContext;
    private Combo localeCombo;
    private FileSelectionControl fileSelectionControl;
    private final MessagesImportPMO messagesImportPMO;
    private ComboViewerField<ISupportedLanguage> localeComboField;

    protected MessagesImportPage(String name, IStructuredSelection selection) {
        super(name);
        this.selection = selection;
        setTitle(Messages.MessagesImportPage_pageTitle);
        messagesImportPMO = new MessagesImportPMO();
        getMessagesImportPMO().addPropertyChangeListener(new MessageImportUiUpdater(this));
    }

    @Override
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
    }

    @Override
    public void createControl(Composite parent) {
        uiToolkit = new UIToolkit(null);
        bindingContext = new BindingContext();

        Composite rootComposite = uiToolkit.createGridComposite(parent, 2, false, false);

        Composite labelEditComposite = createImportControl(rootComposite);
        createLanguagesComposite(labelEditComposite);

        createFormatControl(labelEditComposite);
        createLocaleControl(labelEditComposite);
        createIdentificationControl(labelEditComposite);

        setControl(rootComposite);
        initDefaults();
        validatePage();

    }

    private Composite createImportControl(Composite rootComposite) {
        Composite labelEditComposite = uiToolkit.createLabelEditColumnComposite(rootComposite);

        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTarget);
        target = new IpsPckFragmentRootRefControl(labelEditComposite, true, uiToolkit);

        bindingContext.bindContent(new IpsPckFragmentRootRefField(target), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT);
        return labelEditComposite;
    }

    private void createLanguagesComposite(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTranslations);
        fileSelectionControl = new FileSelectionControl(labelEditComposite, uiToolkit, NONE);
        fileSelectionControl.getDialog().setFilterExtensions(new String[] { "*.csv", "*.properties" }); //$NON-NLS-1$ //$NON-NLS-2$
        bindingContext.bindContent(new TextButtonField(fileSelectionControl), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FILE_NAME);
    }

    private void createFormatControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportWizard_labelFormat);

        Composite composite = uiToolkit.createGridComposite(labelEditComposite, 2, true, false);

        uiToolkit.createRadiobutton(composite, Messages.MessagesImportWizard_labelFormatProperties);
        uiToolkit.createRadiobutton(composite, Messages.MessagesImportWizard_labelFormatCSV);
        createFormatSettingsControl(labelEditComposite);

    }

    private void createFormatSettingsControl(Composite labelEditComposite) {
        uiToolkit.createFormLabel(labelEditComposite, StringUtils.EMPTY);
        Group formatgroup = uiToolkit
                .createGroup(labelEditComposite, Messages.MessagesImportWizard_labelFormatSettings);
        formatgroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        Composite formatSettingsComposite = uiToolkit.createLabelEditColumnComposite(formatgroup);
        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsDelimiter);
        uiToolkit.createText(formatSettingsComposite);

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsIdentifier);
        uiToolkit.createText(formatSettingsComposite);

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsColumn);
        uiToolkit.createText(formatSettingsComposite);

    }

    protected void createLocaleControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelLocale);
        localeCombo = uiToolkit.createCombo(labelEditComposite);
        localeComboField = new ComboViewerField<ISupportedLanguage>(localeCombo, ISupportedLanguage.class);
        localeComboField.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof ISupportedLanguage) {
                    ISupportedLanguage supportedLanguage = (ISupportedLanguage)element;
                    return supportedLanguage.getLanguageName();
                }
                return super.getText(element);
            }
        });
        bindingContext.bindContent(localeComboField, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_LOCALE);

    }

    private void createIdentificationControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportWizard_labelIdentification);
        Composite composite = uiToolkit.createGridComposite(labelEditComposite, 2, true, false);
        uiToolkit.createRadiobutton(composite, Messages.MessagesImportWizard_labelIdentificationName);
        uiToolkit.createRadiobutton(composite, Messages.MessagesImportWizard_labelIdentificationCode);
    }

    private void initDefaults() {
        if (selection == null) {
            return;
        }
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)firstElement;
            IIpsElement ipsElement = (IIpsElement)adaptableObject.getAdapter(IIpsElement.class);
            if (ipsElement == null) {
                IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
                ipsElement = (IIpsElement)resource.getAdapter(IIpsElement.class);
                if (ipsElement == null) {
                    ipsElement = (IIpsElement)resource.getProject().getAdapter(IIpsElement.class);
                }
            }
            if (ipsElement instanceof IIpsProject) {
                IIpsProject ipsProject = (IIpsProject)ipsElement;
                getMessagesImportPMO().setIpsPackageFragmentRoot(ipsProject.getIpsPackageFragmentRoots()[0]);
            } else if (ipsElement instanceof IIpsPackageFragmentRoot) {
                IIpsPackageFragmentRoot pckFragRoot = (IIpsPackageFragmentRoot)ipsElement;
                getMessagesImportPMO().setIpsPackageFragmentRoot(pckFragRoot);
            } else if (ipsElement instanceof IIpsPackageFragment) {
                IIpsPackageFragment pckFrag = (IIpsPackageFragment)ipsElement;
                getMessagesImportPMO().setIpsPackageFragmentRoot(pckFrag.getRoot());
            } else if (ipsElement instanceof IIpsSrcFile) {
                IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
                getMessagesImportPMO().setIpsPackageFragmentRoot(ipsSrcFile.getIpsPackageFragment().getRoot());
            } else if (ipsElement instanceof IIpsObjectPartContainer) {
                IIpsObjectPartContainer partContainer = (IIpsObjectPartContainer)ipsElement;
                getMessagesImportPMO().setIpsPackageFragmentRoot(
                        partContainer.getIpsSrcFile().getIpsPackageFragment().getRoot());
            }
        }
    }

    void validatePage() {
        MessageList msgList = getMessagesImportPMO().validate();
        if (msgList.isEmpty()) {
            setErrorMessage(null);
            setPageComplete(true);
        } else {
            setErrorMessage(msgList.getMessageWithHighestSeverity().getText());
            setPageComplete(false);
        }

    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    /**
     * @return Returns the messagesImportPMO.
     */
    public MessagesImportPMO getMessagesImportPMO() {
        return messagesImportPMO;
    }

    private static class MessageImportUiUpdater implements PropertyChangeListener {

        private final MessagesImportPage page;

        public MessageImportUiUpdater(MessagesImportPage page) {
            this.page = page;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT.equals(evt.getPropertyName())) {
                updateAvailableLocales();
            }

            page.validatePage();
        }

        void updateAvailableLocales() {
            Set<ISupportedLanguage> supportedLanguages = page.getMessagesImportPMO().getAvailableLocales();
            page.localeComboField
                    .setInput(supportedLanguages.toArray(new ISupportedLanguage[supportedLanguages.size()]));

        }

    }

}
