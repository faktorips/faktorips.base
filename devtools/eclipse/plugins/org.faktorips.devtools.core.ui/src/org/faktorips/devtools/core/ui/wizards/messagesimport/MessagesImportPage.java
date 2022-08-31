/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import java.util.Locale;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.VerifyEvent;
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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.ComboViewerField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controller.fields.IpsPckFragmentRootRefField;
import org.faktorips.devtools.core.ui.controller.fields.RadioButtonGroupField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.FileSelectionControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.devtools.core.ui.controls.RadioButtonGroup;
import org.faktorips.devtools.core.ui.inputformat.AbstractInputFormat;
import org.faktorips.devtools.core.ui.inputformat.IntegerNumberFormat;
import org.faktorips.devtools.core.ui.wizards.messagesimport.MessagesImportPMO.ImportFormat;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;

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
    private FormattingTextField<Character> formatDelimiter;
    private FormattingTextField<String> identifierColumnIndex;
    private FormattingTextField<String> textColumnIndex;
    private RadioButtonGroupField<ValidationRuleIdentification> ruleIdentifierRadioButtons;
    private RadioButtonGroupField<ImportFormat> formatRadioButtons;
    private Checkbox warningCheckbox;
    private Group formatSettingsGroup;
    private Control formatSettingsSpacer;

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

        Composite rootComposite = uiToolkit.createGridComposite(parent, 1, false, false);
        GridLayout layout = (GridLayout)rootComposite.getLayout();
        layout.marginLeft = 5;

        createSourceGroup(rootComposite);
        createTargetGroup(rootComposite);

        bindContent();

        setControl(rootComposite);
        initDefaults();
        validatePage();
        bindingContext.updateUI();
    }

    private void createSourceGroup(Composite rootComposite) {
        Group sourceGroup = uiToolkit.createGridGroup(rootComposite, Messages.MessagesImportPage_Label_SourceGroup, 2,
                false);

        createImportFileComposite(sourceGroup);
        createFormatControl(sourceGroup);
        createLocaleControl(sourceGroup);
        createIdentificationControl(sourceGroup);
    }

    private Composite createTargetGroup(Composite rootComposite) {
        Group targetGroup = uiToolkit.createGridGroup(rootComposite, Messages.MessagesImportPage_Label_TargetGroup, 2,
                false);
        Label targetLabel = uiToolkit.createLabel(targetGroup, Messages.MessagesImportPage_labelTarget);
        setWidthHint(targetLabel, 182);
        target = new IpsPckFragmentRootRefControl(targetGroup, true, uiToolkit);

        uiToolkit.createFormLabel(targetGroup, IpsStringUtils.EMPTY);
        warningCheckbox = uiToolkit.createCheckbox(targetGroup, Messages.MessagesImportWizard_checkboxEnableWarnings);
        return targetGroup;
    }

    private void createImportFileComposite(Composite labelEditComposite) {
        Label fileLabel = uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelImportFile);
        setWidthHint(fileLabel, 182);
        fileSelectionControl = new FileSelectionControl(labelEditComposite, uiToolkit, NONE);
        fileSelectionControl
                .getDialog()
                .setFilterExtensions(
                        new String[] {
                                ImportFormat.CSV.getFilenamePattern()
                                        + ";" + ImportFormat.PROPERTIES.getFilenamePattern(), //$NON-NLS-1$
                                ImportFormat.CSV.getFilenamePattern(), ImportFormat.PROPERTIES.getFilenamePattern() });
    }

    private void createFormatControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportWizard_labelFormat);

        LinkedHashMap<ImportFormat, String> radioButtons = new LinkedHashMap<>();
        radioButtons.put(ImportFormat.PROPERTIES, Messages.MessagesImportWizard_labelFormatProperties);
        radioButtons.put(ImportFormat.CSV, Messages.MessagesImportWizard_labelFormatCSV);
        RadioButtonGroup<ImportFormat> radioGroup = uiToolkit.createRadioButtonGroup(labelEditComposite, radioButtons);
        formatRadioButtons = new RadioButtonGroupField<>(radioGroup);

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
        formatSettingsSpacer = uiToolkit.createHorizontalSpacer(labelEditComposite, 0);
        formatSettingsGroup = uiToolkit.createGroup(labelEditComposite,
                Messages.MessagesImportWizard_labelFormatSettings);
        GridData groupGD = new GridData(GridData.FILL, GridData.FILL, true, true);
        // groupGD.horizontalSpan = 2;
        formatSettingsGroup.setLayoutData(groupGD);

        Composite formatSettingsComposite = uiToolkit.createLabelEditColumnComposite(formatSettingsGroup);

        Label formatLabel = uiToolkit.createFormLabel(formatSettingsComposite,
                Messages.MessagesImportWizard_labelFormatSettingsDelimiter);
        formatDelimiter = new FormattingTextField<>(uiToolkit.createText(formatSettingsComposite),
                new DelimiterInputFormat());
        setWidthHint(formatLabel, 170);

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsIdentifier);
        identifierColumnIndex = new FormattingTextField<>(uiToolkit.createText(formatSettingsComposite),
                IntegerNumberFormat.newInstance(ValueDatatype.INTEGER));

        uiToolkit.createFormLabel(formatSettingsComposite, Messages.MessagesImportWizard_labelFormatSettingsColumn);
        textColumnIndex = new FormattingTextField<>(uiToolkit.createText(formatSettingsComposite),
                IntegerNumberFormat.newInstance(ValueDatatype.INTEGER));
    }

    protected void createLocaleControl(Composite labelEditComposite) {
        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelLocale);
        localeCombo = uiToolkit.createCombo(labelEditComposite);
        localeComboField = new ComboViewerField<>(localeCombo, ISupportedLanguage.class);
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
        LinkedHashMap<ValidationRuleIdentification, String> radioButtons = new LinkedHashMap<>();
        radioButtons.put(ValidationRuleIdentification.QUALIFIED_RULE_NAME,
                Messages.MessagesImportWizard_labelIdentificationName);
        radioButtons.put(ValidationRuleIdentification.MESSAGE_CODE,
                Messages.MessagesImportWizard_labelIdentificationCode);
        RadioButtonGroup<ValidationRuleIdentification> radioGroup = uiToolkit.createRadioButtonGroup(
                labelEditComposite, radioButtons);
        ruleIdentifierRadioButtons = new RadioButtonGroupField<>(radioGroup);
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

        bindingContext.bindContent(ruleIdentifierRadioButtons, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_RULE_IDENTIFIER);
        bindingContext.bindContent(localeComboField, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_SUPPORTED_LANGUAGE);
        bindingContext.bindContent(warningCheckbox, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_ENABLE_WARNINGS_FOR_MISSING_MESSAGES);

        bindFormatSettings();
    }

    private void bindFormatSettings() {
        bindingContext.bindVisible(formatSettingsSpacer, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_ENABLED, true);
        bindingContext.bindVisible(formatSettingsGroup, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_ENABLED, true);

        bindingContext.bindEnabled(formatDelimiter.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_ENABLED);
        bindingContext
                .bindContent(formatDelimiter, getMessagesImportPMO(), MessagesImportPMO.PROPERTY_COLUMN_DELIMITER);
        bindingContext.bindEnabled(identifierColumnIndex.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_ENABLED);
        bindingContext.bindContent(identifierColumnIndex, getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IDENTIFIER_COLUMN_INDEX);
        bindingContext.bindEnabled(textColumnIndex.getControl(), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FORMAT_SETTINGS_ENABLED);
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
        IIpsElement ipsElement = adaptableObject.getAdapter(IIpsElement.class);
        if (ipsElement == null) {
            IResource resource = adaptableObject.getAdapter(IResource.class);
            if (resource != null) {
                ipsElement = resource.getAdapter(IIpsElement.class);
                if (ipsElement == null) {
                    ipsElement = resource.getProject().getAdapter(IIpsElement.class);
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

    private static class DelimiterInputFormat extends AbstractInputFormat<Character> {

        public DelimiterInputFormat() {
            super(IpsStringUtils.EMPTY, Locale.getDefault());
        }

        @Override
        protected Character parseInternal(String stringToBeparsed) {
            if (stringToBeparsed.length() == 1) {
                return stringToBeparsed.charAt(0);
            } else {
                return null;
            }
        }

        @Override
        protected String formatInternal(Character value) {
            return value.toString();
        }

        @Override
        protected void verifyInternal(VerifyEvent e, String resultingText) {
            e.doit = (resultingText.length() <= 1);
        }

        @Override
        protected void initFormat(Locale locale) {
            // do nothing
        }

    }

}
