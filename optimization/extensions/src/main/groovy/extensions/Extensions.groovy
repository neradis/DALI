package extensions

import groovy.transform.CompileStatic
import org.w3c.dom.Element

import java.util.concurrent.ConcurrentMap

@CompileStatic
class Extensions {
    public static final int MAX_REPLACE_ATTEMPTS = 8


    static <K, V> V getWithDefault(Map<K, V> self, K key, Closure<V> defaultProvider) {
        def value = self.get(key)

        if (value == null && !(key in self)) {
            if (defaultProvider.maximumNumberOfParameters > 1)
                throw new IllegalArgumentException()
            if (defaultProvider.maximumNumberOfParameters == 1 && K.class.isCase(defaultProvider.parameterTypes[0])) {
                value = defaultProvider.call(key)
            } else {
                value = defaultProvider.call()
            }
            self.put(key, value)
        }
        return value
    }

    static <K, V> HashMap<K, V> filterByKeys(Map<K, V> self, Collection<K> keys) {
        def ret = new HashMap<K, V>(keys.size())
        self.entrySet().each { Map.Entry<K, V> e ->
            if (e.key in keys) ret.put(e.key, e.value)
        }
        return ret
    }

    static <K, V, T> HashMap<K, T> convertValues(Map<K, V> self, Closure<T> converter) {
        def ret = new HashMap<K, T>(self.size())
        self.entrySet().each { Map.Entry<K, V> e ->
            ret.put(e.key, (T) converter.call(e.value))
        }
        return ret
    }

    static Map<String, String> getAttributeMap(Element self) {
        def attr = self.attributes
        def ret = new HashMap<String, String>()
        for (i in 0..<attr.length) {
            def a = attr.item(i)
            ret.put(a.nodeName, a.nodeValue)
        }
        (Map<String, String>) ret
    }


    static <K, V extends Comparable<V>> boolean updateMaximum(ConcurrentMap<K, V> self, K key, V maxCand) {
        final ConcurrentMap<K, V> receiver = self
        updateMaxRec(receiver, key, maxCand, 0)
    }

    private static <K, V extends Comparable<V>> boolean updateMaxRec(ConcurrentMap<K, V> self, K key, V maxCand,
                                                                     int attempt) {
        if (attempt >= MAX_REPLACE_ATTEMPTS) {
            throw new ConcurrentModificationException("Unable to successfully update maximum after " +
                    "$MAX_REPLACE_ATTEMPTS attempts (probably too frequent concurrent modification)")
        }

        def currentMax = (V) self.get(key)
        if (!self.containsKey(key)) {
            if (self.putIfAbsent(key, maxCand).is(null)) {
                return true //sucessfully placed first value as initial max
            } else {
                return updateMaxRec(self, key, maxCand, attempt + 1) //concurrent update of max value -> retry
            }
        } else if (currentMax.compareTo(maxCand) < 0) {
            if (self.replace(key, currentMax, maxCand)) {
                return true
            } else {
                return updateMaxRec(self, key, maxCand, attempt + 1)
            }
        }
        return false
    }

    /*static ImmutableList<String> tokenize(String self, String delim) {
        def tokenizer = new StringTokenizer(self, delim)
        def tokens = new LinkedList<String>()
        while(tokenizer.hasMoreTokens()) tokens.add(tokenizer.nextToken())
        return ImmutableList.copyOf(tokens)
    }*/

    // compilation fails: unable to resolve class T
    /*static <T> Iterable<T> makeIterable(Iterator<T> self) {
        final Iterator<T> selfVar = self

        return new Iterable<T>() {

            @Override
            Iterator<T> iterator() {
                return selfVar
            }
        }
    }*/
}
