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

    public enum RenderMode {
        SIMPLE, VANILLA
    }

    @ClientOnly
    public static class Rendering {
        // Entity and Block Entity Culling
        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        // Leaf Culling
        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        // Chest Optimization
        @SubCategory("Chest Optimization")
        public ChestOptimization chestOptimization = new ChestOptimization();

        // Performance Optimization
        @SubCategory("Performance")
        public PerformanceOptimization performance = new PerformanceOptimization();

        // Particle Optimization
        @SubCategory("Particle Optimization")
        public ParticleOptimization particleOptimization = new ParticleOptimization();

        // Advanced Optimization
        @SubCategory("Advanced")
        public AdvancedOptimization advanced = new AdvancedOptimization();

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

            public List<String> entityBlacklist = Arrays.asList("minecraft:player", "minecraft:villager");
            public List<String> entityWhitelist = Arrays.asList("minecraft:ender_dragon");
        }

        public static class LeafCulling {
            public boolean enableLeafCulling = true;
        }

        public static class ChestOptimization {
            public boolean enableChestOptimization = true;
            public boolean optimizeEnderChests = true;
            public boolean optimizeTrappedChests = false;

            @Range(min = 1, max = 128)
            public int maxRenderDistance = 32;

            public RenderMode chestRenderMode = RenderMode.SIMPLE;
        }

        public static class PerformanceOptimization {
            public boolean skipOutlineWhenNoGlowing = true;
            public boolean reduceFpsWhenInactive = false;

            @Range(min = 5, max = 60)
            public int inactiveFpsLimit = 10;

            public boolean reduceRenderDistanceWhenInactive = false;

            @Range(min = 2, max = 12)
            public int inactiveRenderDistance = 2;

            @Range(min = 0, max = 1000)
            public int maxFps = 0;

            public boolean fpsOptimization = true;
        }

        public static class ParticleOptimization {
            public boolean enableParticleOptimization = true;
            public boolean enableParticleLOD = true;

            @Range(min = 4.0, max = 64.0)
            public double lodDistanceThreshold = 16.0;

            @Range(min = 0.0, max = 1.0)
            public double lodReductionFactor = 0.3;

            public List<String> lodParticleWhitelist = Arrays.asList("minecraft:rain", "minecraft:smoke");
            public List<String> lodParticleBlacklist = Arrays.asList("minecraft:portal", "minecraft:enchant");
        }

        public static class AdvancedOptimization {
            public boolean enableFixedTimestep = false;

            @Range(min = 0.001, max = 0.1)
            public double fixedTimestepInterval = 0.05;

            public boolean enableFixedLight = true;
            public boolean enableReflex = true;

            @Range(min = -1000000, max = 1000000)
            public long reflexOffsetNs = 0L;

            public boolean reflexDebug = false;
            public boolean enableGpuCollision = true;

            @Range(min = 1024, max = 10000000)
            public int gpuCollisionMaxPairs = 65536;

            public boolean fixPearlLeak = true;
            public boolean fixProjectileLerp = true;
        }
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
        public boolean ite = true;

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