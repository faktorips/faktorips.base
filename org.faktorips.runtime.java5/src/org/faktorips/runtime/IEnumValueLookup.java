package org.faktorips.runtime;

import java.util.List;

public interface IEnumValueLookup<T> {
    
    public List<T> getAllValues();
    
    public T getValue(Object id);
}
