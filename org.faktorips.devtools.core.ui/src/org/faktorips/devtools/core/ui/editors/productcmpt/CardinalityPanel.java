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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.MessageCueController;
import org.faktorips.util.message.MessageList;

/**
 * Panel to display cardinality. Note that this is <strong>NOT</strong> a control.
 * 
 * @author Thorsten Guenther
 */
public class CardinalityPanel implements IDataChangeableReadWriteAccess {
    private UIToolkit uiToolkit;

    private Text minKard;
    private Text maxKard;
    private Label minKardLabel;
    private Label maxKardLabel;
    private Button optional;
    private Label optionalLabel;
    private Button mandatory;
    private Label mandatorylLabel;
    private Button other;
    private Label otherLabel;
    private Composite root;

    private ModifyListener minListener;
    private ModifyListener maxListener;

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
        minKardLabel = toolkit.createFormLabel(specialPane, Messages.PolicyAttributesSection_minimum);
        minKard = toolkit.createText(specialPane);
        minKard.setLayoutData(new GridData(30, SWT.DEFAULT));
        maxKardLabel = toolkit.createFormLabel(specialPane, Messages.PolicyAttributesSection_maximum);
        maxKard = toolkit.createText(specialPane);
        maxKard.setLayoutData(new GridData(30, SWT.DEFAULT));
        toolkit.createVerticalSpacer(specialPane, 3).setBackground(kardinalityPane.getBackground());

        kardinalityPane.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
        toolkit.getFormToolkit().paintBordersFor(root);
    }

    /**
     * Update values and enablement-state on cardinality changes
     */
    private void cardinalityChanged() {
        String min = minKard.getText();
        String max = maxKard.getText();
        mandatory.setSelection(false);
        optional.setSelection(false);
        other.setSelection(false);
        minKard.setEnabled(false);
        maxKard.setEnabled(false);
        maxKardLabel.setEnabled(false);
        minKardLabel.setEnabled(false);

        if (min.equals("1") && max.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
            mandatory.setSelection(true);
        } else if (min.equals("0") && max.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
            optional.setSelection(true);
        } else {
            other.setSelection(true);
            minKard.setEnabled(true);
            maxKard.setEnabled(true);
            maxKardLabel.setEnabled(true);
            minKardLabel.setEnabled(true);
        }
    }

    /**
     * Method to enable or disable this panel.
     */
    public void setEnabled(boolean enabled) {
        boolean selected = optional.getSelection() || mandatory.getSelection() || other.getSelection();

        mandatory.setEnabled(enabled && selected);
        mandatorylLabel.setEnabled(enabled && selected);
        optional.setEnabled(enabled && selected);
        optionalLabel.setEnabled(enabled && selected);
        other.setEnabled(enabled && selected);
        otherLabel.setEnabled(enabled && selected);
        if (other.getSelection() || !selected) {
            minKard.setEnabled(enabled && selected);
            minKardLabel.setEnabled(enabled && selected);
            maxKard.setEnabled(enabled && selected);
            maxKardLabel.setEnabled(enabled && selected);
        }
    }

    /**
     * Listener to update on cardinality modifications.
     * 
     * @author Thorsten Guenther
     */
    private class KardinalitySelectionListener implements SelectionListener {

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e) {
            removeMessageCue();
            boolean enabled = e.getSource() == other;
            minKard.setEnabled(enabled);
            maxKard.setEnabled(enabled);
            maxKardLabel.setEnabled(enabled);
            minKardLabel.setEnabled(enabled);

            if (e.getSource() == optional) {
                setText(minKard, "0", minListener); //$NON-NLS-1$
                setText(maxKard, "1", maxListener); //$NON-NLS-1$
            } else if (e.getSource() == mandatory) {
                setText(minKard, "1", minListener); //$NON-NLS-1$
                setText(maxKard, "1", maxListener); //$NON-NLS-1$
            }

        }

        /**
         * Removes the message cue by setting empty lists
         */
        private void removeMessageCue() {
            MessageList emptyList = new MessageList();
            MessageCueController.setMessageCue(minKard, emptyList);
            MessageCueController.setMessageCue(maxKard, emptyList);
        }

        /**
         * Set the text to the given control and updates the given listener.
         */
        private void setText(Text text, String newValue, ModifyListener listener) {
            String oldValue = text.getText();
            if (oldValue.equals(newValue)) {
                return;
            }

            text.setText(newValue);

            Event e = new Event();
            e.widget = text;
            e.text = newValue;
            listener.modifyText(new ModifyEvent(e));
        }

        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

    }

    /**
     * Returns the control which displays the min cardinality
     */
    public Control getMinCardinalityControl() {
        return minKard;
    }

    /**
     * Returns the control which displays the max cardinality
     */
    public Control getMaxCardinalityControl() {
        return maxKard;
    }

    /**
     * Set the value for the min cardinality
     */
    public void setMinCardinality(String newText) {
        minKard.setText(newText);
        cardinalityChanged();
    }

    /**
     * Set the value for the max cardinality
     */
    public void setMaxCardinality(String newText) {
        maxKard.setText(newText);
        cardinalityChanged();
    }

    /**
     * Returns the value for the min cardinality
     */
    public String getMinCardinality() {
        return minKard.getText();
    }

    /**
     * Returns the value for the max cardinality
     */
    public String getMaxCardinality() {
        return maxKard.getText();
    }

    /**
     * Adds a modification listener to the min cardianlity control
     */
    public void addMinModifyListener(ModifyListener listener) {
        minListener = listener;
        minKard.addModifyListener(listener);
    }

    /**
     * Adds a modification listener to the max cardianlity control
     */
    public void addMaxModifyListener(ModifyListener listener) {
        maxListener = listener;
        maxKard.addModifyListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        if (!isDataChangeable()) {
            uiToolkit.setDataChangeable(optional, changeable);
            uiToolkit.setDataChangeable(mandatory, changeable);
            uiToolkit.setDataChangeable(other, changeable);
            uiToolkit.setDataChangeable(minKard, changeable);
            uiToolkit.setDataChangeable(maxKard, changeable);
        }
    }
}
