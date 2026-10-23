# check_upstream.ps1 — Helper script to inspect new upstream commits from storytold/pdfcraft

Write-Host "Fetching updates from upstream (storytold/pdfcraft)..." -ForegroundColor Cyan
git fetch upstream main

$newCommits = git log HEAD..upstream/main --oneline
if ($newCommits) {
    Write-Host "`nFound new upstream commits:" -ForegroundColor Green
    Write-Host $newCommits -ForegroundColor Yellow
    Write-Host "`nTo sync and auto-port, ask Antigravity to run: 'Sync upstream updates and auto-port to Android'" -ForegroundColor Cyan
} else {
    Write-Host "`nYour repository is fully up to date with upstream storytold/pdfcraft!" -ForegroundColor Green
}
