componentconstructors['landmarks'] = function(dynmap, configuration) {
	var markers = {};
	
	var link = $("<link>");
	link.attr({
		type: 'text/css',
		rel: 'stylesheet',
		href: 'css/landmarks.css'
	});
	$("head").append( link ); 
	
	$(dynmap).bind('worldupdating', function() {
		$.getJSON('markers.json', function(data) {
			for(i = 0; i < data.length; i++)
			{
				var name = data[i].name;
				if(markers[name])
				{
					var markerPosition = dynmap.map.getProjection().fromWorldToLatLng(data[i].x, data[i].y, data[i].z);
					markers[name]['marker'].setPosition(markerPosition);
					markers[name]['data'] = data[i];
					markers[name]['marker'].toggle(dynmap.world.name == data[i].world);
				}
				else
				{
					markers[name] = {}
					markers[name]['data'] = data[i];
					markers[name]['marker'] = createMarker(data[i]);
					markers[name]['marker'].toggle(dynmap.world.name == data[i].world);
				}
			}
			
			// TODO remove markers
		});
	});
	
	$(dynmap).bind('mapchanged', function() {
		for(var name in markers)
		{
			var data = markers[name]['data'];
			var markerPosition = dynmap.map.getProjection().fromWorldToLatLng(data.x, data.y, data.z);
			markers[name]['marker'].setPosition(markerPosition);
			markers[name]['marker'].toggle(dynmap.world.name == data.world);
		}
	});
}

function createMarker(data)
{
	var markerPosition = dynmap.map.getProjection().fromWorldToLatLng(data.x, data.y, data.z);
	return new CustomMarker(markerPosition, dynmap.map, function(div) {
			$(div)
				.addClass('Marker')
				.addClass('landmarkMarker')
				.append($('<span/>')
					.addClass('landmarkName')
					.text(data.name));
		});
	}
