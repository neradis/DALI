package de.uni_leipzig.mack.config

import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import groovy.util.logging.Log4j

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
@Log4j('logger')
@TypeChecked
class ConfigurationLoader {
    protected final static List<String> CONFIG_RESOURCE_NAMES = ImmutableList.of('dali.config')
    public static final String DEFAULTS_SECTION_NAME = 'DEFAULT_SETTINGS'

    static Config loadConfigForEnv(String envName) {
        def stableConfigs = CONFIG_RESOURCE_NAMES.collect { String name ->
            URL configLocation = Resources.getResource(name)
            def confObj = new ConfigSlurper(envName).parse(configLocation)
            confObj = StableConfigObject.copyOf(confObj)
            mergeInDefaults(confObj)
        }
        stableConfigs = stableConfigs.inject { ConfigObject acc, ConfigObject add -> acc.merge(add) }
        return new Config(stableConfigs)
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static protected ConfigObject mergeInDefaults(ConfigObject config) {
        if (config.containsKey(DEFAULTS_SECTION_NAME)) {
            for (Map.Entry defaultSectionEntry in config[DEFAULTS_SECTION_NAME]) {
                logger.trace("Looking for merge candidates for default section '$defaultSectionEntry.key'")
                ConfigObject defaultSection = defaultSectionEntry.value
                def configSections = config[defaultSectionEntry.key]
                for (ConfigObject section in configSections.values()) {
                    logger.trace("Merging ${defaultSection} into ${section}")
                    mergeInDefaults(section, defaultSection)
                    logger.trace("Merge result: ${section}")
                }
            }
        }
        return config
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    static mergeInDefaults(Map current, Map additional) {
        for (addEntry in additional) {
            if (current.containsKey(addEntry.key)) {
                def currentValue = current[addEntry.key]
                if (currentValue instanceof Map && addEntry.value instanceof Map) {
                    mergeInDefaults(currentValue, addEntry.value) // recur
                } else if (currentValue instanceof Map || addEntry.value instanceof Map) {
                    throw new ConfigurationException('Unable to merge in default configuration: clash of atomic and ' +
                            'nested configuration subelements')
                }
            } else {
                current[addEntry.key] = addEntry.value
            }
        }
    }
}

class ConfigurationException extends GroovyRuntimeException {
    ConfigurationException(String message, Throwable cause) {
        super(message, cause)
    }

    ConfigurationException(String message) {
        super(message)
    }
}

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
@TypeChecked
@Log4j('logger')
class StableConfigObject extends ConfigObject {

    private StableConfigObject() {
        super()
    }

    private setDelegateMap(Map delegate) {
        def delegateField = ConfigObject.class.getDeclaredField('delegateMap')
        def access = delegateField.accessible
        delegateField.accessible = true
        delegateField.set(this, delegate)
        delegateField.accessible = access
    }


    static StableConfigObject copyOf(ConfigObject configObject) {
        def backing = new LinkedHashMap<String, Object>()

        for (e in configObject.entrySet()) {
            def entry = (Map.Entry) e
            String key = (String) entry.key
            def value = entry.value
            if (value instanceof ConfigObject) {
                backing[key] = StableConfigObject.copyOf(value)
            } else {
                backing[key] = value
            }
        }
        StableConfigObject result = new StableConfigObject()
        result.setDelegateMap(backing)
        if (configObject.configFile) {
            result.configFile = configObject.configFile
        }
        return result
    }

    @Override
    Object getProperty(String name) {
        if (name == "configFile")
            return this.configFile
        return get(name)
    }
}

@TypeChecked
@Log4j('logger')
class Config extends ConfigObject{
    protected static Map<String,Config> cache = Maps.newHashMap()

    @Delegate protected ConfigObject delegate

    static Config byProperty() {
        def env = System.properties.getProperty('environment')
        forEnvironment(env)
    }

    static Config forEnvironment(String env) {
        cache.getWithDefault(env, { ConfigurationLoader.loadConfigForEnv(env) })
    }

    protected Config(ConfigObject delegate) {
        this.delegate = delegate
    }

    def <T> T getConf(Closure selector, Class<T> expectedType) {
        try {
            def val = selector.call(delegate)
            if(val.is(null)) throw new NullPointerException('Config value not present')
            if(!expectedType.isInstance(val)) throw new ClassCastException('Unexpected type of config value')
            return expectedType.cast(val)
        } catch (Exception e) {
            throw new ConfigurationException('Error fetching config value', e)
        }
    }
}
