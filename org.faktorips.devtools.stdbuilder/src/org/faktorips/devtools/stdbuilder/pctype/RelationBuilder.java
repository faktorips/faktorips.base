package org.faktorips.devtools.stdbuilder.pctype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * RelationBuilder is a helper class for the faktorips code generation,
 * responsible for code related to relation handling
 */
public abstract class RelationBuilder {  
    
    private IPolicyCmptType pcType;
    private BaseJavaSourceFileBuilder sourceFileBuilder;
    
    public RelationBuilder(IPolicyCmptType pcType, BaseJavaSourceFileBuilder sourceFileBuilder) {
        this.pcType = pcType;
        this.sourceFileBuilder = sourceFileBuilder;
    }

    protected abstract void buildRelation(IRelation relation) throws CoreException;

    protected abstract void buildContainerRelation(IRelation containerRelation, IRelation[] subRelations) 
        throws CoreException;

    protected void log(IStatus status){
        getCuBuilder().addToBuildStatus(status);
    }

    protected IPolicyCmptType getPcType() {
        return pcType;
    }

    protected BaseJavaSourceFileBuilder getCuBuilder(){
        return sourceFileBuilder;
    }

    public List getContainerRelationsAsList(IPolicyCmptType pcType) throws CoreException{
        IRelation[] relations = pcType.getRelations();
        ArrayList list = new ArrayList();
        for (int i = relations.length - 1; i >= 0; i--) {
            String containerRelationName = relations[i].getContainerRelation();
            if(StringUtils.isNotEmpty(containerRelationName)) {
                IRelation containerRelation = findContainerRelation(pcType, containerRelationName);
                if(containerRelation != null && ! list.contains(containerRelation)) {
                    list.add(containerRelation);
                }
            }
        }
        return list;
    }
    
    public IRelation[] getContainerRelations(IPolicyCmptType pcType) throws CoreException {
        List relations = getContainerRelationsAsList(pcType);
        return (IRelation[]) relations.toArray(new IRelation[relations.size()]);
    }

    void buildContainerRelations() throws CoreException {
        HashMap map = groupByContainerRelation(pcType.getRelations());
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            Collection al = (Collection)entry.getValue();
            IRelation[] subRelations = (IRelation[])al.toArray(new IRelation[al.size()]);
            String containerRelationName = (String)entry.getKey();
            IRelation containerRelation = findContainerRelation(pcType, containerRelationName);
            if (containerRelation == null) {
                log(new IpsStatus("Error building methods for container relation "
                        + containerRelationName + " and class " + pcType.getJavaClassName()
                        + ": container relation not found"));
            }
            try {
                buildContainerRelation(containerRelation, subRelations);
            } catch (Exception e) {
                log(new IpsStatus(IStatus.ERROR, "Error building container relation "
                        + containerRelationName + " of " + pcType.getJavaClassName(), e));
            }
        }
    }

    void buildRelations() throws CoreException {
        buildRelations(pcType.getRelations());
    }

    private HashMap groupByContainerRelation(IRelation[] relations) throws CoreException {
        HashMap map = new HashMap();
        for (int i = relations.length - 1; i >= 0; i--) {
            if (!relations[i].validate().containsErrorMsg()) {
                IRelation rel = relations[i];
                String containerRelationName = rel.getContainerRelation();
                if (StringUtils.isNotEmpty(containerRelationName)) {
                    addToContainerRelationMap(map, rel, containerRelationName);
                } else {                
                    IRelation reverseComposition = rel.findContainerRelationOfTypeReverseComposition();
                    if(reverseComposition != null) {
                        addToContainerRelationMap(map, rel, reverseComposition.getName());
                    }                
                }
            }
        }
        return map;
    }

    private void addToContainerRelationMap(HashMap map, IRelation rel, String containerRelationName) {
        Collection coll = (Collection)map.get(containerRelationName);
        if (coll == null) {
            coll = new ArrayList();
        }
        coll.add(rel);
        map.put(containerRelationName, coll);
    }

    private IRelation findContainerRelation(IPolicyCmptType pcType, String containerRelationName) throws CoreException {
        if (StringUtils.isEmpty(containerRelationName)) {
            return null;
        }
        IRelation containerRelation = pcType.getRelation(containerRelationName);
        if (containerRelation != null) {
            return containerRelation;
        }
        IPolicyCmptType supertype = pcType.findSupertype();
        if (supertype==null) {
            return null;
        }
        return findContainerRelation(supertype, containerRelationName);
    }

    protected void buildRelations(IRelation[] relations) throws CoreException {
        for (int i = relations.length - 1; i >= 0; i--) {
            if (!relations[i].validate().containsErrorMsg()) {
                try {
                    buildRelation(relations[i]);
                } catch (Exception e) {
                    log(new IpsStatus(IStatus.ERROR, "Error building relation "
                            + relations[i].getName() + " of " + getCuBuilder().getQualifiedClassName(), e));
                }
            }
        }
    }
}