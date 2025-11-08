package org.craftamethyst.tritium.config;

import me.zcraft.tritiumconfig.annotation.ClientOnly;
import me.zcraft.tritiumconfig.annotation.ConfigVersion;
import me.zcraft.tritiumconfig.annotation.Range;
import me.zcraft.tritiumconfig.annotation.SubCategory;

import java.util.Arrays;
import java.util.List;

@ConfigVersion(1)
public class TritiumConfigBase {
    @SubCategory("Performance")
    public Performance performance = new Performance();

    @ClientOnly
    @SubCategory("Rendering")
    public Rendering rendering = new Rendering();

    @ClientOnly
    @SubCategory("Client Optimizations")
    public ClientOptimizations clientOptimizations = new ClientOptimizations();

    @SubCategory("Network")
    public Network network = new Network();

    @SubCategory("Entities")
    public Entities entities = new Entities();

    @SubCategory("Tech Optimizations")
    public TechOptimizations techOptimizations = new TechOptimizations();

    @ClientOnly
    @SubCategory("Fixes")
    public Fixes fixes = new Fixes();

    @SubCategory("Server Performance")
    public ServerPerformance serverPerformance = new ServerPerformance();

    public static class Performance {
        public boolean bambooLight = true;
    }

    @ClientOnly
    public static class Rendering {
        // Entity and Block Entity Culling
        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        // Leaf Culling
        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        public static class EntityCulling {
            public boolean enableCulling = true;
            public boolean enableEntityCulling = true;
            public boolean enableBlockEntityCulling = true;
            public boolean enableTickStopping = false;
            public boolean enableNameTagCulling = true;

            @Range(min = 1, max = 256)
            public int horizontalRange = 64;

            @Range(min = 1, max = 256)
            public int verticalRange = 32;

            @Range(min = 1, max = 100)
            public double hitboxSizeLimit = 10.0;

            public List<String> entityBlacklist = Arrays.asList("minecraft:player", "minecraft:villager");
            public List<String> entityWhitelist = List.of("minecraft:ender_dragon");
        }

        public static class LeafCulling {
            public boolean enableLeafCulling = true;
            public boolean hideInnerLeaves = false;
            public boolean enableFaceOcclusionCulling = true;
        }
    }

    @ClientOnly
    public static class ClientOptimizations {
        public boolean fastLanguageSwitch = true;
        public boolean resourcePackCache = true;
    }

    public static class Network {
        // Future network optimizations will be added here
    }

    public static class Entities {
        public boolean optimizeEntities = true;
        public boolean tickRaidersInRaid = true;
        public boolean ite = true;

        @Range(min = 1, max = 256)
        public int horizontalRange = 64;

        @Range(min = 1, max = 256)
        public int verticalRange = 32;

        public List<String> entityWhitelist = List.of("minecraft:ender_dragon");
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