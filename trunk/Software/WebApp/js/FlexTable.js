/**
 *	@author	Man Hoang
 *	@version	1.0
 */

var cnTFlexTable	= 'TFlexTable';
var cnFocusedRow	= 'Focused';
var cnSelectedRow	= 'Selected';
var cnMatchedString	= 'TMatchedString';

var STableRow		= 'table-row';

/**
 *
 *	@param	container	{HTMLTableElement}	An empty table.
 */
function TFlexTable(container) {
/* private */
	
	var self = this;
	var fElement = newElement('table', cnTFlexTable);
	container.appendChild(fElement);
	var fRows = fElement.rows;
	var fRowCount = 0;
	var fColumnCount = 0;
	
	var fData;
	var fFilterColumnIndex = -1;
	var fFilterValue = '';
	
	var fSelectedIndex = -1;	// Index of the selected row
	var fFocusedIndex = -1;
	var fTextBox = null;	// HTMLInputElement box associated with this table.

	/**
	 *	Scrolls the list to show an row if it is invisible.
	 *
	 *	@param	row	{HTMLElement}	The row to be shown.
	 */
	function showRow(row) {
		var t = row.offsetTop - fElement.clientTop;
		if (t < fElement.scrollTop)
			fElement.scrollTop = t;
		else {
			t += row.offsetHeight - fElement.clientHeight;
			if (t > fElement.scrollTop)
				fElement.scrollTop = t;
		}
	}
	/**
	 *	Focus the next displayed item.
	 *
	 *	@param	direction	> 0 means down, else up.
	 */
	this.focusNextRow = function (direction) {	
		var i = -1;
		if (fFocusedIndex > -1 && fRows[fFocusedIndex].style.display != SNone)
			i = fFocusedIndex;
							
		if (direction > 0) {
			do {
				i++;
			} while ((i < fRows.length) && (fRows[i].style.display == SNone));
			if (i == fRows.length)
				i = -1;
		} else {
			if (i == -1)
				i = fRows.length;
			do {
				i--;
			} while ((i > -1) && (fRows[i].style.display == SNone));
		}

		self.setFocusedIndex(i);
	}
	
	function rowClick(event) {
		self.setSelectedIndex(this.sectionRowIndex);
		event.preventDefault();
	}
	
	function rowMouseOut(event) {
		self.setFocusedIndex(-1);
		event.preventDefault();
	}
	
	function rowMouseOver(event) {
		self.setFocusedIndex(this.sectionRowIndex);
		event.preventDefault();
	}

	function textBoxFocus() {
		self.filter(-1, this.value);
	}
	
	function textBoxBlur() {
		if (self.autoClearTextBox) {
			this.value = '';
			self.filter(-1, '');
		}
	}
	
	function textBoxKeyDown(event) {
		switch (event.which) {
			case 13:	/* Enter */
				self.setSelectedIndex(fFocusedIndex);
				break;
			case 38:	/* Up   */
			case 40:	/* Down */
				self.focusNextRow(event.which - 39);
				if (fFocusedIndex > -1) {
					if (browser.isChrome || browser.isIE)
						this.value = fRows[fFocusedIndex].cells[fFilterColumnIndex].innerText;
				} else
					this.value = fFilterValue;
				break;
			case 9:		/* Tab */
			case 27:	/* Esc */
				if (self.autoClearTextBox)
					this.value = '';
				break;
			//default:
			//	self.filter(-1, this.value);
			//	return;
		}
	}
	
	function textBoxKeyUp(event) {
		var c = event.which;
		if (c != 38 && c != 40)
			self.filter(-1, this.value);
	}
	
	this.getColumnCount = function () {
		if (fRows.length > 0)
			return fRows[0].cells.length;
		return 0;
	}
	
	this.getRowCount = function () {
		return fRows.length;
	}
	
	/**
	 *	Returns the DOM fElement of this table.
	 */
	this.getElement = function () {
		return fElement;
	}

	/**
	 *	Returns the index of the focused row or -1 if none.
	 */
	this.getFocusedIndex = function () {
		return fFocusedIndex;
	}

	/**
	 *	Returns the index of the selected row or -1 if none.
	 */
	this.getSelectedIndex = function () {
		return fSelectedIndex;
	}

	/**
	 *	Should be called when there is at least one row.
	 */
	this.setMaxRowCount = function(value) {
		var h = 'auto';
		if (value > 0)
			h = value * fRows[0].offsetHeight + SPixel;
		fElement.style.maxHeight = h;
	}
	
	this.setSelectedIndex = function (value) {
		if (fSelectedIndex != value) {
			if (fSelectedIndex > -1)
				fRows[fSelectedIndex].className = '';
			
			if (value > -1) {
				var row = fRows[value];
				row.className = cnSelectedRow;
				showRow(row);
			}
			
			fSelectedIndex = value;
			
			if (self.onChange)
				self.onChange();
		}
	}

	this.setFocusedIndex = function (value) {
		if (fFocusedIndex != value) {
			if (fFocusedIndex > -1) {
				var row = fRows[fFocusedIndex];
				if (row.sectionRowIndex != fSelectedIndex)
					row.className = '';
				else
					row.className = cnSelectedRow;
			}
			
			if (value > -1) {
				var row = fRows[value];
				if (row.sectionRowIndex != fSelectedIndex)
					row.className = cnFocusedRow;
				showRow(row);
			}
			
			fFocusedIndex = value;

			if (self.onFocusChange) {
				self.onFocusChange();
			}
		}
	}
	
	this.attachTextBox = function (textBox, columnIndex) {
		fFilterColumnIndex = columnIndex;
		if (fTextBox != textBox) {
			if (fTextBox) {
				fTextBox.removeEventListener(SFocus	 , textBoxFocus	, false);
				fTextBox.removeEventListener(SBlur	 , textBoxBlur	, false);
				fTextBox.removeEventListener(SKeyUp	 , textBoxKeyUp	, false);
				fTextBox.removeEventListener(SKeyDown, textBoxKeyDown, false);
			}
			if (textBox) {
				textBox.addEventListener(SFocus	 , textBoxFocus	 , false);
				textBox.addEventListener(SBlur	 , textBoxBlur	 , false);
				textBox.addEventListener(SKeyUp	 , textBoxKeyUp	 , false);
				textBox.addEventListener(SKeyDown, textBoxKeyDown, false);
			}
			fTextBox = textBox;
		}
	}

	/**
	 *	Filters the table.
	 *
	 *	@param	columnIndex	{Integer}	Index of the column to be filtered.
	 *	@param	value		{String}	Filter value.
	 *	@param	caseSensitive	{Boolean}	If this param is true, performs a case insensitive filter.
	 */
	this.filter = function (columnIndex, value, caseSensitive) {
		if (!fRows)
			return;
		
		if (columnIndex > -1)
			fFilterColumnIndex = columnIndex;
		
		if (fFilterValue != value) {
			fFilterValue = value;

			self.setFocusedIndex(-1);
			
			var showAll = (fFilterValue == null) || (fFilterValue == '');
			var tagBegin = '<span class="' + cnMatchedString + '">';
			var tagEnd = '</span>';
			var modifier = caseSensitive ? '' : 'i';
			var regExp = new RegExp(fFilterValue, modifier);
			var matches;
			var row;
			var cell;
			var cellText;
			for (var i = fRows.length - 1; i >= 0; i--) {
				//cellText = fData[i + 1].toArray()[fFilterColumnIndex].toString();
				row = fRows[i];
				cell = row.cells[fFilterColumnIndex];
				cellText = cell.innerHTML.replace(tagBegin, '').replace(tagEnd, '');
				cell.innerHTML = cellText;
				
				if (showAll == false) {
					matches = cellText.match(regExp);
					if (matches) {
						cell.innerHTML = cellText.replace(matches[0], tagBegin + matches[0] + tagEnd);
						row.style.display = STableRow;
					} else
						row.style.display = SNone;
				} else
					row.style.display = STableRow;
			}
			fElement.scrollTop = 0;
		}
	}

	/**
	 *	Inserts a new row into the table.
	 *
	 *	@param	index	{Integer}	Index of the row to be inserted. -1 means at the end of the table.
	 *	@return	Reference to the inserted row.
	 */
	this.insertRow = function(index) {
		var row = fElement.insertRow(index);
		row.onclick = rowClick;
		row.onmouseout = rowMouseOut;
		row.onmouseover = rowMouseOver;
		return row;
	}

	this.deleteRow = function(index) {
		fElement.deleteRow(index);
	}
	
	this.cells = function(row, col) {
		return fElement.rows[row].cells[col];
	}
	
	this.autoClearTextBox = true;
	
	/**	onChange()
	 *	This event happens when another row is selected
	 *	Implement this handler to capture the event
	 */
	this.onChange = null;

	/**
	 *	onFocusChange()
	 *	Happens when the temporary selected row has been changed.
	 */
	this.onFocusChange = null;
}
