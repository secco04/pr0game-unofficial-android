package de.lobianco.pr0gameunofficial

object FleetTableFormatter {

    /**
     * JavaScript Code to optimize fleet table display for mobile
     */
    fun getFormatterScript(fontSize: Int = 11): String {
        return """
(function() {
    'use strict';
    console.log('=== Fleet Table Formatter: Starting ===');
    console.log('Fleet Table Formatter: Font size = ${fontSize}px');
    
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
        console.log('Fleet Table Formatter: Waiting for DOM...');
        document.addEventListener('DOMContentLoaded', formatFleetTable);
        return;
    }
    
    formatFleetTable();
    
    function formatFleetTable() {
        console.log('Fleet Table Formatter: Formatting...');
        
        // Find all table rows with fleet data
        const tables = document.querySelectorAll('table');
        let fleetTable = null;
        
        tables.forEach(table => {
            const headers = table.querySelectorAll('tr td');
            if (headers.length > 0) {
                const firstHeader = headers[0].textContent.trim();
                if (firstHeader === 'ID') {
                    fleetTable = table;
                    console.log('Fleet Table Formatter: Found fleet table');
                }
            }
        });
        
        if (!fleetTable) {
            console.log('Fleet Table Formatter: Fleet table not found');
            return;
        }
        
        // Get tbody
        const tbody = fleetTable.querySelector('tbody');
        if (tbody) {
            // Prevent ALL line breaks in tbody and set font size
            tbody.style.whiteSpace = 'nowrap';
            tbody.style.fontSize = '${fontSize}px';
            console.log('Fleet Table Formatter: Set tbody to nowrap with font size ${fontSize}px');
        }
        
        // Get all data rows (skip header rows)
        const rows = Array.from(fleetTable.querySelectorAll('tr'));
        const dataRows = rows.filter(row => {
            const firstCell = row.querySelector('td:first-child');
            return firstCell && /^\d+$/.test(firstCell.textContent.trim());
        });
        
        console.log('Fleet Table Formatter: Processing ' + dataRows.length + ' fleet rows');
        
        dataRows.forEach(row => {
            const cells = row.querySelectorAll('td');
            if (cells.length < 9) return;
            
            // Reduce padding on all cells and prevent wrapping
            // Use += to preserve existing inline styles (like color:lime)
            cells.forEach(cell => {
                cell.style.cssText += 'padding: 4px; white-space: nowrap;';
            });
            
            // Fix mission cell (index 1) - remove <br> tags
            const missionCell = cells[1];
            if (missionCell) {
                // Replace <br> with space to prevent line breaks
                const brTags = missionCell.querySelectorAll('br');
                brTags.forEach(br => {
                    const space = document.createTextNode(' ');
                    br.parentNode.replaceChild(space, br);
                });
            }
            
            // Format date/time cells (index 4, 6 = Ankunft cells)
            [4, 6].forEach(index => {
                if (cells[index]) {
                    formatDateTime(cells[index]);
                }
            });
            
            // Reorganize command buttons (last cell)
            const commandCell = cells[8];
            if (commandCell) {
                reorganizeCommandButtons(commandCell);
            }
        });
        
        console.log('=== Fleet Table Formatter: DONE! ===');
        
        // Also format transport selection table (Großer/Kleiner Transporter)
        formatTransportSelection();
    }
    
    function formatTransportSelection() {
        console.log('Fleet Table Formatter: Looking for transport selection...');
        
        // Find table with transport buttons
        const tables = document.querySelectorAll('table');
        let transportTable = null;
        
        tables.forEach(table => {
            const gtButton = table.querySelector('#gt_select');
            const ktButton = table.querySelector('#kt_select');
            if (gtButton || ktButton) {
                transportTable = table;
                console.log('Fleet Table Formatter: Found transport selection table');
            }
        });
        
        if (!transportTable) {
            console.log('Fleet Table Formatter: Transport selection not found');
            return;
        }
        
        // Apply font size to entire table
        const tbody = transportTable.querySelector('tbody');
        if (tbody) {
            tbody.style.fontSize = '${fontSize}px';
        }
        
        // Style all cells
        const cells = transportTable.querySelectorAll('td');
        cells.forEach(cell => {
            cell.style.fontSize = '${fontSize}px';
            cell.style.padding = '4px';
        });
        
        // Style input fields
        const inputs = transportTable.querySelectorAll('input[type="number"]');
        inputs.forEach(input => {
            input.style.fontSize = '${fontSize}px';
            input.style.padding = '4px';
        });
        
        // Style buttons
        const buttons = transportTable.querySelectorAll('button');
        buttons.forEach(button => {
            button.style.fontSize = '${fontSize}px';
            button.style.padding = '4px 8px';
        });
        
        console.log('Fleet Table Formatter: Transport selection formatted');
    }
    
    function formatDateTime(cell) {
        // Format: "12. Jan 2026, 20:35:34" -> "12.01.2026 20:35"
        const text = cell.textContent.trim();
        
        // Match pattern: "DD. Mon YYYY, HH:MM:SS"
        const match = text.match(/(\d+)\.\s+(\w+)\s+(\d+),\s+(\d+):(\d+):(\d+)/);
        if (match) {
            const day = match[1].padStart(2, '0');
            const month = match[2];
            const year = match[3];
            const time = match[4] + ':' + match[5];
            
            // Convert month name to number
            const months = {
                'Jan': '01', 'Feb': '02', 'Mär': '03', 'Mar': '03', 'Apr': '04',
                'Mai': '05', 'Jun': '06', 'Jul': '07', 'Aug': '08',
                'Sep': '09', 'Okt': '10', 'Nov': '11', 'Dez': '12'
            };
            const monthNum = months[month] || month;
            
            // Update text but keep original color style
            cell.textContent = day + '.' + monthNum + '.' + year + ' ' + time;
            // Don't override cell.style - preserve original inline styles (like color:lime)
            
            console.log('Fleet Table Formatter: Formatted date from "' + text + '" to "' + cell.textContent + '"');
        }
    }
    
    function reorganizeCommandButtons(cell) {
        // Find forms and aborttime span
        const forms = cell.querySelectorAll('form');
        const abortSpan = cell.querySelector('.aborttime');
        
        if (forms.length === 0) return;
        
        // Clear cell and rebuild with horizontal layout
        cell.innerHTML = '';
        cell.style.padding = '4px';
        cell.style.whiteSpace = 'nowrap';
        cell.style.overflow = 'hidden';
        
        // Create container for buttons - NO WRAP!
        const buttonContainer = document.createElement('div');
        buttonContainer.style.display = 'flex';
        buttonContainer.style.flexDirection = 'row';
        buttonContainer.style.gap = '4px';
        buttonContainer.style.alignItems = 'center';
        buttonContainer.style.flexWrap = 'nowrap';  // NEVER wrap
        
        // Add aborttime first (if exists)
        if (abortSpan) {
            abortSpan.style.marginRight = '4px';
            abortSpan.style.fontSize = '${fontSize}px';
            abortSpan.style.whiteSpace = 'nowrap';
            buttonContainer.appendChild(abortSpan);
        }
        
        // Add all forms with compact buttons
        forms.forEach(form => {
            const button = form.querySelector('input[type="submit"]');
            if (button) {
                button.style.padding = '2px 4px';
                button.style.fontSize = '${fontSize}px';
                button.style.margin = '0';
                button.style.whiteSpace = 'nowrap';
            }
            form.style.display = 'inline-block';
            form.style.margin = '0';
            buttonContainer.appendChild(form);
        });
        
        cell.appendChild(buttonContainer);
    }
})();
        """.trimIndent()
    }
}