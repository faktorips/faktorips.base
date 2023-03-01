/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.GregorianCalendar;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.internal.IpsStyler;
import org.faktorips.devtools.core.ui.internal.generationdate.GenerationDate;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptVRuleReference;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.util.StringUtil;

public class ProductStructureLabelProvider extends LabelProvider implements IStyledLabelProvider {

    /** need to know for decorations */
    private boolean showAssociationNodes = false;
    private boolean showCardinalities = true;

    private GenerationDate generationDate;

    private boolean showTableStructureUsageName = false;

    @Override
    public Image getImage(Object element) {
        if (element instanceof IProductCmptReference) {
            return IpsUIPlugin.getImageHandling().getImage(((IProductCmptReference)element).getProductCmpt());
        } else if (element instanceof IProductCmptTypeAssociationReference) {
            return IpsUIPlugin.getImageHandling()
                    .getImage(((IProductCmptTypeAssociationReference)element).getAssociation());
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            return IpsUIPlugin.getImageHandling()
                    .getImage(((IProductCmptStructureTblUsageReference)element).getTableContentUsage());
        } else if (element instanceof IProductCmptVRuleReference) {
            IValidationRuleConfig config = ((IProductCmptVRuleReference)element).getValidationRuleConfig();
            return IpsUIPlugin.getImageHandling().getImage(config);
        } else if (element instanceof ViewerLabel) {
            return ((ViewerLabel)element).getImage();
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IProductCmptReference) {
            return getProductCmptLabel(((IProductCmptReference)element).getProductCmpt());
        } else if (element instanceof IProductCmptTypeAssociationReference) {
            IProductCmptTypeAssociation association = ((IProductCmptTypeAssociationReference)element).getAssociation();
            if (association.is1ToMany()) {
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedPluralLabel(association);
            } else {
                return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association);
            }
        } else if (element instanceof IProductCmptStructureTblUsageReference) {
            ITableContentUsage tcu = ((IProductCmptStructureTblUsageReference)element).getTableContentUsage();
            if (showTableStructureUsageName) {
                return IIpsModel.get().getMultiLanguageSupport().getDefaultCaption(tcu);
            } else {
                return StringUtil.unqualifiedName(tcu.getTableContentName());
            }
        } else if (element instanceof IProductCmptVRuleReference) {
            return getRuleLabel((IProductCmptVRuleReference)element);
        } else if (element instanceof ViewerLabel) {
            return ((ViewerLabel)element).getText();
        }
        return Messages.ProductStructureLabelProvider_undefined;
    }

    /**
     * The returned label is the name to be displayed for the {@link IValidationRuleConfig}
     * referenced the given {@link IProductCmptVRuleReference}. First tries to find the configured
     * {@link IValidationRule} and return its label using the MultiLanguageSupport. If that fails
     * the original name of the {@link IValidationRuleConfig} (it ipsElement name) is returned.
     * 
     * @param element the reference to return a text/label for
     * @return the label for the given IProductCmptVRuleReference
     */
    private String getRuleLabel(IProductCmptVRuleReference element) {
        IValidationRuleConfig ruleConfig = element.getValidationRuleConfig();
        return IIpsModel.get().getMultiLanguageSupport().getLocalizedCaption(ruleConfig);
    }

    public String getProductCmptLabel(IProductCmpt productCmpt) {
        return productCmpt.getName();
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

    public void setAdjustmentDate(GenerationDate generationDate) {
        this.generationDate = generationDate;
    }

    public GenerationDate getAdjustmentDate() {
        return generationDate;
    }

    @Override
    public StyledString getStyledText(Object element) {
        StyledString styledString = new StyledString(getText(element));
        if (element instanceof IProductCmptReference productCmptReference) {
            // show cardinality
            IProductCmptTypeAssociationReference parent = productCmptReference.getParent();
            if (parent != null) {
                if (showCardinalities && constrainsPolicyCmptTypeAssociation(parent.getAssociation())) {
                    IProductCmptLink link = productCmptReference.getLink();
                    styledString.append(" " + new StyledString(link.getCardinality().format(), //$NON-NLS-1$
                            IpsStyler.DEFAULT_CARDINALITY_STYLER));
                }

                // show association nodes
                if (!isShowAssociationNodes()) {
                    styledString.append(getRolenameLabel(productCmptReference), IpsStyler.ROLENAME_STYLER);
                }

                // generation labels
                styledString.append(getGenerationLabel(productCmptReference.getProductCmpt()),
                        IpsStyler.QUALIFIER_STYLER);

            } else if (element instanceof IProductCmptVRuleReference) {
                styleRule((IProductCmptVRuleReference)element, styledString);
            } else if (element instanceof IProductCmptTypeAssociationReference) {
                if (showCardinalities) {
                    stylePolicyAssociation((IProductCmptTypeAssociationReference)element, styledString);
                }
            }
        }
        return styledString;

    }

    private boolean constrainsPolicyCmptTypeAssociation(IProductCmptTypeAssociation productAssociation) {
        IIpsProject ipsProject = productAssociation.getIpsProject();
        return ipsProject != null && productAssociation.constrainsPolicyCmptTypeAssociation(ipsProject);
    }

    protected void styleRule(IProductCmptVRuleReference vRuleRef, StyledString styledString) {
        if (!vRuleRef.getValidationRuleConfig().isActive()) {
            // gray-out inactive rules
            styledString.setStyle(0, styledString.length(), IpsStyler.DEACTIVATED_STYLER);
            styledString.append(Messages.ProductStructureLabelProvider_inactiveDecoration,
                    IpsStyler.DEACTIVATED_STYLER);
        }
    }

    protected void stylePolicyAssociation(IProductCmptTypeAssociationReference productTypeAssociation,
            StyledString styledString) {
        IProductCmptTypeAssociation productAssociation = productTypeAssociation.getAssociation();
        IPolicyCmptTypeAssociation policyAssociation = productAssociation
                .findMatchingPolicyCmptTypeAssociation(productAssociation.getIpsProject());
        if (policyAssociation != null) {
            styledString.append(new StyledString(StringUtil.getRangeString(policyAssociation.getMinCardinality(),
                    policyAssociation.getMaxCardinality()), IpsStyler.MODEL_CARDINALITY_STYLER));
        }
    }

    /**
     * returns the rolename of the association this reference belongs to, if there are more than one
     * associations with the same target
     */
    private String getRolenameLabel(IProductCmptReference productCmptReference) {
        IProductCmptStructureReference parent = productCmptReference.getParent();
        // get the parent of the reference, should be a ProductCmptTypeAssociationReference
        if (parent instanceof IProductCmptTypeAssociationReference associationReference) {
            // for associations always show the rolename
            if (associationReference.getAssociation().isAssoziation()) {
                return getRolenameLabel(associationReference.getAssociation());
            }
            parent = associationReference.getParent();
            // The parent of the ProductCmptTypeAssociationReference should be a
            // ProductCmptReference
            if (parent instanceof IProductCmptReference parentCmptReference) {
                // getting all associations of the parent ProductCmptReference
                IProductCmptTypeAssociationReference[] associationReferences = parentCmptReference.getStructure()
                        .getChildProductCmptTypeAssociationReferences(parentCmptReference, true);
                for (IProductCmptTypeAssociationReference aReference : associationReferences) {
                    // if the association is another one but have the same target... show role name
                    if (aReference != associationReference && aReference.getAssociation().getTarget()
                            .equals(associationReference.getAssociation().getTarget())) {
                        return getRolenameLabel(associationReference.getAssociation());
                    }
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    private String getRolenameLabel(IAssociation association) {
        return " - " + IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel(association); //$NON-NLS-1$
    }

    private String getGenerationLabel(IProductCmpt productCmpt) {
        GregorianCalendar date = null;
        if (getAdjustmentDate() != null) {
            date = getAdjustmentDate().getValidFrom();
        }
        IIpsObjectGeneration generation = productCmpt.getGenerationEffectiveOn(date);
        if (generation == null) {
            // no generations available,
            // show additional text to inform that no generations exists
            String generationText = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                    .getGenerationConceptNameSingular();
            return " " + NLS.bind(Messages.ProductStructureExplorer_label_NoGenerationForDate, generationText); //$NON-NLS-1$
        } else {
            GenerationDate genAdjDate = new GenerationDate(generation.getValidFrom(), generation.getValidTo());
            if (!genAdjDate.equals(generationDate)) {
                return " (" + genAdjDate.getText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                return ""; //$NON-NLS-1$
            }
        }
    }

    public void setShowCardinalities(boolean showCardinalities) {
        this.showCardinalities = showCardinalities;
    }

    public void setShowAssociationNodes(boolean showAssciations) {
        showAssociationNodes = showAssciations;
    }

    public boolean isShowAssociationNodes() {
        return showAssociationNodes;
    }

    public void toggleShowCardinalities() {
        showCardinalities = !showCardinalities;
    }

}
