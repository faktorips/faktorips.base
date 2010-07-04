package org.faktorips.devtools.htmlexport.test.standard;


import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

public class StandardDocumentorScriptTutorialProjectTest extends XmlAbstractTestCase {

	protected String zielpfad;
	protected IIpsProject ipsProject;
	protected DocumentorConfiguration documentorConfig;
	protected HtmlExportOperation documentor;

	private IWorkspace workspace;
	
	public StandardDocumentorScriptTutorialProjectTest() throws Exception {
    	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);

	}
	/*
    @Override
    protected void setUp() throws Exception {
    	IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                IpsPlugin.getDefault().reinitModel(); // also starts the listening process
            }
        };
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, null);
    }
	*/
	
	private void setUpProject(IWorkspace workspace, String projectName) {
		IProject project = workspace.getRoot().getProject(projectName);

		IpsModel model = new IpsModel();
		ipsProject = model.getIpsProject(project);

		documentorConfig = new DocumentorConfiguration();
		
        // So werden Dateien im Projekt gespeichert
		IPath location = ipsProject.getProject().getLocation();
		
		documentorConfig.setPath(location + File.separator + "html");
		documentorConfig.setIpsProject(ipsProject);
		documentorConfig.setLayouter(new HtmlLayouter(".resource"));

		documentor = new HtmlExportOperation(documentorConfig);
        
        documentorConfig.addDocumentorScript(new StandardDocumentorScript());
        documentorConfig.setLinkedIpsObjectTypes(documentorConfig.getIpsProject().getIpsModel().getIpsObjectTypes());
	}

    public void testWriteWithoutExceptionHausratmodell() throws Exception  {
		String projectName = "org.faktorips.tutorial.de.Hausratmodell";
		setUpProject(workspace, projectName);
    	
        deletePreviousGeneratedFiles();
        
        long start = System.currentTimeMillis();
        documentor.run(new NullProgressMonitor());
        System.out.println("=====================\nMODELL: " + (System.currentTimeMillis() - start) + "\n=====================");
    }

    public void testWriteWithoutExceptionHausratprodukte() throws Exception  {
		String projectName = "org.faktorips.tutorial.de.Hausratprodukte";
		setUpProject(workspace, projectName);
    	
        deletePreviousGeneratedFiles();

        long start = System.currentTimeMillis();
        documentor.run(new NullProgressMonitor());
        System.out.println("=====================\nPRODUKTE: " + (System.currentTimeMillis() - start) + "\n=====================");
    }

	protected void deletePreviousGeneratedFiles() {
		File file = new File(documentorConfig.getPath());
		if (file.exists()) {
			file.delete();
		}
	}
}
