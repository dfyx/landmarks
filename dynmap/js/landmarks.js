//Changelog:
//
//ThulinmaVersion 0.4:
//- Added support for mixed icon sizes.
//- Added support for sublists of types.
//- Added support for toggling sublists on/off by clicking the sublist title.
//
//ThulinmaVersion 0.3: (not publicly released)
//- Fixed landmarks not disappearing when deleted.
//- Fixed landmarklist updating only after a few seconds when switching worlds.
//
//ThulinmaVersion 0.2:
//- Added icon support
//- Landmark list now sorted.
//
//ThulinmaVersion 0.1:
//- First release

componentconstructors['landmarks'] = function(dynmap, configuration) {
	var markers = {};
  var toggled = {};

	var link = $("<link>");
	link.attr({
		type: 'text/css',
		rel: 'stylesheet',
		href: 'css/landmarks.css'
	});
	$("head").append( link );

  $(".landmarktypename").live("click", function(){
    landmarkToggle($(this).text());
  });

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
         landmarkShow(markers[name]);
        }else{
          landmarkHide(markers[name]);
        }

      }

      // Remove markers that are still outdated
      $.each(markers, function(key, value) {
        if(markers[key]['outdated'] === true){
          landmarkHide(markers[key]);
          markers[key]['marker'].remove();
          delete markers[key];
        }
      });

      //fix positions of icons if not 14x14 pixels
      $(".landmarkMarker img").each(function(){
        var ml = -Math.round($(this).width()/2)+"px";
        var mt = -Math.round($(this).width()/2)+"px";
        if (($(this).parent().css("margin-left") !== ml) || ($(this).parent().css("margin-top") !== mt)){
          $(this).parent().css("margin-left", ml).css("margin-top", mt);
        }
      });

    });
  });

  $(dynmap).bind('mapchanging', function() {
    for(var name in markers){landmarkHide(markers[name]);}
  });

  $(dynmap).bind('mapchanged', function() {
    for(var name in markers){
      var data = markers[name]['data'];
      if (dynmap.world.name == data.world){
        var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data.world, data.x, data.y, data.z));
        markers[name]['marker'].setLatLng(markerPosition);
        landmarkShow(markers[name]);
      } else if(dynmap.map.hasLayer(markers[name]['marker'])) {
        landmarkHide(markers[name]);
      }
    }
  });

  function landmarkToggle(t){
    if ($(".landmarklist li[name=\""+t+"\"] ul").is(":visible")){
      for (var name in markers){
        if (markers[name]['data'].type == t){
          if (dynmap.map.hasLayer(markers[name]['marker'])){dynmap.map.removeLayer(markers[name]['marker']);}
        }
      }
      $(".landmarklist li[name=\""+t+"\"] ul").hide("slow");
    }else{
      $(".landmarklist li[name=\""+t+"\"] ul").show("slow");
      for (var name in markers){
        if (markers[name]['data'].type == t){
          if (dynmap.world.name == markers[name]['data'].world){
            landmarkShow(markers[name]);
          }
        }
      }
    }
  };
  
  function landmarkHide(m){
    if (dynmap.map.hasLayer(m['marker'])){dynmap.map.removeLayer(m['marker']);}
    $(".landmarktype.landmarktype_"+m['data'].type+" li[name=\""+m['data'].name+"\"]").remove();
    if ($(".landmarklist li[name=\""+m['data'].type+"\"]").children().length == 0){
      $(".landmarklist li[name=\""+m['data'].type+"\"]").remove();
    }
  };
  
  function landmarkShow(m){
    if (!m['data'].type){m['data'].type = "Default";}
    var type = m['data'].type;
    var name = m['data'].name;
    
    if ($(".landmarklist li[name=\""+type+"\"]").length == 0){
      var lastelem = $(".landmarklist").children().first();
      var appendIt = false;
      while ((lastelem.length > 0) && (lastelem.text().toLowerCase() < type.toLowerCase())){
        lastelem = lastelem.next();
      }
      if (lastelem.length == 0){appendIt = true;}
      if (appendIt){
        $("<li name=\""+type+"\" />").append("<img src=\"images/landmark_"+type.toLowerCase()+".png\"> <span class=\"landmarktypename\">"+type+"</span><ul class=\"landmarktype landmarktype_"+type+"\"></ul>").appendTo(".landmarklist");
      }else{
        $("<li name=\""+type+"\" />").append("<img src=\"images/landmark_"+type.toLowerCase()+".png\"> <span class=\"landmarktypename\">"+type+"</span><ul class=\"landmarktype landmarktype_"+type+"\"></ul>").insertBefore(lastelem);
      }
    }
    if ($(".landmarktype.landmarktype_"+type+" li[name=\""+name+"\"]").length == 0){
      var lastelem = $(".landmarktype[name=\""+type+"\"]").children().first();
      var appendIt = false;
      while ((lastelem.length > 0) && (lastelem.text().toLowerCase() < data.name.toLowerCase())){
        lastelem = lastelem.next();
      }
      if (lastelem.length == 0){appendIt = true;}
      if (appendIt){
        $("<li name=\""+name+"\" />").text(name).click(gotoLandmark).appendTo(".landmarktype.landmarktype_"+type);
      }else{
        $("<li name=\""+name+"\" />").text(name).click(gotoLandmark).insertBefore(lastelem);
      }
    }
    if ($(".landmarktype.landmarktype_"+type+" li[name=\""+name+"\"]").is(":visible")){
      dynmap.map.addLayer(m['marker']);
    }
  };

  function gotoLandmark(){
    dynmap.map.panTo(markers[$(this).attr("name")]['marker'].getLatLng());
  };

};

function createMarker(data){
  var markerPosition = dynmap.getProjection().fromLocationToLatLng(new Location(data.world, data.x, data.y, data.z));
  return new L.CustomMarker(markerPosition, { elementCreator: function() {
    var div = document.createElement('div');
    if (!data.type){data.type = "Default";}
    $(div).addClass('Marker').addClass('landmarkMarker').append($('<img/>').attr("src", "images/landmark_"+data.type.toLowerCase()+".png")).append($('<span/>').addClass('landmarkName').text(data.name));
    return div;
  }});
};

