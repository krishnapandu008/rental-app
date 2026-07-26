#!/bin/bash

# ============================================================
# Deploy script – stages, commits, and pushes to dev and/or main.
# ============================================================

# Ensure script runs from the repository root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

# Color definitions
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Show usage
show_usage() {
  echo -e "${GREEN}Usage:${NC}"
  echo "  ./deploy.sh \"Commit message\"               # Interactive (asks environment)"
  echo "  ./deploy.sh \"Commit message\" --dev         # Deploy only to staging (dev)"
  echo "  ./deploy.sh \"Commit message\" --main        # Deploy directly to production (main) – with confirmation"
  echo "  ./deploy.sh \"Commit message\" --both        # Deploy to both staging and production"
  echo "  ./deploy.sh --help                          # Show this help"
  echo ""
  echo -e "${GREEN}Examples:${NC}"
  echo "  ./deploy.sh \"fix: typo\""
  echo "  ./deploy.sh \"feat: new feature\" --dev"
  echo "  ./deploy.sh \"hotfix: critical\" --main"
  echo "  ./deploy.sh \"chore: update config\" --both"
}

# Parse arguments
if [ $# -eq 0 ]; then
  # Interactive mode (no args) – used for double‑click
  echo -e "${YELLOW}🔹 Choose deployment mode:${NC}"
  echo "  dev   – deploy only to staging (dev)"
  echo "  main  – deploy only to production (main)"
  echo "  both  – deploy to both staging and production"
  read -p "Enter mode (dev/main/both): " mode
  case "$mode" in
    dev) DEV_ONLY=true ;;
    main) MAIN_ONLY=true ;;
    both) BOTH=true ;;
    *) echo -e "${RED}❌ Invalid mode. Aborting.${NC}"; exit 1 ;;
  esac
  echo -e "${YELLOW}🔹 Enter commit message:${NC}"
  read -p "Commit message: " COMMIT_MSG
  if [ -z "$COMMIT_MSG" ]; then
    echo -e "${RED}❌ Commit message cannot be empty. Aborting.${NC}"
    exit 1
  fi
else
  # Command-line mode
  COMMIT_MSG="$1"
  shift
  DEV_ONLY=false
  MAIN_ONLY=false
  BOTH=false

  if [[ "$COMMIT_MSG" == "--help" || "$COMMIT_MSG" == "-h" ]]; then
    show_usage
    exit 0
  fi

  if [ $# -eq 0 ]; then
    echo "🔹 No deployment mode specified."
    read -p "Deploy to dev only, main only, or both? (dev/main/both) " mode
    case "$mode" in
      dev) DEV_ONLY=true ;;
      main) MAIN_ONLY=true ;;
      both) BOTH=true ;;
      *) echo -e "${RED}❌ Invalid choice.${NC}"; DEV_ONLY=false ;;
    esac
  else
    for arg in "$@"; do
      case "$arg" in
        --dev) DEV_ONLY=true ;;
        --main) MAIN_ONLY=true ;;
        --both) BOTH=true ;;
        *) echo -e "${RED}❌ Unknown option: $arg${NC}"; exit 1 ;;
      esac
    done
  fi
fi

# ---- Helper functions ----
git_push_dev() {
  echo -e "${GREEN}📦 Staging all changes...${NC}"
  git add .

  echo -e "${GREEN}📝 Committing with message: \"$COMMIT_MSG\"${NC}"
  git commit -m "$COMMIT_MSG"

  echo -e "${GREEN}🚀 Pushing to dev (staging)...${NC}"
  git push origin dev
  if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Push to dev failed. Aborting.${NC}"
    exit 1
  fi
  echo -e "${GREEN}✅ dev branch updated.${NC}"
}

git_push_main() {
  echo -e "${GREEN}🔀 Switching to main...${NC}"
  git checkout main

  echo -e "${GREEN}⬇️  Pulling latest main...${NC}"
  git pull origin main

  echo -e "${GREEN}🔀 Merging dev into main...${NC}"
  git merge dev

  echo -e "${GREEN}🚀 Pushing main to production...${NC}"
  git push origin main
  if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Push to main failed. Aborting.${NC}"
    exit 1
  fi

  echo -e "${GREEN}🔙 Switching back to dev...${NC}"
  git checkout dev
}

# ---- Main execution ----
if [ "$DEV_ONLY" = true ]; then
  echo -e "${YELLOW}🔹 Deploying ONLY to dev (staging).${NC}"
  git_push_dev
  echo -e "${GREEN}✅ Done. Production (main) is unchanged.${NC}"

elif [ "$MAIN_ONLY" = true ]; then
  echo -e "${YELLOW}🔹 Deploying ONLY to main (production).${NC}"
  echo -e "${YELLOW}⚠️  Warning: This bypasses staging. Are you sure?${NC}"
  read -p "Continue? (y/N) " confirm
  if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}⏩ Aborted.${NC}"
    exit 0
  fi
  # For main-only, we still need to be on dev to have the latest changes
  git_push_dev
  git_push_main
  echo -e "${GREEN}✅ Done. Only main (production) updated.${NC}"

elif [ "$BOTH" = true ]; then
  echo -e "${YELLOW}🔹 Deploying to BOTH dev and main.${NC}"
  git_push_dev
  git_push_main
  echo -e "${GREEN}✅ Done. Staging and production are in sync.${NC}"

else
  # Fallback (if no mode set – shouldn't happen)
  echo -e "${YELLOW}🔹 No explicit mode. You will be asked during the process.${NC}"
  git_push_dev
  read -p "Merge dev into main and push to production? (y/N) " answer
  if [[ "$answer" =~ ^[Yy]$ ]]; then
    git_push_main
    echo -e "${GREEN}✅ Done. Staging and production are in sync.${NC}"
  else
    echo -e "${YELLOW}⏩ Skipped merging to main. Only dev is updated.${NC}"
  fi
fi

# ============================================================
# 🔒 FORCE PAUSE – Keeps the window open after execution
# ============================================================
echo -e "\n${YELLOW}Press any key to close this window...${NC}"
read -n 1 -s