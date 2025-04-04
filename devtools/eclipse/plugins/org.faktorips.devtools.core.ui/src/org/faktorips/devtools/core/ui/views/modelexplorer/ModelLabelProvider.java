/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.abstraction.AAbstraction;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.views.IpsProblemsLabelDecorator;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.ipsobject.IpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This class provides the ModelExplorer with labels for its tree-elements. Label names for
 * PackageFragments are dependent on the current layout style indicated by the
 * <code>isFlatLayout</code> flag.
 *
 * @author Stefan Widmaier
 */
public class ModelLabelProvider implements ILabelProvider {

    private static final String IPS_OBJECT_PATH_CONTAINER_GIF = "IpsObjectPathContainer.gif"; //$NON-NLS-1$

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

    // CSOFF: CyclomaticComplexity
    @Override
    public Image getImage(Object element) {
        return switch (element) {
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
            case IIpsProject ipsProject -> getImage(((IIpsProject)element).getProject());
            case IIpsElement ipsElement -> defaultLabelProvider.getImage(ipsElement);
            case ReferencedIpsProjectViewItem viewItem -> defaultLabelProvider.getImage(viewItem.getIpsProject());
            case IIpsObjectPathContainer $ -> resourceManager
                    .get(IpsUIPlugin.getImageHandling().getSharedImageDescriptor(IPS_OBJECT_PATH_CONTAINER_GIF, true));
            case AAbstraction abstraction -> getImage(abstraction.unwrap());
            case IResource resource -> {
                // check if the resource is an ips source file, in this case return the image of the
                // ips
                // source,
                // Note: if we use the IWorkbenchAdapter to retrieve the image, we get the
                // standard icon of the ips object (resolved by the filename and defined in the
                // extension point)
                // - but the element is no valid ips object (e.g. not inside an ips package) -
                // therefore to differ between "valid" and "invalid" ips objects in the model
                // explorer
                // we return
                // a different icon

                if (IpsObjectType.getTypeForExtension(((IResource)element).getFileExtension()) != null) {
                    yield IpsUIPlugin.getImageHandling().getDefaultImage(IpsSrcFile.class);
                }

                IWorkbenchAdapter adapter = null;
                if (element instanceof IAdaptable adaptable) {
                    adapter = adaptable.getAdapter(IWorkbenchAdapter.class);
                }
                if (adapter == null) {
                    yield null;
                }
                ImageDescriptor descriptor = adapter.getImageDescriptor(element);
                yield descriptor == null ? null : resourceManager.get(descriptor);
            }
            default -> null;
        };
    }
    // CSON: CyclomaticComplexity

    // CSOFF: CyclomaticComplexity
    @Override
    public String getText(Object element) {
        return switch (element) {
            case IIpsElement ipsElement -> {
                if (element instanceof IIpsPackageFragment fragment) {
                    if (fragment.isDefaultPackage()) {
                        yield Messages.ModelExplorer_defaultPackageLabel;
                    }
                    if (isFlatLayout) {
                        yield fragment.getName();
                    } else {
                        String name = fragment.getName();
                        int index = name.lastIndexOf('.');
                        if (index == -1) {
                            yield name;
                        }
                        yield name.substring(index + 1);
                    }
                } else if (productDefinitionLabelProvider && element instanceof IIpsProject ipsProject
                        && !ipsProject.isProductDefinitionProject()) {
                    // if this label provider shows product definition aspects then show additional
                    // text
                    // for no product definition project
                    yield ipsProject.getName()
                            + NLS.bind(" ({0})", Messages.ModelLabelProvider_noProductDefinitionProjectLabel); //$NON-NLS-1$
                }
                yield defaultLabelProvider.getText(element);
            }
            case IProject project when project.isOpen() -> {
                String labelAddition = productDefinitionLabelProvider
                        ? Messages.ModelLabelProvider_noProductDefinitionProjectLabel
                        : Messages.ModelExplorer_nonIpsProjectLabel;
                yield ((IProject)element).getName() + NLS.bind(" ({0})", labelAddition); //$NON-NLS-1$

            }
            case IIpsObjectPathContainer container -> container.getName();
            case ReferencedIpsProjectViewItem viewItem -> viewItem.getName();
            case AAbstraction abstraction -> getText(abstraction.unwrap());
            default -> ((IResource)element).getName();
        };
    }
    // CSON: CyclomaticComplexity

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
