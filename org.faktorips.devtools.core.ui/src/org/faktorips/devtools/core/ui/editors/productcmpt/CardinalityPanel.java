/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.MessageCueController;
import org.faktorips.util.message.MessageList;

/**
 * Panel to display cardinality. Note that this is <strong>NOT</strong> a control.
 * <p>
 * To edit a {@link IProductCmptLink} call {@link #setProductCmptLinkToEdit(IProductCmptLink)} and
 * this panel will be updated with the links values. If <code>null</code> is passed to the method,
 * this panel resets and disables itself.
 * 
 * 
 * @author Thorsten Guenther
 * @author Stefan Widmaier
 */
public class CardinalityPanel implements IDataChangeableReadWriteAccess {

    private UIToolkit uiToolkit;

    private Text minCard;
    private Text maxCard;
    private Text defaultCard;
    private Label minCardLabel;
    private Label maxCardLabel;
    private Label defaultCardLabel;
    private Button optional;
    private Label optionalLabel;
    private Button mandatory;
    private Label mandatorylLabel;
    private Button other;
    private Label otherLabel;
    private Composite root;

    private IpsObjectUIController uiController;
    private CardinalityField minCardField;
    private CardinalityField maxCardField;
    private CardinalityField defaultCardField;

    private IProductCmptLink currentLink;

    private boolean dataChangeable;

    /**
     * Creates a new Cardinality panel
     */
    public CardinalityPanel(Composite parent, UIToolkit toolkit) {
        uiToolkit = toolkit;

        // first create a composite to fill the complete space
        root = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 1;
        root.setLayout(layout);

        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        root.setLayoutData(layoutData);

        // then, create a composite to be put at to and this
        // composite becomes the border!
        Composite kardinalityPane = toolkit.createLabelEditColumnComposite(root);
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        layoutData.verticalAlignment = SWT.TOP;
        kardinalityPane.setLayoutData(layoutData);

        // create header
        layoutData = ((GridData)toolkit.createLabel(kardinalityPane, Messages.RelationsSection_cardinality)
                .getLayoutData());
        layoutData.horizontalSpan = 2;
        layoutData.horizontalAlignment = SWT.CENTER;

        // create radio buttons
        KardinalitySelectionListener listener = new KardinalitySelectionListener();
        optional = toolkit.createRadioButton(kardinalityPane, ""); //$NON-NLS-1$
        optional.addSelectionListener(listener);
        optionalLabel = toolkit.createLabel(kardinalityPane, Messages.CardinalityPanel_labelOptional);
        mandatory = toolkit.createRadioButton(kardinalityPane, ""); //$NON-NLS-1$
        mandatory.addSelectionListener(listener);
        mandatorylLabel = toolkit.createLabel(kardinalityPane, Messages.CardinalityPanel_labelMandatory);
        other = toolkit.createRadioButton(kardinalityPane, ""); //$NON-NLS-1$
        other.addSelectionListener(listener);
        otherLabel = toolkit.createLabel(kardinalityPane, Messages.CardinalityPanel_labelOther);

        // create other input controls
        toolkit.createLabel(kardinalityPane, ""); //$NON-NLS-1$
        Composite specialPane = toolkit.createLabelEditColumnComposite(kardinalityPane);
        specialPane.setLayout(new GridLayout(2, false));
        minCardLabel = toolkit.createFormLabel(specialPane, Messages.PolicyAttributesSection_minimum);
        minCard = toolkit.createText(specialPane);
        minCard.setLayoutData(new GridData(30, SWT.DEFAULT));
        maxCardLabel = toolkit.createFormLabel(specialPane, Messages.PolicyAttributesSection_maximum);
        maxCard = toolkit.createText(specialPane);
        maxCard.setLayoutData(new GridData(30, SWT.DEFAULT));
        defaultCardLabel = toolkit.createFormLabel(specialPane, Messages.CardinalityPanel_LabelDefaultCardinality);
        defaultCard = toolkit.createText(specialPane);
        defaultCard.setLayoutData(new GridData(30, SWT.DEFAULT));
        toolkit.createVerticalSpacer(specialPane, 3).setBackground(kardinalityPane.getBackground());

        // ModifyListener cardinalityChangedListener = new ModifyListener() {
        // @Override
        // public void modifyText(ModifyEvent e) {
        // cardinalityChanged();
        // }
        // };
        // minKard.addModifyListener(cardinalityChangedListener);
        // maxKard.addModifyListener(cardinalityChangedListener);
        // defaultKard.addModifyListener(cardinalityChangedListener);

        kardinalityPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(root);
    }

    /**
     * Configures this {@link CardinalityPanel} to change the cardinality values of the given link
     * when the user modifies the text controls. If the given {@link IProductCmptLink} is
     * <code>null</code> this panel will be deactivated.
     * 
     * @param link the {@link IProductCmptLink} currently edited by this panel or <code>null</code>
     *            if none.
     */
    public void setProductCmptLinkToEdit(IProductCmptLink link) {
        currentLink = link;
        if (currentLink == null) {
            deactivateCardinalityPanel();
        } else {

            boolean cardinalityPanelEnabled;
            try {
                cardinalityPanelEnabled = currentLink.constrainsPolicyCmptTypeAssociation(currentLink.getIpsProject());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                cardinalityPanelEnabled = false;
            }

            if (!cardinalityPanelEnabled) {
                deactivateCardinalityPanel();
                return;
            } else {
                if (uiController != null) {
                    removeFields();
                }
                if (uiController == null || !uiController.getIpsObjectPartContainer().equals(currentLink)) {
                    uiController = new IpsObjectUIController(currentLink);
                }
                addFields(currentLink);
                uiController.updateUI();

                setEnabled(true);
            }
        }
    }

    private void deactivateCardinalityPanel() {
        setEnabled(false);
        removeFields();
    }

    private void removeFields() {
        if (uiController != null) {
            uiController.remove(minCardField);
            uiController.remove(maxCardField);
            uiController.remove(defaultCardField);
        }
    }

    private void addFields(IProductCmptLink link) {
        minCardField = new CardinalityField(getMinCardinalityTextControl());
        maxCardField = new CardinalityField(getMaxCardinalityTextControl());
        defaultCardField = new CardinalityField(getDefaultCardinalityTextControl());
        uiController.add(minCardField, link, IProductCmptLink.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardField, link, IProductCmptLink.PROPERTY_MAX_CARDINALITY);
        uiController.add(defaultCardField, link, IProductCmptLink.PROPERTY_DEFAULT_CARDINALITY);
    }

    public void refresh() {
        if (uiController != null) {
            uiController.updateUI();
        }
    }

    /**
     * Method to enable or disable this panel. If no checkbox (optional, mandatory, other) is
     * selected all controls will be disabled, regardless of the given value. This is the case if no
     * ProdCmptLink is selected in the {@link LinksSection}s tree, thus this panel is disabled
     * completely.
     */
    public void setEnabled(boolean enabled) {
        mandatory.setEnabled(enabled);
        mandatorylLabel.setEnabled(enabled);
        optional.setEnabled(enabled);
        optionalLabel.setEnabled(enabled);
        other.setEnabled(enabled);
        otherLabel.setEnabled(enabled);

        minCard.setEnabled(false);
        maxCard.setEnabled(false);
        defaultCard.setEnabled(false);
        maxCardLabel.setEnabled(false);
        minCardLabel.setEnabled(false);
        defaultCardLabel.setEnabled(false);

        if (!enabled) {
            mandatory.setSelection(false);
            optional.setSelection(false);
            other.setSelection(false);
            setFieldValue(minCardField, new Integer(0), false);
            setFieldValue(maxCardField, new Integer(0), false);
            setFieldValue(defaultCardField, new Integer(0), false);
        } else {
            String min = minCard.getText();
            String max = maxCard.getText();
            String def = defaultCard.getText();

            if (min.equals("1") && max.equals("1") && def.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                mandatory.setSelection(true);
                optional.setSelection(false);
                other.setSelection(false);
            } else if (min.equals("0") && max.equals("1") && def.equals("0")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                optional.setSelection(true);
                mandatory.setSelection(false);
                other.setSelection(false);
            } else {
                other.setSelection(true);
                mandatory.setSelection(false);
                optional.setSelection(false);

                minCard.setEnabled(true);
                maxCard.setEnabled(true);
                defaultCard.setEnabled(true);
                maxCardLabel.setEnabled(true);
                minCardLabel.setEnabled(true);
                defaultCardLabel.setEnabled(true);
            }
        }
    }

    /**
     * Listener to update on cardinality modifications.
     * 
     * @author Thorsten Guenther
     * @author Stefan Widmaier
     */
    private class KardinalitySelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {

            removeMessageCue();
            boolean enabled = e.getSource() == other;
            minCard.setEnabled(enabled);
            maxCard.setEnabled(enabled);
            defaultCard.setEnabled(enabled);
            maxCardLabel.setEnabled(enabled);
            minCardLabel.setEnabled(enabled);
            defaultCardLabel.setEnabled(enabled);

            /*
             * Setting the cardinality values using their fields causes an inconsistent GUI state
             * and incorrect model data. This is due to the fact that edit fields update the model
             * asynchronously (see EventBroadcaster). Following calls of setValue on other fields
             * might be "too early" as the Text controls will also be updated by triggered
             * modelChangeEvents that then write possibly incorrect or outdated data to the GUI.
             * 
             * Calling the model-setters directly forces synchronous processing and works around
             * those problems.
             */
            if (e.getSource() == optional) {
                currentLink.setMinCardinality(0);
                currentLink.setMaxCardinality(1);
                currentLink.setDefaultCardinality(0);
            } else if (e.getSource() == mandatory) {
                currentLink.setMinCardinality(1);
                currentLink.setMaxCardinality(1);
                currentLink.setDefaultCardinality(1);
            }

        }

        /**
         * Removes the message cue by setting empty lists
         */
        private void removeMessageCue() {
            MessageList emptyList = new MessageList();
            MessageCueController.setMessageCue(minCard, emptyList);
            MessageCueController.setMessageCue(maxCard, emptyList);
            MessageCueController.setMessageCue(defaultCard, emptyList);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

    protected void setFieldValue(CardinalityField field, Integer integer, boolean triggerValueChanged) {
        if (field != null) {
            field.setValue(integer, triggerValueChanged);
        }
    }

    /**
     * Returns the Text control which displays the min cardinality
     */
    private Text getMinCardinalityTextControl() {
        return minCard;
    }

    /**
     * Returns the Text control which displays the max cardinality
     */
    private Text getMaxCardinalityTextControl() {
        return maxCard;
    }

    /**
     * Returns the control which displays the default cardinality
     */
    private Text getDefaultCardinalityTextControl() {
        return defaultCard;
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        if (!isDataChangeable()) {
            uiToolkit.setDataChangeable(optional, changeable);
            uiToolkit.setDataChangeable(mandatory, changeable);
            uiToolkit.setDataChangeable(other, changeable);
            uiToolkit.setDataChangeable(minCard, changeable);
            uiToolkit.setDataChangeable(maxCard, changeable);
            uiToolkit.setDataChangeable(defaultCard, changeable);
        }
    }

}
