/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modeloverview;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.type.Association;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

public class IpsModelOverviewLabelProvider extends LabelProvider implements IStyledLabelProvider {

    private boolean showCardinalities = true;
    private boolean showRolenames = true;
    private LocalResourceManager resourceManager;

    public IpsModelOverviewLabelProvider() {
        super();
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof ComponentNode) {
            ComponentNode node = (ComponentNode)element;
            String imageName;
            if (node.getValue() instanceof PolicyCmptType) {
                imageName = "PolicyCmptType.gif"; //$NON-NLS-1$
            } else {
                imageName = "ProductCmptType.gif"; //$NON-NLS-1$
            }

            if (node.isRepetition()) {
                return (Image)resourceManager.get(IpsUIPlugin.getImageHandling().getSharedOverlayImage(imageName,
                        "ovr16/loop_ovr.gif", IDecoration.BOTTOM_LEFT)); //$NON-NLS-1$
            } else {
                IAdaptable adaptable = node.getValue();
                Image result = IpsUIPlugin.getImageHandling().getImage(adaptable);
                if (result != null) {
                    return result;
                }
            }
        } else if (element instanceof CompositeNode) {
            return IpsUIPlugin.getImageHandling().getSharedImage("AssociationType-Aggregation.gif", true); //$NON-NLS-1$
        } else if (element instanceof SubtypeNode) {
            return IpsUIPlugin.getImageHandling().getSharedImage("over_co.gif", true); //$NON-NLS-1$
        }
        return super.getImage(element);
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ComponentNode) {
            ComponentNode node = (ComponentNode)element;
            return node.getValue().getName();
        } else if (element instanceof CompositeNode) {
            return ""; //$NON-NLS-1$
        } else if (element instanceof SubtypeNode) {
            return ""; //$NON-NLS-1$
        }
        return super.getText(element);
    }

    @Override
    public StyledString getStyledText(Object element) {
        String label = getText(element);
        StyledString styledLabel = new StyledString(label);

        if (element instanceof AssociationComponentNode) {
            IAssociation node = ((AssociationComponentNode)element).getAssociation();

            if (showRolenames) {
                styledLabel
                        .append(new StyledString(" - " + node.getTargetRoleSingular(), StyledString.QUALIFIER_STYLER)); //$NON-NLS-1$
            }

            if (showCardinalities) {
                styledLabel
                        .append(new StyledString(
                                " ["    + getCardinalityText(node.getMinCardinality()) + ".." + getCardinalityText(node.getMaxCardinality()) + "]", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                StyledString.COUNTER_STYLER));
            }

        }
        return styledLabel;
    }

    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }

    private String getCardinalityText(int cardinality) {
        if (cardinality == Association.CARDINALITY_MANY) {
            return "*"; //$NON-NLS-1$
        }
        return "" + cardinality; //$NON-NLS-1$
    }

    public void setShowCardinalities(boolean showCardinalities) {
        this.showCardinalities = showCardinalities;
    }

    public void toggleShowCardinalities() {
        this.showCardinalities = !this.showCardinalities;
    }

    public void setShowRolenames(boolean showRolenames) {
        this.showRolenames = showRolenames;
    }

    public void toggleShowRolenames() {
        this.showRolenames = !this.showRolenames;
    }
}
