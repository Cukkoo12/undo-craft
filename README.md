# 🔄 Undo Craft Mod

Undo Craft is a Minecraft mod that allows you to undo your last crafting action. You can easily revert the most recent crafting operation at the crafting table.

![Demo](demo.gif)

## ✨ Features

- **Undo Last Craft**: Reverts the most recent crafting operation
- **Client-Side**: Works client-side only, no server installation required
- **All Loaders**: Supports Fabric, Forge, and NeoForge
- **Multi-Version**: Supports 1.20.1, 1.21.4, and 26.1.2+

## 📸 Screenshots

![Screenshot 1](1.png)

![Screenshot 2](2.png)

![Screenshot 3](3.png)

![Screenshot 4](4.png)

## 🎮 Usage

1. Right-click on a crafting table
2. You will see the **↩ Undo** button in the top-left corner of the crafting interface
3. After accidentally crafting an item, click the button to get your materials back

> **Note**: The undo action only works for the last craft. History resets when the crafting table is closed and reopened, or when a different craft is made.

## 📥 Downloads

| Minecraft Version | Fabric | Forge | NeoForge |
|-------------------|--------|-------|----------|
| 1.20.1 | ✅ | ✅ | ❌ |
| 1.21.4 | ✅ | ✅ | ✅ |
| 26.1.2+ | ✅ | ✅ | ✅ |

## 🛠️ Requirements

| Minecraft | Java | Mod Loader | Required APIs |
|-----------|------|------------|---------------|
| 1.20.1 | Java 17 | Fabric 0.15.0+ | Fabric API 0.91.0+ |
| 1.20.1 | Java 17 | Forge 47.2.0+ | - |
| 1.21.4 | Java 21 | Fabric 0.16.0+ | Fabric API 0.110.0+ |
| 1.21.4 | Java 21 | Forge 54.0.0+ | - |
| 1.21.4 | Java 21 | NeoForge 21.4.0+ | - |
| 26.1.2 | Java 21 | Fabric 0.16.0+ | Fabric API 0.119.0+ |
| 26.1.2 | Java 21 | Forge 54.0.0+ | - |
| 26.1.2 | Java 21 | NeoForge 21.4.0+ | - |

## 📂 Project Structure

```
undo-craft/
├── src/                    # Fabric 26.1.2 source code
├── forge/                  # Forge 26.1.2 port
├── neoforge/               # NeoForge 26.1.2 port
├── fabric-1.21.4/          # Fabric 1.21.4 port
├── forge-1.21.4/           # Forge 1.21.4 port
├── neoforge-1.21.4/        # NeoForge 1.21.4 port
├── fabric-1.20.1/          # Fabric 1.20.1 port
└── forge-1.20.1/           # Forge 1.20.1 port
```

## 🔨 Development

### Building

```bash
# Fabric 26.1.2
./gradlew build

# Forge/NeoForge ports
cd forge && ./gradlew build
cd neoforge && ./gradlew build

# 1.21.4 ports
cd fabric-1.21.4 && ./gradlew build
cd forge-1.21.4 && ./gradlew build
cd neoforge-1.21.4 && ./gradlew build

# 1.20.1 ports
cd fabric-1.20.1 && ./gradlew build
cd forge-1.20.1 && ./gradlew build
```

## 📝 License

This project is licensed under the MIT License.

```
MIT License

Copyright (c) 2026 Musa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

**Developer**: Musa
