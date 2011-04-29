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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * Section to edit supertype, abstract flag and configured <tt>IPolicyCmptType</tt>.
 * 
 * @author Jan Ortmann
 */
public class ProductDefinitionSection extends IpsSection {

    private IIpsProjectProperties iIpsProjectProperties;
    private IPersistenceOptions persistenceOptions;

    public ProductDefinitionSection(IIpsProjectProperties iIpsProjectProperties, Composite parent, UIToolkit toolkit) {
        super(parent, ExpandableComposite.TITLE_BAR, GridData.FILL_HORIZONTAL, toolkit);

        this.iIpsProjectProperties = iIpsProjectProperties;
        this.persistenceOptions = iIpsProjectProperties.getPersistenceOptions();
        initControls();
        setText(Messages.PersistenceOptions_title);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {

        client.setLayout(new GridLayout(1, false));
        Composite composite = toolkit.createGridComposite(client, 2, false, false);
        // Composite composite = toolkit.createLabelEditColumnComposite(client);

        toolkit.createFormLabel(composite, Messages.Overview_modelProject);
        Checkbox modelProjectCheckbox = toolkit.createCheckbox(composite);
        if (iIpsProjectProperties.isModelProject()) {
            modelProjectCheckbox.setChecked(true);
        }

        toolkit.createFormLabel(composite, Messages.Overview_productDefinitionProject);
        Checkbox productDefinitionProjectCheckbox = toolkit.createCheckbox(composite);
        if (iIpsProjectProperties.isProductDefinitionProject()) {
            productDefinitionProjectCheckbox.setChecked(true);
        }

    }

    @Override
    protected void performRefresh() {
        bindingContext.updateUI();
    }

}
