# Use Node 20 slim for better compatibility with Next.js 15 and to avoid alpine musl issues
FROM node:20-slim AS base

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app

# Copy workspace manifests so npm can resolve the frontend workspace correctly
COPY package.json package-lock.json ./
COPY apps/frontend/package.json apps/frontend/package-lock.json ./apps/frontend/

# Install only the frontend workspace dependencies using the root lockfile
RUN npm ci --prefer-offline --no-audit --workspace apps/frontend --include-workspace-root

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app

# Copy installed dependencies from deps stage (root + workspace node_modules)
COPY --from=deps /app/node_modules ./node_modules
COPY --from=deps /app/apps/frontend/node_modules ./apps/frontend/node_modules

# Copy application source
COPY package.json package-lock.json ./
COPY apps/frontend ./apps/frontend

# Disable telemetry during build
ENV NEXT_TELEMETRY_DISABLED=1

# Build the application
WORKDIR /app/apps/frontend
RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app

# Install curl for healthcheck (better than wget on debian-based slim images)
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1

# Create user and group
RUN groupadd --system --gid 1001 nodejs && \
    useradd --system --uid 1001 --gid nodejs nextjs

# Create public directory and copy assets
RUN mkdir -p public
COPY --from=builder /app/apps/frontend/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next && chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
COPY --from=builder --chown=nextjs:nodejs /app/apps/frontend/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/apps/frontend/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]
