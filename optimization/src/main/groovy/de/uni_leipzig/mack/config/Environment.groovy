package de.uni_leipzig.mack.config

import groovy.transform.TypeChecked

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@TypeChecked
enum Environment {
    DEVELOPMENT('development', 'dev'),
    PRODUCTION('production', 'prod'),
    TEST('test', 'test');

    public static final Environment DEFAULT = Environment.DEVELOPMENT;
    String name
    String prefix

    Environment(String name, String prefix) {
        this.name = name
        this.prefix = prefix
    }

    String getName() {
        return name
    }

    String getPrefix() {
        return prefix
    }

    static Environment byProperty() {
        def envName = System.properties.getProperty('environment') ?: Environment.DEFAULT.name
        byString(envName)
    }

    static Environment byString(String s) {
        def cands = Environment.values().grep { Environment e ->
            s.toLowerCase().startsWith(e.prefix)
        }
        switch (cands.size()) {
            case 0: throw new IllegalArgumentException("No environment matches '$s'"); break
            case 1: return cands.first(); break
            default: throw new IllegalArgumentException("Abmiguity '$s' would match several environment"); break
        }
    }
}
