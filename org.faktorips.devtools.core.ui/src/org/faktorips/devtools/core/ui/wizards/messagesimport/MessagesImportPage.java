/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleIdentification;
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
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRootRefField;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.inputformat.IntegerNumberFormat;
import org.faktorips.util.message.MessageList;

/**
 * Wizard page for translated messages for validation rules.
 * 
 */
public class MessagesImportPage extends WizardDataTransferPage {

    private IpsPckFragmentRootRefControl target;
    private final IStructuredSelection selection;
    private UIToolkit uiToolkit;
    private BindingContext bindingContext;
    private Combo localeCombo;
    private FileSelectionControl fileSelectionControl;
    private final MessagesImportPMO messagesImportPMO;
    private ComboViewerField<ISupportedLanguage> localeComboField;
    private TextField formatDelimiter;
    private FormattingTextField<String> identifierColumnIndex;
    private FormattingTextField<String> textColumnIndex;
    private RadioButtonGroupField<ValidationRuleIdentification> ruleIndetifierRadioButtons;
    private RadioButtonGroupField<String> formatRadioButtons;
    private Checkbox warningCheckbox;
    private Group formatSettingsGroup;

    protected MessagesImportPage(String name, IStructuredSelection selection) {
        super(name);
        this.selection = selection;
        setTitle(Messages.MessagesImportPage_pageTitle);
        messagesImportPMO = new MessagesImportPMO();
        getMessagesImportPMO().addPropertyChangeListener(new MessageImportUiUpdater(this));
    }

    @Override
    public void handleEvent(Event event) {
        // Nothing to do
    }

    @Override
    public void createControl(Composite parent) {
        uiToolkit = new UIToolkit(null);
        bindingContext = new BindingContext();

        Composite rootComposite = uiToolkit.createGridComposite(parent, 2, false, false);

        Composite labelEditComposite = createImportControl(rootComposite);
        createImportFileComposite(labelEditComposite);

        createFormatControl(labelEditComposite);
        createLocaleControl(labelEditComposite);
        createIdentificationControl(labelEditComposite);
        createWarningCheckbox(labelEditComposite);

        bindContent();

        setControl(rootComposite);
        initDefaults();
        validatePage();

    }

    private Composite createImportControl(Composite rootComposite) {
        Composite labelEditComposite = uiToolkit.createLabelEditColumnComposite(rootComposite);
        GridLayout layout = (GridLayout)labelEditComposite.getLayout();
        layout.marginLeft = 5;

        Label targetLabel = uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTarget);
        setWidthHint(targetLabel, 182);
        target = new IpsPckFragmentRootRefControl(labelEditComposite, true, uiToolkit);
        return labelEditComposite;
    }

    private void createImportFileComposite(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelImportFile);
        fileSelectionControl = new FileSelectionControl(labelEditComposite, uiToolkit, NONE);
        fileSelectionControl.getDialog().setFilterExtensions(new String[] { "*.csv", "*.properties" }); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void createFormatControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportWizard_labelFormat);

        LinkedHashMap<String, String> radioButtons = new LinkedHashMap<String, String>();
        radioButtons.put(MessagesImportPMO.FORMAT_PROPERTIES_FILE, Messages.MessagesImportWizard_labelFormatProperties);
        radioButtons.put(MessagesImportPMO.FORMAT_CSV_FILE, Messages.MessagesImportWizard_labelFormatCSV);
        RadioButtonGroup<String> radioGroup = uiToolkit.createRadioButtonGroup(labelEditComposite, radioButtons);
        radioGroup.setSelection(MessagesImportPMO.FORMAT_CSV_FILE);
        formatRadioButtons = new RadioButtonGroupField<String>(radioGroup);

        createFormatSettingsControl(labelEditComposite);
    }

    private void setWidthHint(Control c, int widthHint) {
        GridData textGD;
        if (c.getLayoutData() instanceof GridData) {
            textGD = (GridData)c.getLayoutData();
        } else {
            textGD = new GridData();
        }
        textGD.widthHint = widthHint;
        c.setLayoutData(textGD);
    }

    private void createFormatSettingsControl(Composite labelEditComposite) {
        formatSettingsGroup = uiToolkit.createGroup(labelEditComposite,
                Messages.MessagesImportWizard_labelFormatSettings);
        GridData groupGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        groupGD.horizontalSpan = 2;
        formatSettingsGroup.setLayoutData(groupGD);

        Composite formatSettingsComposite = uiToolkit.createLabelEditColumnComposite(formatSettingsGroup);

        Label formatLabel = uiToolkit.createFormLabel(formatSettingsComposite,
                Messages.MessagesImportWizard_labelFormatSettingsDelimiter);
        formatDelimiter = new TextField(uiToolkit.createText(formatSettingsComposite));
        setWidthHint(formatLabel, 175);

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsIdentifier);
        identifierColumnIndex = new FormattingTextField<String>(uiToolkit.createText(formatSettingsComposite),
                IntegerNumberFormat.newInstance(ValueDatatype.INTEGER));

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsColumn);
        textColumnIndex = new FormattingTextField<String>(uiToolkit.createText(formatSettingsComposite),
                IntegerNumberFormat.newInstance(ValueDatatype.INTEGER));
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
    }

    private void createIdentificationControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportWizard_labelIdentification);
        LinkedHashMap<ValidationRuleIdentification, String> radioButtons = new LinkedHashMap<ValidationRuleIdentification, String>();
        radioButtons.put(ValidationRuleIdentification.QUALIFIED_RULE_NAME,
                Messages.MessagesImportWizard_labelIdentificationName);
        radioButtons.put(ValidationRuleIdentification.MESSAGE_CODE,
                Messages.MessagesImportWizard_labelIdentificationCode);
        RadioButtonGroup<ValidationRuleIdentification> radioGroup = uiToolkit.createRadioButtonGroup(
                labelEditComposite, radioButtons);
        radioGroup.setSelection(ValidationRuleIdentification.QUALIFIED_RULE_NAME);
        ruleIndetifierRadioButtons = new RadioButtonGroupField<ValidationRuleIdentification>(radioGroup);
    }

    private void createWarningCheckbox(Composite labelEditComposite) {
        uiToolkit.createFormLabel(labelEditComposite, StringUtils.EMPTY);
        warningCheckbox = uiToolkit.createCheckbox(labelEditComposite,
                Messages.MessagesImportWizard_checkboxEnableWarnings);
    }

    private void bindContent() {
        bindingContext.bindContent(new IpsPckFragmentRootRefField(target), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT);
        bindingContext.bindContent(new TextButtonField(fileSelectionControl), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FILE_NAME);

        bindingContext.bindContent(formatRadioButtons, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_FORMAT);
        bindingContext
                .bindContent(formatDelimiter, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_COLUMN_DELIMITER);
        bindingContext.bindContent(identifierColumnIndex, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IDENTIFIER_COLUMN_INDEX);
        bindingContext.bindContent(textColumnIndex, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_TEXT_COLUMN_INDEX);

        bindingContext.bindContent(ruleIndetifierRadioButtons, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_RULE_IDENTIFIER);
        bindingContext.bindContent(localeComboField, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_SUPPORTED_LANGUAGE);
        bindingContext.bindContent(warningCheckbox, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_ENABLE_WARNINGS);

        bindFormatSettings();
    }

    private void bindFormatSettings() {
        bindingContext.bindVisible(formatSettingsGroup, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_VISIBLE, true);

        bindingContext.bindEnabled(formatDelimiter.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT, MessagesImportPMO.FORMAT_CSV_FILE);
        bindingContext
                .bindContent(formatDelimiter, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_COLUMN_DELIMITER);
        bindingContext.bindEnabled(identifierColumnIndex.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT, MessagesImportPMO.FORMAT_CSV_FILE);
        bindingContext.bindContent(identifierColumnIndex, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IDENTIFIER_COLUMN_INDEX);
        bindingContext.bindEnabled(textColumnIndex.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT, MessagesImportPMO.FORMAT_CSV_FILE);
        bindingContext.bindContent(textColumnIndex, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_TEXT_COLUMN_INDEX);
    }

    private void initDefaults() {
        if (selection == null) {
            return;
        }
        Object firstElement = selection.getFirstElement();
        if (firstElement instanceof IAdaptable) {
            IAdaptable adaptableObject = (IAdaptable)firstElement;
            IIpsElement ipsElement = getIpsElement(adaptableObject);
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

    private IIpsElement getIpsElement(IAdaptable adaptableObject) {
        IIpsElement ipsElement = (IIpsElement)adaptableObject.getAdapter(IIpsElement.class);
        if (ipsElement == null) {
            IResource resource = (IResource)adaptableObject.getAdapter(IResource.class);
            if (resource != null) {
                ipsElement = (IIpsElement)resource.getAdapter(IIpsElement.class);
                if (ipsElement == null) {
                    ipsElement = (IIpsElement)resource.getProject().getAdapter(IIpsElement.class);
                }
            }
        }
        return ipsElement;
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
