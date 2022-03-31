/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;

/**
 * Sorter for the ModelExplorer-TreeViewer. Sorts folders displayed in the ModelExplorer by the
 * sorting number set in the folder properties. PackageFragments are placed above Files,
 * PolicyCmptTypes are placed above table structures.
 * 
 * @author Stefan Widmaier
 */
public class ModelExplorerSorter extends ViewerComparator {

    private static final Map<IpsObjectType, ModelExplorerCategory> TYPE_TO_CATEGORY = new HashMap<>();

    static {
        TYPE_TO_CATEGORY.put(IpsObjectType.POLICY_CMPT_TYPE, ModelExplorerCategory.CAT_POLICY_CMPT_TYPE);
        TYPE_TO_CATEGORY.put(IpsObjectType.PRODUCT_CMPT_TYPE, ModelExplorerCategory.CAT_PRODUCT_CMPT_TYPE);
        TYPE_TO_CATEGORY.put(IpsObjectType.ENUM_TYPE, ModelExplorerCategory.CAT_ENUM_TYPE);
        TYPE_TO_CATEGORY.put(IpsObjectType.TABLE_STRUCTURE, ModelExplorerCategory.CAT_TABLE_STRUCTURE);
        TYPE_TO_CATEGORY.put(IpsObjectType.TEST_CASE_TYPE, ModelExplorerCategory.CAT_TEST_CASE_TYPE);
        TYPE_TO_CATEGORY.put(IpsObjectType.PRODUCT_TEMPLATE, ModelExplorerCategory.CAT_PRODUCT_CMPT);
        TYPE_TO_CATEGORY.put(IpsObjectType.PRODUCT_CMPT, ModelExplorerCategory.CAT_PRODUCT_CMPT);
        TYPE_TO_CATEGORY.put(IpsObjectType.ENUM_CONTENT, ModelExplorerCategory.CAT_ENUM_CONTENT);
        TYPE_TO_CATEGORY.put(IpsObjectType.TABLE_CONTENTS, ModelExplorerCategory.CAT_TABLE_CONTENTS);
        TYPE_TO_CATEGORY.put(IpsObjectType.TEST_CASE, ModelExplorerCategory.CAT_TEST_CASE);
    }

    private boolean supportCategories;

    public ModelExplorerSorter(boolean supportCategories) {
        this.supportCategories = supportCategories;
    }

    /**
     * @param supportCategories The supportCategories to set.
     */
    public void setSupportCategories(boolean supportCategories) {
        this.supportCategories = supportCategories;
    }

    /**
     * @return Returns the supportCategories.
     */
    public boolean isSupportCategories() {
        return supportCategories;
    }

    @Override
    public int category(Object element) {
        if (element instanceof IIpsObjectPathContainer) {
            // Containers are the number one
            return ModelExplorerCategory.CAT_IPS_CONTAINERS.getOrder();
        }
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = (IIpsElement)element;
            if (ipsElement instanceof IIpsObject) {
                return category(((IIpsObject)ipsElement).getIpsObjectType());
            } else if (ipsElement instanceof IIpsSrcFile) {
                return category(((IIpsSrcFile)ipsElement).getIpsObjectType());
            } else {
                // Projects and Fragments above other values (IpsObjectParts doesn't matter)
                return ModelExplorerCategory.CAT_PROJECT.getOrder();
            }
        } else {
            if (element instanceof IProject) {
                // IProjects in same category as IpsProject
                return ModelExplorerCategory.CAT_PROJECT.getOrder();
            } else if (element instanceof IFolder) {
                // other Folders after IpsFragments(Root)
                return ModelExplorerCategory.CAT_FOLDER.getOrder();
            } else {
                // any other item above all
                return Integer.MAX_VALUE;
            }
        }
    }

    private int category(IpsObjectType ipsObjectType) {
        if (!isSupportCategories()) {
            return ModelExplorerCategory.CAT_OTHER_IPS_OBJECTS.getOrder();
        }
        return getCategoryForIpsObjectType(ipsObjectType);
    }

    private int getCategoryForIpsObjectType(IpsObjectType ipsObjectType) {
        ModelExplorerCategory category = TYPE_TO_CATEGORY.get(ipsObjectType);
        if (category != null) {
            return category.getOrder();
        } else {
            return ModelExplorerCategory.CAT_OTHER_IPS_OBJECTS.getOrder();
        }
    }

    // CSOFF: CyclomaticComplexity
    @Override
    public int compare(Viewer viewer, Object o1, Object o2) {
        int cat1 = category(o1);
        int cat2 = category(o2);
        if (cat1 != cat2) {
            return cat1 - cat2;
        }

        if (isAnyNull(o1, o2)) {
            return super.compare(viewer, o1, o2);
        }
        if (isBothPackageFragmentRoot(o1, o2)) {
            return comparePackageFragmentRoot(o1, o2);
        }
        if (isBothPackageFragment(o1, o2)) {
            return comparePackageFragment(o1, o2);
        }

        if (isBothProdutCmptGeneration(o1, o2)) {
            return compareProductGenerations(o1, o2);
        }
        int typeMemberOrder1 = getTypeMemberOrder(o1);
        int typeMemberOrder2 = getTypeMemberOrder(o2);
        if (typeMemberOrder1 > -1) {
            if (typeMemberOrder1 == typeMemberOrder2) {
                return super.compare(viewer, o1, o2);
            } else if (typeMemberOrder2 > -1) {
                return typeMemberOrder1 - typeMemberOrder2;
            }
        }

        if (isBothProject(o1, o2)) {
            return getProjectName(o1).compareToIgnoreCase(getProjectName(o2));
        }

        if (isBothIpsSrcFile(o1, o2)) {
            return ((IIpsSrcFile)o1).getIpsPackageFragment().getChildOrderComparator().compare((IIpsSrcFile)o1,
                    (IIpsSrcFile)o2);
        }

        return super.compare(viewer, o1, o2);
    }
    // CSON: CyclomaticComplexity

    private boolean isBothIpsSrcFile(Object o1, Object o2) {
        return o1 instanceof IIpsSrcFile && o2 instanceof IIpsSrcFile;
    }

    private boolean isBothProject(Object o1, Object o2) {
        return (o1 instanceof IIpsProject || o1 instanceof IProject)
                && (o2 instanceof IIpsProject || o2 instanceof IProject);
    }

    private boolean isBothPackageFragmentRoot(Object o1, Object o2) {
        return o1 instanceof IIpsPackageFragmentRoot && o2 instanceof IIpsPackageFragmentRoot;
    }

    private boolean isBothPackageFragment(Object o1, Object o2) {
        return o1 instanceof IIpsPackageFragment && o2 instanceof IIpsPackageFragment;
    }

    private boolean isBothProdutCmptGeneration(Object o1, Object o2) {
        return o1 instanceof IProductCmptGeneration && o2 instanceof IProductCmptGeneration;
    }

    private boolean isAnyNull(Object o1, Object o2) {
        return o1 == null || o2 == null;
    }

    private int compareProductGenerations(Object o1, Object o2) {
        IProductCmptGeneration g1 = (IProductCmptGeneration)o1;
        IProductCmptGeneration g2 = (IProductCmptGeneration)o2;
        return g1.getValidFrom().after(g2.getValidFrom()) ? -1 : g1.getValidFrom().before(g2.getValidFrom()) ? 1 : 0;
    }

    private int comparePackageFragmentRoot(Object o1, Object o2) {
        IIpsPackageFragmentRoot root1 = ((IIpsPackageFragmentRoot)o1);
        IIpsPackageFragmentRoot root2 = ((IIpsPackageFragmentRoot)o2);
        return root1.getIpsObjectPathEntry().getIndex() - root2.getIpsObjectPathEntry().getIndex();
    }

    private int comparePackageFragment(Object o1, Object o2) {
        IIpsPackageFragment fragment = ((IIpsPackageFragment)o1);
        IIpsPackageFragment fragment2 = ((IIpsPackageFragment)o2);
        // place defaultpackage at top
        if (fragment.isDefaultPackage()) {
            return -1;
        }
        if (fragment2.isDefaultPackage()) {
            return 1;
        }
        // sort IpsPackages by SortDefinition
        return fragment.getParentIpsPackageFragment().getChildOrderComparator().compare(fragment, fragment2);
    }

    private String getProjectName(Object o) {
        if (o instanceof IProject) {
            return ((IProject)o).getName();
        } else if (o instanceof IIpsProject) {
            return ((IIpsProject)o).getName();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private int getTypeMemberOrder(Object member) {
        if (member instanceof IAttribute) {
            return 1;
        }
        if (member instanceof IAssociation) {
            return 2;
        }
        if (member instanceof IMethod) {
            return 3;
        }
        if (member instanceof ITableStructure) {
            return 4;
        }
        if (member instanceof IValidationRule) {
            return 5;
        }
        return -1;

    }
}
