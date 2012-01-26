/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controls.ProductCmptRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * A dialog to edit a relation.
 */
public class LinkEditDialog extends IpsPartEditDialog2 {

    private IProductCmptLink link;

    private ExtensionPropertyControlFactory extFactory;

    private TextButtonField targetField;
    private CardinalityField minCardinalityField;
    private CardinalityField maxCardinalityField;
    private CardinalityField defaultCardinalityField;

    private ProductCmptRefControl targetControl;
    private IProductCmpt[] toExclude = new IProductCmpt[0];

    private LinkEditDialogPMO pmo;

    public LinkEditDialog(IProductCmptLink link, Shell parentShell) {
        super(link, parentShell, Messages.RelationEditDialog_editRelation, true);
        this.link = link;
        extFactory = new ExtensionPropertyControlFactory(link.getClass());
        pmo = new LinkEditDialogPMO();
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.RelationEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));
        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = getToolkit().createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        getToolkit().createFormLabel(workArea, Messages.RelationEditDialog_target);
        targetControl = new ProductCmptRefControl(link.getIpsProject(), workArea, getToolkit());
        try {
            IProductCmptTypeAssociation typeRelation = link.findAssociation(link.getIpsProject());
            if (typeRelation != null) {
                IProductCmptType productCmptType = typeRelation.findTargetProductCmptType(link.getIpsProject());
                if (productCmptType != null) {
                    targetControl.setProductCmptType(productCmptType, true);
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        targetControl.setProductCmptsToExclude(toExclude);

        boolean cardinalityEnabled;
        try {
            cardinalityEnabled = link.constrainsPolicyCmptTypeAssociation(link.getIpsProject());
        } catch (CoreException e) {
            IpsPlugin.log(e);
            cardinalityEnabled = false;
        }

        getToolkit().createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMin);
        Text minCardinalityText = getToolkit().createText(workArea);
        minCardinalityText.setEnabled(cardinalityEnabled);

        getToolkit().createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMax);
        Text maxCardinalityText = getToolkit().createText(workArea);
        maxCardinalityText.setEnabled(cardinalityEnabled);

        getToolkit().createFormLabel(workArea, Messages.LinkEditDialog_cardinalityDefault);
        Text defaultCardinalityText = getToolkit().createText(workArea);
        defaultCardinalityText.setEnabled(cardinalityEnabled);

        // create fields
        targetField = new TextButtonField(targetControl);
        minCardinalityField = new CardinalityField(minCardinalityText);
        maxCardinalityField = new CardinalityField(maxCardinalityText);
        defaultCardinalityField = new CardinalityField(defaultCardinalityText);

        getBindingContext().bindContent(targetField, link, IProductCmptLink.PROPERTY_TARGET);
        getBindingContext().bindContent(minCardinalityField, link, IProductCmptLink.PROPERTY_MIN_CARDINALITY);
        getBindingContext().bindContent(maxCardinalityField, link, IProductCmptLink.PROPERTY_MAX_CARDINALITY);
        getBindingContext().bindContent(defaultCardinalityField, link, IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY);

        getBindingContext().bindEnabled(targetControl, pmo, LinkEditDialogPMO.PROPERTY_TARGET_SELECTION_ENABLED);

        extFactory.createControls(workArea, getToolkit(), link);
        extFactory.bind(getBindingContext());

        return c;
    }

    public void setProductCmptsToExclude(IProductCmpt[] toExclude) {
        if (targetControl != null) {
            targetControl.setProductCmptsToExclude(toExclude);
        } else {
            this.toExclude = toExclude;
        }
    }

    public void setTargetSelectionEnabled(boolean enabled) {
        pmo.setTargetSelectionEnabled(enabled);
    }

    public static class LinkEditDialogPMO extends PresentationModelObject {

        public static final String PROPERTY_TARGET_SELECTION_ENABLED = "targetSelectionEnabled"; //$NON-NLS-1$

        private boolean targetSelectionEnabled = true;

        /**
         * @param targetSelectionEnabled The targetSelectionEnabled to set.
         */
        public void setTargetSelectionEnabled(boolean targetSelectionEnabled) {
            this.targetSelectionEnabled = targetSelectionEnabled;
        }

        /**
         * @return Returns the targetSelectionEnabled.
         */
        public boolean isTargetSelectionEnabled() {
            return targetSelectionEnabled;
        }

    }

}
