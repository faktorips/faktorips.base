/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IDeprecation;

/**
 * A composite that allows to edit the {@link IDeprecation} attached to an
 * {@link IpsObjectPartContainer}.
 */
public final class DeprecationEditComposite extends Composite {

    private final UIToolkit uiToolkit;
    private final BindingContext bindingContext;
    private IpsObjectPartContainer deprecatedElement;
    private IDeprecation deprecationInfo;

    private Composite parent;
    private Checkbox deprecationCheckbox;
    private Label versionLabel;
    private Text versionText;
    private Label forRemovalLabel;
    private Checkbox forRemovalCheckbox;
    private DescriptionEditComposite descriptionEditComposite;

    public DeprecationEditComposite(Composite parent, IpsObjectPartContainer deprecatedElement, UIToolkit uiToolkit,
            BindingContext bindingContext) {
        super(parent, SWT.NONE);

        this.parent = parent;
        this.deprecatedElement = deprecatedElement;
        this.uiToolkit = uiToolkit;
        this.bindingContext = bindingContext;
        deprecationInfo = deprecatedElement.getDeprecation();
        if (deprecationInfo == null) {
            deprecationInfo = deprecatedElement.newDeprecation();
        }

        createLayout();
    }

    private void createLayout() {
        Composite composite = uiToolkit.createGridComposite(parent, 2, true, false);
        Composite subComposite = uiToolkit.createGridComposite(composite, 2, false, true);

        // checkbox: is deprecated?
        uiToolkit.createFormLabel(subComposite, Messages.DeprecationSection_isDeprecated);
        deprecationCheckbox = uiToolkit.createCheckbox(subComposite);
        bindingContext.bindContent(deprecationCheckbox, deprecatedElement,
                IpsObjectPartContainer.PROPERTY_DEPRECATED);
        deprecationCheckbox.addListener(SWT.Selection, $ -> setDeprecationInfoEnabled());
        deprecationCheckbox.setChecked(deprecatedElement.isDeprecated());
        uiToolkit.grabHorizontalSpace(deprecationCheckbox, false);

        // field: deprecated since
        versionLabel = uiToolkit.createFormLabel(subComposite,
                Messages.DeprecationSection_version);
        versionText = uiToolkit.createText(subComposite);
        versionText.setToolTipText(Messages.DeprecationSection_versionTooltip);
        versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bindingContext.bindContent(versionText, deprecationInfo, IDeprecation.PROPERTY_SINCE_VERSION_STRING);
        uiToolkit.paintBordersForComposite(subComposite);

        // checkbox: for removal?
        forRemovalLabel = uiToolkit.createFormLabel(subComposite, Messages.DeprecationSection_forRemoval);
        forRemovalCheckbox = uiToolkit.createCheckbox(subComposite);
        uiToolkit.grabHorizontalSpace(forRemovalCheckbox, false);
        bindingContext.bindContent(forRemovalCheckbox, deprecationInfo, IDeprecation.PROPERTY_FOR_REMOVAL);

        // composite: deprecation descriptions
        descriptionEditComposite = new DescriptionEditComposite(composite, deprecationInfo, uiToolkit,
                bindingContext);

        setDeprecationInfoEnabled();

        bindingContext.updateUI();
    }

    private void setDeprecationInfoEnabled() {
        boolean isDeprecated = deprecationCheckbox.isChecked();
        versionLabel.setEnabled(isDeprecated);
        versionText.setEnabled(isDeprecated);
        forRemovalLabel.setEnabled(isDeprecated);
        forRemovalCheckbox.setEnabled(isDeprecated);
        descriptionEditComposite.setEnabled(isDeprecated);
    }

    public void refresh() {
        descriptionEditComposite.refresh();
    }
}
