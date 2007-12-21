/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer.GenerationRootNode;
import org.faktorips.util.StringUtil;

public class ProductStructureLabelProvider implements ILabelProvider {

	private List listeners = new ArrayList();
	
    private boolean showTableStructureUsageName = false;
    
    private GenerationRootNode generationRootNode;
    
	/**
	 * {@inheritDoc}
	 */
	public void addListener(ILabelProviderListener listener) {
		listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		this.listeners = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLabelProperty(Object element, String property) {
		return true; 
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getImage(Object element) {
		if (element instanceof IProductCmptReference) {
			return ((IProductCmptReference)element).getProductCmpt().getImage();
		}
		else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getImage();
        }
        else if (element instanceof IProductCmptStructureTblUsageReference) {
            return ((IProductCmptStructureTblUsageReference)element).getTableContentUsage().getImage();
        }   
        else if (element instanceof ViewerLabel){
            return ((ViewerLabel)element).getImage();
        }
	    return IpsPlugin.getDefault().getImage(Messages.ProductStructureLabelProvider_undefined);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getText(Object element) {
		if (element instanceof IProductCmptReference) {
            return generationRootNode.getProductCmptNoGenerationLabel(((IProductCmptReference)element).getProductCmpt());
		}
		else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getName();
        }
        else if (element instanceof IProductCmptStructureTblUsageReference) {
            ITableContentUsage tcu = ((IProductCmptStructureTblUsageReference)element).getTableContentUsage();
            String tableUsageLabelText = showTableStructureUsageName?tcu.getStructureUsage() + ": ":""; //$NON-NLS-1$ //$NON-NLS-2$
            return  StringUtils.capitalize(tableUsageLabelText) + StringUtil.unqualifiedName(tcu.getTableContentName()); //$NON-NLS-1$
        }
        else if (element instanceof ViewerLabel){
            return ((ViewerLabel)element).getText();
        }
		return Messages.ProductStructureLabelProvider_undefined;
	}

    public void setGenerationRootNode(GenerationRootNode generationRootNode) {
        this.generationRootNode = generationRootNode;
    }

    /**
     * Definines if the table content usage role name will be displayed beside the referenced table content (<code>true</code>),
     * or if the corresponding table structure usage name will be hidden (<code>false</code>).
     */
    public void setShowTableStructureUsageName(boolean showTableStructureUsageName) {
        this.showTableStructureUsageName = showTableStructureUsageName;
    }

    /**
     * Returns <code>true</code> if the table structure usage role name will be displayed or not.
     */
    public boolean isShowTableStructureUsageName() {
        return showTableStructureUsageName;
    }    
}
