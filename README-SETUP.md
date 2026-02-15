# RAG PDF AI – הגדרה והרצה

## דרישות

- Java 17+
- PostgreSQL (מסד `rag_llm`)
- Node.js 18+ (לממשק rag-ui)
- **מפתח API של Gemini** מ-[Google AI Studio](https://aistudio.google.com/apikey)

## אבטחת מפתחות (חובה)

**אל תשמור מפתחות API בתוך הקוד או ב-Git.**

### Gemini API

הגדר משתנה סביבה לפני הרצת השרת:

**Windows (PowerShell):**
```powershell
$env:GEMINI_API_KEY = "המפתח-שלך-מ-AI-Studio"
```

**Windows (CMD):**
```cmd
set GEMINI_API_KEY=המפתח-שלך-מ-AI-Studio
```

**Linux / macOS:**
```bash
export GEMINI_API_KEY="המפתח-שלך-מ-AI-Studio"
```

### JWT (אופציונלי בפרודקשן)

בפרודקשן מומלץ להגדיר סוד חזק:

```bash
export JWT_SECRET="מחרוזת-ארוכה-ואקראית-לפחות-32-תווים"
```

אם לא מוגדר – משתמשים בברירת מחדל לפיתוח בלבד.

## מסד נתונים

1. צור מסד PostgreSQL בשם `rag_llm`.
2. הרץ את הטבלאות (אופציונלי אם `spring.jpa.hibernate.ddl-auto=update` פעיל):
   ```bash
   psql -U postgres -d rag_llm -f src/main/resources/schema.sql
   ```
3. וודא ב-`application.properties` את ה-URL, שם המשתמש והסיסמה של ה-DB.

## הרצת השרת (Backend)

```bash
# וודא ש-GEMINI_API_KEY מוגדר
mvn spring-boot:run
```

השרת יעלה על פורט 8080.

## הרצת הממשק (rag-ui)

```bash
cd rag-ui
npm install
npm run dev
```

פתח בדפדפן את הכתובת שמופיעה (בדרך כלל http://localhost:5173). הממשק משתמש ב-proxy ל-API על פורט 8080.

## סיכום שינויים בפרויקט

- **Auth:** רישום והתחברות (JWT), טבלת `users`.
- **PDF:** העלאת קבצים (עד 50MB), טבלאות `pdf_files` (עם `user_id`), `pdf_chunks` (עם embedding).
- **שאלות ותשובות:** שמירה ב-`question_answers`, היסטוריה למשתמש ולמסמך.
- **ממשק:** חלון אחד, היסטוריית קבצים אנכית, רקע ואפקטים, כפתור העלאה מימין לשדה הכתיבה.
- **Gemini:** מודל `gemini-2.0-flash`, API v1beta ל-generateContent.
- **אבטחה:** מפתח Gemini רק ממשתנה סביבה `GEMINI_API_KEY`, ללא מפתחות ב-Git.
