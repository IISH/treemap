package org.iish.treemap.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import org.iish.treemap.model.tabular.TabularData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/**
 * Guice dependency injection interface binding.
 */
public class TreemapModule extends AbstractModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(TreemapModule.class);

    /**
     * Configures a {@link Binder} via the exposed methods.
     */
    @Override
    protected void configure() { }

    /**
     * Parses the config file and provides it as a singleton.
     *
     * @return The configuration.
     */
    @Provides
    @Singleton
    public Config providesConfig() {
        try {
            String configPath = System.getProperty("treemap.config");
            LOGGER.info("Loading configuration from {}.", configPath);

            Yaml yaml = new Yaml(new Constructor(Config.class));
            return (Config) yaml.load(new FileInputStream(configPath));
        }
        catch (FileNotFoundException e) {
            LOGGER.error("Failed to load the configuration!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a new cache.
     *
     * @param config The cache configuration.
     * @return A new cache.
     */
    @Provides
    @Singleton
    public Cache<String, TabularData> providesCache(Config config) {
        LOGGER.info("Building a cache for datasets with a maximum size of {} items and which " +
                "expires after {} hours without access.", config.cache.maximumSize, config.cache.maxHoursAccessTime);

        return CacheBuilder.newBuilder()
                .maximumSize(config.cache.maximumSize)
                .expireAfterAccess(config.cache.maxHoursAccessTime, TimeUnit.HOURS)
                .build();
    }
}
