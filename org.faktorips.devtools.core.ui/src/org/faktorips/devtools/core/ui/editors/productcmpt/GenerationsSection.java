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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.actions.IpsAction;
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
        super(ID, page.getProductCmpt(), parent, IpsPlugin.getDefault().getIpsPreferences()
                .getChangesOverTimeNamingConvention().getGenerationConceptNamePlural(), toolkit);
        this.page = page;
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new GenerationsComposite((ITimedIpsObject)getIpsObject(), parent, toolkit);
    }

    /**
     * Set the active generation (which means, the generation to show/edit) in the editor. If the
     * generation to set would not be editable, the user is asked if a switch is really wanted.
     */
    private void setActiveGeneration(IProductCmptGeneration generation) {
        if (generation == null) {
            return;
        }
        if (generation == page.getProductCmptEditor().getGenerationEffectiveOnCurrentEffectiveDate()) {
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
        if (IpsPlugin.getDefault().getIpsPreferences().isWorkingModeBrowse()) {
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
        if (!IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration()
                && generation.getValidFrom().before(new GregorianCalendar())) {
            page.getProductCmptEditor().setActiveGeneration(generation, false);
            return;
        }
        String genName = IpsPlugin.getDefault().getIpsPreferences().getChangesOverTimeNamingConvention()
                .getGenerationConceptNameSingular(true);
        String title = NLS.bind(Messages.GenerationsSection_titleShowGeneration, genName);
        Object[] args = new Object[3];
        args[0] = genName;
        args[1] = generation.getName();
        args[2] = IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate();
        String message = NLS.bind(Messages.GenerationsSection_msgShowGeneration, args);

        MessageDialog dlg = new MessageDialog(
                page.getSite().getShell(),
                title,
                null,
                message,
                MessageDialog.QUESTION,
                new String[] { Messages.GenerationsSection_buttonChangeEffectiveDate,
                        Messages.GenerationsSection_buttonKeepEffectiveDate, Messages.GenerationsSection_buttonCancel },
                0);
        int result = dlg.open();
        if (result == 2) {
            return; // cancel
        }
        if (result == 0) {
            IpsPlugin.getDefault().getIpsPreferences().setWorkingDate(generation.getValidFrom());
        }
        page.getProductCmptEditor().setActiveGeneration(generation, true);
    }

    private IProductCmptGeneration getActiveGeneration() {
        return (IProductCmptGeneration)page.getProductCmptEditor().getActiveGeneration();
    }

    /**
     * A composite that shows a policy component's attributes in a viewer and allows to edit
     * attributes in a dialog, create new attributes and delete attributes.
     */
    public class GenerationsComposite extends IpsPartsComposite implements IDeleteListener {

        private OpenGenerationInEditorAction openAction;

        public GenerationsComposite(ITimedIpsObject ipsObject, Composite parent, UIToolkit toolkit) {
            super(ipsObject, parent, false, true, true, false, true, toolkit);

            super.setEditDoubleClickListenerEnabled(false);

            getViewer().getControl().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    Object selected = ((IStructuredSelection)getViewer().getSelection()).getFirstElement();
                    if (selected instanceof IProductCmptGeneration) {
                        setActiveGeneration((IProductCmptGeneration)selected);
                    }
                }
            });

            addDeleteListener(this);

            openAction = new OpenGenerationInEditorAction(getViewer());
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

        @Override
        protected IIpsObjectPart newIpsPart() {
            return null;
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new GenerationEditDialog((IProductCmptGeneration)part, shell);
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
            public String getText(Object element) {
                if (!(element instanceof IProductCmptGeneration)) {
                    return super.getText(element);
                }
                IProductCmptGeneration gen = (IProductCmptGeneration)element;
                String comment = ""; //$NON-NLS-1$
                if (page.getProductCmptEditor().isEffectiveOnCurrentEffectiveDate(gen)) {
                    comment = comment + Messages.GenerationsSection_validFrom
                            + IpsPlugin.getDefault().getIpsPreferences().getFormattedWorkingDate();
                }
                Boolean validFromInPast = gen.isValidFromInPast();
                if ((validFromInPast == null || validFromInPast.booleanValue())
                        && !IpsPlugin.getDefault().getIpsPreferences().canEditRecentGeneration()) {
                    if (!comment.equals("")) { //$NON-NLS-1$
                        comment = comment + ","; //$NON-NLS-1$
                    }
                    comment = comment + Messages.GenerationsSection_validFromInPast;
                }
                return super.getText(element) + comment;
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

        @Override
        protected void openLink() {
            openAction.run();
        }
    }

    private class OpenGenerationInEditorAction extends IpsAction {
        public OpenGenerationInEditorAction(ISelectionProvider selectionProvider) {
            super(selectionProvider);
        }

        @Override
        public void run(IStructuredSelection selection) {
            Object selected = selection.getFirstElement();
            if (selected instanceof IProductCmptGeneration) {
                IProductCmptGeneration generation = (IProductCmptGeneration)selected;
                try {
                    IEditorPart editor = IpsUIPlugin.getDefault().openEditor(generation.getProductCmpt());
                    if (editor instanceof ProductCmptEditor) {
                        // set the selected generation
                        ProductCmptEditor productCmptEditor = (ProductCmptEditor)editor;
                        productCmptEditor.setActiveGeneration(generation, true);

                        // edit generation: set working date to generations valid from date
                        // only if the edit working mode is enabled and
                        // recent generations could be changed
                        // otherwise show generation read-only
                        IpsPreferences ipsPreferences = IpsPlugin.getDefault().getIpsPreferences();
                        if (ipsPreferences.canEditRecentGeneration() && ipsPreferences.isWorkingModeEdit()) {
                            ipsPreferences.setWorkingDate(generation.getValidFrom());
                        }
                    }
                } catch (Exception e) {
                    // TODO catch Exception needs to be documented properly or specialized
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }

    }

}
