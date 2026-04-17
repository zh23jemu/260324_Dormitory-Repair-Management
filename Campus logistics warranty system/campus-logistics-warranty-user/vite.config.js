import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: parseInt(process.env.VITE_APP_PORT) || 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true, // 修改请求头中的 Origin
          rewrite: (path) => {
            // console.log('path', path, path.replace(/^\/api/, ''));
            return path.replace(/^\/api/, '')
          }, // 重写
      }
    }
  },
  define: {
    '__VITE_APP_API_BASE_URL__': process.env.VITE_APP_API_BASE_URL
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: [
      {
        // 配置别名
        find: '@', replacement: '/src'
      }
    ]
  }
})
