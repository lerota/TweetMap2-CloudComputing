<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <title>TweetMap</title>
    <style>
      html, body, #map-canvas {
        height: 100%;
        margin: 0px;
        padding: 0px
      }
      #panel {
        position: absolute;
        top: 5px;
        left: 50%;
        margin-left: -180px;
        z-index: 5;
        background-color: #fff;
        padding: 5px;
        border: 1px solid #999;
      }
      div.absolute{
      	position: absolute;
 	 	top: 230px;
 	  	left: -200px;
  	 	width: 400px;
  	 	height:200px; 
      }
      
    </style>
    <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=visualization"></script>

    
    <script src="//code.jquery.com/jquery-1.11.2.min.js"></script>
	<script src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
	
	<script src="js/markerclusterer.js" type="text/javascript"></script>
    
    <!-- Bootstrap Core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom CSS -->
    <link href="css/clean-blog.min.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="http://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet" type="text/css">
    <link href='http://fonts.googleapis.com/css?family=Lora:400,700,400italic,700italic' rel='stylesheet' type='text/css'>
    <link href='http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800' rel='stylesheet' type='text/css'>
    
    <script>   
    var finance;
    var entertainment;
    var sports;
    var technology;
    var stringObj = ['Finance','Entertainment','Sports','Technology'];
    var numberObj = [0,0,0,0];

	var map, pointArrayMap, pointArrayScore, heatmap, marker;
	var taxiDataMap = [];
	var taxiDataTrend = [];
	var taxiDataScore = [];
	var taxiData = [];
	var layers = [];
	
	var lati;
	var longi;
	var score;
	var latlng,latlngi;
	
	var markerCluster;
	var markers = [];
	var markerLoc;
	
	var markerTrds;

	var chatClient = new WebSocket("ws://54.164.147.68:8080/aws");
	<!-- var chatClient = new WebSocket("ws://localhost:8080/twitMapTest4/aws");-->
	
	var markers = [];

	var curKW = "";
	var reset = false;

	chatClient.onopen = function(){
		setInterval(function(){
			if (reset){
				if(layers.length>0){
					for(i=0;i<layers.length;i++){
						layers[i].setMap(null);
						layers[i] = null;
					}
					layers = [];
				}
				reset = false;
			}
			chatClient.send(curKW);
		},3000);
	}

	chatClient.onmessage = function(evt) {
		taxiData = [];
		taxiDataMap = [];
		taxiDataTrend = [];
		taxiDataScore = [];
		stringObj = [];
		numberObj = [];
	    var rawString = evt.data;
	  	var rawJSON = JSON.parse(rawString);
	  	for (var first in rawJSON) {
	  		var flag = rawJSON[first].flag;
	  		break;
	  	}
	  	if(flag == "location"){
	  		for (var loc in rawJSON){
			    	var lat = rawJSON[loc].latitude;
			    	var lng = rawJSON[loc].longitude;
			    	if(lat&&lng){
			    		latlng = new google.maps.LatLng(lat,lng);
			    		taxiDataMap.push(latlng);
			    	}
	  		}
		    if(taxiDataMap){
		    	pointArrayMap.clear();
		    	for (i=0; i<taxiDataMap.length; i++){
		    		pointArrayMap.push(taxiDataMap[i]);
		    	}
		    }
	  	} else if(flag == "trends"){
	  		
	  		for (var loc in rawJSON){
		    	var lat = rawJSON[loc].latitude;
		    	var lng = rawJSON[loc].longitude;
				var trd = rawJSON[loc].trend;
		    	if(lat&&lng){
		    		lat = Number(lat);
		    		lng = Number(lng);
		    		latlng = {lat: lat, lng: lng};
					var trdeach = {latlng:latlng, trd:trd};
		    		taxiDataTrend.push(trdeach);
		    	}		    	
	  		}
        	update();

	  	} else {
	  		for (var loc in rawJSON) {
		        score = rawJSON[loc].score;
		        taxiDataScore.push(score);
		    } 
	  		for (var loc in rawJSON) {
	  			lati = rawJSON[loc].latitude;
		    	longi = rawJSON[loc].longitude;
		    	if(lati&&longi){
		    		latlngi = new google.maps.LatLng(Number(lati),Number(longi));
		    		taxiData.push(latlngi);
		    	}	        
		    } 
        	update();
        	if(taxiData){
        		pointArrayScore.clear();
		    	for (i=0; i<taxiData.length; i++){
		    		pointArrayScore.push(taxiData[i]);
		    	}
		    }
	  	}  
	}

	function kwselect(kw){
		reset = true;
		taxiDataMap = [];
		taxiDataTrend = [];
		taxiDataScore = [];
		taxiData = [];
		curKW = kw;
	}

	function initialize() {
	  var mapOptions = {
	    zoom: 2,
	    center: new google.maps.LatLng(46, 0),
	    mapTypeId: google.maps.MapTypeId.ROADMAP,
	    noClear:false
	  };
	  
	  map = new google.maps.Map(document.getElementById('map-canvas'),
	      mapOptions);
	  
	  pointArrayMap = new google.maps.MVCArray(taxiDataMap);
	  heatmap = new google.maps.visualization.HeatmapLayer({
	    data: pointArrayMap
	  });	
	  heatmap.setMap(map);
	  
	  lati = Number(lati);
	  longi = Number(longi);
	  var myLatLng = {lat: lati, lng: longi};
	  
	  pointArrayScore = new google.maps.MVCArray(taxiData);
	  marker = new google.maps.Marker({
		    position: myLatLng,
		    map: map,
		    title: score
		});
	 
	}
	
	function update() {
		  
		  lati = Number(lati);
		  longi = Number(longi);
		  var myLatLng = {lat: lati, lng: longi};
		  
		  pointArrayScore = new google.maps.MVCArray(taxiData);
		  marker = new google.maps.Marker({
			    position: myLatLng,
			    map: map,
			    title: score
			});
		  markers.push(marker);
		  markerCluster = new MarkerClusterer(map, markers);
		  
		  for(i=0; i<taxiDataTrend.length;i++){
			  var tmp = taxiDataTrend.pop();
			  console.log(tmp.latlng);
			  console.log(tmp.trd);
			  markerTrds = new google.maps.Marker({
				    position: tmp.latlng,
				    map: map,
				    title: tmp.trd,
				    icon: 'http://findicons.com/files/icons/2330/open_source_icons_gcons/32/flag.png'
				});
			  markerTrds.setMap(map);
		  }		  
	}
	
	google.maps.event.addDomListener(window, 'load', initialize);
    </script>  
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>      
  </head>

  <body>
  <!-- Navigation -->
    <nav class="navbar navbar-default navbar-custom navbar-fixed-top">
        <div class="container-fluid">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header page-scroll">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
            </div>
        </div>
        <!-- /.container -->
    </nav>

    <!-- Page Header -->
    <!-- Set your background image for this header on the line below. -->
    <header class="intro-header" style="background-image: url('img/home-bg.png')">
        <div class="container">
            <div class="row">
                <div class="col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1">
                    <div class="site-heading" style="color:#FFF; text-shadow: 0 0 20px #5bc0de">
                        <h1>TweetMap</h1>
                        <hr class="small">
                        
                        <div class="absolute" id="piechart" style="width: 400px; height: 200px"></div>
                        <div class="btn-group" style="top:140px; right:80px">
  						<button class="btn btn-info dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-expanded="true">
    						Show Trends
    						<span class="caret"></span>
  						</button>
  						<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu1" style="left:10px">
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('United States')">United States</a></li>
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('France')">France</a></li>
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('United Kingdom')">United Kingdom</a></li>
  						</ul>
  						</div>        
                        <div class="btn-group" style="top:140px; left:40px">
  						<button class="btn btn-info dropdown-toggle" type="button" id="dropdownMenu2" data-toggle="dropdown" aria-expanded="true">
    						Choose the Keyword
    						<span class="caret"></span>
  						</button>
  						<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu2" style="left:50px">
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('football')">Football</a></li>
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('Taylor Swift')">Taylor Swift</a></li>
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('Accenture')">Accenture LLC.</a></li>
  						<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('apple')">Apple</a></li>
  						</ul>
  						</div>
  						<div class="btn-group" style="top:140px; left:130px">
  						<button class="btn btn-info dropdown-toggle" type="button" id="dropdownMenu3" data-toggle="dropdown" aria-expanded="true">
    						Choose the Category
    						<span class="caret"></span>
  						</button>
  						<ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu3" style="left:50px">
    					<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('finance')">Finance</a></li>
    					<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('technology')">Technology</a></li>
    					<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('sports')">Sports</a></li>
    					<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('entertainment')">Entertainment</a></li>
    					<li role="presentation"><a role="menuitem" tabindex="-1" href="#" style="text-align:center" onclick="kwselect('all')">All</a></li>
  						</ul>
						</div>
                    </div>
                </div>
            </div>
        </div>
    </header>
    
    <!-- <div id="scoreText" style="margin-top:0px;"> </div> -->

    <!-- Main Content -->
        <div id="map-canvas" style="margin-top:0px;"> </div>
    <hr>

    <!-- Footer -->
    <footer>
        <div class="container">
            <div class="row">
                <div class="col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1">
                    <ul class="list-inline text-center"> </ul>
                    <p class="copyright text-muted">Copyright &copy; 2015</p>
                </div>
            </div>
        </div>
    </footer>

    <!-- jQuery -->
    <script src="js/jquery.js"></script>

    <!-- Bootstrap Core JavaScript -->
    <script src="js/bootstrap.min.js"></script>

    <!-- Custom Theme JavaScript -->
    <script src="js/clean-blog.min.js"></script>

</body>

</html>