/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productdefinition;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.DateControl;
import org.faktorips.devtools.core.ui.inputformat.GregorianCalendarFormat;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.ITimedIpsObject;

/**
 * Allows creation of new {@linkplain IIpsObjectGeneration IPS Object Generations} to one or many
 * {@linkplain ITimedIpsObject Timed IPS Objects}.
 */
public class NewGenerationWizard extends Wizard {

    private final NewGenerationPMO pmo = new NewGenerationPMO();

    private final List<ITimedIpsObject> timedIpsObjects;

    /**
     * @param timedIpsObjects a list containing all {@linkplain ITimedIpsObject Timed IPS Objects}
     *            to create new {@linkplain IIpsObjectGeneration IPS Object Generations} for
     */
    public NewGenerationWizard(List<ITimedIpsObject> timedIpsObjects) {
        this.timedIpsObjects = timedIpsObjects;
        setWindowTitle(NLS.bind(Messages.NewGenerationWizard_title, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNameSingular()));
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewGenerationWizard.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        addPage(new ChooseValidityDatePage(pmo, timedIpsObjects));
    }

    @Override
    public boolean performFinish() {
        // Execute runnable
        try {
            getContainer().run(true, true, new NewGenerationRunnable(pmo, timedIpsObjects));
        } catch (InvocationTargetException | InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

        // Update default validity date
        IpsUIPlugin.getDefault().setDefaultValidityDate(pmo.getValidFrom());

        return true;
    }

    /**
     * Allows the user to select a validity date (valid-from) for the new
     * {@linkplain IIpsObjectGeneration IPS Object Generations} to create.
     */
    private static class ChooseValidityDatePage extends WizardPage {

        private final BindingContext bindingContext = new BindingContext();

        private final NewGenerationPMO pmo;

        protected ChooseValidityDatePage(NewGenerationPMO pmo, List<ITimedIpsObject> timedIpsObjects) {
            super("ChooseValidityDate"); //$NON-NLS-1$
            this.pmo = pmo;

            // Set page title
            setTitle(Messages.ChooseValidityDatePage_pageTitle);

            // Set info message
            if (timedIpsObjects.size() == 1) {
                String objectName = timedIpsObjects.get(0).getName();
                setMessage(NLS.bind(Messages.ChooseValidityDatePage_msgPageInfoSingular,
                        getGenerationConceptNameSingular(true), objectName));
            } else {
                setMessage(NLS.bind(Messages.ChooseValidityDatePage_msgPageInfoPlural,
                        getGenerationConceptNameSingular(true), timedIpsObjects.size()));
            }

            addValidationListener();
        }

        private String getGenerationConceptNameSingular(boolean usageInsideSentence) {
            return IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                    .getGenerationConceptNameSingular(usageInsideSentence);
        }

        private void addValidationListener() {
            pmo.addPropertyChangeListener($ -> {
                // Reset error message
                setErrorMessage(null);
                if (pmo.getValidFrom() == null) {
                    setErrorMessage(Messages.ChooseValidityDatePage_msgValidFromInvalid);
                }
            });
        }

        @Override
        public void createControl(Composite parent) {
            UIToolkit toolkit = new UIToolkit(null);
            Composite pageControl = toolkit.createLabelEditColumnComposite(parent);
            setControl(pageControl);

            // Valid from
            toolkit.createLabel(pageControl, Messages.ChooseValidityDatePage_labelValidFrom);
            DateControl validFromDateControl = new DateControl(pageControl, toolkit);
            Text validFromTextControl = validFromDateControl.getTextControl();
            bindingContext.bindContent(new FormattingTextField<>(validFromTextControl,
                    GregorianCalendarFormat.newInstance()), pmo, NewGenerationPMO.PROPERTY_VALID_FROM);

            // Skip existing generations
            toolkit.createLabel(pageControl, StringUtils.EMPTY);
            Checkbox skipExistingGenerationsCheckbox = toolkit.createCheckbox(pageControl, NLS.bind(
                    Messages.ChooseValidityDatePage_labelSkipExistingGenerations,
                    getGenerationConceptNameSingular(true)));
            bindingContext.bindContent(skipExistingGenerationsCheckbox, pmo,
                    NewGenerationPMO.PROPERTY_SKIP_EXISTING_GENERATIONS);

            bindingContext.updateUI();
        }

        @Override
        public void dispose() {
            super.dispose();
            bindingContext.dispose();
        }

    }

}
