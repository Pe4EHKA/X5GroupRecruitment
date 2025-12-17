const path = require('path');

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone',
  // Explicitly set the monorepo root for output tracing to avoid noisy
  // "workspace root" warnings during build/lint in Next.js 15.
  outputFileTracingRoot: path.join(__dirname, '..'),
};

module.exports = nextConfig;
