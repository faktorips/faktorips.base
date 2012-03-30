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

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controller.fields.GregorianCalendarFormat;
import org.faktorips.devtools.core.ui.controls.DateControl;

/**
 * Allows creation of new {@link IIpsObjectGeneration generations} to one or many
 * {@link ITimedIpsObject timed IPS objects}.
 */
public class NewGenerationWizard extends Wizard {

    private final NewGenerationPMO pmo = new NewGenerationPMO();

    private final List<ITimedIpsObject> timedIpsObjects;

    /**
     * @param timedIpsObjects a list containing all {@link ITimedIpsObject timed IPS objects} to
     *            create new {@link IIpsObjectGeneration generations} for
     */
    public NewGenerationWizard(List<ITimedIpsObject> timedIpsObjects) {
        this.timedIpsObjects = timedIpsObjects;
        setWindowTitle(Messages.NewGenerationWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/CreateNewGenerationWizard.png")); //$NON-NLS-1$
    }

    @Override
    public void addPages() {
        addPage(new ChooseValidityDatePage(pmo, timedIpsObjects));
    }

    @Override
    public boolean performFinish() {
        for (ITimedIpsObject timedIpsObject : timedIpsObjects) {
            timedIpsObject.newGeneration(pmo.getValidFrom());
        }
        return true;
    }

    /**
     * Allows the user to select a validity date (valid from) for the new
     * {@link IIpsObjectGeneration generations} to create.
     */
    private static class ChooseValidityDatePage extends WizardPage {

        private final BindingContext bindingContext = new BindingContext();

        private final NewGenerationPMO pmo;

        protected ChooseValidityDatePage(NewGenerationPMO pmo, List<ITimedIpsObject> timedIpsObjects) {
            super("ChooseValidityDate"); //$NON-NLS-1$
            this.pmo = pmo;
            setTitle(Messages.ChooseValidityDatePage_pageTitle);
            if (timedIpsObjects.size() == 1) {
                setMessage(NLS.bind(Messages.ChooseValidityDatePage_pageInfoSingular, timedIpsObjects.get(0).getName()));
            } else {
                setMessage(NLS.bind(Messages.ChooseValidityDatePage_pageInfoPlural, timedIpsObjects.size()));
            }
        }

        @Override
        public void createControl(Composite parent) {
            UIToolkit toolkit = new UIToolkit(null);
            Composite pageControl = toolkit.createLabelEditColumnComposite(parent);
            setControl(pageControl);

            toolkit.createLabel(pageControl, Messages.ChooseValidityDatePage_labelValidFrom);
            DateControl dateControl = new DateControl(pageControl, toolkit);
            Text textControl = dateControl.getTextControl();
            bindingContext.bindContent(
                    new FormattingTextField<GregorianCalendar>(textControl, GregorianCalendarFormat.newInstance()),
                    pmo, NewGenerationPMO.PROPERTY_VALID_FROM);

            bindingContext.updateUI();
        }

        @Override
        public void dispose() {
            super.dispose();
            bindingContext.dispose();
        }

    }

}
