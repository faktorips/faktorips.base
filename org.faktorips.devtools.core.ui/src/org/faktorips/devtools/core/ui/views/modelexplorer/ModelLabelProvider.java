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

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;

/**
 * This class provides the ModelExplorer with labels for its tree-elements. Label names for
 * PackageFragments are dependent on the current layout style indicated by the
 * <code>isFlatLayout</code> flag.
 * 
 * @author Stefan Widmaier
 */
public class ModelLabelProvider implements ILabelProvider {

    private DefaultLabelProvider defaultLabelProvider;

    private boolean isFlatLayout = false;

    private boolean productDefinitionLabelProvider = false;

    private ResourceManager resourceManager;

    private IpsProblemsLabelDecorator problemsDecorator;

    public ModelLabelProvider() {
        this(false);
    }

    public ModelLabelProvider(boolean flatLayout) {
        isFlatLayout = flatLayout;
        defaultLabelProvider = new DefaultLabelProvider();
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IIpsProject) {
            /*
             * Use the project images provided by eclipse (with nature overlay images), instead of
             * calling the workbench adapter for IpsProjects (which returns an IpsProject-Icon
             * only). This is necessary as there are IpsProjects with other natures that need to be
             * displayed as such (e.g. analysis and transformation projects).
             * 
             * This was not implemented using Adapters as IpsProjects should be displayed with
             * IpsProject-Icon in situations other than the ModelExplorer (like the ipsProject
             * references).
             */
            return getImage(((IIpsProject)element).getProject());
        }
        if (element instanceof IIpsElement) {
            return defaultLabelProvider.getImage(element);
        } else if (element instanceof IResource) {
            // check if the resource is an ips source file, in this case return the image of the ips
            // source,
            // Note: if we use the IWorkbenchAdapter to retrieve the image, we get the
            // standard icon of the ips object (resolved by the filename and defined in the
            // extension point)
            // - but the element is no valid ips object (e.g. not inside an ips package) -
            // therefore to differ between "valid" and "invalid" ips objects in the model explorer
            // we return
            // a different icon

            if (IpsObjectType.getTypeForExtension(((IResource)element).getFileExtension()) != null) {
                return IpsUIPlugin.getImageHandling().getDefaultImage(IpsSrcFile.class);
            }

            IWorkbenchAdapter adapter = null;
            if (element instanceof IAdaptable) {
                adapter = (IWorkbenchAdapter)((IAdaptable)element).getAdapter(IWorkbenchAdapter.class);
            }
            if (adapter == null) {
                return null;
            }
            ImageDescriptor descriptor = adapter.getImageDescriptor(element);
            if (descriptor == null) {
                return null;
            }
            Image image = (Image)resourceManager.get(descriptor);
            return image;
        }

        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof IIpsElement) {

            if (element instanceof IIpsPackageFragment) {
                IIpsPackageFragment fragment = (IIpsPackageFragment)element;
                if (fragment.isDefaultPackage()) {
                    return Messages.ModelExplorer_defaultPackageLabel;
                }
                if (isFlatLayout) {
                    return fragment.getName();
                } else {
                    String name = fragment.getName();
                    int index = name.lastIndexOf('.');
                    if (index == -1) {
                        return name;
                    }
                    return name.substring(index + 1);
                }
            } else if (productDefinitionLabelProvider && element instanceof IIpsProject
                    && !((IIpsProject)element).isProductDefinitionProject()) {
                // if this label provider shows product definition aspects then show additional text
                // for no product definition project
                return ((IIpsProject)element).getName()
                        + NLS.bind(" ({0})", Messages.ModelLabelProvider_noProductDefinitionProjectLabel); //$NON-NLS-1$
            }

            return defaultLabelProvider.getText(element);

        } else if (element instanceof IProject && ((IProject)element).isOpen()) {
            String labelAddition = productDefinitionLabelProvider ? Messages.ModelLabelProvider_noProductDefinitionProjectLabel
                    : Messages.ModelExplorer_nonIpsProjectLabel;
            return ((IProject)element).getName() + NLS.bind(" ({0})", labelAddition); //$NON-NLS-1$

        } else {
            return ((IResource)element).getName();
        }
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        // Nothing to do
    }

    @Override
    public void dispose() {
        resourceManager.dispose();
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        // Nothing to do
    }

    /**
     * Sets the flag for flat respectivly hierarchical labelnames
     */
    void setIsFlatLayout(boolean b) {
        isFlatLayout = b;
        getProblemsDecorator().setFlatLayout(isFlatLayout);
    }

    private IpsProblemsLabelDecorator getProblemsDecorator() {
        if (problemsDecorator == null) {
            IBaseLabelProvider decorator = IpsUIPlugin.getDefault().getWorkbench().getDecoratorManager()
                    .getBaseLabelProvider(IpsProblemsLabelDecorator.EXTENSION_ID);
            if (decorator instanceof IpsProblemsLabelDecorator) {
                problemsDecorator = (IpsProblemsLabelDecorator)decorator;
            }
        }
        if (problemsDecorator == null) {
            return new IpsProblemsLabelDecorator();
        } else {
            return problemsDecorator;
        }

    }

    /**
     * @param productDefinitionLabelProvider The productDefinitionLabelProvider to set.
     */
    public void setProductDefinitionLabelProvider(boolean productDefinitionLabelProvider) {
        this.productDefinitionLabelProvider = productDefinitionLabelProvider;
    }

}
