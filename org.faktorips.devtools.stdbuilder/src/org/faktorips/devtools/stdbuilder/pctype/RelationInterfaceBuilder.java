
package org.faktorips.devtools.stdbuilder.pctype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

public abstract class RelationInterfaceBuilder extends RelationBuilder {

    public RelationInterfaceBuilder(IPolicyCmptType pcType, BaseJavaSourceFileBuilder sourceFileBuilder) {
        super(pcType, sourceFileBuilder);
    }

    protected void buildContainerRelation(IRelation containerRelation, IRelation[] subRelations) throws CoreException {
        buildRelations(getContainerRelations(getPcType()));
    }

}
