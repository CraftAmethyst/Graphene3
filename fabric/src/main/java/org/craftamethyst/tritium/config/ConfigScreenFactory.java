package org.craftamethyst.tritium.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.zcraft.tritiumconfig.config.TritiumConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.craftamethyst.tritium.TritiumCommon;

public class ConfigScreenFactory {

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.tritium.title"))
                .setSavingRunnable(() -> {
                    TritiumConfig.save();
                    TritiumCommon.LOG.info("Configuration saved from GUI");
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        TritiumConfigBase config = TritiumConfig.get();

        // Performance Category
        ConfigCategory performanceCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.performance"));
        performanceCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.performance.bambooLight"),
                        config.performance.bambooLight)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.performance.bambooLight.tooltip"))
                .setSaveConsumer(newValue -> config.performance.bambooLight = newValue)
                .build());

        // Rendering Category (Client-only)
        ConfigCategory renderingCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.rendering"));

        // Leaf Culling
        renderingCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.rendering.leafCulling.enableLeafCulling"),
                        config.rendering.leafCulling.enableLeafCulling)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.rendering.leafCulling.enableLeafCulling.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.leafCulling.enableLeafCulling = newValue)
                .build());

        renderingCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.rendering.leafCulling.hideInnerLeaves"),
                        config.rendering.leafCulling.hideInnerLeaves)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.tritium.rendering.leafCulling.hideInnerLeaves.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.leafCulling.hideInnerLeaves = newValue)
                .build());

        renderingCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.rendering.leafCulling.enableFaceOcclusionCulling"),
                        config.rendering.leafCulling.enableFaceOcclusionCulling)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.rendering.leafCulling.enableFaceOcclusionCulling.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.leafCulling.enableFaceOcclusionCulling = newValue)
                .build());

        // Occlusion Culling
        renderingCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.rendering.occlusionCulling.enableEntityCulling"),
                        config.rendering.occlusionCulling.enableEntityCulling)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.rendering.occlusionCulling.enableEntityCulling.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.occlusionCulling.enableEntityCulling = newValue)
                .build());

        renderingCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.rendering.occlusionCulling.enableBlockEntityCulling"),
                        config.rendering.occlusionCulling.enableBlockEntityCulling)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.rendering.occlusionCulling.enableBlockEntityCulling.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.occlusionCulling.enableBlockEntityCulling = newValue)
                .build());

        renderingCategory.addEntry(entryBuilder.startDoubleField(
                        Component.translatable("config.tritium.rendering.occlusionCulling.hitboxSizeLimit"),
                        config.rendering.occlusionCulling.hitboxSizeLimit)
                .setDefaultValue(10.0)
                .setMin(1.0)
                .setMax(100.0)
                .setTooltip(Component.translatable("config.tritium.rendering.occlusionCulling.hitboxSizeLimit.tooltip"))
                .setSaveConsumer(newValue -> config.rendering.occlusionCulling.hitboxSizeLimit = newValue)
                .build());

        // Client Optimizations Category
        ConfigCategory clientOptsCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.clientOptimizations"));
        clientOptsCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.clientOptimizations.fastLanguageSwitch"),
                        config.clientOptimizations.fastLanguageSwitch)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.clientOptimizations.fastLanguageSwitch.tooltip"))
                .setSaveConsumer(newValue -> config.clientOptimizations.fastLanguageSwitch = newValue)
                .build());

        clientOptsCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.clientOptimizations.resourcePackCache"),
                        config.clientOptimizations.resourcePackCache)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.clientOptimizations.resourcePackCache.tooltip"))
                .setSaveConsumer(newValue -> config.clientOptimizations.resourcePackCache = newValue)
                .build());

        // Entities Category
        ConfigCategory entitiesCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.entities"));
        entitiesCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.entities.optimizeEntities"),
                        config.entities.optimizeEntities)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.entities.optimizeEntities.tooltip"))
                .setSaveConsumer(newValue -> config.entities.optimizeEntities = newValue)
                .build());

        entitiesCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.entities.tickRaidersInRaid"),
                        config.entities.tickRaidersInRaid)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.entities.tickRaidersInRaid.tooltip"))
                .setSaveConsumer(newValue -> config.entities.tickRaidersInRaid = newValue)
                .build());

        entitiesCategory.addEntry(entryBuilder.startIntField(
                        Component.translatable("config.tritium.entities.horizontalRange"),
                        config.entities.horizontalRange)
                .setDefaultValue(64)
                .setMin(1)
                .setMax(256)
                .setTooltip(Component.translatable("config.tritium.entities.horizontalRange.tooltip"))
                .setSaveConsumer(newValue -> config.entities.horizontalRange = newValue)
                .build());

        entitiesCategory.addEntry(entryBuilder.startIntField(
                        Component.translatable("config.tritium.entities.verticalRange"),
                        config.entities.verticalRange)
                .setDefaultValue(32)
                .setMin(1)
                .setMax(256)
                .setTooltip(Component.translatable("config.tritium.entities.verticalRange.tooltip"))
                .setSaveConsumer(newValue -> config.entities.verticalRange = newValue)
                .build());

        // Tech Optimizations Category
        ConfigCategory techOptsCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.techOptimizations"));
        techOptsCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.techOptimizations.lambdaEventListeners"),
                        config.techOptimizations.lambdaEventListeners)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.techOptimizations.lambdaEventListeners.tooltip"))
                .setSaveConsumer(newValue -> config.techOptimizations.lambdaEventListeners = newValue)
                .build());

        // Fixes Category (Client-only)
        ConfigCategory fixesCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.fixes"));
        fixesCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.fixes.buttonFix"),
                        config.fixes.buttonFix)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.fixes.buttonFix.tooltip"))
                .setSaveConsumer(newValue -> config.fixes.buttonFix = newValue)
                .build());

        fixesCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.fixes.noGLog"),
                        config.fixes.noGLog)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.fixes.noGLog.tooltip"))
                .setSaveConsumer(newValue -> config.fixes.noGLog = newValue)
                .build());

        // Server Performance Category
        ConfigCategory serverPerfCategory = builder.getOrCreateCategory(Component.translatable("config.tritium.category.serverPerformance"));
        serverPerfCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.serverPerformance.noiseSamplingCache"),
                        config.serverPerformance.noiseSamplingCache)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.serverPerformance.noiseSamplingCache.tooltip"))
                .setSaveConsumer(newValue -> config.serverPerformance.noiseSamplingCache = newValue)
                .build());

        serverPerfCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.tritium.serverPerformance.asyncWorldSave"),
                        config.serverPerformance.asyncWorldSave)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.tritium.serverPerformance.asyncWorldSave.tooltip"))
                .setSaveConsumer(newValue -> config.serverPerformance.asyncWorldSave = newValue)
                .build());

        serverPerfCategory.addEntry(entryBuilder.startIntField(
                        Component.translatable("config.tritium.serverPerformance.asyncWorldSaveTimeoutSeconds"),
                        config.serverPerformance.asyncWorldSaveTimeoutSeconds)
                .setDefaultValue(30)
                .setMin(1)
                .setMax(300)
                .setTooltip(Component.translatable("config.tritium.serverPerformance.asyncWorldSaveTimeoutSeconds.tooltip"))
                .setSaveConsumer(newValue -> config.serverPerformance.asyncWorldSaveTimeoutSeconds = newValue)
                .build());

        return builder.build();
    }
}
