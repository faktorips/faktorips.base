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

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.forms.IpsSection;

/**
 * A section that allows to edit the {@link IDescription}s of {@link IDescribedElement}s.
 * 
 * @author Jan Ortmann
 * @author Alexander Weickmann
 */
public class DescriptionSection extends IpsSection {

    private final IDescribedElement describedElement;

    private final IIpsProject ipsProject;

    private DescriptionEditComposite descriptionEditComposite;

    public DescriptionSection(IDescribedElement described, IIpsProject ipsProject, Composite parent, UIToolkit toolkit) {
        this(described, ipsProject, parent, Section.TITLE_BAR, toolkit);
    }

    public DescriptionSection(IDescribedElement described, IIpsProject ipsProject, Composite parent, int style,
            UIToolkit toolkit) {

        super(parent, style, GridData.FILL_BOTH, toolkit);

        this.describedElement = described;
        this.ipsProject = ipsProject;

        initControls();
        setText(Messages.DescriptionSection_description);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        descriptionEditComposite = new DescriptionEditComposite(client, describedElement, ipsProject, toolkit);
    }

    @Override
    protected void performRefresh() {
        if (describedElement == null) {
            return;
        }
        descriptionEditComposite.refresh();
    }

}
