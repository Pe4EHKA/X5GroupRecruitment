# Use Node 20 slim for better compatibility with Next.js 15 and to avoid alpine musl issues
# Pull from gcr.io mirror to avoid transient Docker Hub auth outages
FROM mirror.gcr.io/library/node:20-slim AS base

# Install dependencies only when needed
FROM base AS deps
WORKDIR /app

# Copy the workspace package files (frontend uses the root lockfile)
COPY package.json package-lock.json ./
COPY apps/frontend/package.json ./apps/frontend/package.json

# Install dependencies (use install instead of ci to handle workspace lockfile)
# Note: strict-ssl is disabled for enterprise/CI environments with self-signed certificates
# In production, configure npm to use your corporate CA bundle instead
RUN npm config set strict-ssl false && \
    npm install --prefer-offline --no-audit --legacy-peer-deps

# Rebuild the source code only when needed
FROM base AS builder
WORKDIR /app

# Copy installed dependencies from deps stage
COPY --from=deps /app/node_modules ./node_modules

# Copy application source
COPY apps/frontend/package.json ./
COPY apps/frontend ./

# Disable telemetry during build
ENV NEXT_TELEMETRY_DISABLED=1

# Build the application
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
COPY --from=builder /app/public ./public

# Set the correct permission for prerender cache
RUN mkdir .next && chown nextjs:nodejs .next

# Automatically leverage output traces to reduce image size
# https://nextjs.org/docs/advanced-features/output-file-tracing
# Next.js outputs the standalone bundle under a nested folder that matches the
# workspace name ("frontend"), so copy the standalone output and flatten the
# workspace folder if it exists to ensure server.js is available at /app/server.js
# at runtime. This makes the image resilient whether Next.js emits the
# standalone build inside a workspace folder (e.g., /standalone/frontend) or at
# the root of /standalone.
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./standalone

RUN SERVER_DIR=$(find ./standalone -maxdepth 2 -type f -name server.js -printf '%h' -quit) && \
    if [ -z "$SERVER_DIR" ]; then \
      echo "Could not locate standalone server.js in /app/.next/standalone" >&2; \
      exit 1; \
    fi && \
    cp -a "$SERVER_DIR"/. ./ && \
    rm -rf standalone
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["node", "server.js"]
