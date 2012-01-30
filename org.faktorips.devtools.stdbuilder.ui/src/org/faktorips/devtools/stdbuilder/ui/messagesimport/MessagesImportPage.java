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

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
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

        Composite rootComposite = uiToolkit.createGridComposite(parent, 1, false, false);

        Composite labelEditComposite = uiToolkit.createLabelEditColumnComposite(rootComposite);

        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTarget);
        target = new IpsPckFragmentRootRefControl(labelEditComposite, true, uiToolkit);

        bindingContext.bindContent(new IpsPckFragmentRootRefField(target), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT);

        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTranslations);
        fileSelectionControl = new FileSelectionControl(labelEditComposite, uiToolkit, NONE);
        fileSelectionControl.getDialog().setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$
        bindingContext.bindContent(new TextButtonField(fileSelectionControl), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FILE_NAME);
        createLocaleControl(rootComposite);

        setControl(rootComposite);
        initDefaults();
        validatePage();

    }

    protected void createLocaleControl(Composite rootComposite) {

        Composite optionComposite = uiToolkit.createLabelEditColumnComposite(rootComposite);

        uiToolkit.createLabel(optionComposite, Messages.MessagesImportPage_labelLocale);
        localeCombo = uiToolkit.createCombo(optionComposite);
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
