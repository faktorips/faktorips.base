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
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.internal.model.type.TypeHierarchy;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.util.QNameUtil;

public class NewProductCmptPMO extends PresentationModelObject {

    public static final String PROPERTY_IPS_PROJECT = "ipsProject"; //$NON-NLS-1$

    public static final String PROPERTY_PACKAGE_ROOT = "packageRoot"; //$NON-NLS-1$

    public static final String PROPERTY_IPS_PACKAGE = "ipsPackage"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_BASE_TYPE = "selectedBaseType"; //$NON-NLS-1$

    public static final String PROPERTY_CAN_EDIT_RUNTIME_ID = "canEditRuntimeId"; //$NON-NLS-1$

    public static final String PROPERTY_SELECTED_TYPE = "selectedType"; //$NON-NLS-1$

    public static final String PROPERTY_KIND_ID = "kindId"; //$NON-NLS-1$

    public static final String PROPERTY_VERSION_ID = "versionId"; //$NON-NLS-1$

    public static final String PROPERTY_RUNTIME_ID = "runtimeId"; //$NON-NLS-1$

    public static final String PROPERTY_NEED_VERSION_ID = "needVersionId"; //$NON-NLS-1$

    private IIpsProject ipsProject;

    private IIpsPackageFragmentRoot packageRoot;

    private IIpsPackageFragment ipsPackage;

    private IProductCmptType selectedBaseType;

    private IProductCmptType selectedType;

    private List<IProductCmptType> baseTypes = new ArrayList<IProductCmptType>();

    private String kindId = StringUtils.EMPTY;

    private String versionId = StringUtils.EMPTY;

    private String runtimeId = StringUtils.EMPTY;

    private final GregorianCalendar workingDate;

    private IProductCmpt contextProductCmpt = null;

    private ArrayList<IProductCmptType> subtypes;

    private final NewProdutCmptValidator validator;

    public NewProductCmptPMO(GregorianCalendar workingDate) {
        this.workingDate = workingDate;
        validator = new NewProdutCmptValidator(this);
    }

    /**
     * @param ipsProject The ipsProject to set.
     */
    public void setIpsProject(IIpsProject ipsProject) {
        IIpsProject oldProject = ipsProject;
        this.ipsProject = ipsProject;

        updateBaseTypeList();
        selectedBaseType = null;

        IProductCmptNamingStrategy namingStrategy = ipsProject.getProductCmptNamingStrategy();
        setVersionId(namingStrategy.getNextVersionId(contextProductCmpt, workingDate));

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

    public boolean isNeedVersionId() {
        if (ipsProject == null) {
            return true;
        }
        IProductCmptNamingStrategy namingStrategy = ipsProject.getProductCmptNamingStrategy();
        return namingStrategy.supportsVersionId();
    }

    /**
     * @param packageRoot The packageRoot to set.
     */
    public void setPackageRoot(IIpsPackageFragmentRoot packageRoot) {
        IIpsPackageFragmentRoot oldValue = this.packageRoot;
        this.packageRoot = packageRoot;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_PACKAGE_ROOT, oldValue, packageRoot));
    }

    /**
     * @return Returns the packageRoot.
     */
    public IIpsPackageFragmentRoot getPackageRoot() {
        return packageRoot;
    }

    /**
     * @param ipsPackage The ipsPackage to set.
     */
    public void setIpsPackage(IIpsPackageFragment ipsPackage) {
        IIpsPackageFragment oldPackage = this.ipsPackage;
        this.ipsPackage = ipsPackage;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_IPS_PACKAGE, oldPackage, ipsPackage));
    }

    /**
     * @return Returns the ipsPackage.
     */
    public IIpsPackageFragment getIpsPackage() {
        return ipsPackage;
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
        IIpsProject ipsProject = getIpsProject();
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
                        if (superTypeIpsSrcFile != null
                                && Boolean.valueOf(superTypeIpsSrcFile
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
     * @return Returns the baseTypes.
     */
    public List<IProductCmptType> getBaseTypes() {
        List<IProductCmptType> sortedBaseTypes = new ArrayList<IProductCmptType>(baseTypes);
        // TODO Sort the list by any criteria
        // Collections.sort(sortedBaseTypes, new StrutcureComparator(baseTypes, findIpsProject()));
        return sortedBaseTypes;
    }

    /**
     * @param selectedBaseType The selectedBaseType to set.
     */
    public void setSelectedBaseType(IProductCmptType selectedBaseType) {
        IProductCmptType oldSelection = this.selectedBaseType;
        this.selectedBaseType = selectedBaseType;
        updateSubtypeList();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_BASE_TYPE, oldSelection, selectedBaseType));

    }

    /**
     * @param selectedType The selectedType to set.
     */
    public void setSelectedType(IProductCmptType selectedType) {
        IProductCmptType oldSelection = this.selectedType;
        this.selectedType = selectedType;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_SELECTED_TYPE, oldSelection, selectedType));
    }

    /**
     * @return Returns the selectedType.
     */
    public IProductCmptType getSelectedType() {
        return selectedType;
    }

    /**
     * @return Returns the selectedBaseType.
     */
    public IProductCmptType getSelectedBaseType() {
        return selectedBaseType;
    }

    public boolean isCanEditRuntimeId() {
        return IpsPlugin.getDefault().getIpsPreferences().canModifyRuntimeId();
    }

    public List<IProductCmptType> getSubtypes() {
        return subtypes;
    }

    private void updateSubtypeList() {
        ArrayList<IProductCmptType> result = new ArrayList<IProductCmptType>();
        if (selectedBaseType == null) {
            subtypes = result;
        } else {
            try {
                TypeHierarchy subtypeHierarchy = TypeHierarchy.getSubtypeHierarchy(selectedBaseType, getIpsProject());
                List<IType> subtypesList = subtypeHierarchy.getAllSubtypes(selectedBaseType);
                for (IType type : subtypesList) {
                    result.add((IProductCmptType)type);
                }
                subtypes = result;
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
            if (!subtypes.isEmpty()) {
                setSelectedType(subtypes.get(0));
            } else {
                setSelectedType(null);
            }
        }
    }

    /**
     * @param kindId The kindId to set.
     */
    public void setKindId(String name) {
        String oldName = this.kindId;
        this.kindId = name;
        updateRuntimeId();
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_KIND_ID, oldName, name));
    }

    /**
     * @return Returns the kindId.
     */
    public String getKindId() {
        return kindId;
    }

    /**
     * @param versionId The versionId to set.
     */
    public void setVersionId(String versionId) {
        String oldId = this.versionId;
        this.versionId = versionId;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_VERSION_ID, oldId, versionId));
    }

    /**
     * @return Returns the versionId.
     */
    public String getVersionId() {
        return versionId;
    }

    private void updateRuntimeId() {
        try {
            setRuntimeId(getIpsProject().getProductCmptNamingStrategy().getUniqueRuntimeId(getIpsProject(),
                    getFullName()));
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        } catch (IllegalArgumentException e) {
            setRuntimeId(StringUtils.EMPTY);
        }
    }

    public String getFullName() {
        return getIpsProject().getProductCmptNamingStrategy().getProductCmptName(kindId, versionId);
    }

    public String getQualifiedName() {
        return QNameUtil.concat(getIpsPackage().getName(), getFullName());
    }

    /**
     * @param runtimeId The runtimeId to set.
     */
    public void setRuntimeId(String runtimeId) {
        String oldRuntimeId = this.runtimeId;
        this.runtimeId = runtimeId;
        notifyListeners(new PropertyChangeEvent(this, PROPERTY_RUNTIME_ID, oldRuntimeId, runtimeId));
    }

    /**
     * @return Returns the runtimeId.
     */
    public String getRuntimeId() {
        return runtimeId;
    }

    public void initDefaults(IResource resource) {
        IIpsElement ipsElement = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
        if (ipsElement instanceof IIpsPackageFragmentRoot) {
            IIpsPackageFragmentRoot ipsPackageRoot = (IIpsPackageFragmentRoot)ipsElement;
            setIpsProject(ipsPackageRoot.getIpsProject());
            setPackageRoot(ipsPackageRoot);
        }
        if (ipsElement instanceof IIpsPackageFragment) {
            IIpsPackageFragment packageFragment = (IIpsPackageFragment)ipsElement;
            setPackageRoot(packageFragment.getRoot());
            setIpsPackage(packageFragment);
        }
        if (ipsElement instanceof IIpsSrcFile) {
            IIpsSrcFile ipsSrcFile = (IIpsSrcFile)ipsElement;
            setIpsProject(ipsSrcFile.getIpsProject());
            setPackageRoot(ipsSrcFile.getIpsPackageFragment().getRoot());
            setIpsPackage(ipsSrcFile.getIpsPackageFragment());
            if (ipsSrcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
                try {
                    contextProductCmpt = (IProductCmpt)((IIpsSrcFile)ipsElement).getIpsObject();
                    IProductCmptType cmptType = contextProductCmpt.findProductCmptType(contextProductCmpt
                            .getIpsProject());
                    if (cmptType != null) {
                        initDefaultType(cmptType, contextProductCmpt.getIpsProject());
                    }
                    setKindId(contextProductCmpt.findProductCmptKind().getName());
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }
        if (getPackageRoot() == null) {
            IProject project = resource.getProject();
            IIpsProject ipsProject = IpsPlugin.getDefault().getIpsModel().getIpsProject(project);
            setIpsProject(ipsProject);
        }
    }

    private void initDefaultType(IProductCmptType cmptType, IIpsProject ipsProject) {
        SelectedBaseTypeVisitor selectedBaseTypeVisitor = new SelectedBaseTypeVisitor(getBaseTypes(), ipsProject);
        try {
            selectedBaseTypeVisitor.start(cmptType);
            setSelectedBaseType(selectedBaseTypeVisitor.selectedBaseType);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        setSelectedType(cmptType);
    }

    /**
     * @return Returns the validator.
     */
    public NewProdutCmptValidator getValidator() {
        return validator;
    }

    /**
     * Searches the type hierarchy and looks for a supertype that is part of the base type list.
     * 
     * @author dirmeier
     */
    private static class SelectedBaseTypeVisitor extends TypeHierarchyVisitor<IProductCmptType> {

        private final List<IProductCmptType> baseTypes;

        private IProductCmptType selectedBaseType = null;

        public SelectedBaseTypeVisitor(List<IProductCmptType> baseTypes, IIpsProject ipsProject) {
            super(ipsProject);
            this.baseTypes = baseTypes;
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            if (baseTypes.contains(currentType)) {
                selectedBaseType = currentType;
                return false;
            } else {
                return true;
            }
        }

    }

}
