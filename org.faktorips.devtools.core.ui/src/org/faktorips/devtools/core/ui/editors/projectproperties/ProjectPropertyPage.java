/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.projectproperties;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.ui.UIToolkit;

public abstract class ProjectPropertyPage extends FormPage {
    /** The horizontal space between two sections. */
    public final static int HORIZONTAL_SECTION_SPACE = 15;

    /** The vertical space between two sections. */
    public final static int VERTICAL_SECTION_SPACE = 10;

    private UIToolkit uiToolkit;

    public ProjectPropertyPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        uiToolkit = new UIToolkit(new FormToolkit(Display.getCurrent()));
    }

    abstract protected void createPageContent(Composite formBody, UIToolkit toolkit);

    abstract protected String getPageName();

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);

        ScrolledForm form = managedForm.getForm();
        if (getIIpsProjectProperties() == null) {
            // No valid IPS source file, create nothing.
            return;
        }

        form.setText(getPageName());
        FormToolkit toolkit = managedForm.getToolkit();
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.reflow(true);
        createPageContent(form.getBody(), new UIToolkit(toolkit));
        // registerSelectionProviderActivation(getPartControl());
    }

    /** Returns the <tt>IpsObjectEditor</tt> this page belongs to. */
    public ProjectPropertyEditor getProjectPropertyEditor() {
        return (ProjectPropertyEditor)getEditor();
    }

    public IIpsProjectProperties getIIpsProjectProperties() {
        /*
         * Null checking is necessary since it might be the case that the IPS source file cannot be
         * determined. E.g. in the special case that one tries to open an IPS source file which is
         * not in an IPS package.
         */
        if (getProjectPropertyEditor() != null && getProjectPropertyEditor().getProperty() != null) {
            return getProjectPropertyEditor().getProperty();
        } else {
            return null;
        }
    }

    protected GridLayout createPageLayout(int numOfColumns, boolean equalSize) {
        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        return layout;
    }

    /**
     * Creates a grid composite for the inner page structure. The composite has no margins but the
     * default spacing settings.
     * 
     * @param numOfColumns Number of columns in the grid.
     * @param equalSize Set to <tt>true</tt> if the columns should have the same size.
     */
    protected Composite createGridComposite(UIToolkit toolkit,
            Composite parent,
            int numOfColumns,
            boolean equalSize,
            int gridData) {

        Composite composite = toolkit.getFormToolkit().createComposite(parent);

        GridLayout layout = new GridLayout(numOfColumns, equalSize);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;

        composite.setLayout(layout);
        composite.setLayoutData(new GridData(gridData));

        return composite;
    }
    // private static class LabelSection extends IpsSection {
    //
    // private final ILabeledElement labeledElement;
    //
    // private LabelEditComposite labelEditComposite;
    //
    // private LabelSection(ILabeledElement labeledElement, Composite parent, UIToolkit toolkit) {
    // super(parent, Section.TITLE_BAR, GridData.FILL_BOTH, toolkit);
    //
    // this.labeledElement = labeledElement;
    //
    // initControls();
    // setText(Messages.LabelSection_label);
    // }
    //
    // @Override
    // protected void initClientComposite(Composite client, UIToolkit toolkit) {
    // labelEditComposite = new LabelEditComposite(client, labeledElement);
    // }
    //
    // @Override
    // protected void performRefresh() {
    // if (labeledElement == null) {
    // return;
    // }
    // labelEditComposite.refresh();
    // }
    //
    // }
}
