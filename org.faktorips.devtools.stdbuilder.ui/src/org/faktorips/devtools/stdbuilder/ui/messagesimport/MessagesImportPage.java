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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
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

/**
 * Wizard page for translated messages for validation rules.
 * 
 * TODO This implementation is not completed yet. Have a look at FIPS-626
 * 
 * @author dirmeier
 */
public class MessagesImportPage extends WizardDataTransferPage implements PropertyChangeListener {

    private final IStructuredSelection selection;
    private UIToolkit uiToolkit;
    private BindingContext bindingContext;

    private final MessagesImportPMO messagesImportPMO;
    private ComboViewerField<ISupportedLanguage> localeComboField;

    protected MessagesImportPage(String name, IStructuredSelection selection) {
        super(name);
        this.selection = selection;
        setTitle(Messages.MessagesImportPage_pageTitle);
        messagesImportPMO = new MessagesImportPMO();
        getMessagesImportPMO().addPropertyChangeListener(this);
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

        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTranslations);
        FileSelectionControl fileSelectionControl = new FileSelectionControl(labelEditComposite, uiToolkit, NONE);
        fileSelectionControl.getDialog().setFilterExtensions(new String[] { "*.properties" }); //$NON-NLS-1$
        bindingContext.bindContent(new TextButtonField(fileSelectionControl), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_FILE_NAME);

        uiToolkit.createLabel(labelEditComposite, Messages.MessagesImportPage_labelTarget);
        IpsPckFragmentRootRefControl target = new IpsPckFragmentRootRefControl(labelEditComposite, true, uiToolkit);
        bindingContext.bindContent(new IpsPckFragmentRootRefField(target), getMessagesImportPMO(),
                MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT);

        createOptionsGroup(rootComposite);

        setControl(rootComposite);

        initDefaults();
    }

    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {
        Composite optionComposite = uiToolkit.createLabelEditColumnComposite(optionsGroup);

        uiToolkit.createLabel(optionComposite, Messages.MessagesImportPage_labelLocale);
        Combo localeCombo = uiToolkit.createCombo(optionComposite);
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
        if (selection != null) {
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
    }

    @Override
    protected boolean allowNewContainerName() {
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (MessagesImportPMO.PROPERTY_IPS_PACKAGE_FRAGMENT_ROOT.equals(evt.getPropertyName())) {
            updateAvailableLocales();
        }
        if (MessagesImportPMO.PROPERTY_AVAILABLE_LOCALES.equals(evt.getPropertyName())) {
            setAvailableLocales();
        }
    }

    void updateAvailableLocales() {
        if (getMessagesImportPMO().getIpsPackageFragmentRoot() == null) {
            getMessagesImportPMO().setAvailableLocales(new HashSet<ISupportedLanguage>());
        } else {
            IIpsProject ipsProject = getMessagesImportPMO().getIpsPackageFragmentRoot().getIpsProject();
            Set<ISupportedLanguage> supportedLanguages = ipsProject.getProperties().getSupportedLanguages();
            getMessagesImportPMO().setAvailableLocales(supportedLanguages);
        }
    }

    void setAvailableLocales() {
        Set<ISupportedLanguage> supportedLanguages = getMessagesImportPMO().getAvailableLocales();
        ISupportedLanguage[] languages = new ISupportedLanguage[supportedLanguages.size()];
        int i = 0;
        for (ISupportedLanguage supportedLanguage : supportedLanguages) {
            languages[i] = supportedLanguage;
            i++;
        }
        localeComboField.setInput(languages);
    }

    /**
     * @return Returns the messagesImportPMO.
     */
    public MessagesImportPMO getMessagesImportPMO() {
        return messagesImportPMO;
    }
}
