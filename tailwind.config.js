/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: { sans: ['Plus Jakarta Sans', 'system-ui', 'sans-serif'] },
      colors: {
        rail: { blue: '#1a56db', orange: '#ff6b35', green: '#16a34a' }
      }
    }
  },
  plugins: []
}
