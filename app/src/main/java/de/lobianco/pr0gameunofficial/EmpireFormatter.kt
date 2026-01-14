package de.lobianco.pr0gameunofficial

object EmpireFormatter {

    /**
     * JavaScript Code to make Empire view mobile-friendly with sticky headers and alternating colors
     */
    fun getFormatterScript(firstColumnWidth: Int, alternatingColors: Boolean): String {
        return """
(function() {
    'use strict';
    console.log('=== Empire Formatter: Starting ===');
    console.log('Empire Formatter: First column width = $firstColumnWidth px');
    console.log('Empire Formatter: Alternating colors = $alternatingColors');
    
    // Wait for DOM to be ready
    if (document.readyState === 'loading') {
        console.log('Empire Formatter: Waiting for DOM...');
        document.addEventListener('DOMContentLoaded', formatEmpireView);
        return;
    }
    
    formatEmpireView();
    
    function formatEmpireView() {
        console.log('Empire Formatter: Formatting...');
        
        // Find ALL tables on the page
        const allTables = document.querySelectorAll('table');
        console.log('Empire Formatter: Found ' + allTables.length + ' tables total');
        
        // Find the main content table (has tbody with many rows)
        let mainTable = null;
        allTables.forEach((table, idx) => {
            const tbody = table.querySelector('tbody');
            if (tbody) {
                const rowCount = tbody.querySelectorAll('tr').length;
                console.log('Empire Formatter: Table ' + idx + ' has ' + rowCount + ' rows');
                if (rowCount > 20) { // Empire table has many rows
                    mainTable = table;
                    console.log('Empire Formatter: Selected table ' + idx + ' as main table');
                }
            }
        });
        
        if (!mainTable) {
            console.log('Empire Formatter: Main table not found!');
            return;
        }
        
        const tbody = mainTable.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));
        console.log('Empire Formatter: Processing ' + rows.length + ' rows');
        
        // Find the three header rows
        let planetImagesRow = null;
        let planetNamesRow = null;
        let coordinatesRow = null;
        
        rows.forEach((row, idx) => {
            const firstCell = row.querySelector('td');
            if (!firstCell) return;
            
            const text = firstCell.textContent.trim();
            
            // Planet images row has "Planet (X / Y)" text
            if (text.match(/Planet.*\d+.*\d+/)) {
                planetImagesRow = row;
                console.log('Empire Formatter: Row ' + idx + ' = Planet Images');
            }
            // Names row
            else if (text === 'Name') {
                planetNamesRow = row;
                console.log('Empire Formatter: Row ' + idx + ' = Names');
            }
            // Coordinates row
            else if (text === 'Koordinaten') {
                coordinatesRow = row;
                console.log('Empire Formatter: Row ' + idx + ' = Coordinates');
            }
        });
        
        if (!planetImagesRow || !planetNamesRow || !coordinatesRow) {
            console.log('Empire Formatter: ERROR - Missing rows!', {
                images: !!planetImagesRow,
                names: !!planetNamesRow,
                coords: !!coordinatesRow
            });
            return;
        }
        
        console.log('Empire Formatter: All header rows found!');
        
        // Apply sticky styles with border
        function makeSticky(row, top) {
            row.style.cssText = 
                'position: sticky !important;' +
                'top: ' + top + 'px !important;' +
                'z-index: 100 !important;';
            
            // Add border to cells to prevent text showing through
            row.querySelectorAll('td').forEach(cell => {
                cell.style.borderBottom = '2px solid #0a0e27';
            });
        }
        
        // Get heights
        const h1 = planetImagesRow.offsetHeight;
        const h2 = planetNamesRow.offsetHeight;
        
        console.log('Empire Formatter: Heights -', h1, h2);
        
        // Make sticky
        makeSticky(planetImagesRow, 0);
        makeSticky(planetNamesRow, h1);
        makeSticky(coordinatesRow, h1 + h2);
        
        // Adjust first column width and apply text ellipsis
        console.log('Empire Formatter: Adjusting first column width and text overflow...');
        rows.forEach(row => {
            const firstCell = row.querySelector('td:first-child');
            if (firstCell) {
                firstCell.style.width = '$firstColumnWidth' + 'px';
                firstCell.style.maxWidth = '$firstColumnWidth' + 'px';
                firstCell.style.minWidth = '$firstColumnWidth' + 'px';
                firstCell.style.overflow = 'hidden';
                firstCell.style.textOverflow = 'ellipsis';
                firstCell.style.whiteSpace = 'nowrap';
            }
        });
        
        // Apply alternating colors to planet columns (if enabled)
        if ($alternatingColors) {
            console.log('Empire Formatter: Applying alternating colors...');
            rows.forEach(row => {
                // Skip sticky rows
                if (row === planetImagesRow || row === planetNamesRow || row === coordinatesRow) return;
                
                // Skip section header rows (with th elements)
                if (row.querySelector('th')) return;
                
                const cells = row.querySelectorAll('td');
                if (cells.length < 3) return;
                
                // Apply alternating colors to planet columns (skip first 2: label + total)
                cells.forEach((cell, index) => {
                    if (index < 2) return; // Skip label and total columns
                    
                    const planetIndex = index - 2;
                    const bgColor = planetIndex % 2 === 0 ? '#1e2530' : '#1e2b39';
                    cell.style.backgroundColor = bgColor;
                    cell.style.borderLeft = '1px solid #0a0e27';
                    cell.style.borderRight = '1px solid #0a0e27';
                });
            });
        } else {
            console.log('Empire Formatter: Alternating colors disabled');
        }
        
        console.log('=== Empire Formatter: DONE! ===');
    }
})();
        """.trimIndent()
    }
}
