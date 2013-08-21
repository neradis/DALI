package extensions;

import groovy.lang.Closure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class JavaExtensions {

    public static <T> Iterable<T> makeIterable(Iterator<T> self) {
        final Iterator<T> iterator = self;

        return new Iterable<T>() {
            //@Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }

    public static <K, V> V get(Map<K, V> map, K key, Closure<V> defaultProvider) {
        if (!map.containsKey(key)) {
            map.put(key, defaultProvider.getMaximumNumberOfParameters() > 0 ? defaultProvider.call(key) :
                    defaultProvider.call());
        }
        return map.get(key);
    }

    public static <T> T second(List<T> self) {
        if (self.size() < 2) {
            throw new NoSuchElementException("Cannot access second() element from a list of size " + self.size());
        }
        return self.get(1);
    }

    public static Double bounded(Double self, Double lowerBound, Double upperBound) {
        return Math.max(Math.min(self, upperBound), lowerBound);
    }

    public static <T, V> Iterator<V> transform(Iterator<T> self, Closure<V> closure) {
        final Iterator<T> self_ = self;
        final Closure<V> cl = closure;
        return new Iterator<V>() {

            @Override
            public boolean hasNext() {
                return self_.hasNext();
            }

            @Override
            public V next() {
                return cl.call(self_.next());
            }

            @Override
            public void remove() {
                self_.remove();
            }
        };
    }
}
