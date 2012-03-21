/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IDeleteListener;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.SimpleIpsPartsSection;

/**
 * A section that displays a timed pdobject's generations.
 */
public class GenerationsSection extends SimpleIpsPartsSection {

    private static final String ID = "org.faktorips.devtools.core.ui.editors.productcmpt.GenerationsSection"; //$NON-NLS-1$

    /**
     * The page owning this section.
     */
    private ProductCmptPropertiesPage page;

    /**
     * Create a new Section to display generations.
     * 
     * @param page The page owning this section.
     * @param parent The composite which is parent for this section
     * @param toolkit The toolkit to help creating the UI
     */
    public GenerationsSection(ProductCmptPropertiesPage page, Composite parent, UIToolkit toolkit) {
        super(ID, page.getProductCmpt(), parent, page.getSite(), IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(), toolkit);
        this.page = page;
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new GenerationsComposite((ITimedIpsObject)getIpsObject(), parent, toolkit);
    }

    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)page.getProductCmptEditor().getActiveGeneration();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite implements IDeleteListener {

        IIpsObjectGeneration newGeneration;

        public GenerationsComposite(ITimedIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, getSite(), true, true, true, false, true, false, false, false, toolkit);

            super.setEditDoubleClickListenerEnabled(false);

            getViewer().getControl().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
                    if (selected instanceof IProductCmptGeneration) {
                        page.getProductCmptEditor().setActiveGeneration((IProductCmptGeneration)selected, true);
                    }
                }
            });

            addDeleteListener(this);
        }

        public ITimedIpsObject getTimedIpsObject() {
            return (ITimedIpsObject)getIpsObject();
        }

        @Override
        protected IStructuredContentProvider createContentProvider() {
            return new ContentProvider();
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new LabelProvider();
        }

        /**
         * Creates the new generation, if the "New" button in the editor was selected
         */
        @Override
        protected IIpsObjectPart newIpsPart() {
            IProductCmptGeneration selectedGeneration = getActiveGeneration();

            newGeneration = page.getProductCmpt().newGeneration(selectedGeneration.getValidFrom());

            return newGeneration;
        }

        /**
         * Sets the default validity date and selects new generation, if the "OK" button in the
         * dialog was selected.
         */
        @Override
        protected void newPartConfirmed() {
            page.getProductCmptEditor().setActiveGeneration(newGeneration, true);
            IProductCmptGeneration selectedGeneration = getActiveGeneration();
            IpsUIPlugin.getDefault().setDefaultValidityDate(selectedGeneration.getValidFrom());
        }

        @Override
        protected EditDialog createNewDialog(IIpsObjectPart part, Shell shell) {
            return new GenerationEditDialog((IProductCmptGeneration)part, shell, true);
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new GenerationEditDialog((IProductCmptGeneration)part, shell, false);
        }

        @Override
        public boolean aboutToDelete(IIpsObjectPart part) {
            if (page.getProductCmpt().getGenerationsOrderedByValidDate().length == 2) {
                super.deleteButton.setEnabled(false);
            }
            return true;
        }

        @Override
        public void deleted(IIpsObjectPart part) {
            page.getProductCmptEditor().setActiveGeneration(getSelectedGeneration(), true);
        }

        private IProductCmptGeneration getSelectedGeneration() {
            IIpsObjectPart selected = getSelectedPart();
            if (selected instanceof IProductCmptGeneration) {
                return (IProductCmptGeneration)selected;
            }
            return null;
        }

        @Override
        protected void updateButtonEnabledStates() {
            super.updateButtonEnabledStates();
            boolean moreThenOneGeneration = page.getProductCmpt().getGenerationsOrderedByValidDate().length > 1;
            boolean editable = IpsUIPlugin.isEditable(page.getProductCmpt().getIpsSrcFile());
            deleteButton.setEnabled(moreThenOneGeneration && editable);
        }

        private class ContentProvider implements IStructuredContentProvider {
            @Override
            public Object[] getElements(Object inputElement) {
                return getTimedIpsObject().getGenerationsOrderedByValidDate();
            }

            @Override
            public void dispose() {
                // nothing todo
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // nothing todo
            }
        }

        private class LabelProvider extends DefaultLabelProvider {

            private ResourceManager resourceManager;

            public LabelProvider() {
                resourceManager = new LocalResourceManager(JFaceResources.getResources());
            }

            @Override
            public void dispose() {
                resourceManager.dispose();
                super.dispose();
            }

            @Override
            public Image getImage(Object element) {
                if (!(element instanceof IProductCmptGeneration)) {
                    return super.getImage(element);
                }
                IProductCmptGeneration generation = (IProductCmptGeneration)element;
                Image image = super.getImage(element);
                if (getActiveGeneration() == generation) {
                    return image;
                } else {
                    ImageDescriptor disableImageDescriptor = IpsUIPlugin.getImageHandling()
                            .createDisabledImageDescriptor(ImageDescriptor.createFromImage(image));
                    return (Image)resourceManager.get(disableImageDescriptor);
                }
            }

        }

    }

}
