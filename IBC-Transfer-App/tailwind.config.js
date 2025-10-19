/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './src/**/*.{html,ts}', // Ensures Tailwind scans all your Angular files
  ],
  theme: {
    extend: {
      // Define IBC Consult brand colors
      colors: {
        'ibc-primary': '#0A2342', // Deep Navy (Main Branding)
        'ibc-secondary': '#00A3E0', // Light Cyan (Accent/Border)
        'ibc-background': '#F5F5F5', // Light Gray Background
        'ibc-text-dark': '#333333', // Dark Text
        'ibc-error': '#DC3545', // Error Red
      },
    },
  },
  plugins: [],
};
