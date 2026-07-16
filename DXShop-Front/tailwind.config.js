/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        taobao: {
          50: '#fff3f0',
          100: '#ffe4db',
          200: '#ffc9b8',
          300: '#ffa88f',
          400: '#ff7d5b',
          500: '#FF5000',
          600: '#e04800',
          700: '#b83a00',
          800: '#963000',
          900: '#7a2700',
        },
      },
      fontFamily: {
        sans: ['"PingFang SC"', '"Microsoft YaHei"', '"Helvetica Neue"', 'Arial', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
