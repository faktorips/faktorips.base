/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpt.ProductCmptValidations;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptType2RefControl;
import org.faktorips.devtools.core.ui.wizards.IpsObjectPage;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 *
 */
public class ProductCmptPage extends IpsObjectPage {

    private ProductCmptType2RefControl typeRefControl;
    private Text versionId;
    private Text constName;
    private Text runtimeId;
    private Button defaultRuntimeIdBtn;
    private Text fullName;
    private boolean canModifyRuntimeId;

    // product cmpt template
    private IProductCmpt sourceProductCmpt;

    public ProductCmptPage(IStructuredSelection selection) throws JavaModelException {
        super(IpsObjectType.PRODUCT_CMPT, selection, Messages.ProductCmptPage_title);
        canModifyRuntimeId = IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {

        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelTemplate);

        typeRefControl = toolkit.createProductCmptTypeRefControl(null, nameComposite, true);
        TextButtonField pcTypeField = new TextButtonField(typeRefControl);
        pcTypeField.addChangeListener(this);

        toolkit.createLabel(nameComposite, Messages.ProductCmptPage_labelConstNamePart);
        constName = toolkit.createText(nameComposite);
        String label = NLS.bind(Messages.ProductCmptPage_labelVersionId, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getVersionConceptNameSingular());
        toolkit.createLabel(nameComposite, label);
        versionId = toolkit.createText(nameComposite);

        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelRuntimeId);
        Composite runtimeIdComposite = toolkit.createComposite(nameComposite);
        runtimeIdComposite.setLayout(toolkit.createNoMarginGridLayout(2, false));
        runtimeIdComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        runtimeId = toolkit.createText(runtimeIdComposite);
        runtimeId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(runtimeId.getText())) {
                    runtimeId.setText(getDefaultRuntimeId());
                }
            }
        });
        runtimeId.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent event) {
                try {
                    validatePage();
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        });
        if (canModifyRuntimeId) {
            defaultRuntimeIdBtn = toolkit.createButton(runtimeIdComposite,
                    Messages.ProductCmptPage_buttonDefaultRuntimeId);
            defaultRuntimeIdBtn.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    runtimeId.setText(getDefaultRuntimeId());
                }
            });
        }
        toolkit.createFormLabel(nameComposite, Messages.ProductCmptPage_labelFullName);

        fullName = addNameField(toolkit, nameComposite);

        updateEnableState();

        versionId.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                IProductCmptNamingStrategy ns = getNamingStrategy();
                if (ns != null) {
                    showMessage(ns.validateVersionId(versionId.getText()));
                }
                updateFullName();
                updateRuntimeId();
            }
        });

        constName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                IProductCmptNamingStrategy ns = getNamingStrategy();
                if (ns != null) {
                    showMessage(ns.validateKindId(constName.getText()));
                }
                updateFullName();
                updateRuntimeId();
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @throws CoreException
     */
    @Override
    protected void setDefaults(IResource selectedResource) throws CoreException {
        super.setDefaults(selectedResource);
        IIpsObject obj = getSelectedIpsObject();
        IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
        if (!(obj instanceof IProductCmpt)) {
            if (namingStrategy != null && namingStrategy.supportsVersionId()) {
                versionId.setText(namingStrategy.getNextVersionId(null));
            }
            if (obj instanceof IProductCmptType) {
                // set default product cmpt type only if the selected obj is configurable by product
                // cmpt type
                IProductCmptType type = (IProductCmptType)obj;
                typeRefControl.setText(type.getQualifiedName());
            }
            if (obj instanceof IPolicyCmptType) {
                // set default product cmpt type only if the selected policy type is configurable
                IPolicyCmptType pcType = (IPolicyCmptType)obj;
                if (pcType.isConfigurableByProductCmptType()) {
                    typeRefControl.setText(pcType.getProductCmptType());
                }
            }
        } else {
            IProductCmpt productCmpt = (IProductCmpt)obj;
            if (namingStrategy != null) {
                if (namingStrategy.supportsVersionId()) {
                    versionId.setText(namingStrategy.getNextVersionId(productCmpt));
                    constName.setText(namingStrategy.getKindId(namingStrategy.getNextName(productCmpt)));
                }
            } else {
                setIpsObjectName(productCmpt.getName());
            }
            typeRefControl.setText(productCmpt.getProductCmptType());
        }

        // update defaults with defaults from the copied product cmpt, if available
        try {
            updateDefaultsFromProductCmpt();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    String getProductCmptType() {
        return typeRefControl.getText();
    }

    String getRuntimeId() {
        return runtimeId.getText();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sourceFolderChanged() {
        super.sourceFolderChanged();
        IIpsPackageFragmentRoot root = getIpsPackageFragmentRoot();
        if (root != null) {
            typeRefControl.setIpsProject(root.getIpsProject());
        } else {
            typeRefControl.setIpsProject(null);
        }
        updateEnableState();
    }

    @Override
    protected void validatePageExtension() throws CoreException {
        if (getErrorMessage() != null) {
            return;
        }
        MessageList list = new MessageList();
        ProductCmptValidations.validateProductCmptType(null, typeRefControl.getText(), list, typeRefControl
                .getIpsProject());
        if (!list.isEmpty()) {
            setErrorMessage(list.getMessageWithHighestSeverity());
            return;
        }
        String runtimeIdErrorMsg = validateRuntimeId();
        if (StringUtils.isNotEmpty(runtimeIdErrorMsg)) {
            setErrorMessage(runtimeIdErrorMsg);
            return;
        }
    }

    private String validateRuntimeId() throws CoreException {
        // check correct format of the given runtime id
        MessageList ml = getIpsProject().getProductCmptNamingStrategy().validateRuntimeId(runtimeId.getText());
        Message msg = ml.getFirstMessage(Message.ERROR);
        if (msg != null) {
            return msg.getText();
        }

        // check for duplicate runtime id
        if (null != getIpsProject().findProductCmptByRuntimeId(runtimeId.getText())) {
            return NLS.bind(Messages.ProductCmptPage_msgRuntimeIdCollision, runtimeId.getText());
        }

        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the currentyl active naming strategy.
     */
    private IProductCmptNamingStrategy getNamingStrategy() {
        IIpsProject project = getIpsProject();
        if (project != null) {
            try {
                return project.getProductCmptNamingStrategy();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void packageChanged() {
        super.packageChanged();
        updateEnableState();
    }

    private void updateEnableState() {
        IProductCmptNamingStrategy namingStrategy = getNamingStrategy();
        boolean enabled = namingStrategy != null && namingStrategy.supportsVersionId();
        this.constName.setEnabled(enabled);
        this.versionId.setEnabled(enabled);
        this.fullName.setEnabled(!enabled);

        if (enabled) {
            if (namingStrategy.validate(fullName.getText()).isEmpty()) {
                versionId.setText(namingStrategy.getVersionId(fullName.getText()));
                constName.setText(namingStrategy.getKindId(fullName.getText()));
            }
        }

        if (canModifyRuntimeId) {
            setEnableRuntimeId(true);
        } else {
            setEnableRuntimeId(false);
        }
    }

    private void setEnableRuntimeId(boolean enable) {
        runtimeId.setEnabled(enable);
        if (defaultRuntimeIdBtn != null) {
            defaultRuntimeIdBtn.setEnabled(enable);
        }
    }

    private void showMessage(MessageList list) {
        if (!list.isEmpty()) {
            setErrorMessage(list.getMessage(0));
        } else {
            setErrorMessage((String)null);
        }
    }

    private void updateFullName() {
        fullName.setText(getNamingStrategy().getProductCmptName(constName.getText(), versionId.getText()));
    }

    private String getDefaultRuntimeId() {
        String defaultRuntimeId = ""; //$NON-NLS-1$
        if (getIpsProject() == null) {
            return ""; //$NON-NLS-1$
        }
        try {
            IProductCmptNamingStrategy productCmptNamingStrategy = getIpsProject().getProductCmptNamingStrategy();
            if (productCmptNamingStrategy != null) {
                defaultRuntimeId = productCmptNamingStrategy.getUniqueRuntimeId(getIpsProject(), getIpsObjectName());
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return defaultRuntimeId;
    }

    /*
     * Updates the runtime id, if the runtime edit field is read-only
     */
    private void updateRuntimeId() {
        if (!runtimeId.isEnabled()) {
            runtimeId.setText(getDefaultRuntimeId());
        }
    }

    /**
     * Sets the product cmpt which will be used as template for the input fields.
     */
    public void setDefaultProductCmpt(IProductCmpt sourceProductCmpt) throws CoreException {
        this.sourceProductCmpt = sourceProductCmpt;
    }

    /*
     * Updates the fields with the values from the product component template if available
     */
    private void updateDefaultsFromProductCmpt() throws CoreException {
        if (sourceProductCmpt != null) {
            IProductCmptNamingStrategy ns = getNamingStrategy();
            if (ns != null) {
                constName.setText(ns.getKindId(sourceProductCmpt.getName()));
            } else {
                constName.setText(sourceProductCmpt.getName());
            }
            IPolicyCmptType pct = sourceProductCmpt.findPolicyCmptType(getIpsProject());
            if (pct != null) {
                typeRefControl.setText(pct.getProductCmptType());
                // because the new product cmpt is based on an existing product cmpt,
                // the product cmpt type couldn't be changed
                typeRefControl.setEnabled(false);
            }
            versionId.setText(sourceProductCmpt.getVersionId());
            runtimeId.setText(getDefaultRuntimeId());
            constName.setSelection(constName.getTextLimit());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finishIpsObjects(IIpsObject ipsObject, List<IIpsObject> modifiedIpsObjects) throws CoreException {
        IProductCmpt productCmpt = (IProductCmpt)ipsObject;
        productCmpt.setProductCmptType(getProductCmptType());
        GregorianCalendar date = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        if (date == null) {
            return;
        }
        productCmpt.setRuntimeId(getRuntimeId());
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        generation.setValidFrom(date);
        productCmpt.fixAllDifferencesToModel(productCmpt.getIpsProject());
    }

    /**
     * Sets the focus to the source folder control if empty if not to the name control.
     */
    @Override
    protected void setDefaultFocus() {
        super.setDefaultFocus();
        if (StringUtils.isEmpty(typeRefControl.getText())) {
            typeRefControl.setFocus();
            return;
        }
        constName.setFocus();
    }

}
