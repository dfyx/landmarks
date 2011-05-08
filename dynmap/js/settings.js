componentconstructors['settings'] = function(dynmap, configuration) {
	var link = $("<link>");
	link.attr({
		type: 'text/css',
		rel: 'stylesheet',
		href: 'css/settings.css'
	});
	$("head").append( link ); 
	
	$('.playerlist').css('height', 'auto');
};

function addSettingsPanel(name, properties)
{
	var id = 'settings-' + name.toLowerCase().replace(/[^a-z0-9]/g, '-');
	var panel = $('.sidebar .panel');
	panel.append('<fieldset id="' + id + '"></fieldset>');
	var fieldset = $('#' + id);
	fieldset.append('<legend>' + name + '</legend>');
	
	for(i = 0; i < properties.length; i++)
	{
		properties[i].id = fieldset.attr('id');
		if(properties[i].name)
		{
			properties[i].id += '-' + properties[i].name;
		}
		else
		{
			properties[i].id += '-' + properties[i].label.toLowerCase().replace(/[^a-z0-9]/g, '-');
		}
		
		switch(properties[i].type)
		{
			case 'checkbox':
				addSettingsCheckbox(fieldset, properties[i]);
				break;
			case 'radio':
				addSettingsRadio(fieldset, properties[i]);
				break;
		}
	}
}

function addSettingsCheckbox(fieldset, property)
{
	var str = '<div class="settings-checkbox-container"><input type="checkbox" id="' + property.id + '" name="' + name + '"';
	if(property.value)
	{
		str += ' checked="checked"';
	}
	str += ' /><label for="' + property.id + '">' + property.label + '</label></div>';
	fieldset.append(str);
	
	if(property.onchange)
	{
		$('#' + property.id).change(property.onchange);
	}
}

function addSettingsRadio(fieldset, property)
{
	var str = '<div id="' + property.id + '" class="settings-radio-container">';
	if(property.label)
	{
		str += '<label for="' + property.id + '">' + property.label + '</label>';
	}
	
	for(i = 0; i < property.values.length; i++)
	{
		var id = property.id + '-' + property.values[i].toLowerCase().replace(/[^a-z0-9]/, '-');
		str += '<div><input type="radio" id="' + id + '" name="' + property.name + '" value="' + property.values[i] + '"';
		if(property.value == property.values[i])
		{
			str += ' checked="checked"';
		}
		str += ' /><label for="' + id + '">' + property.values[i] + '</label></div>';
	}
	
	str += '</div>';
	fieldset.append(str);
	
	if(property.onchange)
	{
		$('#' + property.id + ' input').change(property.onchange);
	}
}
