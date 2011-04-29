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

    public LinkEditDialog(IProductCmptLink link, Shell parentShell) {
        super(link, parentShell, Messages.RelationEditDialog_editRelation, true);
        this.link = link;
        extFactory = new ExtensionPropertyControlFactory(link.getClass());
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

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_target);
        targetControl = new ProductCmptRefControl(link.getIpsProject(), workArea, uiToolkit);
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

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMin);
        Text minCardinalityText = uiToolkit.createText(workArea);
        minCardinalityText.setEnabled(cardinalityEnabled);

        uiToolkit.createFormLabel(workArea, Messages.RelationEditDialog_cardinalityMax);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        maxCardinalityText.setEnabled(cardinalityEnabled);

        uiToolkit.createFormLabel(workArea, Messages.LinkEditDialog_cardinalityDefault);
        Text defaultCardinalityText = uiToolkit.createText(workArea);
        defaultCardinalityText.setEnabled(cardinalityEnabled);

        // create fields
        targetField = new TextButtonField(targetControl);
        minCardinalityField = new CardinalityField(minCardinalityText);
        maxCardinalityField = new CardinalityField(maxCardinalityText);
        defaultCardinalityField = new CardinalityField(defaultCardinalityText);

        bindingContext.bindContent(targetField, link, IProductCmptLink.PROPERTY_TARGET);
        bindingContext.bindContent(minCardinalityField, link, IProductCmptLink.PROPERTY_MIN_CARDINALITY);
        bindingContext.bindContent(maxCardinalityField, link, IProductCmptLink.PROPERTY_MAX_CARDINALITY);
        bindingContext.bindContent(defaultCardinalityField, link, IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY);

        extFactory.createControls(workArea, uiToolkit, link);
        extFactory.bind(bindingContext);

        return c;
    }

    public void setProductCmptsToExclude(IProductCmpt[] toExclude) {
        if (targetControl != null) {
            targetControl.setProductCmptsToExclude(toExclude);
        } else {
            this.toExclude = toExclude;
        }
    }

}
