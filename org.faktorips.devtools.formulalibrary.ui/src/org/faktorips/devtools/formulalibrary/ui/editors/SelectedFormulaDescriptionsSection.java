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

package org.faktorips.devtools.formulalibrary.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.DescriptionEditComposite;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * The <tt>SelectedFormulaDescriptionSection</tt> generates an <tt>IpsSection</tt> with
 * <tt>DescriptionEditComposite</tt>.
 * <p>
 * This shows a description of the formula.
 * 
 * @author HBaagil
 */
public class SelectedFormulaDescriptionsSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.formulalibrary.ui.editors.SelectedFormulaDescriptionsSection"; //$NON-NLS-1$
    private FormulaFunctionPmo formulaFunctionPmo;
    private FormulaFunctionDescriptionChangeListener formulaFunctionDescriptionChangeListener;
    private DescriptionEditComposite descriptionEditComposite;
    private Composite selectedFormulaDescriptionComposite;

    /**
     * Creates a new <tt>SelectedFormulaDescriptionSection</tt>.
     * 
     * @param parent <tt>Composite</tt> this <tt>IpsSection</tt> belongs to.
     * @param toolkit The <tt>UIToolkit</tt> for look and feel controls.
     * @param formulaFunctionPmo The <tt>IpsObjectPartPmo</tt> as presentation model object for
     *            <tt>IFormulaFunction</tt>.
     */
    protected SelectedFormulaDescriptionsSection(Composite parent, UIToolkit toolkit,
            FormulaFunctionPmo formulaFunctionPmo) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        this.formulaFunctionPmo = formulaFunctionPmo;
        initControls();
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        createDescriptionSection(client);
        formulaFunctionDescriptionChangeListener = new FormulaFunctionDescriptionChangeListener(
                descriptionEditComposite, formulaFunctionPmo);
        formulaFunctionPmo.addPropertyChangeListener(formulaFunctionDescriptionChangeListener);
        getBindingContext().updateUI();
    }

    private void createDescriptionSection(Composite parent) {
        selectedFormulaDescriptionComposite = getToolkit().createGridComposite(parent, 1, false, false);
        descriptionEditComposite = new DescriptionEditComposite(selectedFormulaDescriptionComposite, null, getToolkit());
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        this.formulaFunctionPmo.removePropertyChangeListener(formulaFunctionDescriptionChangeListener);
    }

    private static final class FormulaFunctionDescriptionChangeListener implements PropertyChangeListener {

        private final DescriptionEditComposite descriptionEditComposite;
        private final FormulaFunctionPmo formulaFunctionPmo;

        public FormulaFunctionDescriptionChangeListener(DescriptionEditComposite descriptionEditComposite,
                FormulaFunctionPmo formulaFunctionPmo) {
            this.descriptionEditComposite = descriptionEditComposite;
            this.formulaFunctionPmo = formulaFunctionPmo;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (FormulaFunctionPmo.PROPERTY_IPS_OBJECT_PART_CONTAINER.equals(evt.getPropertyName())) {
                boolean isEnabled = false;
                IDescribedElement describedElement = null;
                if (formulaFunctionPmo.getFormulaFunction() != null) {
                    isEnabled = true;
                    describedElement = formulaFunctionPmo.getFormulaFunction().getFormulaMethod();
                }
                descriptionEditComposite.setEnabled(isEnabled);
                descriptionEditComposite.setDescribedElement(describedElement);
                descriptionEditComposite.refresh();
            }
        }
    }
}
