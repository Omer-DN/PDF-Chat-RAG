import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import { Sparkles, FileText, Upload, Send, Bot, User, Loader2, LogOut, History } from "lucide-react";

const API_BASE = import.meta.env.DEV ? "/api" : "http://localhost:8080/api";

const getStoredToken = () => localStorage.getItem("rag_token");
const getStoredUser = () => {
  try {
    const s = localStorage.getItem("rag_user");
    return s ? JSON.parse(s) : null;
  } catch {
    return null;
  }
};

function apiClient() {
  const token = getStoredToken();
  const client = axios.create({ baseURL: API_BASE });
  client.interceptors.request.use((config) => {
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
  return client;
}

export default function App() {
  const [token, setToken] = useState(getStoredToken);
  const [user, setUser] = useState(getStoredUser);
  const [authMode, setAuthMode] = useState("login");
  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState("");

  const [pdfId, setPdfId] = useState(null);
  const [selectedFileName, setSelectedFileName] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [askError, setAskError] = useState("");
  const [fileList, setFileList] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  const fileInputRef = useRef(null);
  const chatEndRef = useRef(null);

  const scrollToBottom = () => chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  useEffect(() => { scrollToBottom(); }, [messages, loading]);

  const fetchFileList = async () => {
    try {
      const { data } = await apiClient().get("/pdf/list");
      setFileList(Array.isArray(data) ? data : []);
    } catch {
      setFileList([]);
    }
  };

  useEffect(() => {
    if (token) fetchFileList();
  }, [token]);

  const loadFileFromHistory = async (id, filename) => {
    setHistoryLoading(true);
    setPdfId(id);
    setSelectedFileName(filename);
    setMessages([]);
    try {
      const { data } = await apiClient().get(`/pdf/${id}/history`);
      const list = Array.isArray(data) ? data : [];
      const next = [];
      list.forEach((item) => {
        next.push({ role: "user", text: item.question });
        next.push({ role: "assistant", text: item.answer });
      });
      setMessages(next);
    } catch {
      setMessages([]);
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    const form = e.target;
    const username = form.username?.value?.trim();
    const password = form.password?.value;
    if (!username || !password) {
      setAuthError("נא למלא שם משתמש וסיסמה.");
      return;
    }
    setAuthError("");
    setAuthLoading(true);
    try {
      const { data } = await axios.post(`${API_BASE}/auth/login`, { username, password });
      localStorage.setItem("rag_token", data.token);
      localStorage.setItem("rag_user", JSON.stringify({ id: data.userId, username: data.username, email: data.email }));
      setToken(data.token);
      setUser({ id: data.userId, username: data.username, email: data.email });
    } catch (err) {
      setAuthError(err.response?.data?.message || "שגיאה בהתחברות.");
    } finally {
      setAuthLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    const form = e.target;
    const username = form.username?.value?.trim();
    const email = form.email?.value?.trim();
    const password = form.password?.value;
    if (!username || !email || !password) {
      setAuthError("נא למלא את כל השדות.");
      return;
    }
    setAuthError("");
    setAuthLoading(true);
    try {
      const { data } = await axios.post(`${API_BASE}/auth/register`, { username, email, password });
      localStorage.setItem("rag_token", data.token);
      localStorage.setItem("rag_user", JSON.stringify({ id: data.userId, username: data.username, email: data.email }));
      setToken(data.token);
      setUser({ id: data.userId, username: data.username, email: data.email });
    } catch (err) {
      setAuthError(err.response?.data?.message || "שגיאה ברישום.");
    } finally {
      setAuthLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("rag_token");
    localStorage.removeItem("rag_user");
    setToken(null);
    setUser(null);
    setPdfId(null);
    setMessages([]);
  };

  const handleUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.type !== "application/pdf") {
      setUploadError("נא לבחור קובץ PDF בלבד.");
      return;
    }
    setUploadError("");
    setUploading(true);
    setPdfId(null);
    setMessages([]);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const { data } = await apiClient().post("/pdf/upload", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setPdfId(data.pdfId);
      setSelectedFileName(file.name);
      fetchFileList();
    } catch (err) {
      const status = err.response?.status;
      let msg = err.response?.data?.message;
      if (status === 401) msg = "יש להתחבר מחדש.";
      else if (status === 413) msg = "הקובץ גדול מדי. גודל מקסימלי 50MB.";
      else if (!msg) msg = err.message?.includes("Network") || !err.response ? "אין חיבור לשרת. וודא שהשרת רץ על פורט 8080." : "שגיאה בתקשורת עם השרת.";
      setUploadError(msg);
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  };

  const handleAsk = (e) => {
    e?.preventDefault();
    const q = question.trim();
    if (!q || !pdfId || loading) return;
    setAskError("");
    setMessages((prev) => [...prev, { role: "user", text: q }]);
    setQuestion("");
    setLoading(true);
    apiClient()
      .post(`/pdf/${pdfId}/ask`, { question: q })
      .then(({ data }) => {
        setMessages((prev) => [...prev, { role: "assistant", text: data.answer || "לא התקבלה תשובה מהשרת." }]);
      })
      .catch((err) => {
        const errorMsg = err.response?.status === 401 ? "יש להתחבר מחדש." : (err.response?.data?.message || "שגיאה בשליחת השאלה.");
        setMessages((prev) => [...prev, { role: "assistant", text: `מצטער, חלה שגיאה: ${errorMsg}` }]);
      })
      .finally(() => setLoading(false));
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleAsk(e);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-4" dir="rtl">
        <div className="auth-card w-full max-w-md rounded-2xl overflow-hidden p-8">
          <div className="flex items-center gap-3 mb-8">
            <div className="p-2 rounded-xl bg-cyan-500/20">
              <Sparkles className="w-8 h-8 text-cyan-400" />
            </div>
            <h1 className="text-2xl font-bold text-slate-100">RAG PDF AI</h1>
          </div>
          <div className="flex gap-1 mb-6 p-1 rounded-xl bg-slate-800/50">
            <button
              type="button"
              onClick={() => { setAuthMode("login"); setAuthError(""); }}
              className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors ${authMode === "login" ? "bg-cyan-500/30 text-cyan-300 shadow-lg" : "text-slate-400 hover:text-slate-200"}`}
            >
              התחברות
            </button>
            <button
              type="button"
              onClick={() => { setAuthMode("register"); setAuthError(""); }}
              className={`flex-1 py-2.5 rounded-lg text-sm font-medium transition-colors ${authMode === "register" ? "bg-cyan-500/30 text-cyan-300 shadow-lg" : "text-slate-400 hover:text-slate-200"}`}
            >
              הרשמה
            </button>
          </div>
          {authMode === "login" ? (
            <form onSubmit={handleLogin} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1.5">שם משתמש</label>
                <input name="username" type="text" className="w-full rounded-xl bg-slate-800/80 border border-slate-600 text-slate-100 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500/50 placeholder-slate-500" placeholder="username" autoComplete="username" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1.5">סיסמה</label>
                <input name="password" type="password" className="w-full rounded-xl bg-slate-800/80 border border-slate-600 text-slate-100 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 placeholder-slate-500" placeholder="••••••••" autoComplete="current-password" />
              </div>
              <button type="submit" disabled={authLoading} className="w-full bg-gradient-to-r from-cyan-500 to-cyan-600 text-white rounded-xl py-3.5 font-semibold hover:from-cyan-400 hover:to-cyan-500 disabled:opacity-50 flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/25">
                {authLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : null}
                התחבר
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1.5">שם משתמש</label>
                <input name="username" type="text" className="w-full rounded-xl bg-slate-800/80 border border-slate-600 text-slate-100 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 placeholder-slate-500" placeholder="username" autoComplete="username" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1.5">אימייל</label>
                <input name="email" type="email" className="w-full rounded-xl bg-slate-800/80 border border-slate-600 text-slate-100 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 placeholder-slate-500" placeholder="email@example.com" autoComplete="email" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-1.5">סיסמה</label>
                <input name="password" type="password" className="w-full rounded-xl bg-slate-800/80 border border-slate-600 text-slate-100 px-4 py-3 focus:outline-none focus:ring-2 focus:ring-cyan-500/50 placeholder-slate-500" placeholder="••••••••" autoComplete="new-password" />
              </div>
              <button type="submit" disabled={authLoading} className="w-full bg-gradient-to-r from-cyan-500 to-cyan-600 text-white rounded-xl py-3.5 font-semibold hover:from-cyan-400 hover:to-cyan-500 disabled:opacity-50 flex items-center justify-center gap-2 shadow-lg shadow-cyan-500/25">
                {authLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : null}
                הרשם
              </button>
            </form>
          )}
          {authError && <p className="mt-4 text-amber-400 text-sm text-center font-medium">{authError}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col p-3 md:p-4 relative z-10" dir="rtl">
      {/* אורבות צבעוניות ברקע */}
      <div className="bg-orbs" aria-hidden>
        <div className="bg-orb bg-orb-1" />
        <div className="bg-orb bg-orb-2" />
        <div className="bg-orb bg-orb-3" />
        <div className="bg-orb bg-orb-4" />
      </div>
      <div className="ai-dots" aria-hidden>
        {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((i) => (
          <div key={i} className="ai-dot" />
        ))}
      </div>
      <div className="ai-scanline" aria-hidden />

      <div className="flex-1 flex min-h-0 gap-3">
        {/* עמודה אנכית – היסטוריית קבצים */}
        <aside className="w-56 md:w-64 shrink-0 flex flex-col glass-panel rounded-2xl overflow-hidden border border-slate-700/30">
          <div className="shrink-0 flex items-center gap-2 px-4 py-3 border-b border-slate-700/50">
            <History className="w-4 h-4 text-cyan-400" />
            <span className="text-sm font-semibold text-slate-200">היסטוריית קבצים</span>
          </div>
          <div className="flex-1 overflow-y-auto p-2 history-sidebar">
            {fileList.length === 0 ? (
              <p className="text-slate-500 text-sm p-3 text-center">אין קבצים. העלה PDF או בחר מסמך קיים.</p>
            ) : (
              <ul className="space-y-1.5">
                {fileList.map((f) => (
                  <li key={f.id}>
                    <button
                      type="button"
                      onClick={() => loadFileFromHistory(f.id, f.filename)}
                      className={`file-item w-full text-right rounded-xl px-3 py-2.5 border ${pdfId === f.id ? "bg-cyan-500/20 border-cyan-400/50 text-cyan-100" : "bg-slate-700/30 border-slate-600/50 text-slate-200 hover:bg-slate-700/50 hover:border-cyan-500/30"}`}
                    >
                      <span className="block truncate text-sm font-medium" title={f.filename}>{f.filename}</span>
                      <span className="block text-xs text-slate-500 mt-0.5">{f.uploadedAt?.slice(0, 10)}</span>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </aside>

        {/* חלון ראשי – צ'אט */}
        <div className="flex-1 min-w-0 flex flex-col glass-panel rounded-2xl overflow-hidden">
        {/* Header */}
        <header className="shrink-0 flex items-center justify-between px-4 md:px-6 py-3 border-b border-slate-700/50">
          <div className="flex items-center gap-3 min-w-0">
            <div className="p-1.5 rounded-xl bg-cyan-500/20">
              <Sparkles className="w-6 h-6 text-cyan-400" />
            </div>
            <h1 className="text-lg font-bold text-slate-100 truncate">RAG PDF AI</h1>
            {pdfId && (
              <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/20 border border-emerald-400/30 max-w-[200px] min-w-0" title={selectedFileName || ""}>
                <div className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse shrink-0" />
                <span className="truncate text-sm text-emerald-200">{selectedFileName || "מסמך"}</span>
              </div>
            )}
          </div>
          <div className="flex items-center gap-2 shrink-0">
            <span className="text-sm text-slate-400 truncate max-w-[120px]">{user?.username}</span>
            <button type="button" onClick={handleLogout} className="p-2 rounded-xl text-slate-400 hover:text-slate-200 hover:bg-slate-700/50 transition-colors" title="התנתק">
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </header>

        {/* אזור הצ'אט */}
        <div className="flex-1 min-h-0 flex flex-col overflow-hidden">
          <div className="flex-1 overflow-y-auto p-4 md:p-6 space-y-4 chat-container">
            {historyLoading && (
              <div className="flex justify-center py-8">
                <Loader2 className="w-10 h-10 text-cyan-400 animate-spin" />
              </div>
            )}
            {messages.length === 0 && !loading && !historyLoading && (
              <div className="h-full min-h-[280px] flex flex-col items-center justify-center text-slate-500">
                <Bot className="w-16 h-16 text-slate-600 mb-4" strokeWidth={1} />
                <p className="text-lg">{pdfId ? "מה תרצה לדעת על המסמך?" : "טען קובץ PDF או בחר מסמך מההיסטוריה כדי להתחיל"}</p>
              </div>
            )}
            {messages.map((m, i) => (
              <div key={i} className={`flex gap-3 message-bubble ${m.role === "user" ? "flex-row-reverse" : "flex-row"}`}>
                <div className={`flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center ${m.role === "user" ? "bg-cyan-500/30 text-cyan-300" : "bg-slate-600/50 text-slate-300"}`}>
                  {m.role === "user" ? <User size={20} /> : <Bot size={20} />}
                </div>
                <div className={`max-w-[85%] rounded-2xl px-4 py-3 ${m.role === "user" ? "bg-cyan-500/25 text-cyan-100 border border-cyan-400/20 rounded-tr-md" : "bg-slate-700/60 text-slate-200 border border-slate-600/50 rounded-tl-md"}`}>
                  <p className="text-sm leading-relaxed whitespace-pre-wrap">{m.text}</p>
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex gap-3">
                <div className="w-10 h-10 rounded-xl bg-slate-600/50 flex items-center justify-center shrink-0">
                  <Bot className="w-5 h-5 text-slate-400" />
                </div>
                <div className="rounded-2xl rounded-tl-md px-4 py-3 bg-slate-700/60 border border-slate-600/50">
                  <div className="flex gap-1.5">
                    <span className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce" />
                    <span className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce [animation-delay:0.15s]" />
                    <span className="w-2 h-2 bg-cyan-400 rounded-full animate-bounce [animation-delay:0.3s]" />
                  </div>
                </div>
              </div>
            )}
            <div ref={chatEndRef} />
          </div>

          {/* שורת שליחה + טעינת קבצים */}
          <div className="shrink-0 p-4 border-t border-slate-700/50 bg-slate-900/40">
            {uploadError && (
              <p className="text-amber-400 text-sm mb-2 text-center font-medium">{uploadError}</p>
            )}
            {askError && (
              <p className="text-amber-400 text-sm mb-2 text-center font-medium">{askError}</p>
            )}
            <div className="flex gap-2 flex-row-reverse">
              <input
                type="text"
                value={question}
                onChange={(e) => setQuestion(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder={pdfId ? "שאל שאלה על המסמך... (Enter לשליחה)" : "טען מסמך או בחר מההיסטוריה"}
                className="flex-1 rounded-xl bg-slate-800 border border-slate-600 text-slate-100 px-4 py-3.5 focus:outline-none input-glow placeholder-slate-500 disabled:opacity-60 disabled:cursor-not-allowed min-w-0"
                disabled={!pdfId || loading}
              />
              <button
                type="button"
                onClick={handleAsk}
                disabled={!pdfId || loading || !question.trim()}
                className="btn-send rounded-xl px-5 py-3.5 text-white font-medium flex items-center justify-center shrink-0"
              >
                {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Send className="w-5 h-5" />}
              </button>
              <input ref={fileInputRef} type="file" accept="application/pdf" onChange={handleUpload} className="hidden" disabled={uploading} />
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploading}
                className="btn-upload rounded-xl px-4 py-3.5 text-slate-300 font-medium flex items-center justify-center gap-2 shrink-0"
                title="טען קובץ PDF"
              >
                {uploading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Upload className="w-5 h-5" />}
              </button>
            </div>
          </div>
        </div>
        </div>
      </div>
    </div>
  );
}
