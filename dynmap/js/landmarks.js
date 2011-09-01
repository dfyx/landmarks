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
      // Set all markers to outdated
      $.each(markers, function(key, value) {
        markers[key]['outdated'] = true;
      });

      if ($("#landmarks").length == 0){
        dynmap.sidebar.children().children().first().next().before("<fieldset id=\"landmarks\">");
        $("#landmarks").append("<legend>Landmarks</legend><ul class=\"landmarklist\"></ul>");
      }

      for(i = 0; i < data.length; i++){
        var name = data[i].name;
        if(markers[name]){
          var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data[i].world, data[i].x, data[i].y, data[i].z));
          markers[name]['outdated'] = false;
          markers[name]['marker'].setLatLng(markerPosition);
          markers[name]['data'] = data[i];
        }else{
          markers[name] = {}
          markers[name]['data'] = data[i];
          markers[name]['marker'] = createMarker(data[i]);
        }
        if (dynmap.world.name == data[i].world){
          var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data[i].world, data[i].x, data[i].y, data[i].z));
          markers[name]['marker'].setLatLng(markerPosition);
          dynmap.map.addLayer(markers[name]['marker']);
          if ($(".landmarklist li[name=\""+data[i].name+"\"]").length == 0){
            if (!data[i].type){data[i].type = "Default";}
            var lastelem = $(".landmarklist").children().first();
            var appendIt = false;
            while ((lastelem.length > 0) && (lastelem.text().toLowerCase() < data[i].name.toLowerCase())){
              lastelem = lastelem.next();
            }
            if (lastelem.length == 0){appendIt = true;}
            if (appendIt){
              $("<li name=\""+data[i].name+"\" />").css("background-image", "url(images/landmark_"+data[i].type.toLowerCase()+".png)").text(data[i].name).click(gotoLandmark).appendTo(".landmarklist");
            }else{
              $("<li name=\""+data[i].name+"\" />").css("background-image", "url(images/landmark_"+data[i].type.toLowerCase()+".png)").text(data[i].name).click(gotoLandmark).insertBefore(lastelem);
            }
          }
        }else{
          $(".landmarklist li[name=\""+data[i].name+"\"]").remove();
          if(dynmap.map.hasLayer(markers[name]['marker'])) {
            dynmap.map.removeLayer(markers[name]['marker']);
          }
        }

      }

      // Remove markers that are still outdated
      $.each(markers, function(key, value) {
        if(markers[key]['outdated'] === true){
          $(".landmarklist li[name=\""+data[i].name+"\"]").remove();
          markers[key]['marker'].remove();
          delete markers[key];
        }
      });

    });
  });

  $(dynmap).bind('mapchanging', function() {
    for(var name in markers){dynmap.map.removeLayer(markers[name]['marker']);}
  });

  $(dynmap).bind('mapchanged', function() {
    for(var name in markers){
      var data = markers[name]['data'];
      if (dynmap.world.name == data.world){
        var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data.world, data.x, data.y, data.z));
        markers[name]['marker'].setLatLng(markerPosition);
        dynmap.map.addLayer(markers[name]['marker']);
      } else if(dynmap.map.hasLayer(markers[name]['marker'])) {
        dynmap.map.removeLayer(markers[name]['marker']);
      }
    }
  });


  function gotoLandmark(){
    dynmap.map.panTo(markers[$(this).attr("name")]['marker'].getLatLng());
  }

}

function createMarker(data){
  var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data.world, data.x, data.y, data.z));
  return new L.CustomMarker(markerPosition, { elementCreator: function() {
    var div = document.createElement('div');
    if (!data.type){data.type = "Default";}
    $(div).addClass('Marker').addClass('landmarkMarker').css("background-image", "url(images/landmark_"+data.type.toLowerCase()+".png)").append($('<span/>').addClass('landmarkName').text(data.name));
    return div;
  }});
};

