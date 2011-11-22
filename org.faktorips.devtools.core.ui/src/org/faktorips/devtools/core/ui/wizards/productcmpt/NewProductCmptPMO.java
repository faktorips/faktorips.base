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

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;

public class NewProductCmptPMO extends PresentationModelObject {

    public static final String PROPERTY_IPSPROJECT = "ipsProject"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_BASE_TYPE = "selectedBaseType";

    private String ipsProject;

    private IProductCmptType selectedBaseType;

    private List<IProductCmptType> baseTypes;

    /**
     * @param ipsProject The ipsProject to set.
     */
    public void setIpsProject(String ipsProject) {
        String oldValue = this.ipsProject;
        this.ipsProject = ipsProject;
        updateBaseTypeList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPSPROJECT, oldValue, ipsProject));
    }

    private IIpsProject findIpsProject() {
        return IpsPlugin.getDefault().getIpsModel().getIpsProject(ipsProject);
    }

    /**
     * Searches all {@link IProductCmptType} in selected project and adding fills the list of base
     * types.
     * <p>
     * Every type that has either no super type or which super type is an layer supertype is added
     * as an base type.
     */
    private void updateBaseTypeList() {
        if (ipsProject == null) {
            return;
        }
        baseTypes = new ArrayList<IProductCmptType>();
        IIpsProject ipsProject = findIpsProject();
        if (ipsProject == null) {
            return;
        }
        try {
            IIpsSrcFile[] findIpsSrcFiles = ipsProject.findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
            for (IIpsSrcFile ipsSrcFile : findIpsSrcFiles) {
                boolean layerSupertype = Boolean.valueOf(ipsSrcFile
                        .getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE));
                if (!layerSupertype) {
                    String superType = ipsSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
                    if (StringUtils.isNotEmpty(superType)) {
                        IIpsSrcFile superTypeIpsSrcFile = ipsProject.findIpsSrcFile(new QualifiedNameType(superType,
                                IpsObjectType.PRODUCT_CMPT_TYPE));
                        if (Boolean.valueOf(superTypeIpsSrcFile
                                .getPropertyValue(IProductCmptType.PROPERTY_LAYER_SUPERTYPE))) {
                            baseTypes.add((IProductCmptType)ipsSrcFile.getIpsObject());
                        }
                    } else {
                        baseTypes.add((IProductCmptType)ipsSrcFile.getIpsObject());
                    }
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * @return Returns the ipsProject.
     */
    public String getIpsProject() {
        return ipsProject;
    }

    /**
     * @return Returns the baseTypes.
     */
    public List<IProductCmptType> getBaseTypes() {
        return baseTypes;
    }

    /**
     * @param selectedBaseType The selectedBaseType to set.
     */
    public void setSelectedBaseType(IProductCmptType selectedBaseType) {
        IProductCmptType oldSelection = this.selectedBaseType;
        this.selectedBaseType = selectedBaseType;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_BASE_TYPE, oldSelection, selectedBaseType));

    }

    /**
     * @return Returns the selectedBaseType.
     */
    public IProductCmptType getSelectedBaseType() {
        return selectedBaseType;
    }
}
