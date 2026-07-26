#!/bin/bash

# Check if commit message is provided
if [ -z "$1" ]; then
  echo "❌ Usage: ./deploy.sh \"Your commit message\""
  exit 1
fi

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}📦 Staging all changes...${NC}"
git add .

echo -e "${GREEN}📝 Committing with message: $1${NC}"
git commit -m "$1"

echo -e "${GREEN}🚀 Pushing to dev (staging)...${NC}"
git push origin dev

# Ask if they want to merge to main
read -p "$(echo -e ${YELLOW}🔄 Merge dev into main and push to production? (y/N) ${NC})" answer
if [[ "$answer" =~ ^[Yy]$ ]]; then
  echo -e "${GREEN}🔀 Switching to main...${NC}"
  git checkout main

  echo -e "${GREEN}⬇️  Pulling latest main...${NC}"
  git pull origin main

  echo -e "${GREEN}🔀 Merging dev into main...${NC}"
  git merge dev

  echo -e "${GREEN}🚀 Pushing main to production...${NC}"
  git push origin main

  echo -e "${GREEN}🔙 Switching back to dev...${NC}"
  git checkout dev

  echo -e "${GREEN}✅ Done! Staging and production are in sync.${NC}"
else
  echo -e "${YELLOW}⏩ Skipped merging to main. Only dev is updated.${NC}"
fi