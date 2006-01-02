package org.faktorips.devtools.core.model.pctype;


/**
 *
 */
public class Parameter {
    
    private int index;
    private String name;
    private String datatype;
    
    public Parameter(int index) {
        this(index, "", "");
    }
    
    public Parameter(int index, String name, String datatype) {
        this.index = index;
        this.name = name;
        this.datatype = datatype;
    }
    
    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String newName) {
        name = newName;
    }
    
    public String getDatatype() {
        return datatype;
    }
    
    public void setDatatype(String newDatatype) {
        datatype = newDatatype;
    }
    
    public String toString() {
        return index + " " + datatype + " " + name;
    }

}
