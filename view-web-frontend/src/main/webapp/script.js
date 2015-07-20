var helpButtons = document.querySelectorAll('.help_button')

var forEach = function (array, callback, scope) {
	for (var i = 0; i < array.length; i++) {
		callback.call(scope, array[i])
	}
}

for (var i = 0; i < helpButtons.length; i++) {
	helpButtons[i].addEventListener('click', function(e) {
		e.preventDefault()
		e.stopPropagation()
		thisPopup = this.nextElementSibling
		visiblePopup = closePopup()
		if ( visiblePopup !== thisPopup ) {
			this.parentNode.toggleClass('-show-popup')
		}
	})
}

document.addEventListener('click', function() { closePopup() })

forEach( document.querySelectorAll('.more-options-toggle'), function(e) {
	e.addEventListener('click', function() {
		document.querySelector('.more-options').slideToggle()
		forEach( document.querySelectorAll('.more-options-toggle'), function(e) {
			e.toggleClass('-visible')
		})
	})
})

function closePopup() {
	help = document.querySelector('.help.-show-popup')
	if ( help ) {
		help.removeClass('-show-popup')
		return help.querySelector('.help_popup')
	}
}

HTMLElement.prototype.addClass = function(className) {
	this.className += ' ' + className;
}

HTMLElement.prototype.removeClass = function(className) {
	var newClassName = '',
		classes = this.className.split(' ')
	for ( var i = 0; i < classes.length; i++ ) {
		if ( classes[i] !== className ) newClassName += classes[i] + ' '
	}
	this.className = newClassName
}

HTMLElement.prototype.toggleClass = function(className) {
	var classes = this.className.match(/\S+/g) || [],
		index = classes.indexOf(className)
	index >= 0 ? classes.splice(index, 1) : classes.push(className)
	this.className = classes.join(' ')
}

HTMLElement.prototype.slideToggle = function() {
	var el = this,
		down = (el.style.height == '')

	if ( down ) el.style.height = ''
	el.style.display = 'block'
	el.style.overflow = 'hidden'
	var finalHeight = el.offsetHeight

	if ( down ) {
		var height = 0
		el.style.height = 0
	} else {
		var height = parseInt(el.style.height, 10)
	}

	var iterations = 20,
		duration = 400,
		interval = duration / iterations,
		heightIncrement = finalHeight / (duration / interval)
	var tween = function() {
		if ( down ) {
			height += heightIncrement
			window.scrollBy(0, heightIncrement);
		} else {
			height -= heightIncrement
			window.scrollBy(0, -heightIncrement);
		}
		el.style.height = height + 'px'
		if ( down ) {
			if ( height < finalHeight ) {
				setTimeout(tween, interval)
			} else {
				el.style.overflow = ''
			}
		} else {
			if ( height > 0 ) {
				setTimeout(tween, interval)
			} else {
				el.style.display = 'none'
				el.style.height = ''
			}
		}
	}
	tween()
}
