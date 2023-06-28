/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./frontend/target/**/frontend-*/pureframes*.js",
    "./frontend/index.html",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}

