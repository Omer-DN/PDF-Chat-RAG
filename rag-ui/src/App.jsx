import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Sparkles, FileText, Upload, Send, Bot, User, Loader2, LogIn, Info } from 'lucide-react';

const API_BASE = "http://localhost:8080/api";

export default function App() {
    // State לניהול המשתמש
    const [user, setUser] = useState(null);
    const [regData, setRegData] = useState({ username: '', email: '', password: '' });

    // State לניהול הצ'אט והקבצים
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [files, setFiles] = useState([]);
    const [currentFile, setCurrentFile] = useState(null);

    // State למצבי טעינה
    const [isLoading, setIsLoading] = useState(false);
    const [isActionLoading, setIsActionLoading] = useState(false);

    const chatEndRef = useRef(null);
    useEffect(() => { chatEndRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

    // פונקציית רישום משתמש
    const handleRegister = async () => {
        if (!regData.username || !regData.email || !regData.password) return;
        setIsActionLoading(true);
        try {
            const res = await axios.post(`${API_BASE}/users/register`, regData);
            setUser(res.data); // שמירת המשתמש כולל ה-ID מה-Backend
            setMessages([{ id: 'w', text: `ברוך הבא ${res.data.username}! העלה PDF כדי להתחיל.`, isBot: true }]);
        } catch (err) {
            alert("שגיאה ברישום. וודא שה-Backend רץ.");
        } finally {
            setIsActionLoading(false);
        }
    };

    // פונקציית העלאת קובץ (משתמשת ב-user.id)
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
            setMessages(prev => [...prev, { id: Date.now(), text: `קובץ "${file.name}" נטען בהצלחה.`, isBot: true }]);
        } catch (err) {
            setMessages(prev => [...prev, { id: Date.now(), text: "שגיאה בטעינת הקובץ.", isBot: true }]);
        } finally {
            setIsActionLoading(false);
        }
    };

    // פונקציית שליחת שאלה
    const sendMessage = async () => {
        if (!input.trim() || !currentFile || isLoading) return;
        const question = input;
        setInput('');
        setMessages(prev => [...prev, { id: Date.now(), text: question, isBot: false }]);
        setIsLoading(true);

        try {
            const formData = new FormData();
            formData.append('pdfId', currentFile.id);
            formData.append('userId', user.id);
            formData.append('question', question);

            const res = await axios.post(`${API_BASE}/rag/ask`, formData);
            setMessages(prev => [...prev, { id: Date.now(), text: res.data.answer, isBot: true }]);
        } catch (err) {
            setMessages(prev => [...prev, { id: Date.now(), text: "שגיאה בתקשורת עם Gemini.", isBot: true }]);
        } finally {
            setIsLoading(false);
        }
    };

    // תצוגת מסך רישום (אם אין משתמש)
    if (!user) {
        return (
            <div className="h-screen w-full flex items-center justify-center p-4 bg-transparent" dir="rtl">
                <div className="glass-strong p-10 rounded-[2.5rem] shadow-2xl max-w-md w-full text-center message-bubble">
                    <div className="w-16 h-16 bg-gradient-to-br from-blue-600 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-lg">
                        <LogIn className="text-white w-8 h-8" />
                    </div>
                    <h1 className="text-3xl font-bold gradient-text mb-6">יצירת חשבון</h1>
                    <div className="space-y-4">
                        <input type="text" placeholder="שם משתמש" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all"
                               onChange={e => setRegData({...regData, username: e.target.value})} />
                        <input type="email" placeholder="אימייל" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all"
                               onChange={e => setRegData({...regData, email: e.target.value})} />
                        <input type="password" placeholder="סיסמה" className="w-full p-4 border-2 border-slate-100 rounded-xl outline-none focus:border-blue-500 transition-all"
                               onChange={e => setRegData({...regData, password: e.target.value})} />
                        <button onClick={handleRegister} disabled={isActionLoading} className="w-full py-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white rounded-xl font-bold text-lg shadow-lg active:scale-95 transition-all">
                            {isActionLoading ? <Loader2 className="animate-spin mx-auto" /> : "התחברות"}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    // תצוגת הצ'אט (אם יש משתמש)
    return (
        <div className="flex h-screen" dir="rtl">
            <aside className="w-72 glass-strong border-l border-white/20 flex flex-col shadow-2xl">
                <div className="p-6 border-b flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center shadow-md">
                        <Sparkles className="w-6 h-6 text-white" />
                    </div>
                    <h1 className="font-bold text-lg gradient-text truncate">שלום, {user.username}</h1>
                </div>
                <div className="flex-1 overflow-y-auto p-4 chat-container">
                    <div className="text-xs font-bold text-slate-400 uppercase mb-4">מסמכים פעילים</div>
                    {files.map(f => (
                        <div key={f.id} onClick={() => setCurrentFile(f)} className={`p-3 rounded-xl mb-2 cursor-pointer transition-all ${currentFile?.id === f.id ? 'bg-blue-600 text-white shadow-md' : 'bg-white/50'}`}>
                            <FileText className="inline ml-2 w-4 h-4" /> {f.name}
                        </div>
                    ))}
                </div>
            </aside>

            <main className="flex-1 flex flex-col relative">
                <header className="glass-strong border-b p-5 flex justify-between items-center shadow-lg">
                    <h2 className="text-xl font-bold gradient-text">צ'אט RAG חכם</h2>
                    <label className="cursor-pointer bg-gradient-to-r from-blue-600 to-purple-600 text-white px-5 py-2 rounded-xl shadow-md font-semibold active:scale-95 transition-all">
                        {isActionLoading ? <Loader2 className="animate-spin w-5 h-5" /> : <Upload className="inline ml-2 w-5 h-5" />}
                        {isActionLoading ? 'מעלה...' : 'העלה PDF'}
                        <input type="file" className="hidden" accept=".pdf" onChange={handleUpload} />
                    </label>
                </header>

                <div className="flex-1 overflow-y-auto p-6 space-y-4 chat-container">
                    <div className="max-w-4xl mx-auto">
                        {messages.map((m, i) => (
                            <div key={i} className={`flex ${m.isBot ? 'justify-end' : 'justify-start'} mb-6`}>
                                <div className={`p-5 rounded-3xl shadow-xl message-bubble border border-white/20 ${m.isBot ? 'bg-gradient-to-br from-blue-600 to-purple-600 text-white' : 'bg-white text-slate-800'}`}>
                                    {m.text}
                                </div>
                            </div>
                        ))}
                        {isLoading && <div className="text-center italic text-white/70 animate-pulse">Gemini חושב...</div>}
                        <div ref={chatEndRef} />
                    </div>
                </div>

                <footer className="p-6 glass-strong shadow-2xl">
                    <div className="max-w-4xl mx-auto flex gap-3">
                        <input value={input} onChange={e => setInput(e.target.value)} onKeyPress={e => e.key === 'Enter' && sendMessage()}
                               placeholder={currentFile ? "שאל שאלה..." : "העלה קובץ להתחלה"} disabled={!currentFile || isLoading}
                               className="flex-1 bg-white/90 border-2 border-slate-200 rounded-2xl px-6 py-4 focus:border-blue-500 outline-none text-lg shadow-lg" />
                        <button onClick={sendMessage} disabled={!currentFile || isLoading} className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-4 rounded-xl shadow-lg active:scale-95 disabled:opacity-50">
                            <Send />
                        </button>
                    </div>
                </footer>
            </main>
        </div>
    );
}