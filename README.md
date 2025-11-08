<h1 align="center">Tritium (氚)</h1>
<img height="207" alt="Tritium Logo" src="logo.png" />

An all-in-one optimization mod dedicated to improving client-side rendering performance and server-side stability.
This project is actively under development. Track progress at the [TODO List](TODO.md).


<h2 align="center">Features</h2>

- **[All Versions]** Fast Language Switching: Dramatically speeds up language switching by intercepting resource bundle reloading
- **[Forge Only]** Fast Event System: Replaces generated classes with lambda constructors to accelerate event listener construction
- **[All Versions]** Leaf Culling: Ports OptiFine's smart foliage rendering options
- **[All Versions]** Entity Stacking Optimization: Merges nearby dropped items and experience orbs when entity density is high, similar to Spigot's optimization
- **[All Versions]** Chest Rendering Optimization: Replaces dynamic chest models with static block geometry (removes opening animations for better performance)
- **[All Versions]** Distant Entity Tick Reduction: Stops ticking entities and fluids that are far from the player
- **[All Versions]** Dynamic FPS: Automatically reduces framerate when the game window is unfocused (1 FPS) or minimized (pauses rendering)
- **[All Versions]** Memory Leak Fixes: Resolves various memory leaks that could cause crashes during extended gameplay
- **[All Versions]** GL Error Suppression: Eliminates spurious GL error log spam
- **[All Versions]** GPU Plus: Fixes video memory leaks and introduces new OpenGL 4+ features
- **[Forge Only]** Asynchronous World Saving: Makes world saving asynchronous to significantly improve save performance
- **[Forge Only]** Tech Mod Optimization: Improves performance of technical mods (e.g., GregTech, SFM, AE2), especially beneficial for tech-focused modpacks like ATM9
- **[Select Versions]** Vanilla Bug Fixes: Fixes vanilla bugs and backports fixes from newer versions

<h2 align="center">Supported Minecraft Versions</h2>

| Mod Loader | Supported Versions |
|------------|--------------------|
| NeoForge   | 1.21.x |
| Forge      | 1.19, 1.19.1, 1.19.2, 1.20, 1.20.1 |
| Fabric     | 1.19, 1.19.1, 1.19.2, 1.20, 1.20.1, 1.21.x |
| Quilt      | Not supported |

> **Note:** Available features vary by Minecraft version.

> **Update Policy:** NeoForge and Fabric versions will be updated to support future Minecraft releases. Forge support will remain at 1.20.1.

<h2 align="center">Dependencies</h2>

Install the required dependencies for your mod loader before installing Tritium:

| Mod Loader | Required Dependencies |
|------------|-----------------------|
| NeoForge   | Cloth Config          |
| Forge      | Cloth Config          |
| Fabric     | Cloth Config, ModMenu |

<h2 align="center">Download</h2>

**For Players and Modpack Authors:** Download the [stable release](https://github.com/CraftAmethyst/Tritium/releases)

**For Early Adopters and Developers:** Download development builds from [GitHub Actions](https://github.com/CraftAmethyst/Tritium/actions)

<h2 align="center">Development</h2>

To set up a development environment:

1. Clone the repository:
   ```bash
   git clone https://github.com/CraftAmethyst/Tritium.git
   ```

2. Open the project folder in IntelliJ IDEA:
   - Right-click the folder and select `Open Folder as IntelliJ IDEA Project`
   - IDEA will automatically configure the workspace and sync Gradle dependencies

3. Build the project:
   ```bash
   gradle clean jarJar build
   ```
   The compiled JAR file will be located in `build/libs`

<h2 align="center">Bug Reports</h2>

If you encounter bugs or unexpected behavior, please submit an [Issue](https://github.com/CraftAmethyst/Tritium/issues) with:
- A clear description of the problem
- Crash logs (if applicable)
- Steps to reproduce the issue

<h2 align="center">Contributing</h2>

Contributions are welcome! If you have ideas for improvements or new features, feel free to submit a pull request.

If you find this project helpful, please consider starring the repository. Your support is greatly appreciated!

<h2 align="center">Contributors</h2>

<a href="https://github.com/CraftAmethyst/Tritium/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=CraftAmethyst/Tritium" />
</a>
