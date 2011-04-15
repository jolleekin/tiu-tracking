/**
 *	@class	TTabPanel	The tab panel itself.
 *	@class	TTabNamePanel	The name panel.
 *	@class	TTabName	The name of a normal tab.
 *	@class	TSelectTabName	The name of the selected tab.
 *	@class	TTabContentPanel	The content of a tab.
 *	@author	Man Hoang
 *	@version	1.0
 */
function TTabPanel() {

/* private */

	var self = this;
	var tabCount = 0;
	var element = newElement('div', 'TTabPanel');
	var namePanel = newElement('ul', 'TTabNamePanel');
	var contentPanel = newElement('div', 'TTabContentPanel');
	
	element.appendChild(namePanel);
	element.appendChild(contentPanel);
	
	
/* public */

	this.getElement = function () {
		return element;
	}
	
	/**
	 *	Adds a new tab page.
	 *
	 *	@param	name	{HTMLElement or String}	Name of the tab page.
	 *	@param	content	{HTMLElement or String}	Content of the tab page.
	 *	@return	Index of the added tab page.
	 */
	this.add = function (name, content) {
		var nameNode = newElement('li', 'TTabName');
		if (name instanceof HTMLElement)
			nameNode.appendChild(name);
		else
			nameNode.innerHTML = name;
		namePanel.appendChild(nameNode);
		
		if (content instanceof HTMLElement)
			contentPanel.appendChild(content);
		else {
			var contentNode = newElement('div', 'TTabContent');
			contentNode.style.display = SNone;
			contentNode.innerHTML = content;
			contentPanel.appendChild(contentNode);
		}
		
		tabCount++;
		return tabCount - 1;
	}
	
	/**
	 *	Removes a tab page.
	 *
	 *	@param	index	{Integer}	Index of the tag page to be removed.
	 */
	this.remove = function (index) {
		if (isInRange(index, 0, tabCount - 1)) {
			namePanel.removedChild(namePanel.childNodes[index]);
			contentPanel.removedChild(contentPanel.childNodes[index]);
			tabCount--;
		} else
			throw  SIndexOutOfRange + index;
	}
	
	/**
	 *	Selects a tab page.
	 *
	 *	@param	index {Integer}	Index of the tab page to be selected.
	 */
	this.selectTab = function (index) {
		if (isInRange(index, 0, tabCount - 1)) {
			if (selectedIndex != index) {
				namePanel.childNodes[selectedIndex].className = TTabName;
				contentPanel.childNodes[selectedIndex].style.display = SNone;
				
				namePanel.childNodes[index].className = TSelectTabName;
				contentPanel.childNodes[index].style.display = SBlock;
				
				selectedIndex = index;
				
				if (self.onChanged)
					self.onChanged();
			}
		} else
			throw SIndexOutOfRange + index;
	}
	
	/**
	 *	Returns the index of currently selected tab page.
	 */
	this.getSelectedIndex = function () {
		return selectedIndex;
	}
	
	this.getTabCount = function () {
		return tabCount;
	}
	
	/**
	 *	Event onChanged()
	 *
	 *	Happens when another tag page is selected.
	 */
	 this.onChanged = null;
}