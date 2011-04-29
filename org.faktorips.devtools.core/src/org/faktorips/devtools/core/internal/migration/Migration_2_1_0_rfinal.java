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

package org.faktorips.devtools.core.internal.migration;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetInfo;
import org.faktorips.devtools.core.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * Migration from Faktor-IPS Version 2.1.0.rfinal to 2.2.0.rfinal
 * 
 * @author Peter Erzberger
 */
@SuppressWarnings("restriction")
public class Migration_2_1_0_rfinal extends AbstractIpsProjectMigrationOperation {

    public Migration_2_1_0_rfinal(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
    }

    @Override
    public String getDescription() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getTargetVersion() {
        return "2.2.0.rc1"; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public MessageList migrate(final IProgressMonitor monitor) throws CoreException {
        renameExecRuleMethods(monitor);
        updateIpsProjectFile();
        return new MessageList();
    }

    private void updateIpsProjectFile() throws CoreException {
        IIpsProjectProperties properties = getIpsProject().getProperties();
        IIpsArtefactBuilderSetInfo builderSetInfo = getIpsProject().getIpsModel().getIpsArtefactBuilderSetInfo(
                properties.getBuilderSetId());
        IIpsArtefactBuilderSetConfigModel builderSetConfig = properties.getBuilderSetConfig();
        for (IIpsBuilderSetPropertyDef propertyDef : builderSetInfo.getPropertyDefinitions()) {
            String value = builderSetConfig.getPropertyValue(propertyDef.getName());
            if (StringUtils.isEmpty(value)) {
                builderSetConfig.setPropertyValue(propertyDef.getName(), propertyDef.getDefaultValue(getIpsProject()),
                        propertyDef.getDescription());
            }
        }
        getIpsProject().setProperties(properties);
    }

    /**
     * Renames the execRule-Methods so that the execRule part is removed and the Method starts with
     * a lower case. Next the method body of the execRule-Methods a copied to the new methods and
     * the execRule-Methods are deleted.
     */
    private void renameExecRuleMethods(final IProgressMonitor monitor) throws CoreException {
        Job job = new Job("Migration220") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    for (IPackageFragmentRoot root : getIpsProject().getJavaProject().getPackageFragmentRoots()) {
                        for (IJavaElement javaElement : root.getChildren()) {
                            if (javaElement instanceof IPackageFragment) {
                                IPackageFragment fragment = (IPackageFragment)javaElement;
                                for (IJavaElement packageEl : fragment.getChildren()) {
                                    if (packageEl instanceof ICompilationUnit) {
                                        ICompilationUnit cu = (ICompilationUnit)packageEl;
                                        IType type = cu.findPrimaryType();
                                        if (type instanceof SourceType) {
                                            ASTParser parser = ASTParser.newParser(AST.JLS3);
                                            parser.setSource(cu);
                                            CompilationUnit rootNode = (CompilationUnit)parser.createAST(monitor);
                                            rootNode.recordModifications();
                                            AbstractTypeDeclaration abstractDeclaration = (AbstractTypeDeclaration)rootNode
                                                    .types().get(0);
                                            boolean modified = false;
                                            if (abstractDeclaration instanceof TypeDeclaration) {
                                                TypeDeclaration typeDecl = (TypeDeclaration)abstractDeclaration;
                                                for (MethodDeclaration methodDecl : typeDecl.getMethods()) {
                                                    String methodName = methodDecl.getName().getFullyQualifiedName();
                                                    if (methodName.startsWith("execRule")) { //$NON-NLS-1$
                                                        String newMethodName = StringUtils.uncapitalize(methodName
                                                                .substring("execRule".length(), methodName.length())); //$NON-NLS-1$
                                                        for (MethodDeclaration methodDecl2 : typeDecl.getMethods()) {
                                                            if (methodDecl2.getName().getFullyQualifiedName()
                                                                    .equals(newMethodName)) {
                                                                ASTNode copiedBody = ASTNode.copySubtree(
                                                                        methodDecl2.getAST(), methodDecl.getBody());
                                                                methodDecl2.setBody((Block)copiedBody);
                                                                methodDecl.delete();
                                                                modified = true;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (modified) {
                                                Document document = new Document(cu.getSource());
                                                TextEdit edits = rootNode.rewrite(document, cu.getJavaProject()
                                                        .getOptions(true));
                                                try {
                                                    edits.apply(document);
                                                    cu.getBuffer().setContents(document.get());
                                                    cu.save(monitor, true);
                                                } catch (Exception e) {
                                                    return new IpsStatus(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    return new IpsStatus(e);
                }
                return new IpsStatus(IStatus.OK, ""); //$NON-NLS-1$
            }
        };
        getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
        job.setPriority(Job.BUILD);
        job.setRule(getIpsProject().getProject());
        job.schedule(5000);

        job = new Job("Migration220PostBuild") { //$NON-NLS-1$

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    getIpsProject().getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                } catch (CoreException e) {
                    return new IpsStatus(e);
                }
                return new IpsStatus(IStatus.OK, ""); //$NON-NLS-1$
            }
        };
        job.setPriority(Job.BUILD);
        job.schedule(6000);
    }

}
