# Tritium (氚)

An all-in-one optimization module dedicated to optimizing client-side rendering and server-side stability and fluency.
This project is still under development slowly. You can learn about the progress at [TODO List](TODO.md)

## Feature

- **[Full version]** Fast Language: Dramatically speed up language switching by intercepting resource bundle reloading when switching languages
- **[Forge only]** Fast Event: Replaced generated classes with construction lambdas to speed up construction event listening
- **[Full version]** Leaf Culling: Ported Optifine smart foliage options
- **[Full version]** Optimization of entity stacking: Similar to Spigot's stacking optimization of entities (referring to dropped items and experience balls), if entities in an area are too dense, then these dropped items will be merged directly
- **[Full version]** Chest rendering optimization: Removed dynamic models of chests, leaving them rendered as static block geometry (at the expense of no animation when chests open)
- **[Full version]** Distant object stop tick: If an entity or fluid is too far away from the player, it will no longer tick
- **[Full version]** Dynamic FPS: Automatically reduce framerate when the game is unfocused (down to 1 FPS) or minimized (not rendering at all)
- **[Full version]** Memory Leak Fix: Fixed various memory leaks, such as the game crashing after a while (even if you did nothing)
- **[Full version]** Mask GL Error: Fix GL error log refreshes even though there is no substantive error.
- **[Full version]** GPU Plus: Fix the game's video memory leak problem through various methods, and also bring a variety of new OpenGL 4+ features
- **[Windows only]** GPU entity collision optimization: By placing the physical AABB collision calculation on the GPU, the main thread pressure is shared, achieving significant performance improvement in dense biological scenes.
- **[Full version]** Quickly save the world: Make the world saving thread asynchronous to greatly increase the speed of saving the world
- **[Forge only]** Technology module optimization: Optimize the performance of some technology industry modules (such as GregTech, SFM, AE2) to improve performance, which plays a great role in technology integration packages (such as ATM9)
- **[Some versions]** Vanilla bug fix: Fix some bugs in vanilla, or port fixes from higher versions

## Supported Minecraft Versions

This mod supports the following Minecraft versions:

NeoForge: 1.21.x

Forge: 1.19, 1.19.1, 1.19.2, 1.20, 1.20.1

Fabric: 1.19, 1.19.1, 1.19.2, 1.20, 1.20.1, 1.21.x

Quilt: This mod loader is not supported by any version and will not be supported in the future.


> NOTE: Features supported by Tritium will vary with each Minecraft version

> NeoForge and Fabric will follow up with future Minecraft official version updates, while Forge will stay at 1.20.1

## Download

If you are a regular player or an integration package author, please download the [stable version](https://github.com/CraftAmethyst/Tritium/releases)

If you want to be an early adopter or developer, please download it on [Github Action](https://github.com/CraftAmethyst/Tritium/releases)

## Develop

If you want to modify the source code of this project, please follow the steps below to set up your workspace:

Execute `git clone https://github.com/CraftAmethyst/Tritium.git` to clone this project

Navigate to the project folder, right-click, and click `Open Folder as Intellij IDEA ... Edition Project`

IDEA will then automatically set up the workspace and wait for Gradle synchronization to complete.

If you want to build this project, execute `gradle clean jarJar build` and the Jar file will be generated in `build/lib`

## Feedback question

If you encounter a bug/behavior inconsistency while using this module, please provide feedback in an [Issue](https://github.com/CraftAmethyst/Tritium/issues). It must be clearly organized and must include a crash log and reproduction steps.

## Support us

If you have a better idea for a project, you are always welcome. Every developer is also welcome to submit a PR

If you think this project is helpful to you, please click Star for this project, which is especially important to us.