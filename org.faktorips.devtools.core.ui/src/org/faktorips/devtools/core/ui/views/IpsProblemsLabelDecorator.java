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
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.faktorips.devtools.core.ImageDescriptorRegistry;
import org.faktorips.devtools.core.ImageImageDescriptor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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

    private static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];

    /**
     * Indicates if the LabelDecorator works with a flat or hierarchical view structure where
     * <tt>true</tt> means flat layout and <tt>false</tt> means hierarchical layout. Default is
     * <tt>false</tt> for use with the hierarchical <tt>ProductStructureExplorer</tt>.
     */
    private boolean isFlatLayout = false;

    public static final String EXTENSION_ID = "org.faktorips.devtools.core.ipsproblemsdecorator"; //$NON-NLS-1$

    private ImageDescriptorRegistry registry = null;

    private ArrayList<ILabelProviderListener> listeners = null;

    /**
     * {@inheritDoc}
     */
    public Image decorateImage(Image image, Object element) {
        if (image != null) {
            try {
                ImageDescriptor baseImage = new ImageImageDescriptor(image);
                Rectangle bounds = image.getBounds();
                return getRegistry().get(
                        new JavaElementImageDescriptor(baseImage, computeAdornmentFlags(element), new Point(
                                bounds.width, bounds.height)));
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        return image;
    }

    private int computeAdornmentFlags(Object element) throws CoreException {
        int flag = 0;
        IMarker[] markers = EMPTY_MARKER_ARRAY;
        IResource res = null;
        if (element instanceof IIpsElement) {
            IIpsElement ipsElement = ((IIpsElement)element);
            if (ipsElement != null) {
                if (ipsElement instanceof IIpsProject) {
                    return computeAdornmentFlagsProject((IIpsProject)ipsElement);
                } else {
                    // Following line changed from getEnclosingRessource to getCorrespondingRessource() due to bug 1500
                    // The special handling of ips obejcts parts in former version, was removed as parts return null
                    // as corresponding ressource. If for some reaseon we have to switch back to getEnclosingRessource()
                    // we must readd the special handling to ips object parts.
                    res = ipsElement.getCorrespondingResource();
                    if (res == null || !res.isAccessible()) {
                        return 0;
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
                        markers = res.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_ONE);
                    } else {
                        markers = res.findMarkers(IpsPlugin.PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
                    }
                }
            }
        } else if (element instanceof IResource) {
            markers = ((IResource)element).findMarkers(IpsPlugin.PROBLEM_MARKER, false, IResource.DEPTH_ONE);
        } else {
            return 0;
        }

        for (int i = 0; i < markers.length && (flag != JavaElementImageDescriptor.ERROR); i++) {
            if (markers[i].exists()) {
                int prio = markers[i].getAttribute(IMarker.SEVERITY, -1);
                if (prio == IMarker.SEVERITY_WARNING) {
                    flag = JavaElementImageDescriptor.WARNING;
                } else if (prio == IMarker.SEVERITY_ERROR) {
                    flag = JavaElementImageDescriptor.ERROR;
                }
            }
        }
        return flag;
    }

    /**
     * Collects the error flags of all <tt>IIpsPackageFragmentRoot</tt>s contained in the given ips
     * project and returns the resulting flag. This procedure makes sure no markers of the
     * underlying java-project are interpreted as problems of the ips project and erroneously
     * displayed by the decorator.
     */
    private int computeAdornmentFlagsProject(IIpsProject project) throws CoreException {
        IIpsPackageFragmentRoot[] roots = project.getIpsPackageFragmentRoots();
        int flag = 0;
        for (int i = 0; i < roots.length; i++) {
            flag = flag | computeAdornmentFlags(roots[i]);
        }

        // Check for errors in .ipsproject file.
        flag = flag | computeAdornmentFlags(project.getIpsProjectPropertiesFile());

        return flag;
    }

    private ImageDescriptorRegistry getRegistry() {
        if (registry == null) {
            this.registry = new ImageDescriptorRegistry();
        }
        return this.registry;
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
        if (registry != null) {
            registry.dispose();
            registry = null;
        }
        this.listeners = null;
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
            this.listeners.remove(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decorate(Object element, IDecoration decoration) {
        try {
            int adornmentFlags = computeAdornmentFlags(element);
            if ((adornmentFlags & JavaElementImageDescriptor.ERROR) == JavaElementImageDescriptor.ERROR) {
                decoration.addOverlay(IpsUIPlugin.getDefault().getImageDescriptor("ovr16/error_co.gif")); //$NON-NLS-1$
            } else if ((adornmentFlags & JavaElementImageDescriptor.WARNING) == JavaElementImageDescriptor.WARNING) {
                decoration.addOverlay(IpsPlugin.getDefault().getImageDescriptor("ovr16/warning_co.gif")); //$NON-NLS-1$
            }
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
}
