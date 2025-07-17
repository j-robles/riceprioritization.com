# RICE Prioritization Framework

This is the source code for [https://riceprioritization.com](https://riceprioritization.com). It is a modern, single-file Preact application that helps product managers and teams prioritize projects using the RICE scoring method.

---

## 🚀 Modernized (2025)

- **No build step, no dependencies** — just open `index.html` or serve it statically
- **Preact** for fast, minimal React-like UI
- **Modern CSS** — responsive, mobile-friendly, beautiful
- **All data stays local** — no backend, no tracking, no external APIs
- **Export/Import** — Save and load projects as JSON files
- **Accessibility** — ARIA labels, keyboard navigation, screen reader support
- **Security** — Input validation, CSP, no external scripts

---

## 🛠️ Development & Local Use

1. **Clone this repo**
2. **Serve the site locally** (choose one):

   - With Python 3:
     ```bash
     python3 -m http.server 8000
     # Then open http://localhost:8000 in your browser
     ```
   - Or use any static file server (e.g. `serve`, `http-server`, or just open `index.html` directly)

3. **No build, no install, no Java, no npm required!**

---

## 📱 Features

- **RICE scoring**: (Reach × Impact × Confidence) ÷ Effort
- **Export/Import**: Save/load your projects as JSON
- **URL sharing**: State is encoded in the URL for easy sharing
- **Responsive**: Works great on desktop and mobile
- **Accessibility**: Keyboard and screen reader friendly
- **Security**: All data is local, no tracking, strong CSP

---

## 📊 RICE Framework

The RICE prioritization method evaluates projects based on:

- **R**each: How many people will be affected
- **I**mpact: How much impact it will have (huge/high/mid/low/tiny)
- **C**onfidence: How confident you are in estimates (high/mid/low)
- **E**ffort: How much work it will take

**Formula**: `(Reach × Impact × Confidence) / Effort`

---

## 🤝 Contributing

This is a fork of the original project, now fully static and modernized for easy hosting and maintenance.

---

## 📄 License

Original project by [@mccrmx](https://twitter.com/mccrmx)
