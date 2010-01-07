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
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * This decorator marks <tt>IIpsObject</tt>s, <tt>IIpsPackageFragment</tt>s,
 * <tt>IIpsPackageFragmentRoot</tt>s and <tt>IIpsProject</tt>s with warning and error icons if
 * problems are detected.
 * <p>
 * The <tt>IpsProblemsLabelDecorator</tt> is configurable for flat or hierarchical layout styles in
 * <tt>TreeViewer</tt>s.
 * 
 * @author Stefan Widmaier
 */
public class IpsProblemsLabelDecorator implements ILabelDecorator, ILightweightLabelDecorator {

    /**
     * Indicates if the LabelDecorator works with a flat or hierarchical view structure where
     * <tt>true</tt> means flat layout and <tt>false</tt> means hierarchical layout. Default is
     * <tt>false</tt> for use with the hierarchical <tt>ProductStructureExplorer</tt>.
     */
    private boolean isFlatLayout = false;

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ipsproblemsdecorator"; //$NON-NLS-1$

    private ArrayList<ILabelProviderListener> listeners = null;

    private final static int DEFAULT_FLAG = -1;

    private ResourceManager resourceManager;

    /**
     * {@inheritDoc}
     */
    public Image decorateImage(Image baseImage, Object element) {
        if (baseImage != null) {
            try {
                return (Image)getResourceManager().get(
                        IpsProblemOverlayIcon.createMarkerOverlayIcon(baseImage, findMaxProblemSeverity(element)));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return baseImage;
    }

    private int findMaxProblemSeverity(Object element) throws CoreException {
        IResource res = null;
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = ((IIpsElement)element);
            if (ipsElement != null) {
                if (ipsElement instanceof IIpsProject) {
                    return computeAdornmentFlagsProject((IIpsProject)ipsElement);
                } else {
                    // As we don't show ips source file in the user interface, but the ips object,
                    // we handle ips objects as ips source files.
                    // Added due to bug 1513
                    if (ipsElement instanceof IIpsObject) {
                        ipsElement = ipsElement.getParent();
                    }
                    // Following line changed from getEnclosingRessource to
                    // getCorrespondingRessource() due to bug 1500
                    // The special handling of ips obejcts parts in former version, was removed as
                    // parts return null
                    // as corresponding ressource. If for some reaseon we have to switch back to
                    // getEnclosingRessource()
                    // we must readd the special handling to ips object parts.
                    res = ipsElement.getCorrespondingResource();
                    if (res == null || !res.isAccessible()) {
                        return DEFAULT_FLAG;
                    }
                    if (isFlatLayout && !(element instanceof IIpsPackageFragmentRoot)) {
                        /*
                         * In flat layout every package fragment is represented in its own tree
                         * item, thus package fragments of parent folders should not be decorated.
                         * Only search the package fragments children (files) for problems, no
                         * search is needed in the tree of sub folders. PackageFragmentRoots on the
                         * other hand should always be decorated with the problem markers of their
                         * package fragments.
                         */
                        return res.findMaxProblemSeverity(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_ONE);
                    } else {
                        return res.findMaxProblemSeverity(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
                    }
                }
            } else {
                return DEFAULT_FLAG;
            }
        } else if (element instanceof IResource) {
            return ((IResource)element).findMaxProblemSeverity(IpsPlugin.PROBLEM_MARKER, false, IResource.DEPTH_ONE);
        } else {
            return DEFAULT_FLAG;
        }
    }

    /**
     * Collects the error flags of all <tt>IIpsPackageFragmentRoot</tt>s contained in the given ips
     * project and returns the resulting flag. This procedure makes sure no markers of the
     * underlying java-project are interpreted as problems of the ips project and erroneously
     * displayed by the decorator.
     */
    private int computeAdornmentFlagsProject(IIpsProject project) throws CoreException {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        int result = 0;
        for (int i = 0; i < roots.length; i++) {
            int flag = findMaxProblemSeverity(roots[i]);
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
    }

    /**
     * {@inheritDoc}
     */
    public String decorateText(String text, Object element) {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    public void addListener(ILabelProviderListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ILabelProviderListener>();
        }
        listeners.add(listener);

    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (resourceManager != null) {
            resourceManager.dispose();
        }
        listeners = null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void removeListener(ILabelProviderListener listener) {
        if (listener != null && listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decorate(Object element, IDecoration decoration) {
        try {
            decoration.addOverlay(IpsProblemOverlayIcon.getMarkerOverlay(findMaxProblemSeverity(element)));
        } catch (CoreException e) {
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
