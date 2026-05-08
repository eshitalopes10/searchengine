# Run the setup script (if not done yet)
chmod +x setup-search-engine.sh
./setup-search-engine.sh

# Go into the project folder
cd search-engine

# Initialise git and push
git init
git add .
git commit -m "initial commit: search engine"
git branch -M main
git remote add origin https://github.com/eshitalopes10/searchengine.git
git push -u origin main
