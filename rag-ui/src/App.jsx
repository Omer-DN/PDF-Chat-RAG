import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Sparkles, FileText, Upload, Send, Bot, User, Loader2, LogIn, UserPlus, LogOut, Key, Trash2, X, Linkedin } from 'lucide-react';

// API Base URL - uses environment variable in production, localhost in development
const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

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

    // Load user files when user logs in
    useEffect(() => {
        if (user) {
            loadUserFiles();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [user]);

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
            // Files will be loaded by useEffect when user is set
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

    // Load user files from server
    const loadUserFiles = async () => {
        if (!user) return;
        try {
            const res = await axios.get(`${API_BASE}/pdf/user/${user.id}`);
            const loadedFiles = res.data.map(file => ({
                id: file.id,
                name: file.filename
            }));
            setFiles(loadedFiles);
            
            // If there are files, select the first one and load its history
            if (loadedFiles.length > 0) {
                const firstFile = loadedFiles[0];
                // If no current file or current file not in list, select first file
                if (!currentFile || !loadedFiles.some(f => f.id === currentFile.id)) {
                    await selectFile(firstFile);
                } else {
                    // Reload history for current file
                    await loadChatHistory(currentFile.id);
                }
            }
        } catch (err) {
            console.error("Error loading user files:", err);
        }
    };

    // Load chat history for a specific file
    const loadChatHistory = async (fileId) => {
        if (!user || !fileId) return;
        try {
            const res = await axios.get(`${API_BASE}/pdf/history/${user.id}/${fileId}`);
            const history = res.data;
            
            if (history && history.length > 0) {
                // Convert history to messages format
                const historyMessages = [];
                history.forEach(item => {
                    historyMessages.push({
                        id: `q-${item.createdAt}`,
                        text: item.question,
                        isBot: false
                    });
                    historyMessages.push({
                        id: `a-${item.createdAt}`,
                        text: item.answer,
                        isBot: true
                    });
                });
                setMessages(historyMessages);
            } else {
                // No history, show welcome message
                setMessages([{
                    id: 'welcome',
                    text: `שלום ${user.username}, ברוך הבא למערכת! העלה קובץ PDF או בחר קובץ קיים להתחלה.`,
                    isBot: true
                }]);
            }
        } catch (err) {
            console.error("Error loading chat history:", err);
            setMessages([{
                id: 'welcome',
                text: `שלום ${user.username}, ברוך הבא למערכת!`,
                isBot: true
            }]);
        }
    };

    // Select file and load its history
    const selectFile = async (file) => {
        setCurrentFile(file);
        await loadChatHistory(file.id);
    };

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
            await selectFile(newFile);
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
        
        // הוסף הודעת "חושב..." זמנית
        const thinkingId = 'thinking-' + Date.now();
        setMessages(prev => [...prev, { id: thinkingId, text: 'חושב...', isBot: true, isThinking: true }]);
        
        try {
            const formData = new FormData();
            formData.append('pdfId', currentFile.id.toString());
            formData.append('userId', user.id.toString());
            formData.append('question', question);
            const res = await axios.post(`${API_BASE}/rag/ask`, formData);
            
            // הסר את הודעת "חושב..." והוסף את התשובה
            setMessages(prev => prev.filter(m => m.id !== thinkingId).concat([{ id: Date.now(), text: res.data.answer, isBot: true }]));
        } catch (err) {
            // הסר את הודעת "חושב..." והוסף הודעת שגיאה
            setMessages(prev => prev.filter(m => m.id !== thinkingId).concat([{ id: Date.now(), text: "שגיאה בתקשורת עם Gemini.", isBot: true }]));
        } finally {
            setIsLoading(false);
        }
    };

    // Delete chat history for a specific file
    const deleteChatHistory = async (fileId, e) => {
        e.stopPropagation(); // Prevent file selection
        if (!user || !fileId) return;
        if (!window.confirm("האם אתה בטוח שברצונך למחוק את כל היסטוריית הצ'אט של קובץ זה?")) {
            return;
        }
        
        setIsActionLoading(true);
        try {
            await axios.delete(`${API_BASE}/pdf/history/${user.id}/${fileId}`);
            // If current file, clear messages
            if (currentFile?.id === fileId) {
                setMessages([{
                    id: 'welcome',
                    text: `היסטוריית הצ'אט נמחקה. איך אוכל לעזור?`,
                    isBot: true
                }]);
            }
            alert("היסטוריית הצ'אט נמחקה בהצלחה");
        } catch (err) {
            console.error("Error deleting chat history:", err);
            alert("שגיאה במחיקת היסטוריית הצ'אט");
        } finally {
            setIsActionLoading(false);
        }
    };

    // Delete PDF file and all related data
    const deletePdfFile = async (fileId, e) => {
        e.stopPropagation(); // Prevent file selection
        if (!user || !fileId) return;
        if (!window.confirm("האם אתה בטוח שברצונך למחוק את הקובץ וכל הנתונים הקשורים אליו? פעולה זו לא ניתנת לביטול!")) {
            return;
        }
        
        setIsActionLoading(true);
        try {
            const response = await axios.delete(`${API_BASE}/pdf/${user.id}/${fileId}`);
            // Remove from files list
            setFiles(prev => prev.filter(f => f.id !== fileId));
            // If deleted file was current, select first file or clear
            if (currentFile?.id === fileId) {
                const remainingFiles = files.filter(f => f.id !== fileId);
                if (remainingFiles.length > 0) {
                    await selectFile(remainingFiles[0]);
                } else {
                    setCurrentFile(null);
                    setMessages([{
                        id: 'welcome',
                        text: `הקובץ נמחק. העלה קובץ PDF חדש להתחלה.`,
                        isBot: true
                    }]);
                }
            }
            alert("הקובץ נמחק בהצלחה");
        } catch (err) {
            console.error("Error deleting PDF file:", err);
            // הצג הודעת שגיאה מפורטת יותר
            const errorMessage = err.response?.data?.message || 
                                err.response?.data?.error || 
                                err.message || 
                                "שגיאה במחיקת הקובץ";
            alert(`שגיאה במחיקת הקובץ: ${errorMessage}`);
        } finally {
            setIsActionLoading(false);
        }
    };

    // --- UI Views ---

    if (!user) {
        return (
            <div className="h-screen w-full flex items-center justify-center p-4 relative overflow-hidden" dir="rtl">
                {/* אפקטי רקע נוספים */}
                <div className="absolute inset-0 overflow-hidden">
                    <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse"></div>
                    <div className="absolute top-3/4 right-1/4 w-96 h-96 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse" style={{animationDelay: '1s'}}></div>
                    <div className="absolute bottom-1/4 left-1/2 w-96 h-96 bg-pink-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-pulse" style={{animationDelay: '2s'}}></div>
                </div>
                
                <div className="glass-strong p-10 rounded-[2.5rem] shadow-2xl max-w-md w-full text-center message-bubble relative z-10 hover-lift">
                    <div className="w-20 h-20 bg-gradient-to-br from-blue-600 via-purple-600 to-pink-600 rounded-2xl flex items-center justify-center mx-auto mb-6 shadow-lg text-white glow-effect wave-animation">
                        {isLoginMode ? <LogIn size={36} /> : <UserPlus size={36} />}
                    </div>

                    <h1 className="text-4xl font-bold gradient-text mb-3">
                        {isLoginMode ? "התחברות" : "יצירת חשבון"}
                    </h1>
                    <p className="text-slate-600 mb-8 font-medium text-lg">
                        {isLoginMode ? "שמחים לראות אותך שוב!" : "הצטרף אלינו עוד היום"}
                    </p>

                    <div className="space-y-5">
                        {!isLoginMode && (
                            <input 
                                type="text" 
                                placeholder="שם משתמש" 
                                className="w-full p-4 border-2 border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/20 transition-all font-semibold bg-white/80 backdrop-blur-sm hover:bg-white/90"
                                onChange={e => setAuthData({...authData, username: e.target.value})} 
                            />
                        )}
                        <input 
                            type="email" 
                            placeholder="אימייל" 
                            className="w-full p-4 border-2 border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/20 transition-all font-semibold bg-white/80 backdrop-blur-sm hover:bg-white/90"
                            onChange={e => setAuthData({...authData, email: e.target.value})} 
                        />
                        <input 
                            type="password" 
                            placeholder="סיסמה" 
                            className="w-full p-4 border-2 border-slate-200 rounded-xl outline-none focus:border-blue-500 focus:ring-4 focus:ring-blue-500/20 transition-all font-semibold bg-white/80 backdrop-blur-sm hover:bg-white/90"
                            onChange={e => setAuthData({...authData, password: e.target.value})} 
                        />

                        <button 
                            onClick={handleAuth} 
                            disabled={isActionLoading} 
                            className="w-full py-4 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 text-white rounded-xl font-bold text-lg shadow-xl hover:shadow-2xl active:scale-95 transition-all glow-effect hover-lift disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden group"
                        >
                            <span className="relative z-10 flex items-center justify-center gap-2">
                                {isActionLoading ? (
                                    <>
                                        <Loader2 className="animate-spin w-5 h-5" />
                                        <span>מעבד...</span>
                                    </>
                                ) : (
                                    <span>{isLoginMode ? "כניסה" : "הירשם עכשיו"}</span>
                                )}
                            </span>
                            <div className="absolute inset-0 shimmer-effect opacity-0 group-hover:opacity-100 transition-opacity"></div>
                        </button>

                        <button
                            onClick={() => setIsLoginMode(!isLoginMode)}
                            className="text-blue-600 font-bold mt-4 hover:text-purple-600 transition-colors text-sm hover:underline"
                        >
                            {isLoginMode ? "אין לך חשבון? הירשם כאן" : "כבר יש לך חשבון? התחבר כאן"}
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="flex h-screen relative overflow-hidden" dir="rtl">
            {/* אפקטי רקע טכנולוגיים דינמיים משופרים */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute top-0 right-0 w-2/5 h-2/5 bg-gradient-to-br from-cyan-500/25 via-blue-500/15 to-transparent rounded-full blur-3xl animate-pulse"></div>
                <div className="absolute bottom-0 left-0 w-2/5 h-2/5 bg-gradient-to-tr from-purple-500/25 via-indigo-500/15 to-transparent rounded-full blur-3xl animate-pulse" style={{animationDelay: '2s'}}></div>
                <div className="absolute top-1/2 left-1/2 w-1/3 h-1/3 bg-gradient-to-br from-cyan-400/15 to-purple-400/15 rounded-full blur-3xl animate-pulse" style={{animationDelay: '1s'}}></div>
                <div className="absolute top-1/4 right-1/4 w-1/4 h-1/4 bg-gradient-to-br from-blue-400/20 to-cyan-400/10 rounded-full blur-2xl animate-pulse" style={{animationDelay: '0.5s'}}></div>
            </div>

            {/* Sidebar */}
            <aside className="w-72 glass-strong border-l border-white/30 flex flex-col shadow-2xl z-20 relative">
                <div className="p-6 border-b border-white/20 flex items-center gap-3 bg-gradient-to-r from-white/50 to-transparent">
                    <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 via-purple-600 to-pink-600 flex items-center justify-center shadow-lg glow-effect wave-animation">
                        <Sparkles className="w-7 h-7 text-white" />
                    </div>
                    <div className="flex-1 min-w-0">
                        <h1 className="font-bold text-lg gradient-text truncate">שלום, {user.username}</h1>
                        <p className="text-xs text-slate-500">AI PDF Assistant</p>
                    </div>
                </div>
                <div className="flex-1 overflow-y-auto p-4 chat-container">
                    <div className="text-xs font-bold text-slate-400 uppercase mb-4 tracking-widest flex items-center gap-2">
                        <FileText size={14} /> המסמכים שלי
                    </div>
                    {files.length === 0 ? (
                        <div className="text-sm text-slate-400 text-center py-8">
                            עדיין לא הועלו קבצים
                        </div>
                    ) : (
                        files.map(f => (
                            <div key={f.id} className={`p-3 rounded-xl mb-2 transition-all flex items-center gap-2 group hover-lift ${currentFile?.id === f.id ? 'bg-gradient-to-r from-cyan-500 via-blue-600 to-purple-600 text-white shadow-xl glow-effect' : 'bg-white/60 hover:bg-white/90 backdrop-blur-sm'}`}>
                                <div onClick={() => selectFile(f)} className="flex items-center gap-3 flex-1 cursor-pointer min-w-0">
                                    <div className={`p-2 rounded-lg ${currentFile?.id === f.id ? 'bg-white/20' : 'bg-gradient-to-br from-blue-100 to-purple-100'}`}>
                                        <FileText className={`w-4 h-4 shrink-0 ${currentFile?.id === f.id ? 'text-white' : 'text-blue-600'}`} />
                                    </div>
                                    <span className="truncate text-sm font-medium">{f.name}</span>
                                </div>
                                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={(e) => deleteChatHistory(f.id, e)}
                                        disabled={isActionLoading}
                                        className={`p-2 rounded-lg hover:scale-110 ${currentFile?.id === f.id ? 'text-white hover:bg-red-500/30' : 'text-red-500 hover:bg-red-100'} transition-all disabled:opacity-50 disabled:cursor-not-allowed`}
                                        title="מחק את כל היסטוריית הצ'אט של קובץ זה"
                                    >
                                        <X size={14} />
                                    </button>
                                    <button
                                        onClick={(e) => deletePdfFile(f.id, e)}
                                        disabled={isActionLoading}
                                        className={`p-2 rounded-lg hover:scale-110 ${currentFile?.id === f.id ? 'text-white hover:bg-red-500/30' : 'text-red-500 hover:bg-red-100'} transition-all disabled:opacity-50 disabled:cursor-not-allowed`}
                                        title="מחק את הקובץ וכל הנתונים הקשורים אליו"
                                    >
                                        <Trash2 size={14} />
                                    </button>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <div className="p-4 border-t border-white/20">
                    <button onClick={handleLogout} className="w-full flex items-center justify-center gap-2 p-3 text-red-500 hover:bg-red-50 rounded-xl transition-all font-bold">
                        <LogOut size={20} /> התנתק
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 flex flex-col relative bg-transparent">
                <header className="glass-strong border-b border-white/20 p-5 shadow-lg z-10 bg-gradient-to-r from-white/90 to-white/70">
                    <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-cyan-500 via-blue-600 to-purple-600 flex items-center justify-center shadow-lg glow-effect">
                            <Bot className="w-6 h-6 text-white" />
                        </div>
                        <h2 className="text-2xl font-bold gradient-text">AI PDF Analyzer</h2>
                    </div>
                </header>

                <div className="flex-1 overflow-y-auto p-6 space-y-4 chat-container">
                    <div className="max-w-4xl mx-auto">
                        {messages.map((m, i) => (
                            <div key={i} className={`flex ${m.isBot ? 'justify-end' : 'justify-start'} mb-6 animate-fade-in`}>
                                <div className={`flex items-start gap-3 ${m.isBot ? 'flex-row-reverse' : 'flex-row'}`}>
                                    <div className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 ${m.isBot ? 'bg-gradient-to-br from-blue-600 to-purple-600 shadow-lg' : 'bg-gradient-to-br from-slate-200 to-slate-300'}`}>
                                        {m.isBot ? <Bot className="w-5 h-5 text-white" /> : <User className="w-5 h-5 text-slate-600" />}
                                    </div>
                                    <div className={`p-5 rounded-3xl shadow-xl message-bubble max-w-[80%] ${m.isBot ? 'bg-gradient-to-br from-cyan-500 via-blue-600 to-purple-600 text-white glow-effect' : 'bg-white/90 backdrop-blur-sm text-slate-800 border border-slate-200'}`}>
                                        {m.isThinking ? (
                                            <div className="flex items-center gap-2">
                                                <span className="text-base">{m.text}</span>
                                                <div className="flex gap-1">
                                                    <span className="w-2 h-2 bg-white/80 rounded-full animate-bounce" style={{animationDelay: '0ms'}}></span>
                                                    <span className="w-2 h-2 bg-white/80 rounded-full animate-bounce" style={{animationDelay: '150ms'}}></span>
                                                    <span className="w-2 h-2 bg-white/80 rounded-full animate-bounce" style={{animationDelay: '300ms'}}></span>
                                                </div>
                                            </div>
                                        ) : (
                                            <p className="text-base leading-relaxed whitespace-pre-wrap">{m.text}</p>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                        <div ref={chatEndRef} />
                    </div>
                </div>

                <footer className="p-6 glass-strong shadow-2xl bg-gradient-to-r from-white/90 to-white/70 border-t border-white/20">
                    <div className="max-w-4xl mx-auto flex gap-3">
                        <label className="cursor-pointer bg-gradient-to-r from-cyan-500 via-blue-600 to-purple-600 text-white px-5 py-4 rounded-2xl shadow-xl hover:shadow-2xl font-semibold active:scale-95 transition-all flex items-center gap-2 glow-effect hover-lift relative overflow-hidden group min-w-[140px] justify-center">
                            <span className="relative z-10 flex items-center gap-2">
                                {isActionLoading ? (
                                    <Loader2 className="animate-spin w-5 h-5" />
                                ) : (
                                    <Upload className="w-5 h-5" />
                                )}
                                <span className="hidden sm:inline">העלה PDF</span>
                            </span>
                            <div className="absolute inset-0 shimmer-effect opacity-0 group-hover:opacity-100 transition-opacity"></div>
                            <input type="file" ref={fileInputRef} className="hidden" accept=".pdf" onChange={handleUpload} />
                        </label>
                        <input 
                            value={input} 
                            onChange={e => setInput(e.target.value)} 
                            onKeyPress={e => e.key === 'Enter' && sendMessage()}
                            placeholder={currentFile ? "שאל שאלה..." : "העלה קובץ תחילה..."} 
                            disabled={!currentFile || isLoading}
                            className="flex-1 bg-white/90 backdrop-blur-sm border-2 border-slate-200 rounded-2xl px-6 py-4 focus:border-cyan-500 focus:ring-4 focus:ring-cyan-500/20 outline-none text-lg shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed" 
                        />
                        <button 
                            onClick={sendMessage} 
                            disabled={!currentFile || isLoading} 
                            className="bg-gradient-to-r from-cyan-500 via-blue-600 to-purple-600 text-white p-4 rounded-xl shadow-xl hover:shadow-2xl transition-all disabled:opacity-50 disabled:cursor-not-allowed glow-effect hover-lift relative overflow-hidden group min-w-[60px]"
                        >
                            {isLoading ? (
                                <Loader2 className="animate-spin w-6 h-6" />
                            ) : (
                                <>
                                    <Send size={24} className="relative z-10" />
                                    <div className="absolute inset-0 shimmer-effect opacity-0 group-hover:opacity-100 transition-opacity"></div>
                                </>
                            )}
                        </button>
                    </div>
                </footer>
            </main>
            
            {/* קרדיט בצד - קטן ולא מסתיר */}
            <div className="absolute bottom-4 left-4 z-10 pointer-events-none">
                <div className="flex items-center gap-2 rounded-xl px-3 py-2 pointer-events-auto bg-slate-900/95 backdrop-blur-xl border-2 border-slate-700/80 shadow-2xl hover:shadow-[0_0_30px_rgba(6,182,212,0.4)] transition-all hover:border-cyan-500/50">
                    <span className="text-xs font-bold text-white">Omer Dayan</span>
                    <div className="w-px h-3 bg-slate-600"></div>
                    <span className="text-xs text-slate-400 font-medium">Created by</span>
                    <div className="w-px h-3 bg-slate-600"></div>
                    <a 
                        href="https://www.linkedin.com/in/omer-dayan/" 
                        target="_blank" 
                        rel="noopener noreferrer"
                        className="flex items-center text-slate-400 hover:text-cyan-400 transition-all group"
                        title="LinkedIn Profile"
                    >
                        <Linkedin className="w-4 h-4 group-hover:scale-110 transition-transform group-hover:drop-shadow-[0_0_8px_rgba(6,182,212,0.8)]" />
                    </a>
                </div>
            </div>
        </div>
    );
}