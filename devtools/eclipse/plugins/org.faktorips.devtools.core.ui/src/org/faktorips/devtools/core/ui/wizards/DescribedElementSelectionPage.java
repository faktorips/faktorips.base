/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.wizards.productdefinition.PageUiUpdater;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.runtime.MessageList;

/**
 * A two-column {@link WizardPage} that allows the selection of a {@link IDescribedElement
 * described} {@link IIpsElement} from a {@link ElementSelectionComposite}, validating the selection
 * and advancing to the next page or finishing the wizard on a double-click selection.
 * 
 * @param <P> the presentation model for this page
 * @param <E> the type of element to be selected
 */
public class DescribedElementSelectionPage<P extends PresentationModelObject, E extends IIpsElement & IDescribedElement>
        extends WizardPage {

    private final P pmo;
    private final BindingContext bindingContext = new BindingContext();
    private final ValidatingUiUpdater<P> uiUpdater;

    private String pmoProperty;
    private Supplier<List<E>> selectableValuesSupplier;
    private IBaseLabelProvider labelProvider;
    private ElementSelectionComposite<E> selectionComposite;
    private Class<E> propertyClass;

    // CSOFF: ParameterNumberCheck
    public DescribedElementSelectionPage(String title, String description, P pmo, String pmoProperty,
            Class<E> propertyClass, IBaseLabelProvider labelProvider, Supplier<List<E>> selectableValuesSupplier,
            PresentationModelObjectValidation<P> validation) {
        super(title);
        this.pmo = pmo;
        this.pmoProperty = pmoProperty;
        this.propertyClass = propertyClass;
        this.labelProvider = labelProvider;
        this.selectableValuesSupplier = selectableValuesSupplier;
        this.uiUpdater = new ValidatingUiUpdater<>(this, pmo, validation);
        setTitle(title);
        setDescription(description);
    }
    // CSON: ParameterNumberCheck

    @Override
    public void createControl(Composite parent) {
        UIToolkit toolkit = new UIToolkit(null);
        Composite composite = createRootComposite(parent, toolkit);

        selectionComposite = new ElementSelectionComposite<>(composite, toolkit, bindingContext, pmo, pmoProperty,
                selectableValuesSupplier.get(), labelProvider, propertyClass);

        setControl(composite);

        bindControl();

        uiUpdater.updateUI();
        bindingContext.updateUI();
    }

    private Composite createRootComposite(Composite parent, UIToolkit toolkit) {
        Composite composite = toolkit.createGridComposite(parent, 1, false, false);
        GridLayout layout = (GridLayout)composite.getLayout();
        layout.verticalSpacing = 10;
        layout.marginWidth = 5;
        return composite;
    }

    private void bindControl() {
        pmo.addPropertyChangeListener(uiUpdater);

        selectionComposite.addDoubleClickListener(new DoubleClickListener(this));
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        selectionComposite.clearValidationStatus();
        bindingContext.clearValidationStatus();
    }

    private static class ValidatingUiUpdater<P extends PresentationModelObject> extends PageUiUpdater {

        private final P pmo;
        private PresentationModelObjectValidation<P> presentationModelObjectValidation;

        public ValidatingUiUpdater(DescribedElementSelectionPage<P, ?> page, P pmo,
                PresentationModelObjectValidation<P> validation) {
            super(page);
            this.pmo = pmo;
            this.presentationModelObjectValidation = validation;
        }

        @Override
        protected MessageList validatePage() {
            return presentationModelObjectValidation.apply(pmo);
        }

    }

    private static class DoubleClickListener implements IDoubleClickListener {

        private final WizardPage page;

        public DoubleClickListener(WizardPage page) {
            this.page = page;
        }

        @Override
        public void doubleClick(DoubleClickEvent event) {
            IWizard wizard = page.getWizard();
            if (page.canFlipToNextPage()) {
                wizard.getContainer().showPage(page.getNextPage());
            } else if (wizard.canFinish()) {
                if (wizard.performFinish()) {
                    ((WizardDialog)wizard.getContainer()).close();
                }
            }

        }
    }

    public abstract static class PresentationModelObjectValidation<P extends PresentationModelObject>
            implements Function<P, MessageList> {

        public abstract MessageList validate(P pmo);

        @Override
        public MessageList apply(P p) {
            return validate(p);
        }
    }
}
