/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
    theme: {
        extend: {
            colors: {
                brand: {
                    primary: '#6366f1',   // Indigo מודרני
                    secondary: '#a855f7', // Purple עדין
                    accent: '#06b6d4',    // Cyan לנגיעות AI
                }
            },
            animation: {
                'subtle-float': 'float 6s ease-in-out infinite',
                'slow-spin': 'spin 12s linear infinite',
            },
            keyframes: {
                float: {
                    '0%, 100%': { transform: 'translateY(0)' },
                    '50%': { transform: 'translateY(-10px)' },
                }
            }
        },
    },
    plugins: [],
}