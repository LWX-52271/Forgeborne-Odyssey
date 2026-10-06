# Forgeborne-Odyssey

## 可选联动（Optional Integration）

### Cold Sweat
- **可选前置（soft dependency）**，非必需。未安装时模组完整正常运行，无任何影响。
- 安装 [Cold Sweat](https://www.curseforge.com/minecraft/mc-mods/cold-sweat) 后，**兽皮盔甲**（兽皮帽、兽皮衣、兽皮裤、兽皮鞋）将提供**冷绝缘**，帮助玩家在寒冷环境下维持体温。
- 联动为**纯数据驱动**，无任何 Java 依赖：`data/forgeborneodyssey/cold_sweat/item/insulator/hide_*.json`（头盔/胸甲/护腿/靴子各一文件）。
- 每件兽皮甲提供 **20%** 寒冷衰减（`cold_sweat:cold_dampening`），全套共 **80%**，显著延迟体温下降。