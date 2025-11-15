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
        public FastBambooLight FastBambooLight = new FastBambooLight();

        public static class FastBambooLight {
            public boolean bambooLight = true;
        }
    }

    @ClientOnly
    public static class Rendering {
        @SubCategory("ChestRenderingOpt")
        public ChestRenderingOpt cro = new ChestRenderingOpt();

        @SubCategory("FastBlit")
        public FastBlit FastBlit = new FastBlit();

        @SubCategory("Reflex")
        public Reflex reflex = new Reflex();

        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        public static class ChestRenderingOpt {
            public boolean chest_rendering_optimization = false;
        }

        public static class FastBlit {
            public boolean fastBlit = true;
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
        public FastLanguage fastLanguage = new FastLanguage();

        @SubCategory("FastResourcePack")
        public FastResourcePack fastResourcePack = new FastResourcePack();

        @SubCategory("dynamicFPS")
        public DynamicFPS dynamicFPS = new DynamicFPS();

        public static class FastLanguage {
            public boolean fastLanguageSwitch = true;
        }

        public static class FastResourcePack {
            public boolean resourcePackCache = true;
        }

        public static class DynamicFPS {
            public boolean enable = true;
            @Range(min = 1)
            public int minimizedFPS = 1;
        }
    }

    public static class Network {
        // Future network optimizations will be added here
        // 当有具体配置时，按照相同模式添加
    }

    public static class Entities {
        @SubCategory("EntityOpt")
        public EntityOpt entityOpt = new EntityOpt();


        public static class EntityOpt {
            public boolean optimizeEntities = true;
            public boolean tickRaidersInRaid = true;
            public boolean ite = true;
            @Range(min = 1, max = 256)
            public int horizontalRange = 64;
            @Range(min = 1, max = 256)
            public int verticalRange = 32;

            public List<String> entityWhitelist = List.of("minecraft:ender_dragon");
        }

    }

    public static class TechOptimizations {
        // Future technical optimizations will be added here
    }

    @ClientOnly
    public static class Fixes {
        @SubCategory("Button Fix")
        public ButtonFix buttonFix = new ButtonFix();

        @SubCategory("No GLog")
        public NoGLog noGLog = new NoGLog();

        @SubCategory("Memory Leak Fix")
        public MemoryLeakFix memoryLeakFix = new MemoryLeakFix();

        public static class ButtonFix {
            public boolean buttonFix = true;
        }

        public static class NoGLog {
            public boolean noGLog = true;
        }

        public static class MemoryLeakFix {
            public static boolean AE2WTLibCreativeTabLeakFix = true;
            public static boolean ScreenshotByteBufferLeakFix = true;
        }
    }

    public static class ServerPerformance {
        @SubCategory("Noise Sampling Cache")
        public NoiseSamplingCache noiseSamplingCache = new NoiseSamplingCache();

        public static class NoiseSamplingCache {
            public boolean noiseSamplingCache = true;
        }
    }
}