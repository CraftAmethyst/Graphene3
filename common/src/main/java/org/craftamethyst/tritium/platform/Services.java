package org.craftamethyst.tritium.platform;

import org.craftamethyst.tritium.Constants;
import org.craftamethyst.tritium.platform.services.IPlatformHelper;
import org.craftamethyst.tritium.platform.services.IConfigService;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IConfigService CONFIG = load(IConfigService.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
