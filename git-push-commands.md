# פקודות Git – דחיפה לריפו

אם מופיעה שגיאה על `index.lock`, סגור חלונות/תהליכים של Git (ממשק גרפי, טרמינל אחר) ואז הרץ:

```bash
# 1. מעבר לתיקיית הפרויקט
cd "c:\Users\USER\IdeaProjects\practice\modern-backend\rag-llm"

# 2. אם index.lock קיים – מחק (רק אם אין תהליך Git אחר פתוח)
del .git\index.lock

# 3. הוספת .gitignore (מתעלם מ-node_modules, target, .idea)
git add .gitignore rag-ui/.gitignore

# 4. הוספת כל שינויי הקוד (ללא node_modules thanks to .gitignore)
git add pom.xml src/main/java src/main/resources rag-ui

# 5. וידוא ש-node_modules לא נכנס (אם נכנס – הסר מהמעקב)
git reset HEAD rag-ui/node_modules 2>nul
git reset HEAD rag-ui/.vite 2>nul

# 6. קומיט
git commit -m "RAG: Auth (JWT), users, Q&A history, UI redesign, upload limit 50MB, Gemini v1beta"

# 7. דחיפה (אם יש diverged – אולי תצטרך git pull --rebase לפני push)
git push origin gemini-prod-fixes
```

אם ה-branch התפצל מ-origin (יש לך 2 קומיטים ו-18 ב-origin), לפני `push` הרץ אחד מהבאים:

```bash
git pull origin gemini-prod-fixes --rebase
# ואז
git push origin gemini-prod-fixes
```

או אם אתה רוצה לדחוף את ההיסטוריה המקומית שלך (משכתב את הרימוט):

```bash
git push origin gemini-prod-fixes --force-with-lease
```

**אזהרה:** `--force-with-lease` מתאים רק אם אתה בטוח שאף אחד לא דחף קומיטים חדשים ל-`gemini-prod-fixes` שאתה רוצה לשמור.
