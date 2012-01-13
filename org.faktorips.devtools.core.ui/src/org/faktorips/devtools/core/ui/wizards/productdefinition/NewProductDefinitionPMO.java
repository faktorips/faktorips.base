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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.beans.PropertyChangeEvent;
import java.util.GregorianCalendar;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public abstract class NewProductDefinitionPMO extends PresentationModelObject {

    public static final String PROPERTY_IPS_PROJECT = "ipsProject"; //$NON-NLS-1$

    public static final String PROPERTY_PACKAGE_ROOT = "packageRoot"; //$NON-NLS-1$

    public static final String PROPERTY_IPS_PACKAGE = "ipsPackage"; //$NON-NLS-1$

    public static final String PROPERTY_OPEN_EDITOR = "openEditor"; //$NON-NLS-1$

    public static final String PROPERTY_CAN_EDIT_RUNTIME_ID = "canEditRuntimeId"; //$NON-NLS-1$

    public static final String PROPERTY_EFFECTIVE_DATE = "effectiveDate"; //$NON-NLS-1$

    private IIpsProject ipsProject;

    private IIpsPackageFragmentRoot packageRoot;

    private IIpsPackageFragment ipsPackage;

    private boolean openEditor;

    private GregorianCalendar effectiveDate;

    public NewProductDefinitionPMO() {
        super();
    }

    /**
     * @param ipsProject The ipsProject to set.
     */
    public void setIpsProject(IIpsProject ipsProject) {
        IIpsProject oldProject = ipsProject;
        this.ipsProject = ipsProject;
        try {
            setPackageRoot(ipsProject.getSourceIpsPackageFragmentRoots()[0]);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PROJECT, oldProject, ipsProject));
    }

    public IIpsProject getIpsProject() {
        return ipsProject;
    }

    /**
     * @param packageRoot The packageRoot to set.
     */
    public void setPackageRoot(IIpsPackageFragmentRoot packageRoot) {
        IIpsPackageFragmentRoot oldValue = this.packageRoot;
        this.packageRoot = packageRoot;
        if (getIpsPackage() == null) {
            setIpsPackage(packageRoot.getDefaultIpsPackageFragment());
        }
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_PACKAGE_ROOT, oldValue, packageRoot));
    }

    /**
     * @return Returns the packageRoot.
     */
    public IIpsPackageFragmentRoot getPackageRoot() {
        return packageRoot;
    }

    /**
     * @return Returns the ipsPackage.
     */
    public IIpsPackageFragment getIpsPackage() {
        return ipsPackage;
    }

    /**
     * @param openEditor The openEditor to set.
     */
    public void setOpenEditor(boolean openEditor) {
        boolean oldValue = this.openEditor;
        this.openEditor = openEditor;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_OPEN_EDITOR, oldValue, openEditor));
    }

    /**
     * @return Returns the openEditor.
     */
    public boolean isOpenEditor() {
        return openEditor;
    }

    /**
     * @param ipsPackage The ipsPackage to set.
     */
    public void setIpsPackage(IIpsPackageFragment ipsPackage) {
        IIpsPackageFragment oldPackage = this.ipsPackage;
        this.ipsPackage = ipsPackage;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PACKAGE, oldPackage, ipsPackage));
    }

    protected abstract NewProductDefinitionValidator getValidator();

    public boolean isCanEditRuntimeId() {
        return IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId();
    }

    /**
     * Returns the name of the new object. Name does not need to be a property in this pmo. It is
     * used to create the new {@link IIpsSrcFile}.
     * <p>
     * The name ist not the qualified name. For example in product component the name is the
     * composite of the kind id and the version id of the product component.
     * 
     * @return The name of the new object
     */
    public abstract String getName();

    /**
     * Setting the effective date for the new product component. This may be different from current
     * workbench working date for example if the wizard is started in context of the product
     * structure view using another date.
     * 
     * @param effectiveDate The working date for the new product component
     */
    public void setEffectiveDate(GregorianCalendar effectiveDate) {
        GregorianCalendar oldDate = this.effectiveDate;
        this.effectiveDate = effectiveDate;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_EFFECTIVE_DATE, oldDate, effectiveDate));
    }

    /**
     * @return Returns the effectiveDate.
     */
    public GregorianCalendar getEffectiveDate() {
        if (effectiveDate == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
        }
        return effectiveDate;
    }

}