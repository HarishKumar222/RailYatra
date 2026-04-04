#!/bin/bash
# ================================================================
# RailYatra — One-shot local setup script
# Run: chmod +x setup.sh && ./setup.sh
# ================================================================
set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🚂 RailYatra Setup Starting...${NC}\n"

# Check Java
if ! java -version 2>&1 | grep -q "17\|18\|19\|20\|21"; then
  echo -e "${YELLOW}⚠️  Java 17+ required. Download from https://adoptium.net${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Java OK${NC}"

# Check Node
if ! node -v 2>&1 | grep -q "v1[89]\|v2[0-9]"; then
  echo -e "${YELLOW}⚠️  Node.js 18+ required. Download from https://nodejs.org${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Node.js OK${NC}"

# Check Docker
if command -v docker &> /dev/null; then
  echo -e "${GREEN}✅ Docker found — starting PostgreSQL + Redis${NC}"
  docker-compose up -d postgres redis
  echo "Waiting for DB to be ready..."
  sleep 8
else
  echo -e "${YELLOW}⚠️  Docker not found. Make sure PostgreSQL (port 5432) and Redis (port 6379) are running manually.${NC}"
fi

# Install frontend deps
echo -e "\n${BLUE}📦 Installing frontend dependencies...${NC}"
cd frontend
npm install
cd ..
echo -e "${GREEN}✅ Frontend dependencies installed${NC}"

# Build backend
echo -e "\n${BLUE}🔨 Building backend (this takes ~1-2 minutes first time)...${NC}"
cd backend
mvn clean package -DskipTests -q
cd ..
echo -e "${GREEN}✅ Backend built${NC}"

echo -e "\n${GREEN}════════════════════════════════════════${NC}"
echo -e "${GREEN}🎉 Setup complete! Now run:${NC}"
echo -e ""
echo -e "  Terminal 1 (backend):"
echo -e "  ${BLUE}cd backend && mvn spring-boot:run${NC}"
echo -e ""
echo -e "  Terminal 2 (frontend):"
echo -e "  ${BLUE}cd frontend && npm run dev${NC}"
echo -e ""
echo -e "  Open: ${BLUE}http://localhost:5173${NC}"
echo -e "  Admin: admin@railyatra.com / Admin@123"
echo -e "${GREEN}════════════════════════════════════════${NC}"
