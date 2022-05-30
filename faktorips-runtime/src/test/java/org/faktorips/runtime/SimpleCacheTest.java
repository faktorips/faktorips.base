package org.faktorips.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.runtime.caching.IComputable;
import org.junit.Test;

public class SimpleCacheTest {

    int number = 1;

    @Test
    public void testCompute() throws InterruptedException {
        IComputable<Object, Object> computable = IComputable.of(Object.class, this::getNotCachedNumber);
        SimpleCache cache = new SimpleCache(computable);
        Object computedObjectOne = cache.compute("One");
        assertThat(1, is(computedObjectOne));
        Object computedObjectTwo = cache.compute("Two");
        assertThat(2, is(computedObjectTwo));
        Object computedObjectThree = cache.compute("Three");
        assertThat(3, is(computedObjectThree));
        Object computedObjectOneAgain = cache.compute("One");
        assertThat(1, is(computedObjectOneAgain));
    }

    private Object getNotCachedNumber(@SuppressWarnings("unused") Object key) {
        return number++;
    }
}
