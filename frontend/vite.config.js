import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],

  build: {
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        // Function form required by Rolldown (Vite 8)
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('firebase')) return 'firebase'
            if (id.includes('recharts') || id.includes('d3') || id.includes('victory')) return 'charts'
            if (id.includes('react-dom') || id.includes('react-router')) return 'react-vendor'
            if (id.includes('react')) return 'react-core'
            if (id.includes('axios')) return 'axios'
            return 'vendor'
          }
        },
      },
    },
  },

  server: {
    port: 5173,
    proxy: {
      // All backend calls go through /api — single proxy rule covers everything
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // Legacy alias routes (StaffAliasController, StudentAliasController)
      '/top-students': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '/student-details': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
