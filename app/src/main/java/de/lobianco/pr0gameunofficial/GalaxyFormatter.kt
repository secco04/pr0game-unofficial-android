package de.lobianco.pr0gameunofficial

object GalaxyFormatter {

    /**
     * JavaScript Code um die Galaxy-Ansicht mobile-freundlich umzuformatieren
     */
    fun getFormatterScript(rowHeight: Int = 20): String {
        return """
(function() {
    console.log('Galaxy Formatter: Starting with row height ${rowHeight}px...');
    
    if (!window.location.href.includes('page=galaxy')) {
        console.log('Galaxy Formatter: Not on galaxy page');
        return;
    }
    
    console.log('Galaxy Formatter: On galaxy page, searching for container...');
    
    const container = document.querySelector('.galaxy-grid-container');
    if (!container) {
        console.log('Galaxy Formatter: Container not found!');
        return;
    }
    
    console.log('Galaxy Formatter: Container found, formatting...');
    
    const ROW_HEIGHT = ${rowHeight};
    console.log('Using ROW_HEIGHT: ' + ROW_HEIGHT + 'px');
    
    // Erstelle Mobile Table
    const mobileTable = document.createElement('div');
    mobileTable.style.cssText = 'background: #13181f; color: #fff; font-size: 14px; overflow-x: auto;';
    
    // Table Header
    const header = document.createElement('div');
    header.style.cssText = 'display: flex; background: #13181f; font-weight: bold; padding: 10px 4px; border-bottom: 2px solid #64b5f6; font-size: 14px; color: #849ab7;';
    header.innerHTML = `
        <div style="width: 15px; text-align: center;"></div>
        <div style="width: 150px; padding-left: 4px;">Planet</div>
        <div style="width: 30px; text-align: center;">M</div>
        <div style="width: 30px; text-align: center;">T</div>
        <div style="flex: 1; min-width: 130px; padding-left: 4px;">Spieler (Status) [Allianz]</div>
        <div style="width: 50px; text-align: center;">Aktion</div>
    `;
    mobileTable.appendChild(header);
    
    // Parse alle Positionen (1-17)
    let rowCount = 0;
    for (let pos = 1; pos <= 17; pos++) {
        const row = createTableRow(pos, container);
        if (row) {
            mobileTable.appendChild(row);
            rowCount++;
        }
    }
    
    console.log('Galaxy Formatter: Created ' + rowCount + ' rows');
    
    // Füge Footer hinzu (Legende, Flotten-Info, etc.)
    const footer = createFooter(container);
    if (footer) {
        mobileTable.appendChild(footer);
    }
    
    if (rowCount > 0) {
        container.style.display = 'none';
        container.parentNode.insertBefore(mobileTable, container);
        console.log('Galaxy Formatter: Done!');
    }
    
    function createTableRow(position, originalContainer) {
        // Handle Expedition & Handelszone
        if (position === 16 || position === 17) {
            return createSpecialTableRow(position);
        }
        
        const originalRow = originalContainer.querySelector('[data-info="p_' + position + '"]');
        if (!originalRow) {
            console.log('Galaxy Formatter: Row ' + position + ' not found');
            return null;
        }
        
        const row = document.createElement('div');
        const bgColor = position % 2 === 0 ? '#1e2530' : '#1f2631';
        
        // Prüfe ob eigener Planet
        const isOwn = originalRow.querySelector('.galaxy-planet a')?.textContent.includes('(*)');
        const borderColor = isOwn ? '#64b5f6' : 'transparent';
        
        row.style.cssText = 'display: flex; background: ' + bgColor + '; padding: 0; border-bottom: 1px solid #13181f; border-left: 3px solid ' + borderColor + '; align-items: center; height: ' + ROW_HEIGHT + 'px; font-size: 14px; line-height: ' + ROW_HEIGHT + 'px;';
        
        // Spalte 1: Position (klickbar mit Tooltip)
        const posCell = document.createElement('div');
        posCell.style.cssText = 'width: 15px; height: 14px; display: flex; align-items: center; justify-content: center;';
        
        // Finde Original Position Cell mit Tooltip
        const originalPosCell = originalRow.querySelector('.galaxy-grid-item.position');
        if (originalPosCell) {
            // Clone komplette Position mit Tooltip
            const posClone = originalPosCell.cloneNode(true);
            posClone.style.cssText = 'color: #64b5f6; font-weight: bold; font-size: 14px; cursor: pointer; line-height: 16px;';
            posCell.appendChild(posClone);
        } else {
            posCell.style.cssText += ' color: #64b5f6; font-weight: bold; font-size: 14px;';
            posCell.textContent = position;
        }
        row.appendChild(posCell);
        
        // Spalte 2: Planet
        const planetCell = document.createElement('div');
        planetCell.style.cssText = 'width: 150px; padding-left: 4px; display: flex; align-items: center; gap: 4px; auto: hidden;';
        
        const originalPlanetCell = originalRow.querySelector('.galaxy-planet');
        if (originalPlanetCell) {
            const originalLink = originalPlanetCell.querySelector('a');
            const img = originalPlanetCell.querySelector('img');
            
            if (originalLink) {
                // Kopiere Original-Link komplett mit allen Attributen
                const newLink = originalLink.cloneNode(true);
                newLink.style.cssText = 'display: flex; align-items: center; gap: 4px; text-decoration: none; overflow: hidden;';
                
                // Passe nur die Bildgröße an
                const img = newLink.querySelector('img');
                if (img) {
                    img.style.cssText = 'width: 14px; height: 14px; flex-shrink: 0;';
                }
                
                // Passe Textgröße an
                const textNodes = newLink.childNodes;
                textNodes.forEach(node => {
                    if (node.nodeType === Node.TEXT_NODE && node.textContent.trim()) {
                        const span = document.createElement('span');
                        span.textContent = node.textContent;
                        span.style.cssText = 'font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;';
                        node.replaceWith(span);
                    }
                });
                
                planetCell.appendChild(newLink);
            }
        } else {
            // Leerer Planet
            const colonizeLink = originalRow.querySelector('a[href*="target_mission=7"]');
            if (colonizeLink) {
                const colonizeBtn = document.createElement('a');
                colonizeBtn.href = colonizeLink.href;
                colonizeBtn.textContent = 'Kolonisieren';
                colonizeBtn.style.cssText = 'color: #4CAF50; text-decoration: none; font-size: 14px;';
                planetCell.appendChild(colonizeBtn);
            } else {
                planetCell.textContent = '-';
                planetCell.style.color = '#666';
            }
        }
        row.appendChild(planetCell);
        
        // Spalte 3: Mond (M)
        const moonCell = document.createElement('div');
        moonCell.style.cssText = 'width: 30px; height: ' + ROW_HEIGHT + 'px; display: flex; align-items: center; justify-content: center;';
        const originalMoonCell = originalRow.querySelector('.galaxy-moon');
        const moonImg = originalMoonCell ? originalMoonCell.querySelector('img') : null;
        if (moonImg) {
            const newMoonImg = document.createElement('img');
            newMoonImg.src = moonImg.src;
            newMoonImg.style.cssText = 'width: 14px; height: 14px;';
            moonCell.appendChild(newMoonImg);
        }
        row.appendChild(moonCell);
        
        // Spalte 4: Trümmerfeld (T)
        const debrisCell = document.createElement('div');
        debrisCell.style.cssText = 'width: 30px; height: ' + ROW_HEIGHT + 'px; display: flex; align-items: center; justify-content: center; font-size: 14px;';
        const originalDebrisCell = originalRow.querySelector('.galaxy-debris');
        if (originalDebrisCell && originalDebrisCell.textContent.trim() !== '') {
            // Clone kompletten Debris Inhalt
            const debrisClone = originalDebrisCell.cloneNode(true);
            debrisClone.style.cssText = 'font-size: 14px; line-height: ' + ROW_HEIGHT + 'px;';
            debrisCell.appendChild(debrisClone);
        }
        row.appendChild(debrisCell);
        
        // Spalte 5: Spieler (Status) [Allianz]
        const playerCell = document.createElement('div');
        playerCell.style.cssText = 'flex: 1; min-width: 130px; height: ' + ROW_HEIGHT + 'px; padding-left: 4px; font-size: 14px; overflow: hidden; display: flex; align-items: center; line-height: ' + ROW_HEIGHT + 'px;';
        
        const originalPlayerCell = originalRow.querySelector('.galaxy-player');
        if (originalPlayerCell) {
            // Kopiere die Original Player Links mit Tooltips
            const playerLink = originalPlayerCell.querySelector('a');
            const allianceLink = originalPlayerCell.querySelector('a[allyid]');
            const statusDiv = originalPlayerCell.querySelector('div');
            
            if (playerLink) {
                // Clone Player Link
                const newPlayerLink = playerLink.cloneNode(false);
                const playerSpan = playerLink.querySelector('.galaxy-username');
                
                if (playerSpan) {
                    const newPlayerSpan = document.createElement('span');
                    newPlayerSpan.className = playerSpan.className;
                    newPlayerSpan.textContent = playerSpan.textContent.trim();
                    // Kopiere Original-Farbe
                    newPlayerSpan.style.color = window.getComputedStyle(playerSpan).color;
                    newPlayerLink.appendChild(newPlayerSpan);
                }
                
                newPlayerLink.style.cssText = 'text-decoration: none;';
                playerCell.appendChild(newPlayerLink);
                
                // Status
                if (statusDiv) {
                    const statusSpan = statusDiv.querySelector('[class*="galaxy-short-"]');
                    if (statusSpan) {
                        const newStatusSpan = document.createElement('span');
                        newStatusSpan.className = statusSpan.className;
                        newStatusSpan.textContent = ' (' + statusSpan.textContent.trim() + ')';
                        // Kopiere Original-Farbe
                        newStatusSpan.style.color = window.getComputedStyle(statusSpan).color;
                        playerCell.appendChild(newStatusSpan);
                    }
                }
                
                // Alliance
                if (allianceLink) {
                    playerCell.appendChild(document.createTextNode(' '));
                    const newAllianceLink = allianceLink.cloneNode(false);
                    const allianceSpan = allianceLink.querySelector('.galaxy-alliance');
                    
                    if (allianceSpan) {
                        const newAllianceSpan = document.createElement('span');
                        newAllianceSpan.className = allianceSpan.className;
                        newAllianceSpan.textContent = allianceSpan.textContent.trim();
                        // Kopiere Original-Farbe
                        newAllianceSpan.style.color = window.getComputedStyle(allianceSpan).color;
                        newAllianceLink.appendChild(newAllianceSpan);
                    }
                    
                    newAllianceLink.style.cssText = 'text-decoration: none;';
                    playerCell.appendChild(newAllianceLink);
                }
            } else {
                playerCell.textContent = '-';
                playerCell.style.color = '#666';
            }
        } else {
            playerCell.textContent = '-';
            playerCell.style.color = '#666';
        }
        row.appendChild(playerCell);
        
        // Spalte 6: Aktion (Spionage-Button)
        const actionCell = document.createElement('div');
        actionCell.style.cssText = 'width: 50px; height: 16px; display: flex; align-items: center; justify-content: center;';
        
        const actionsOriginal = originalRow.querySelector('.galaxy-actions');
        if (actionsOriginal) {
            const spyLink = actionsOriginal.querySelector('a');
            if (spyLink) {
                // Clone kompletten Link mit allen Attributen
                const newSpyLink = spyLink.cloneNode(true);
                newSpyLink.style.cssText = 'display: inline-flex; align-items: center; justify-content: center; line-height: 16px;';
                
                // Behalte Original Bildgröße
                const spyImg = newSpyLink.querySelector('img');
                if (spyImg) {
                    spyImg.style.cssText = 'vertical-align: middle; width: 14px; height: 14px;';
                }
                
                actionCell.appendChild(newSpyLink);
            } else {
                actionCell.textContent = '-';
                actionCell.style.color = '#666';
            }
        } else {
            actionCell.textContent = '-';
            actionCell.style.color = '#666';
        }
        row.appendChild(actionCell);
        
        return row;
    }
    
    function createSpecialTableRow(position) {
        const row = document.createElement('div');
        row.style.cssText = 'display: flex; background: #1a2332; padding: 8px 4px; border-bottom: 1px solid #0a0e27; align-items: center;';
        
        // Position
        const posCell = document.createElement('div');
        posCell.style.cssText = 'width: 15px; text-align: center; color: #64b5f6; font-weight: bold;';
        posCell.textContent = position;
        row.appendChild(posCell);
        
        // Spanning cell
        const textCell = document.createElement('div');
        textCell.style.cssText = 'flex: 1; text-align: center; color: #64b5f6; font-weight: 500; font-size: 14px;';
        textCell.textContent = position === 16 ? '🌌 Expedition' : '💱 Handelszone';
        row.appendChild(textCell);
        
        return row;
    }
    
    function createFooter(originalContainer) {
        const footerDiv = document.createElement('div');
        footerDiv.style.cssText = 'background: #13181f; color: #849ab7; font-size: 12px; padding: 8px;';
        
        // Finde Legende Row
        const legendeRow = originalContainer.querySelector('.galaxy-grid-row.legende');
        if (legendeRow) {
            const legendeClone = legendeRow.cloneNode(true);
            legendeClone.style.cssText = 'padding: 8px 0; text-align: center;';
            footerDiv.appendChild(legendeClone);
        }
        
        // Finde Info Rows (Raketen, Flotten, Sonden, Recycler)
        const infoRows = originalContainer.querySelectorAll('.galaxy-grid-row.info');
        infoRows.forEach(row => {
            const infoClone = row.cloneNode(true);
            infoClone.style.cssText = 'padding: 4px 0; text-align: center; color: #849ab7;';
            footerDiv.appendChild(infoClone);
        });
        
        // Finde Sync Row (Export/Import Buttons)
        const syncRow = originalContainer.querySelector('.galaxy-grid-row.sync');
        if (syncRow) {
            const syncClone = syncRow.cloneNode(true);
            syncClone.style.cssText = 'padding: 8px 0; text-align: center;';
            footerDiv.appendChild(syncClone);
        }
        
        return footerDiv.children.length > 0 ? footerDiv : null;
    }
})();
        """.trimIndent()
    }
}