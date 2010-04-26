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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.Described;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section that displays rules for a product component.
 * 
 * @author Thorsten Guenter
 */
public class RulesSection extends SimpleIpsPartsSection {

    /**
     * The page owning this section.
     */
    private ProductCmptPropertiesPage page;

    /**
     * Create a new Section to display rules.
     * 
     * @param page The page owning this section.
     * @param parent The composit which is parent for this section
     * @param toolkit The toolkit to help creating the ui
     */
    public RulesSection(ProductCmptPropertiesPage page, Composite parent, UIToolkit toolkit) {
        super(page.getProductCmpt(), parent, Section.TITLE_BAR, Messages.RulesSection_title, toolkit);
        this.page = page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new RulesComposite((IProductCmpt)getIpsObject(), parent, toolkit);
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    public class RulesComposite extends IpsPartsComposite {
        private Text descriptionText;

        public RulesComposite(IProductCmpt product, Composite parent, UIToolkit toolkit) {
            super(product, parent, false, false, false, false, false, toolkit);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initControls(UIToolkit toolkit) {
            super.initControls(toolkit);

            // add another column to the layout created by the parent
            GridLayout layout = (GridLayout)getLayout();
            layout.numColumns += 1;
            layout.makeColumnsEqualWidth = true;

            // add the textfield to disply the description
            descriptionText = toolkit.createText(this, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.FLAT);
            descriptionText.setEditable(false);

            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.verticalIndent = 1;
            descriptionText.setLayoutData(data);

            // if the selection changes in the viewer, we have to update the description
            super.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    ISelection selection = getViewer().getSelection();
                    if (!(selection instanceof IStructuredSelection)) {
                        descriptionText.setText(""); //$NON-NLS-1$
                        return;
                    }
                    Object selected = ((IStructuredSelection)selection).getFirstElement();
                    if (!(selected instanceof Described)) {
                        descriptionText.setText(""); //$NON-NLS-1$
                        return;
                    }
                    descriptionText.setText(((Described)selected).getDescription());
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return null;
        }

        private class ContentProvider implements IStructuredContentProvider {

            public Object[] getElements(Object inputElement) {
                try {
                    IPolicyCmptType type = page.getProductCmpt().findPolicyCmptType(
                            page.getProductCmpt().getIpsProject());
                    if (type == null) {
                        return new Object[0];
                    }
                    ITypeHierarchy hierarchy = type.getSupertypeHierarchy();
                    return hierarchy.getAllRules(type);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                    return new Object[0];
                }
            }

            public void dispose() {
                // nothing todo
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }

        }

    }

}
