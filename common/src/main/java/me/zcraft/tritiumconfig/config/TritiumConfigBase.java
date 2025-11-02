package me.zcraft.tritiumconfig.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;
import me.zcraft.tritiumconfig.annotation.Range;

import java.util.Arrays;
import java.util.List;

public class TritiumConfigBase {
    public Performance performance = new Performance();

    @ClientOnly
    public Rendering rendering = new Rendering();

    @ClientOnly
    public ClientOptimizations clientOptimizations = new ClientOptimizations();

    public Network network = new Network();
    public Entities entities = new Entities();
    public TechOptimizations techOptimizations = new TechOptimizations();
    
    @ClientOnly
    public Fixes fixes = new Fixes();
    
    public ServerPerformance serverPerformance = new ServerPerformance();

    public static class Performance {
        public boolean bambooLight = true;
    }

    @ClientOnly
    public static class Rendering {
        // Future rendering optimizations will be added here
    }

    @ClientOnly
    public static class ClientOptimizations {
        public boolean fastLanguageSwitch = true;
    }

    public static class Network {
        // Future network optimizations will be added here
    }

    public static class Entities {
        public boolean optimizeEntities = true;
        public boolean tickRaidersInRaid = true;

        @Range(min = 1, max = 256)
        public int horizontalRange = 64;

        @Range(min = 1, max = 256)
        public int verticalRange = 32;

        public List<String> entityWhitelist = Arrays.asList("minecraft:ender_dragon");
    }

    public static class TechOptimizations {
        // Future technical optimizations will be added here
    }

    @ClientOnly
    public static class Fixes {
        public boolean buttonFix = true;
        public boolean noGLog = true;
    }

    public static class ServerPerformance {
        public boolean noiseSamplingCache = true;
    }
}
