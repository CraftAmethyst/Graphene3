package org.craftamethyst.tritium.config;


import me.zcraft.tconfig.annotation.ConfigVersion;
import me.zcraft.tconfig.annotation.Range;
import me.zcraft.tconfig.annotation.SubCategory;

import java.util.Arrays;
import java.util.List;

@ConfigVersion(1)
public class TritiumConfigBase {
    @SubCategory("Performance")
    public Performance performance = new Performance();

    @SubCategory("Network")
    public Network network = new Network();

    @SubCategory("Entities")
    public Entities entities = new Entities();

    @SubCategory("Tech Optimizations")
    public TechOptimizations techOptimizations = new TechOptimizations();

    @SubCategory("Server Performance")
    public ServerPerformance serverPerformance = new ServerPerformance();

    public static class Performance {
        @SubCategory("FastBambooLight")
        public FastBambooLight fastBambooLight = new FastBambooLight();
        public static class FastBambooLight {
            public static boolean bambooLight = true;
        }
    }


    public static class Network {
        // Future network optimizations will be added here
    }

    public static class Entities {
        @SubCategory("EntityOpt")
        public EntityOpt entityOpt = new EntityOpt();
        public static class EntityOpt {
            public static boolean optimizeEntities = true;
            public static boolean tickRaidersInRaid = true;
            public static boolean ite = true;

            @Range(min = 1, max = 256)
            public static int horizontalRange = 64;

            @Range(min = 1, max = 256)
            public static int verticalRange = 32;

            public static  List<String> entityWhitelist = List.of("minecraft:ender_dragon");

        }

        @SubCategory("entityStacking")
        public EntityStacking entityStacking = new EntityStacking();

        public static class EntityStacking{
            public static boolean enable = true;
            public static boolean lockMaxedStacks = true;
            public static boolean showStackCount = true;
            @Range(min = 0)
            public static int maxStackSize = 0;
            @Range(min = 0.1,max = 10)
            public static double mergeDistance = 1.5;
            @Range(min = 0,max = 2)
            public static int listMode=0;
            public static List<String> itemList = Arrays.asList(
                    "minecraft:item"
            );
        }
    }

    public static class TechOptimizations {
        // Future technical optimizations will be added here
    }

    public static class ServerPerformance {
        @SubCategory("Noise Sampling Cache")
        public NoiseSamplingCache noiseSamplingCache = new NoiseSamplingCache();
        public static class NoiseSamplingCache {
            public static boolean noiseSamplingCache = true;
        }
    }
}