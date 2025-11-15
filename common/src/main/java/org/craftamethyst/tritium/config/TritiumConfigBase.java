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
        @SubCategory("FastBambooLight")
        public boolean bambooLight = true;
    }

    @ClientOnly
    public static class Rendering {
        @SubCategory("ChestRenderingOpt")
        public ChestRenderingOpt cro = new ChestRenderingOpt();

        @SubCategory("FastBlit")
        public FastBlit FastBlit = new FastBlit();

        @SubCategory("Reflex")
        public Reflex reflex = new Reflex();

        // Entity and Block Entity Culling
        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        // Leaf Culling
        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        public static class FastBlit {
            public boolean fastBlit = true;
        }

        public static class ChestRenderingOpt {
            public boolean chest_rendering_optimization = false;
        }
        public static class Reflex {
            public static boolean enableReflex = true;
            public static boolean reflexDebug = false;
            @Range(min=-100000, max=100000)
            public static int reflexOffsetNs = 0;
            @Range(min=0, max=1000)
            public static int MAX_FPS = 0;
        }
        public static class EntityCulling {
            public boolean enableCulling = true;
            public boolean enableBlockEntityCulling = true;
            public boolean enableTickStopping = false;
            public boolean enableNameTagCulling = true;

            public List<String> entityBlacklist = Arrays.asList("minecraft:player", "minecraft:villager");
        }

        public static class LeafCulling {
            public boolean enableLeafCulling = true;
            public boolean hideInnerLeaves = false;
            public boolean enableFaceOcclusionCulling = true;
        }
    }

    @ClientOnly
    public static class ClientOptimizations {
        @SubCategory("FastLanguage")
        public boolean fastLanguageSwitch = true;
        @SubCategory("FastResourcePack")
        public boolean resourcePackCache = true;

        @SubCategory("dynamicFPS")
        public DynamicFPS dynamicFPS = new DynamicFPS();

        public static class DynamicFPS{
            public boolean enable = true;
            @Range(min = 1)
            public int minimizedFPS = 1;
        }

    }

    public static class Network {
        // Future network optimizations will be added here
    }

    public static class Entities {
        @SubCategory("EntityOpt")
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
        @SubCategory("Button Fix")
        public boolean buttonFix = true;
        @SubCategory("No GLog")
        public boolean noGLog = true;
        @SubCategory("Memory Leak Fix")
        public static boolean MemoryLeakFix_AE2WTLibCreativeTabLeakFix =true;
        public static boolean MemoryLeakFix_ScreenshotByteBufferLeakFix =true;
    }

    public static class ServerPerformance {
        @SubCategory("Noise Sampling Cache")
        public boolean noiseSamplingCache = true;
    }
}