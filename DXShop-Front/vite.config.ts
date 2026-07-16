import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/user': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/order': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/goods': {
        target: 'http://localhost:8084',
        changeOrigin: true,
      },
      '/api/chat': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/cs': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      },
      '/api/oss': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/mq': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8085',
        changeOrigin: true,
        ws: true,
      },
    },
  },
})
