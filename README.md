# ToolsMod (v1.0.1)

A utility Minecraft mod built with Fabric API, featuring advanced enchantment controls and real-time language switching.

---

## 🇷🇺 Русский

Мод добавляет администраторскую команду `/tools`, которая позволяет зачаровывать предметы выше стандартных игровых лимитов (например, Острота X и вплоть до 255 уровня), а также легко переключать язык интерфейса прямо в игре.

### 🛠 Требования для работы:
* **Java 25**
* **Fabric API 0.150.0 + 26.1.2**
* Права оператора/модератора на сервере (`Permissions.COMMANDS_MODERATOR`)

---

## 🇺🇸 English

Adds a `/tools` command to enchant items up to level 10 (like Sharpness X) and beyond (up to 255), with seamless on-the-fly switching between English and Russian languages.

### 🛠 Requirements:
* **Java 25**
* **Fabric API 0.150.0 + 26.1.2**
* Moderator permissions (`Permissions.COMMANDS_MODERATOR`)

---

## ⚙️ Использование команд / Commands Usage

| Команда / Command | Описание / Description |
| :--- | :--- |
| `/tools enchant <название> <уровень>` | Зачаровывает предмет в главной руке (от 1 до 255). |
| `/tools lang <en\|ru>` | Переключает язык системных уведомлений мода. |

### 📌 Техническая информация (Для разработчиков)
Логика регистрации команд и динамической валидации кавычек инкапсулирована в пакете:
`com.toolsmod.commands.EnchantCommand`