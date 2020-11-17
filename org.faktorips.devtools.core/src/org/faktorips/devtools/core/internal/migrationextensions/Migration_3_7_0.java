/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.migration.DefaultMigration;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;

/**
 * Migration to version 3.7.0.
 * <p>
 * Adds generics to table's addRow method signature.
 * 
 * @since 3.7
 */
public class Migration_3_7_0 extends DefaultMigration {

    private IIpsArtefactBuilder[] artefactBuilders;

    public Migration_3_7_0(IIpsProject projectToMigrate, String featureId) {
        super(projectToMigrate, featureId);
        IIpsArtefactBuilderSet artefactBuilderSet = projectToMigrate.getIpsArtefactBuilderSet();
        artefactBuilders = artefactBuilderSet.getArtefactBuilders();
    }

    @Override
    protected void migrate(IIpsSrcFile srcFile) throws CoreException {
        IpsObjectType srcFileObjectType = srcFile.getIpsObjectType();
        if (srcFileObjectType.equals(IpsObjectType.TABLE_STRUCTURE)) {
            migrateTable(srcFile);
        }
        if (srcFileObjectType.equals(IpsObjectType.TEST_CASE_TYPE)) {
            migrateTestCaseType(srcFile);
        }
    }

    @SuppressWarnings("unchecked")
    private void migrateTestCaseType(IIpsSrcFile srcFile) throws CoreException, JavaModelException {
        for (IIpsArtefactBuilder artefactBuilder : artefactBuilders) {
            if (artefactBuilder.isBuilderFor(srcFile) && artefactBuilder instanceof DefaultJavaSourceFileBuilder) {
                DefaultJavaSourceFileBuilder defaultJavaSourceFileBuilder = (DefaultJavaSourceFileBuilder)artefactBuilder;
                IFile javaFile = defaultJavaSourceFileBuilder.getJavaFile(srcFile);
                ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(javaFile);
                String source = compilationUnit.getSource();
                Document document = new Document(source);

                // creation of DOM/AST from a ICompilationUnit
                ASTParser parser = ASTParser.newParser(AST.JLS11);
                parser.setSource(compilationUnit);
                CompilationUnit astRoot = (CompilationUnit)parser.createAST(null);

                // start record of the modifications
                astRoot.recordModifications();

                // modify the AST
                TypeDeclaration typeDeclaration = (TypeDeclaration)astRoot.types().get(0);
                TypeDeclaration[] types = typeDeclaration.getTypes();
                for (TypeDeclaration typeDeclaration2 : types) {
                    MethodDeclaration[] methods = typeDeclaration2.getMethods();
                    for (MethodDeclaration methodDeclaration : methods) {
                        if ("initProperties".equals(methodDeclaration.getName().toString())) { //$NON-NLS-1$
                            SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration)methodDeclaration
                                    .parameters().get(0);
                            if (variableDeclaration.getName().getIdentifier().equals("pathFromAggregateRoot")) { //$NON-NLS-1$
                                variableDeclaration = (SingleVariableDeclaration)methodDeclaration.parameters().get(2);
                                Type type = variableDeclaration.getType();
                                if (variableDeclaration.getName().getIdentifier().equals("propMap") //$NON-NLS-1$
                                        && !type.isParameterizedType()) {
                                    AST ast = astRoot.getAST();
                                    ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast
                                            .newName(Map.class.getSimpleName())));
                                    newType.typeArguments().add(
                                            ast.newSimpleType(ast.newName(String.class.getSimpleName())));
                                    newType.typeArguments().add(
                                            ast.newSimpleType(ast.newName(String.class.getSimpleName())));
                                    variableDeclaration.setType(newType);
                                }
                            }
                        }
                    }
                }

                // computation of the text edits
                TextEdit edits = astRoot.rewrite(document, compilationUnit.getJavaProject().getOptions(true));

                // computation of the new source code
                try {
                    edits.apply(document);
                } catch (MalformedTreeException e) {
                    throw new CoreException(new Status(Status.ERROR, IpsPlugin.PLUGIN_ID,
                            "Error parsing source file " + srcFile, e)); //$NON-NLS-1$
                } catch (BadLocationException e) {
                    throw new CoreException(new Status(Status.ERROR, IpsPlugin.PLUGIN_ID,
                            "Error parsing source file " + srcFile, e)); //$NON-NLS-1$
                }
                String newSource = document.get();

                // update of the compilation unit
                compilationUnit.getBuffer().setContents(newSource);
                compilationUnit.save(new NullProgressMonitor(), true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void migrateTable(IIpsSrcFile srcFile) throws CoreException, JavaModelException {
        for (IIpsArtefactBuilder artefactBuilder : artefactBuilders) {
            if (artefactBuilder.isBuilderFor(srcFile) && artefactBuilder instanceof DefaultJavaSourceFileBuilder) {
                DefaultJavaSourceFileBuilder defaultJavaSourceFileBuilder = (DefaultJavaSourceFileBuilder)artefactBuilder;
                IFile javaFile = defaultJavaSourceFileBuilder.getJavaFile(srcFile);
                ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(javaFile);
                String source = compilationUnit.getSource();
                Document document = new Document(source);

                // creation of DOM/AST from a ICompilationUnit
                ASTParser parser = ASTParser.newParser(AST.JLS11);
                parser.setSource(compilationUnit);
                CompilationUnit astRoot = (CompilationUnit)parser.createAST(null);

                // start record of the modifications
                astRoot.recordModifications();

                // modify the AST
                TypeDeclaration typeDeclaration = (TypeDeclaration)astRoot.types().get(0);
                MethodDeclaration[] methods = typeDeclaration.getMethods();
                for (MethodDeclaration methodDeclaration : methods) {
                    if ("addRow".equals(methodDeclaration.getName().toString())) { //$NON-NLS-1$
                        SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration)methodDeclaration
                                .parameters().get(0);
                        Type type = variableDeclaration.getType();
                        if (!type.isParameterizedType()) {
                            AST ast = astRoot.getAST();
                            ParameterizedType newType = ast.newParameterizedType(ast.newSimpleType(ast
                                    .newName(List.class.getSimpleName())));
                            newType.typeArguments().add(ast.newSimpleType(ast.newName(String.class.getSimpleName())));
                            variableDeclaration.setType(newType);
                        }
                    }
                }

                // computation of the text edits
                TextEdit edits = astRoot.rewrite(document, compilationUnit.getJavaProject().getOptions(true));

                // computation of the new source code
                try {
                    edits.apply(document);
                } catch (MalformedTreeException e) {
                    throw new CoreException(new Status(Status.ERROR, IpsPlugin.PLUGIN_ID,
                            "Error parsing source file " + srcFile, e)); //$NON-NLS-1$
                } catch (BadLocationException e) {
                    throw new CoreException(new Status(Status.ERROR, IpsPlugin.PLUGIN_ID,
                            "Error parsing source file " + srcFile, e)); //$NON-NLS-1$
                }
                String newSource = document.get();

                // update of the compilation unit
                compilationUnit.getBuffer().setContents(newSource);
                compilationUnit.save(new NullProgressMonitor(), true);
            }
        }
    }

    @Override
    public String getTargetVersion() {
        return "3.7.0"; //$NON-NLS-1$
    }

    @Override
    public String getDescription() {
        return Messages.Migration_3_7_0_description;
    }

    public static class Factory implements IIpsProjectMigrationOperationFactory {

        @Override
        public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                String featureId) {
            return new Migration_3_7_0(ipsProject, featureId);
        }

    }

}
