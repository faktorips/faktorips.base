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

package org.faktorips.devtools.core.ui.search.product;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.search.ui.ISearchQuery;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.search.AbstractSearchPresentationModel;
import org.faktorips.devtools.core.ui.search.scope.IpsSearchWorkspaceScope;

public class ProductSearchPresentationModel extends AbstractSearchPresentationModel {

    private static final String SEARCH_PRODUCT_COMPONENT_TYPE_INDEX = "productCmptTypeIndex"; //$NON-NLS-1$
    private List<IProductCmptType> productCmptTypes;
    private IProductCmptType productCmptType;
    private int productCmptTypeIndex;

    private boolean validSearch = true;

    public boolean isValidSearch() {
        return validSearch;
    }

    public ProductSearchPresentationModel() {
        updateProductComponentTypes();
    }

    @Override
    public void store(IDialogSettings settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public void read(IDialogSettings settings) {
        // TODO Auto-generated method stub

    }

    @Override
    public ISearchQuery createSearchQuery() {
        return new ProductSearchQuery(this);
    }

    public List<String> getProductComponentTypeLabels() {
        List<String> productComponentTypeLabels = new ArrayList<String>();

        for (IProductCmptType productCmptType : productCmptTypes) {
            productComponentTypeLabels.add(getProductCmptTypeCompareValue(productCmptType));
        }

        return productComponentTypeLabels;
    }

    protected String getProductCmptTypeCompareValue(IProductCmptType productCmptType) {
        return productCmptType.getName();
    }

    protected void updateProductComponentTypes() {

        // TODO doch mit IpsModel???
        IpsSearchWorkspaceScope ipsSearchWorkspaceScope = new IpsSearchWorkspaceScope();
        List<IProductCmptType> productCmptTypes;
        try {
            Set<IIpsSrcFile> selectedIpsSrcFiles = ipsSearchWorkspaceScope.getSelectedIpsSrcFiles();

            productCmptTypes = new ArrayList<IProductCmptType>();

            for (IIpsSrcFile srcFile : selectedIpsSrcFiles) {
                if (IpsObjectType.PRODUCT_CMPT_TYPE.equals(srcFile.getIpsObjectType())) {
                    productCmptTypes.add((IProductCmptType)srcFile.getIpsObject());
                }
            }

            Collections.sort(productCmptTypes, new Comparator<IProductCmptType>() {
                @Override
                public int compare(IProductCmptType o1, IProductCmptType o2) {
                    return getProductCmptTypeCompareValue(o1).compareTo(getProductCmptTypeCompareValue(o2));
                }
            });

            validSearch = true;
        } catch (CoreException e) {
            validSearch = false;
            productCmptTypes = Collections.emptyList();
        }
        this.productCmptTypes = productCmptTypes;
    }

    public IProductCmptType getProductCmptType() {
        return productCmptType;
    }

    public void setProductCmptTypeIndex(int newValue) {
        int oldValue = productCmptTypeIndex;
        if (newValue == -1) {
            productCmptType = null;
            validSearch = false;
        } else {
            productCmptType = productCmptTypes.get(newValue);
            validSearch = true;
        }

        notifyListeners(new PropertyChangeEvent(this, SEARCH_PRODUCT_COMPONENT_TYPE_INDEX, oldValue, newValue));

        this.productCmptTypeIndex = newValue;
    }

    public int getProductCmptTypeIndex() {

        return productCmptTypeIndex;
    }
}
