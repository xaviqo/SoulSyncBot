const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: 'target/dist',
  assetsDir: 'static',
  devServer: {
    client: {
      overlay: {
        errors: true,
        warnings: false,
        runtimeErrors: true,
      },
    },
    port : 7316,
    proxy: {
      '/v2/api': {
        target: 'http://localhost:6743',
        ws: true,
        changeOrigin: true
      }
    }
  }
})
