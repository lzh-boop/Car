import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('echarts') || id.includes('zrender')) return 'echarts'
          if (id.includes('element-plus') || id.includes('@element-plus/icons-vue')) {
            return 'element-plus'
          }
          if (id.includes('axios')) return 'axios'
          if (id.includes('vue-router') || id.includes('pinia') || id.includes('vue-echarts')) {
            return 'vue-vendor'
          }
          if (id.includes('/vue') && id.includes('node_modules')) return 'vue-vendor'
        },
      },
    },
    // element-plus / echarts 合并后仍在 ~1MB，属正常；提高阈值避免无意义告警
    chunkSizeWarningLimit: 1200,
  },
  server: {
    port: 5173,
    proxy: {
      // REST API
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // WebSocket — proxy prefix matches the actual backend endpoint prefix /ws/...
      // Backend endpoint: /ws/location/{username}
      // SecurityConfig public path whitelist: /ws/**
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
