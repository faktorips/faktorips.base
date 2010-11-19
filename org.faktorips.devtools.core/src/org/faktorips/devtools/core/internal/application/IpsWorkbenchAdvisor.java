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

package org.faktorips.devtools.core.internal.application;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;
import org.osgi.framework.Bundle;

/**
 * The workbench-advisor for FaktorIps as own product. This class is a simpler version of the
 * internal Eclipse class IDEWorkbenchAdvisor.
 * 
 * @author Thorsten Guenther
 */
class IpsWorkbenchAdvisor extends WorkbenchAdvisor {

    @Override
    public void initialize(IWorkbenchConfigurer configurer) {

        // make sure we always save and restore workbench state
        // note, this does not save the workspace(!) state. This has to be handled explicitly in
        // postShutdown!
        configurer.setSaveAndRestore(true);

        // setup the event loop exception handler
        // TODO do we need this? exceptionHandler = new IDEExceptionHandler(configurer);
        // register workspace adapters
        registerWorkspaceAdapters();

        // register shared images
        declareWorkbenchImages();

        // initialize idle handler
        // idleHelper = new IDEIdleHelper(configurer); do we need this or not?

        // disbale help button in JFace dialogs
        TrayDialog.setDialogHelpAvailable(false);

        // use this image for the help button in dialogs
        ImageRegistry reg = JFaceResources.getImageRegistry();
        reg.put(Dialog.DLG_IMG_HELP,
                IDEInternalWorkbenchImages.getImageDescriptor(IDEInternalWorkbenchImages.IMG_LCL_LINKTO_HELP));

    }

    private void registerWorkspaceAdapters() {
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
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID;
    }

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new IpsWorkbenchWindowAdvisor(configurer);
    }

    /**
     * Declares all IDE-specific workbench images. This includes both "shared" images (named in
     * {@link IDE.SharedImages})
     * 
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImages() {

        final String ICONS_PATH = "$nl$/icons/full/";//$NON-NLS-1$
        final String PATH_ELOCALTOOL = ICONS_PATH + "elcl16/"; // Enabled  toolbar icons.//$NON-NLS-1$
        final String PATH_DLOCALTOOL = ICONS_PATH + "dlcl16/"; // Disabled  toolbar icons.//$NON-NLS-1$
        final String PATH_ETOOL = ICONS_PATH + "etool16/"; // Enabled toolbar icons.//$NON-NLS-1$
        final String PATH_DTOOL = ICONS_PATH + "dtool16/"; // Disabled toolbar icons.//$NON-NLS-1$
        final String PATH_OBJECT = ICONS_PATH + "obj16/"; // Model object icons//$NON-NLS-1$
        final String PATH_WIZBAN = ICONS_PATH + "wizban/"; // Wizard icons//$NON-NLS-1$

        Bundle ideBundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC,
                PATH_ETOOL + "build_exec.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_HOVER, PATH_ETOOL
                + "build_exec.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_BUILD_EXEC_DISABLED, PATH_DTOOL
                + "build_exec.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC,
                PATH_ETOOL + "search_src.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_HOVER, PATH_ETOOL
                + "search_src.gif", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_SEARCH_SRC_DISABLED, PATH_DTOOL
                + "search_src.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_NEXT_NAV,
                PATH_ETOOL + "next_nav.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PREVIOUS_NAV,
                PATH_ETOOL + "prev_nav.gif", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWPRJ_WIZ, PATH_WIZBAN
                + "newprj_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFOLDER_WIZ, PATH_WIZBAN
                + "newfolder_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_NEWFILE_WIZ, PATH_WIZBAN
                + "newfile_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTDIR_WIZ, PATH_WIZBAN
                + "importdir_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_IMPORTZIP_WIZ, PATH_WIZBAN
                + "importzip_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTDIR_WIZ, PATH_WIZBAN
                + "exportdir_wiz.png", false); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_EXPORTZIP_WIZ, PATH_WIZBAN
                + "exportzip_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ, PATH_WIZBAN
                + "workset_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLGBAN_SAVEAS_DLG, PATH_WIZBAN
                + "saveas_wiz.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLGBAN_QUICKFIX_DLG, PATH_WIZBAN
                + "quick_fix.png", false); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT, PATH_OBJECT + "prj_obj.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED, PATH_OBJECT + "cprj_obj.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OPEN_MARKER, PATH_ELOCALTOOL + "gotoobj_tsk.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ELCL_QUICK_FIX_ENABLED, PATH_ELOCALTOOL
                + "smartmode_co.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_DLCL_QUICK_FIX_DISABLED, PATH_DLOCALTOOL
                + "smartmode_co.gif", true); //$NON-NLS-1$

        // task objects
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_HPRIO_TSK,
        // PATH_OBJECT+"hprio_tsk.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_MPRIO_TSK,
        // PATH_OBJECT+"mprio_tsk.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LPRIO_TSK,
        // PATH_OBJECT+"lprio_tsk.gif");

        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_TASK_TSK, PATH_OBJECT + "taskmrk_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDE.SharedImages.IMG_OBJS_BKMRK_TSK, PATH_OBJECT + "bkmrk_tsk.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_COMPLETE_TSK, PATH_OBJECT
                + "complete_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_INCOMPLETE_TSK, PATH_OBJECT
                + "incomplete_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM, PATH_OBJECT
                + "welcome_item.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER, PATH_OBJECT
                + "welcome_banner.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_ERROR_PATH,
                PATH_OBJECT + "error_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_WARNING_PATH,
                PATH_OBJECT + "warn_tsk.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_OBJS_INFO_PATH,
                PATH_OBJECT + "info_tsk.gif", true); //$NON-NLS-1$

        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_LCL_FLAT_LAYOUT, PATH_ELOCALTOOL
                + "flatLayout.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_LCL_HIERARCHICAL_LAYOUT, PATH_ELOCALTOOL
                + "hierarchicalLayout.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_ETOOL_PROBLEM_CATEGORY, PATH_ETOOL
                + "problem_category.gif", true); //$NON-NLS-1$
        declareWorkbenchImage(ideBundle, IDEInternalWorkbenchImages.IMG_LCL_LINKTO_HELP, PATH_ELOCALTOOL
                + "linkto_help.gif", false); //$NON-NLS-1$

        // synchronization indicator objects
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_WBET_STAT,
        // PATH_OVERLAY+"wbet_stat.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_SBET_STAT,
        // PATH_OVERLAY+"sbet_stat.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_CONFLICT_STAT,
        // PATH_OVERLAY+"conflict_stat.gif");

        // content locality indicator objects
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_NOTLOCAL_STAT,
        // PATH_STAT+"notlocal_stat.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_LOCAL_STAT,
        // PATH_STAT+"local_stat.gif");
        // declareRegistryImage(IDEInternalWorkbenchImages.IMG_OBJS_FILLLOCAL_STAT,
        // PATH_STAT+"filllocal_stat.gif");
    }

    /**
     * Declares an IDE-specific workbench image.
     * 
     * @param symbolicName the symbolic name of the image
     * @param path the path of the image file; this path is relative to the base of the IDE plug-in
     * @param shared <code>true</code> if this is a shared image, and <code>false</code> if this is
     *            not a shared image
     * @see IWorkbenchConfigurer#declareImage
     */
    private void declareWorkbenchImage(Bundle ideBundle, String symbolicName, String path, boolean shared) {
        URL url = FileLocator.find(ideBundle, new Path(path), null);
        ImageDescriptor desc = ImageDescriptor.createFromURL(url);
        getWorkbenchConfigurer().declareImage(symbolicName, desc, shared);
    }

    @Override
    public void postShutdown() {
        if (ResourcesPlugin.getWorkspace() != null) {
            disconnectFromWorkspace();
        }
    }

    /**
     * Disconnect from the core workspace.
     * 
     * See Eclipse's IDEWorkbenchAdvisor for more details.
     */
    private void disconnectFromWorkspace() {
        // save the workspace (this has to be coded as the rcp workbench advisor is not aware of the
        // workspace! The workspace is an IDE concept!
        final MultiStatus status = new MultiStatus(IpsPlugin.PLUGIN_ID, 1, Messages.ProblemsSavingWorkspace, null);
        IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) {
                try {
                    status.merge(ResourcesPlugin.getWorkspace().save(true, monitor));
                } catch (CoreException e) {
                    status.merge(e.getStatus());
                }
            }
        };
        try {
            // yes it is internal, but you would need to copy a lot of classes to get the same
            // functionality!
            new ProgressMonitorJobsDialog(null).run(true, false, runnable);
        } catch (InvocationTargetException e) {
            status.merge(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 1, "Internal Error", e.getTargetException()));
        } catch (InterruptedException e) {
            status.merge(new Status(IStatus.ERROR, IpsPlugin.PLUGIN_ID, 1, "Internal Error", e));
        }
        ErrorDialog.openError(null, Messages.ProblemsSavingWorkspace, null, status, IStatus.ERROR | IStatus.WARNING);
        if (!status.isOK()) {
            IpsPlugin.log(status);
        }
    }

}
