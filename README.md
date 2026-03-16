# KotlinDevKeyboard

⌨️ **An Android keyboard (IME) optimized for Kotlin development.**

Symbols that are tedious to type on a standard keyboard — such as `=`, `{}`, and `->` — are accessible from a single screen using flick gestures, long-press, and quick snippets to keep the layout compact and coding-focused.

---

## 📝 About This Project
This project was created as a practical exercise to learn Android app development with **Kotlin** and **Jetpack Compose**.

* **AI-assisted**: AI tools (e.g., Gemini, ChatGPT) were used for brainstorming and documentation generation.
* **Work in progress**: Features, layouts, and snippet lists are actively refined.

---

## ✨ Features
* **Symbol Row** — Frequently used operators and brackets on the top row for quick access.
* **Flick Input** — Swipe a key to input related symbols without switching layouts.
* **Snippet Search (SNP)** — Quickly insert Kotlin keywords or templates via the SNP key.
* **Dynamic Filtering** — While holding the SNP key, type a letter (e.g., `f`) to filter snippets like `fun` or `for`.
* **Space Swipe** — Move the cursor left / right / up / down by swiping the Space key.
* **Tab / ESC Keys** — Dedicated keys for smoother code editing.
* **Quick Number Input** — Numbers can be entered by long-pressing or flicking up on alphabet keys.
* **Theme Support** — Dark, Light, and System themes supported (Dynamic color on Android 12+).

---

## ⌨️ Key Reference
*Note: **SNP** = Snippet key.*

### Symbol Row
| Key | Tap | Long press | Flick Up | Flick Down | Flick Left | Flick Right |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **ESC** | ESC | - | - | - | - | - |
| **SNP** | SNP | - | - | - | - | - |
| **+** | `+` | - | `*` | `%` | `-` | `/` |
| **()** | `()` | - | - | - | `(` | `)` |
| **{}** | `{}` | - | - | - | `{` | `}` |
| **[]** | `[]` | - | - | - | `[` | `]` |
| **$** | `$` | - | `!` | `#` | `&` | `\|` |
| **"** | `"` | `'` | - | - | - | - |
| **:** | `:` | `;` | - | - | - | - |
| **=** | `=` | - | `==` | `!=` | - | - |

### QWERTY Row
| Key | Tap | Long press / Flick Up |
| :---: | :---: | :---: |
| **q – p** | letters (q..p) | **numbers 1–0** (mapped respectively) |

### Bottom Row
| Key | Tap | Flick Up | Flick Down | Flick Left | Flick Right |
| :---: | :---: | :---: | :---: | :---: | :---: |
| **🌐** | Switch IME | - | - | - | - |
| **,** | `,` | `_` | `\` | `<` | - |
| **Space** | Space (Swipe to move cursor) | - | - | - | - |
| **.** | `.` | `?` | `/` | - | `>` |
| **↵** | Enter | - | - | - | - |

---

## 📦 Built-in Snippets (SNP)
The SNP key provides quick access to Kotlin-specific templates. The snippet list is evolving.

### How snippets work
1.  **Open**: Tap the **SNP** key to open the snippet panel anchored to the top row.
2.  **Insert**: Tap a snippet to immediately insert it at the cursor.
3.  **Filter**: While holding the **SNP** key, type a letter (e.g., `f`) to filter snippets that start with that letter. Selecting a filtered snippet (e.g., `fun`) inserts its code exactly as shown.

### Example snippet entries
* `fun` → `fun name(params) { }`
* `for` → `for (item in collection) { }`
* `ifn` → `if (obj == null) { }` (null-safety helper)

### Categories
`OPERATOR`, `CONTROL`, `DECLARATION`, `MODIFIER`, `SCOPE`, `COLLECTION`, `COROUTINE`, `COMPOSE`, `NULL_SAFETY`, `STRING`, `CONVERT`, `EXCEPTION`, `OUTPUT`

---

## 🛠 Tech Stack & Requirements
* **Language**: Kotlin
* **UI**: Jetpack Compose
* **Core API**: InputMethodService (Android IME)
* **Storage**: SharedPreferences (for user settings / theme)
* **SDK**: minSdk 30 / targetSdk 36
* **Build**: Android Studio (BOM compatible)

---

## ⚙️ Setup — Build & Install (from source)
1.  **Clone the repo**:
    ```bash
    git clone [https://github.com/](https://github.com/)<your-username>/KotlinDevKeyboard.git
    ```
2.  **Open** the project in Android Studio.
3.  **Build & run** on a physical device.
4.  **Enable and select the keyboard**:
    * Settings → System → Languages & input → On-screen keyboard → Manage on-screen keyboards → enable **KotlinDevKeyboard**.
    * Then tap the keyboard icon in the navigation bar and select **KotlinDevKeyboard**.

---

## 🔒 Privacy & Permissions
* **Permissions**: This app requires `BIND_INPUT_METHOD` to operate as a keyboard.
* **Data handling**: The app does **not** collect or transmit typed input off-device. Only non-sensitive settings (theme, snippet prefs) are stored locally in SharedPreferences.
* **Transparency**: Because IMEs can access typed text, this project maintains a strict no-telemetry policy.

---

## 🧪 Testing
* **Tested on**: Xperia device — Android 14

---

## 📄 License
This project is licensed under the **Apache License 2.0**. See the [LICENSE](LICENSE) file for details.
