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

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.views.productstructureexplorer.ProductStructureExplorer.GenerationRootNode;
import org.faktorips.util.StringUtil;

public class ProductStructureLabelProvider extends LabelProvider {

    private boolean showTableStructureUsageName = false;

    private GenerationRootNode generationRootNode;

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage(Object element) {
        if (element instanceof IProductCmptReference) {
            return ((IProductCmptReference)element).getProductCmpt().getImage();
        } else if (element instanceof IProductCmptTypeRelationReference) {
            return ((IProductCmptTypeRelationReference)element).getRelation().getImage();
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            return ((IProductCmptStructureTblUsageReference)element).getTableContentUsage().getImage();
        } else if (element instanceof ViewerLabel) {
            return ((ViewerLabel)element).getImage();
        }
        return IpsPlugin.getDefault().getImage(Messages.ProductStructureLabelProvider_undefined);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(Object element) {
        if (element instanceof IProductCmptReference) {
            return generationRootNode
                    .getProductCmptNoGenerationLabel(((IProductCmptReference)element).getProductCmpt());
        } else if (element instanceof IProductCmptTypeRelationReference) {
            IProductCmptTypeAssociation association = ((IProductCmptTypeRelationReference)element).getRelation();
            // if the cardinality of the association is "toMany" then show the name (target role
            // name) in plural
            // otherwise show the default name, which normally is the singular target role name
            if (association.is1ToMany()) {
                return association.getTargetRolePlural();
            } else {
                return association.getName();
            }
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            ITableContentUsage tcu = ((IProductCmptStructureTblUsageReference)element).getTableContentUsage();
            String tableUsageLabelText = showTableStructureUsageName ? tcu.getStructureUsage() + ": " : ""; //$NON-NLS-1$ //$NON-NLS-2$
            return StringUtils.capitalize(tableUsageLabelText) + StringUtil.unqualifiedName(tcu.getTableContentName());
        } else if (element instanceof ViewerLabel) {
            return ((ViewerLabel)element).getText();
        }
        return Messages.ProductStructureLabelProvider_undefined;
    }

    public void setGenerationRootNode(GenerationRootNode generationRootNode) {
        this.generationRootNode = generationRootNode;
    }

    /**
     * Definines if the table content usage role name will be displayed beside the referenced table
     * content (<code>true</code>), or if the corresponding table structure usage name will be
     * hidden (<code>false</code>).
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
