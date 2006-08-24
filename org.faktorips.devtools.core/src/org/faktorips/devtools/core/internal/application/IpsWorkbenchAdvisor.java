/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.application;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

/**
 * The workbench-advisor for FaktorIps as own product.
 * 
 * @author Thorsten Guenther
 */
class IpsWorkbenchAdvisor extends WorkbenchAdvisor {

	public void initialize(IWorkbenchConfigurer configurer) {
		
		IAdapterManager manager = Platform.getAdapterManager();
        IAdapterFactory factory = new ResourceAdapterFactory();
        manager.registerAdapters(factory, IWorkspace.class);
        manager.registerAdapters(factory, IWorkspaceRoot.class);
        manager.registerAdapters(factory, IProject.class);
        manager.registerAdapters(factory, IFolder.class);
        manager.registerAdapters(factory, IFile.class);
        manager.registerAdapters(factory, IMarker.class);

        factory = new PropertyAdapterFactory();
        manager.registerAdapters(factory, IWorkspace.class);
        manager.registerAdapters(factory, IWorkspaceRoot.class);
        manager.registerAdapters(factory, IProject.class);
        manager.registerAdapters(factory, IFolder.class);
        manager.registerAdapters(factory, IFile.class);
        manager.registerAdapters(factory, IMarker.class);

        declareWorkbenchImages();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInitialWindowPerspectiveId() {
		return "org.faktorips.devtools.core.productDefinitionPerspective"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		configurer.setShowFastViewBars(false);
		configurer.setShowPerspectiveBar(false);
		configurer.setTitle(Messages.IpsWorkbenchAdvisor_title);
		return new IpsWorkbenchWindowAdvisor(configurer);
	}

    /**
     * Declares all IDE-specific workbench images. This includes both "shared"
     * images (named in {@link IDE.SharedImages}) 
     * 
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImages() {

        final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; //Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; //Model object icons//$NON-NLS-1$

        Bundle ideBundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
                PATH_OBJECT + "prj_obj.gif"); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle,
                IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT
                        + "cprj_obj.gif"); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER,
                PATH_ELOCALTOOL + "gotoobj_tsk.gif"); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK,
                PATH_OBJECT + "taskmrk_tsk.gif"); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK,
                PATH_OBJECT + "bkmrk_tsk.gif"); //$NON-NLS-1$
    }

    /**
     * Declares an IDE-specific workbench image.
     * 
     * @param symbolicName the symbolic name of the image
     * @param path the path of the image file; this path is relative to the base
     * of the IDE plug-in
     * @param shared <code>true</code> if this is a shared image, and
     * <code>false</code> if this is not a shared image
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImage(Bundle ideBundle, String symbolicName,
            String path) {
		URL url = Platform.find(ideBundle, new Path(path));
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, true);
    }
}