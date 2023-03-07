/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * This decorator marks <code>IIpsObject</code>s, <code>IIpsPackageFragment</code>s,
 * <code>IIpsPackageFragmentRoot</code>s and <code>IIpsProject</code>s with warning and error icons
 * if problems are detected.
 * <p>
 * The <code>IpsProblemsLabelDecorator</code> is configurable for flat or hierarchical layout styles
 * in <code>TreeViewer</code>s.
 * 
 * @author Stefan Widmaier
 */
public class IpsProblemsLabelDecorator implements ILabelDecorator, ILightweightLabelDecorator {

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ipsproblemsdecorator"; //$NON-NLS-1$

    private static final int DEFAULT_FLAG = -1;

    /**
     * Indicates if the LabelDecorator works with a flat or hierarchical view structure where
     * <code>true</code> means flat layout and <code>false</code> means hierarchical layout. Default
     * is <code>false</code> for use with the hierarchical <code>ProductStructureExplorer</code>.
     */
    private boolean isFlatLayout = false;

    private ArrayList<ILabelProviderListener> listeners = null;

    private ResourceManager resourceManager;

    @Override
    public Image decorateImage(Image baseImage, Object element) {
        if (baseImage != null) {
            try {
                return (Image)getResourceManager().get(
                        IpsProblemOverlayIcon.createMarkerOverlayIcon(baseImage, findMaxProblemSeverity(element)));
            } catch (IpsException e) {
                IpsPlugin.log(e);
            }
        }
        return baseImage;
    }

    private int findMaxProblemSeverity(Object element) {
        if (element instanceof IIpsElement ipsElement) {
            if (ipsElement instanceof IIpsProject) {
                return computeAdornmentFlagsProject((IIpsProject)ipsElement);
            } else {
                // As we don't show IPS source file in the user interface, but the IPS object,
                // we handle IPS objects as IPS source files.
                // Added due to bug 1513
                if (ipsElement instanceof IIpsObject) {
                    ipsElement = ipsElement.getParent();
                }
                // Following line changed from getEnclosingRessource to
                // getCorrespondingRessource() due to bug 1500
                // The special handling of IPS objects parts in former version, was removed as
                // parts return null
                // as corresponding resource. If for some reason we have to switch back to
                // getEnclosingRessource()
                // we must read the special handling to IPS object parts.
                IResource res = Wrappers.unwrap(ipsElement.getCorrespondingResource());
                if (res == null || !res.isAccessible()) {
                    return DEFAULT_FLAG;
                }

                /*
                 * In flat layout every package fragment is represented in its own tree item, thus
                 * package fragments of parent folders should not be decorated. Only search the
                 * package fragments children (files) for problems, no search is needed in the tree
                 * of sub folders. PackageFragmentRoots on the other hand should always be decorated
                 * with the problem markers of their package fragments.
                 */
                int depth = IResource.DEPTH_INFINITE;
                if (ipsElement instanceof IIpsPackageFragment packageFragment) {
                    if (packageFragment.isDefaultPackage() || isFlatLayout) {
                        depth = IResource.DEPTH_ONE;
                    }
                }
                try {
                    return res.findMaxProblemSeverity(IpsBuilder.PROBLEM_MARKER, true, depth);
                } catch (CoreException e) {
                    throw new IpsException(e);
                }
            }
        } else if (element instanceof IResource resource) {
            if (resource.isAccessible()) {
                try {
                    return resource.findMaxProblemSeverity(IpsBuilder.PROBLEM_MARKER, false, IResource.DEPTH_ONE);
                } catch (CoreException e) {
                    throw new IpsException(e);
                }
            } else {
                return DEFAULT_FLAG;
            }
        } else {
            return DEFAULT_FLAG;
        }
    }

    /**
     * Collects the error flags of all <code>IIpsPackageFragmentRoot</code>s contained in the given
     * ips project and returns the resulting flag. This procedure makes sure no markers of the
     * underlying java-project are interpreted as problems of the ips project and erroneously
     * displayed by the decorator.
     */
    private int computeAdornmentFlagsProject(IIpsProject project) {
        if (project.getProject().isAccessible()) {
            IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
            int result = 0;
            for (IIpsPackageFragmentRoot root : roots) {
                int flag = findMaxProblemSeverity(root);
                if (flag == IMarker.SEVERITY_ERROR) {
                    return flag;
                } else {
                    result |= flag;
                }
            }

            // Check for errors in .ipsproject file.
            int flag = findMaxProblemSeverity(project.getIpsProjectPropertiesFile());
            if (flag == IMarker.SEVERITY_ERROR) {
                return flag;
            }
            return result |= flag;
        } else {
            return DEFAULT_FLAG;
        }
    }

    @Override
    public String decorateText(String text, Object element) {
        return text;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);

    }

    @Override
    public void dispose() {
        if (resourceManager != null) {
            resourceManager.dispose();
        }
        listeners = null;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public void decorate(Object element, IDecoration decoration) {
        try {
            decoration.addOverlay(IpsProblemOverlayIcon.getMarkerOverlay(findMaxProblemSeverity(element)));
        } catch (IpsException e) {
            IpsPlugin.log(e);
        }
    }

    /**
     * Sets the layout style the decorator should work on.
     */
    public void setFlatLayout(boolean isFlatLayout) {
        this.isFlatLayout = isFlatLayout;
    }

    private ResourceManager getResourceManager() {
        // Lazy load because the decorator is instantiated before JFaceResources
        if (resourceManager == null) {
            resourceManager = new LocalResourceManager(JFaceResources.getResources());
        }
        return resourceManager;
    }
}
