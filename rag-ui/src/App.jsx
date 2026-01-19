import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Sparkles, FileText, Upload, Send, Bot, User, Loader2, LogIn, UserPlus, LogOut, Key } from 'lucide-react';

const API_BASE = "http://localhost:8080/api";

export default function App() {
    // --- States ---
    const [user, setUser] = useState(null);
    const [isLoginMode, setIsLoginMode] = useState(true); // מעבר בין התחברות לרישום
    const [authData, setAuthData] = useState({ username: '', email: '', password: '' });

    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [files, setFiles] = useState([]);
    const [currentFile, setCurrentFile] = useState(null);

    const [isLoading, setIsLoading] = useState(false);
    const [isActionLoading, setIsActionLoading] = useState(false);

    // --- Refs ---
    const chatEndRef = useRef(null);
    const fileInputRef = useRef(null);

    useEffect(() => {
        chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, isLoading]);

    // --- Auth Functions ---

    const handleAuth = async () => {
        // ולידציה בסיסית
        if (!authData.email || !authData.password || (!isLoginMode && !authData.username)) {
            alert("נא למלא את כל השדות הדרושים");
            return;
        }

        setIsActionLoading(true);
        try {
            const endpoint = isLoginMode ? "/users/login" : "/users/register";
            // ב-Login שולחים רק email ו-password. ב-Register שולחים הכל.
            const payload = isLoginMode
                ? { email: authData.email, password: authData.password }
                : authData;

            const res = await axios.post(`${API_BASE}${endpoint}`, payload);

            setUser(res.data);
            setMessages([{
                id: 'welcome',
                text: `שלום ${res.data.username}, ברוך הבא למערכת!`,
                isBot: true
            }]);
        } catch (err) {
            alert(isLoginMode ? "פרטי התחברות שגויים או משתמש לא קיים" : "שגיאה ברישום המשתמש");
        } finally {
            setIsActionLoading(false);
        }
    };

    const handleLogout = () => {
        setUser(null);
        setFiles([]);
        setMessages([]);
        setCurrentFile(null);
        setAuthData({ username: '', email: '', password: '' });
    };

    // --- File & Chat Functions ---

    const handleUpload = async (e) => {
        const file = e.target.files[0];
        if (!file || !user) return;
        setIsActionLoading(true);
        const formData = new FormData();
        formData.append('file', file);
        formData.append('userId', user.id);
        try {
            const res = await axios.post(`${API_BASE}/pdf/upload`, formData);
            const newFile = { id: res.data.pdfId, name: file.name };
            setFiles(prev => [newFile, ...prev]);
            setCurrentFile(newFile);
            if (fileInputRef.current) fileInputRef.current.value = "";
        } catch (err) {
            if (fileInputRef.current) fileInputRef.current.value = "";
            alert("שגיאה בהעלאת הקובץ");
        } finally {
            setIsActionLoading(false);
        }
    };

    const sendMessage = async () => {
        if (!input.trim() || !currentFile || isLoading) return;
        const question = input;
        setInput('');
        setMessages(prev => [...prev, { id: Date.now(), text: question, isBot: false }]);
        setIsLoading(true);
        try {
            const formData = new FormData();
            formData.append('pdfId', currentFile.id.toString());
            formData.append('userId', user.id.toString());
            formData.append('question', question);
            const res = await axios.post(`${API_BASE}/rag/ask`, formData);
            setMessages(prev => [...prev, { id: Date.now(), text: res.data.answer, isBot: true }]);
        } catch (err) {
            setMessages(prev => [...prev, { id: Date.now(), text: "שגיאה בתקשורת עם Gemini.", isBot: true }]);
        } finally {
            setIsLoading(false);
        }
    };

    // --- UI Views ---

    if (!user) {
        return (
            <div className="h-screen w-full flex items-center justify-center p-4 bg-slate-50" dir="rtl">
                <div className="glass-strong p-10 rounded-[2.5rem] shadow-2xl max-w-md w-full text-center message-bubble">
                    <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-lg text-white">
                        {isLoginMode ? <LogIn size={32} /> : <UserPlus size={32} />}
                    </div>

                    <h1 className="text-3xl font-bold gradient-text mb-2">
                        {isLoginMode ? "התחברות" : "יצירת חשבון"}
                    </h1>
                    <p className="text-slate-500 mb-8 font-medium">
                        {isLoginMode ? "שמחים לראות אותך שוב!" : "הצטרף אלינו עוד היום"}
                    </p>

                    <div className="space-y-4">
                        {!isLoginMode && (
                            <input type="text" placeholder="שם משתמש" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all font-semibold"
                                   onChange={e => setAuthData({...authData, username: e.target.value})} />
                        )}
                        <input type="email" placeholder="אימייל" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all font-semibold"
                               onChange={e => setAuthData({...authData, email: e.target.value})} />
                        <input type="password" placeholder="סיסמה" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all font-semibold"
                               onChange={e => setAuthData({...authData, password: e.target.value})} />

                        <button onClick={handleAuth} disabled={isActionLoading} className="w-full py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-xl font-bold text-lg shadow-lg active:scale-95 transition-all">
                            {isActionLoading ? <Loader2 className="animate-spin mx-auto" /> : (isLoginMode ? "כניסה" : "הירשם עכשיו")}
                        </button>

                        <button
                            onClick={() => setIsLoginMode(!isLoginMode)}
                            className="text-blue-600 font-bold mt-4 hover:underline text-sm"
                        >
                            {isLoginMode ? "אין לך חשבון? הירשם כאן" : "כבר יש לך חשבון? התחבר כאן"}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="flex h-screen" dir="rtl">
            {/* Sidebar */}
            <aside className="w-72 glass-strong border-l border-white/20 flex flex-col shadow-2xl z-20">
                <div className="p-6 border-b flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center shadow-md">
                        <Sparkles className="w-6 h-6 text-white" />
                    </div>
                    <h1 className="font-bold text-lg gradient-text truncate">שלום, {user.username}</h1>
                </div>
                <div className="flex-1 overflow-y-auto p-4 chat-container">
                    <div className="text-xs font-bold text-slate-400 uppercase mb-4 tracking-widest flex items-center gap-2">
                        <FileText size={14} /> המסמכים שלי
                    </div>
                    {files.map(f => (
                        <div key={f.id} onClick={() => setCurrentFile(f)} className={`p-3 rounded-xl mb-2 cursor-pointer transition-all flex items-center gap-3 ${currentFile?.id === f.id ? 'bg-blue-600 text-white shadow-lg' : 'bg-white/50 hover:bg-white/80'}`}>
                            <FileText className="w-4 h-4 shrink-0" />
                            <span className="truncate text-sm font-medium">{f.name}</span>
                        </div>
                    ))}
                </div>
                <div className="p-4 border-t border-white/20">
                    <button onClick={handleLogout} className="w-full flex items-center justify-center gap-2 p-3 text-red-500 hover:bg-red-50 rounded-xl transition-all font-bold">
                        <LogOut size={20} /> התנתק
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 flex flex-col relative bg-transparent">
                <header className="glass-strong border-b p-5 flex justify-between items-center shadow-lg z-10">
                    <h2 className="text-xl font-bold gradient-text">AI PDF Analyzer</h2>
                    <label className="cursor-pointer bg-gradient-to-r from-blue-600 to-purple-600 text-white px-5 py-2 rounded-xl shadow-md font-semibold active:scale-95 transition-all flex items-center gap-2">
                        {isActionLoading ? <Loader2 className="animate-spin w-5 h-5" /> : <Upload className="w-5 h-5" />}
                        <span>העלה PDF</span>
                        <input type="file" ref={fileInputRef} className="hidden" accept=".pdf" onChange={handleUpload} />
                    </label>
                </header>

                <div className="flex-1 overflow-y-auto p-6 space-y-4 chat-container">
                    <div className="max-w-4xl mx-auto">
                        {messages.map((m, i) => (
                            <div key={i} className={`flex ${m.isBot ? 'justify-end' : 'justify-start'} mb-6`}>
                                <div className={`p-5 rounded-3xl shadow-xl message-bubble border ${m.isBot ? 'bg-gradient-to-br from-blue-600 to-purple-700 text-white' : 'bg-white text-slate-800 border-slate-100'}`}>
                                    {m.text}
                                </div>
                            </div>
                        ))}
                        <div ref={chatEndRef} />
                    </div>
                </div>

                <footer className="p-6 glass-strong shadow-2xl">
                    <div className="max-w-4xl mx-auto flex gap-3">
                        <input value={input} onChange={e => setInput(e.target.value)} onKeyPress={e => e.key === 'Enter' && sendMessage()}
                               placeholder={currentFile ? "שאל שאלה..." : "העלה קובץ תחילה..."} disabled={!currentFile || isLoading}
                               className="flex-1 bg-white/90 border-2 border-slate-200 rounded-2xl px-6 py-4 focus:border-blue-500 outline-none text-lg shadow-lg" />
                        <button onClick={sendMessage} disabled={!currentFile || isLoading} className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-4 rounded-xl shadow-lg">
                            <Send size={24} />
                        </button>
                    </div>
                </footer>
            </main>
        </div>
    );
}