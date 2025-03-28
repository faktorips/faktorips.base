package org.faktorips.devtools.model.builder.java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.ExtendedExprCompiler;
import org.faktorips.devtools.model.builder.fl.StandardIdentifierResolver;
import org.faktorips.devtools.model.builder.naming.BuilderAspect;
import org.faktorips.devtools.model.builder.settings.ValueSetMethods;
import org.faktorips.devtools.model.builder.xmodel.table.XTable;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IBuilderKindId;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.ArgumentCheck;

/**
 * An {@link IIpsArtefactBuilderSet} implementation that is used in a PlainJavaIpsModel.
 */
public class ModelBuilderSet extends JavaBuilderSet {

    public static final String ID = "org.faktorips.devtools.model.builder.modelbuilderset";

    public ModelBuilderSet() {
    }

    public ModelBuilderSet(IIpsProject project) {
        setIpsProject(project);
    }

    @Override
    public boolean isSupportTableAccess() {
        return true;
    }

    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) {

        Datatype returnType = fct.getIpsProject().findDatatype(fct.getType());
        JavaCodeFragment code = new JavaCodeFragment();
        ITableStructure tableStructure = fct.getTableStructure();

        CompilationResultImpl result = new CompilationResultImpl(code, returnType);
        code.appendClassName(getModelNode(tableStructure, XTable.class).getQualifiedName(BuilderAspect.IMPLEMENTATION));
        // create get instance method by using the qualified name of the table content
        code.append(".getInstance(" + MethodNames.GET_THIS_REPOSITORY + "(), \"" + tableContentsQualifiedName //$NON-NLS-1$ //$NON-NLS-2$
                + "\").findRowNullRowReturnedForEmptyResult("); //$NON-NLS-1$

        for (int i = 0; i < argResults.length; i++) {
            if (i > 0) {
                code.append(", "); //$NON-NLS-1$
            }
            code.append(argResults[i].getCodeFragment());
            result.addMessages(argResults[i].getMessages());
        }
        code.append(").get"); //$NON-NLS-1$
        code.append(StringUtils.capitalize(fct.getAccessedColumn().getName()));
        code.append("()"); //$NON-NLS-1$

        return result;
    }

    @Override
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) {
        if (exprCompiler instanceof ExtendedExprCompiler) {
            return new StandardIdentifierResolver(formula, (ExtendedExprCompiler)exprCompiler, this);
        } else {
            throw new RuntimeException(
                    "Illegal expression compiler, only ExtendedExpressionCompiler is allowed but found "
                            + exprCompiler.getClass());
        }
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return true;
    }

    @Override
    protected LinkedHashMap<IBuilderKindId, IIpsArtefactBuilder> createBuilders() {
        return new LinkedHashMap<>();
    }

    @Override
    public List<IJavaElement> getGeneratedJavaElements(IIpsObjectPartContainer ipsObjectPartContainer) {
        ArgumentCheck.notNull(ipsObjectPartContainer);
        List<IJavaElement> javaElements = new ArrayList<>();
        for (IIpsArtefactBuilder builder : getArtefactBuilders()) {
            IIpsArtefactBuilder builderTemp = builder;
            if (!(builderTemp instanceof JavaSourceFileBuilder javaBuilder)) {
                continue;
            }
            IIpsSrcFile ipsSrcFile = ipsObjectPartContainer.getAdapter(IIpsSrcFile.class);
            if (javaBuilder.isBuilderFor(ipsSrcFile)) {
                javaElements.addAll(javaBuilder.getGeneratedJavaElements(ipsObjectPartContainer));
            }
        }

        return javaElements;
    }

    @Override
    public boolean usesUnifiedValueSets() {
        return ValueSetMethods.Unified.name().equals(
                getConfig().getPropertyValueAsString(CONFIG_PROPERTY_UNIFY_VALUE_SET_METHODS));
    }
}
