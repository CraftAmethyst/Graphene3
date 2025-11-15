package org.craftamethyst.tritium.config;

import me.zcraft.tc.annotation.ClientOnly;
import me.zcraft.tc.annotation.ConfigVersion;
import me.zcraft.tc.annotation.Range;
import me.zcraft.tc.annotation.SubCategory;

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
        public CRO cro = new CRO();
        @SubCategory("FastBlit")
        public FastBlit fastBlit = new FastBlit();
        // GPU Plus
        @SubCategory("GPUPlus")
        public GpuPlus GpuPlus = new GpuPlus();

        public static class CRO {
            public boolean chest_rendering_optimization = false;
        }

        public static class FastBlit {
            public boolean fastBlit = true;
        }

        public static class GpuPlus {
            public static boolean gpuPlus = true;
            public boolean gpuPlusVbo = true;
            public boolean gpuPlusIndex = true;
        }

        @SubCategory("Reflex")
        public Reflex reflex = new Reflex();

        // Entity and Block Entity Culling
        @SubCategory("Entity Culling")
        public EntityCulling entityCulling = new EntityCulling();

        // Leaf Culling
        @SubCategory("Leaf Culling")
        public LeafCulling leafCulling = new LeafCulling();

        public static class Reflex {
            public boolean enableReflex = true;
            public boolean reflexDebug = false;
            @Range(min=-100000, max=100000)
            public int reflexOffsetNs = 0;
            @Range(min=0, max=1000)
            public int MAX_FPS = 0;
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
        public FL FL= new FL();
        @SubCategory("FastResourcePack")
        public FastResourcePack FastResourcePack= new FastResourcePack();

        public static class FL {
            public boolean fastLanguageSwitch = true;
        }

        public static class FastResourcePack {
            public boolean resourcePackCache = true;
        }
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

        @SubCategory("entityStacking")
        public EntityStacking entityStacking = new EntityStacking();

        public static class EntityStacking{
            public boolean enable = true;

            @Range(min = 1)
            public int lagTicks = 20;
            @Range(min = 2)
            public int maxEntityCount = 4;
            @Range(min = 0.01D)
            public double range = 3.2D;
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