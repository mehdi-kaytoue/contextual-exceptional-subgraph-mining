
//= require jquery

//= require jquery_ujs
//= require vendor/jquery-ui
//= require turbolinks
//= require nprogress
//= require nprogress-turbolinks
//= require vendor/highcharts.min
//= require vendor/semantic.min
//= require vendor/md5
//= require vendor/jscolor.min
//= require vendor/tablesort

(function(window) {


    var map;
    var layer;
    var node_coord;
    var radius;
    var projection;
    var padding;
    var marker;
    var res = {};
    var markerLink = null;
    var currentPattern = null;
    var lineSize = 3;
    var line;
    var xpName;
    var overlay;
    var color;
    var max_q = null;
    var currentArrowColor = '#FF0000';
    var currentShapeColor = '#F1FF2E';
    var currentLineColor = '#FFA500';
    var currentShapeOpacity = 0.3;
    var edgePower = 1.2;
    var patternsRange = {};


    var iris_shapes = [];
    var labelsName = [];
    var mapOptions = {
        zoom: 13,
        center: new google.maps.LatLng(45.761130, 4.846611),
        styles: [{"featureType":"all","elementType":"labels.text.fill","stylers":[{"color":"#ffffff"}]},{"featureType":"all","elementType":"labels.text.stroke","stylers":[{"color":"#000000"},{"lightness":13}]},{"featureType":"administrative","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#144b53"},{"lightness":14},{"weight":1.4}]},{"featureType":"landscape","elementType":"all","stylers":[{"color":"#08304b"}]},{"featureType":"poi","elementType":"geometry","stylers":[{"color":"#0c4152"},{"lightness":5}]},{"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#0b434f"},{"lightness":25}]},{"featureType":"road.arterial","elementType":"geometry.fill","stylers":[{"color":"#000000"}]},{"featureType":"road.arterial","elementType":"geometry.stroke","stylers":[{"color":"#0b3d51"},{"lightness":16}]},{"featureType":"road.local","elementType":"geometry","stylers":[{"color":"#000000"}]},{"featureType":"transit","elementType":"all","stylers":[{"color":"#146474"}]},{"featureType":"water","elementType":"all","stylers":[{"color":"#021019"}]}],
        mapTypeId: google.maps.MapTypeId.MAP
    }


    Array.prototype.max = function() {
        return Math.max.apply(null, this);
    };
    function setEventD3Listeners(){

        $( "#main table tr:not(:first)" ).on({
            mouseover:function(){patternsTableShowPattern(this) },
            mouseleave:function(){patternsTableReset(this)},
            click:function(){patternsTableClick(this)}
        });

        $('#sliderLineSize').on({
            change:function(){ setLineSize(this); }
        });
    }
    function setDefaultAppListeners(){
        $('.ui.accordion').accordion({closeNested: false, exclusive: false});
        $('.ui.checkbox').checkbox();

        $('table').tablesort();

        $('table').on('tablesort:complete', function(event, tablesort) {
            $('#tableLoader').removeClass('loader');

            $( "#main table tr:not(:first)" ).on({
                click:function(){patternsTableClickAjax(this, xpName)}
            });
            $('[data-variation="inverted"]').popup();
        });

        $('table').on('tablesort:start', function(event, tablesort) {
            event.preventDefault();
            $('#tableLoader').addClass('loader');
        });



        $('.menu_popup').popup();
        $( '#xp_display').on('click', function() {
            $('.ui.labeled.icon.sidebar')
                .sidebar('setting', 'transition', 'scale down')
                .sidebar('toggle')
            ;
        });
        $('#xp_reset').on({
            click:function(){ currentPattern = null; $('.ui.accordion').accordion('toggle', 1);  $('#patternContainerContent').empty(); patternsTableReset(this) }
        })
        $('#search_pattern').on("input propertychange",function(){
            $('#search_pattern').attr('disabled', '');
            searchPatternByRegex( this )
        });
        $('#edgesColor').on("change",function(){
           currentLineColor = "#" + this.jscolor;
            updateDisplay();
        });
        $('#arrowColor').on("change",function(){
            currentArrowColor = "#" + this.jscolor;
            layer.selectAll(".arrowHead").style("stroke", currentArrowColor);
            layer.selectAll(".arrowHead").style('fill', currentArrowColor)
        });
        $('#irisColor').on("change",function(){
            currentShapeColor = "#" + this.jscolor;


            jQuery.each(iris_shapes, function(el){
                iris_shapes[el].setOptions( {fillColor:currentShapeColor,  strokeColor: currentShapeColor}  )
            })


        });
        $('#irisOpacity').on("change",function(){

            currentShapeOpacity = $(this).val();
            document.getElementById("irisOpacity_value").innerHTML= currentShapeOpacity;


            jQuery.each(iris_shapes, function(el){
                iris_shapes[el].setOptions( {fillOpacity:currentShapeOpacity }  )
            })

        });
        $('#edgePower').on("change",function(){

            edgePower = $(this).val();
            document.getElementById("edgePower_value").innerHTML= edgePower;

            updateDisplay();

        });
        $('.menu_popup').on("contextmenu", function(e){

            var xpName = $(this).attr('data-name');
            $('#xp_delete_name').text(xpName);
            $('#xp_delete_description').text($(this).attr('data-content'));



            $('.ui.basic.test.modal')
                .modal({
                    closable  : false,
                    onApprove : function() {
                        window.location.replace("/delete/"+xpName);
                    }
                })
                .modal('show')
            ;
            return false;
        });
        $('.close.icon').on('click', function(){
            $('#infoIris').hide();
        })




        var minNode = parseInt($('#minMaxTablePatterns').attr('data-min-node'));
        var maxNode = parseInt($('#minMaxTablePatterns').attr('data-max-node'));

        var minEdge = parseInt($('#minMaxTablePatterns').attr('data-min-edge'));
        var maxEdge = parseInt($('#minMaxTablePatterns').attr('data-max-edge'));

        $( "#edge-slider-range" ).slider({
            range: true,
            min: minEdge,
            max: maxEdge,
            values: [ minEdge, maxEdge ],
            slide: function(event, ui){filterTablePatternsWithRange( 'edge', event, ui )}
        });
        $( "#edgeRange" ).text( $( "#edge-slider-range" ).slider( "values", 0 ) +
            " - " + $( "#edge-slider-range" ).slider( "values", 1 ) );






        $( "#node-slider-range" ).slider({
            range: true,
            min: minNode,
            max: maxNode,
            values: [ minNode, maxNode ],
            slide: function(event, ui){filterTablePatternsWithRange( 'node', event, ui )}
        });
        $( "#nodeRange" ).text( $( "#node-slider-range" ).slider( "values", 0 ) +
            " - " + $( "#node-slider-range" ).slider( "values", 1 ) );


        $('[data-variation="inverted"]').popup();
    }


    function filterTablePatternsWithRange( column, event, ui ){

        $( "#" + column + "Range" ).text( ui.values[ 0 ] + " - " + ui.values[ 1 ] );

        var rangeStart = parseInt(ui.values[ 0 ]);
        var rangeEnd = parseInt(ui.values[ 1 ]);


        patternsRange[column] = [ rangeStart,  rangeEnd];


        $.each(patternsRange, function(key, value) {
            $('#patternsTable tr:not(:first)').each(function() {

                if ($(this).children( '.' + key + '_column' ).hasClass(key + '_column')){
                    var dataValue = parseFloat($(this).children( '.' + key + '_column' ).attr('data-value'));
                    if( dataValue < value[0] || dataValue > value[1] )
                        $(this).hide();
                    else
                        $(this).show();
                }
            })
        });


    }

    function searchPatternByRegex(e){

        var text = $(e).val();
        var way = $('#reverse_regex').is(':checked') ? true : false;
        $('#patternsTable tr').show();

        var regex = new RegExp( text );
        $('#patternsTable tr:not(:first)').each(function() {

            if(way){
                if ($(this).attr('data-html').match(regex) != null)
                    $(this).hide();
            }else {
                if ($(this).attr('data-html').match(regex) == null)
                    $(this).hide();
            }
        }).promise().done(function(){
            $('#search_pattern').removeAttr('disabled');
        });
    }
    function setLineSize(e){
        var value = $(e).val();
        document.getElementById("LineRange").innerHTML= value;
        lineSize = value;

        redraw( data )
    }
    function patternsTableReset(event){
        if (currentPattern != null)
            return null;

        $( "#main table tr" ).removeClass('error');
        markerLink.style("display", function (o){ return 'block' });
    }
    function patternsTableClick(e){
        if (currentPattern == null) {
            $('.ui.accordion').accordion('toggle', 2);
            currentPattern = patternsTableShowPattern(e);

            var line = '<div class="ui sticky inverted twitter_card  card"><div class="">' +
                '<div class="content" style="color: #fff;">' +
                '<div class="header">Pattern</div>' +
                '</div><div class="content"><h4 class="ui sub header">Attributes</h4><div class="ui small feed"><div class="event"><div class="content" >' +




                jQuery.each(patterns[currentPattern].attr, function(el) {
                line += '<div class="summary">'+  patterns[currentPattern].attrs[el] +'</div>';

            });

            line += '</div></div></div></div><div class="content"><h4 class="ui sub header">Relations</h4><div class="ui small feed">'

            jQuery.each(patterns[currentPattern].links, function(element) {

                line +=  '<div class="event"><div class="content"><div class="summary">'+  element
                jQuery.each(patterns[currentPattern].links[element], function(attr) {
                    line += " --> " + patterns[currentPattern].links[element][attr];
                })
                line += '</div></div></div>'
            });

            $('#patternContainerContent').html(line + '</div></div></div>');



        } else {
            currentPattern = null;
            $('#patternContainerContent').empty();
            patternsTableReset(e);
        }
    }
    function patternsTableShowPattern(event){
        var index = $(event).data('index');
        var pattern = patterns[index]['links'];

        if (currentPattern != null && index != currentPattern)
            return null;



        markerLink.style("display", function (o){
            if(  (pattern[o.name] != undefined && jQuery.inArray(data.nodes[o.target].name, pattern[o.name]) > -1)){
                return 'block'
            } else {
                return 'none'
            }
        });



        return index;
    }
    function updateDisplay(){
        var current = map.getZoom();
        map.setZoom( 12 );
        map.setZoom( current );
    }
    function redraw() {
        markerLink = layer.selectAll(".links")
            .data(data.links)
            .each(pathTransform) // update existing markers
            .enter().append("svg:svg")
            .attr("class", "links")
            .each(pathTransform);


        function pathTransform(d) {
            var t, b, l, r, w, h, currentSvg;
            var d1 = new Object();
            var d2 = new Object();
            $(this).empty();




            d1.x = node_coord[d.source + "," + 0]
            d1.y = node_coord[d.source + "," + 1]
            d2.x = node_coord[d.target + "," + 0]
            d2.y = node_coord[d.target + "," + 1]

            if (d1.y < d2.y) {
                t = d1.y;
                b = d2.y;
            } else {
                t = d2.y;
                b = d1.y;
            }
            if (d1.x < d2.x) {
                l = d1.x;
                r = d2.x;
            } else {
                l = d2.x;
                r = d1.x;
            }
            currentSvg = d3.select(this)
                .style("z-index", "1")
                .style("left", (l + 2 * radius) + "px")
                .style("top", (t + 2 * radius) + "px")
                .style("width", (r - l + 2 * radius) + "px")
                .style("height", (b - t + 2 * radius) + "px");


            var x1 = 0,
                y1 = 0,
                x2 = 0,
                y2 = 0;
            if ((d1.y < d2.y) && (d1.x < d2.x)) {
                x2 = r - l;
                y2 = b - t;
            } else if ((d1.x > d2.x) && (d1.y > d2.y)) {
                x2 = r - l;
                y2 = b - t;
            } else if ((d1.y < d2.y) && (d1.x > d2.x)) {
                x1 = r - l;
                y2 = b - t;
            } else if ((d1.x < d2.x) && (d1.y > d2.y)) {
                x1 = r - l;
                y2 = b - t;
            }



            if (res[d.name] == undefined)
                res[d.name] = [{ source: d1, target: d2}];
            else
                res[d.name].push( { source: d1, target: d2 } );


            currentSvg.append("svg:line")
                .style("stroke-width", lineSize)
                .style("stroke", getPatternColor(d))
                .attr("x1", x1)
                .attr("y1", y1)
                .attr("x2", x2)
                .attr("y2", y2);
            return currentSvg;
        }

        function transform(d, i) {


            var c = d.name.split('/');


            d = new google.maps.LatLng(c[0], c[1]);
            d = projection.fromLatLngToDivPixel(d);

            node_coord[i + "," + 0] = d.x;
            node_coord[i + "," + 1] = d.y;

            return d3.select(this)
                .style("left", (d.x) + "px")
                .style("top", (d.y) + "px");
        }
    }
    function patternsTableClickAjax(e, xpName){

        if (currentPattern == null) {

            var index = $(e).data('index');
            $('.pattern_information').empty( $('#tableLoader').addClass('loader') );



            $( "#main table tr" ).removeClass('error');



            $.get( "/patterns/"+ xpName +"/" + index, function( data ) {
                if( overlay != undefined ){
                    map = new google.maps.Map(d3.select("#graph_container").node(), mapOptions);
                }




                $('#tableLoader').removeClass('loader');
                draw_distribution( data.links )

                $('.pattern_information').css('display', 'block');
                $('.pattern_information').text( JSON.stringify(patterns[index]['attrs']) );

                $(e).addClass('error');
                draw(map, data);


            });
        }

    }
    function draw_distribution(links){
        var results = {};

        jQuery.each(links, function(el){

            if(results['q'] != undefined )
                results['q'].push(parseFloat(links[el]['q'].replace(',', '.')))
            else
                results['q'] = [parseFloat(links[el]['q'].replace(',', '.'))]

            if( results['support_all'] != undefined )
                results['support_all'].push(parseFloat(links[el]['supp_all'].replace(',', '.')))
            else
                results['support_all'] = [parseFloat(links[el]['supp_all'].replace(',', '.'))]

            if( results['support_ctx'] != undefined )
                results['support_ctx'].push(parseFloat(links[el]['supp_ctx'].replace(',', '.')))
            else
                results['support_ctx'] = [parseFloat(links[el]['supp_ctx'].replace(',', '.'))]
        })



        max_q = results['q'].max()


        var i = 0;
        var arr_x = [];
        var arr_y = [];
        var tmp = [];
        while( i < results[ 'support_ctx'].length ){
            var p = results[ 'support_ctx'][i]
            var pp = results[ 'support_all'][i]
            tmp.push( p * 1000000 + pp )

            i++;
        }

        tmp.sort(function(a, b) {return b - a})

        jQuery.each(tmp, function(el){

            var cal = tmp[el] % 1000000;
            arr_y.push(cal )
            arr_x.push( (tmp[el] - cal) / 1000000)
        })



        var selector = $('#patternContainerContent');
        var q = results['q'].sort(function(a, b) {return b - a})


        selector.empty();
        selector.append( '<div id="DistributionContainerQ" style="width: 450px; height: 200px;"></div>' );


        Highcharts.setOptions({
            colors: ['#DB2828', '#239BB6']
        });

        $('#DistributionContainerQ').highcharts({
            chart: {
                type: 'column'
            },
            title: {
                text: 'Distribution of Q'
            },

            credits: {
                enabled: false
            },
            xAxis: {
                categories:[]
            },
            series: [{
                name: 'Q',
                data: results['q']
            }]
        });


        results['support_ctx'].sort(function(a, b) {return b - a})
        results['support_all'].sort(function(a, b) {return b - a})

        selector.append( '<br><div id="DistributionContainerSupport_ctx" style="width: 450px; height: 400px;"></div>' );


        $('#DistributionContainerSupport_ctx').highcharts({
            chart: {
                type: 'column'
            },
            tooltip: {
                pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y}</b> ({point.percentage:.0f}%)<br/>',
                shared: true
            },
            xAxis: {
                categories: []//sortable_all.map(function(obj){return obj[0]})
            },
            title: {
                text: 'Distribution of Support Context'
            },

            credits: {
                enabled: false
            },
            plotOptions: {
                spline: {
                    lineWidth: 4,
                    states: {
                        hover: {
                            lineWidth: 5
                        }
                    },
                    marker: {
                        enabled: false
                    }
                }
            },
            series: [{
                type: 'column',
                name: 'Support all',
                data: arr_y
            }, {
                type: 'column',
                name: 'support context',
                data: arr_x,
            }]
        });


       $('#patternAccordion.ui.accordion').accordion('open', 0);
    }
    function getRandomColor(d) {
        var letters = '0123456789ABCDEF'.split('');
        var color = '#';
        for (var i = 0; i < 6; i++ ) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
    function getPatternColor(d, data){

        color = 'orange'

        jQuery.each(patterns, function(el) {
            var obj = patterns[el]['links']

              if( obj[data.nodes[d.source].name ] != undefined && jQuery.inArray( data.nodes[d.target].name, obj[ data.nodes[d.source].name ] ) > -1 ){
                  color  = patterns[el].color
              }
            }
        )

        return color;



    }
    function displayMapInfo(data){
        var shapesCoordinates = [];
        var markersCoordinates = [];
        jQuery.each(data.nodes, function(el){


            var md5_trend = CryptoJS.MD5(data.nodes[el].iris_trend.toString()).toString();
            if( $.inArray( md5_trend, markersCoordinates ) == -1 ){
                markersCoordinates.push(md5_trend);




                var contentString =   '<div class="ui circular labels"><div class="ui red label"> <div class = "value"> <i class = "fa fa-graduation-cap"></i> '+ data.nodes[el].iris_trend[2] +' </div> <div class = "label" > Schools </div> </div><div class = "ui orange label"> <div class = "value"> <i class = "fa fa-train"></i> '+ data.nodes[el].iris_trend[6] +' </div> <div class = "label" > Train Stations </div> </div> <div class = "ui yellow label" > <div class="value" > <i class="building icon"></i> '+ data.nodes[el].iris_trend[4] +' </div> <div class = "label" > < 50 employees </div></div> <div class="ui olive label"><div class="value" > <i class="building outline icon"></i> '+ data.nodes[el].iris_trend[5] +' </div><div class="label" > > 50 employees </div></div> <div class="ui green label" > <div class="value" > <i class="fa fa-bed"></i> '+ data.nodes[el].iris_trend[7] +' </div ><div class="label" > hotels </div> </div><div class="ui teal label" ><div class="value" ><i class="travel icon"></i> '+ data.nodes[el].iris_trend[8] +' </div > <div class="label"> Tourism </div></div><div class="ui blue label"> <div class="value" > <i class="users icon"></i> '+ data.nodes[el].iris_trend[9] +'</div><div class="label" > population</div> </div > </div >'





                var marker = new google.maps.Marker({
                    position: {lat: parseFloat( data.nodes[el].iris_trend[0].replace(',', '.') ), lng: parseFloat( data.nodes[el].iris_trend[1].replace(',', '.') )},
                    map: map,
                    icon:  {
                        url: '/assets/marker.svg',
                        size: new google.maps.Size(120, 120),
                        origin: new google.maps.Point(0, 0),
                        anchor: new google.maps.Point(17, 34),
                        scaledSize: new google.maps.Size(80, 70)
                    }
                });


                marker.addListener('mouseover', function() {
                    $('#infoIris').show();
                    $('#infoIrisContent').empty();
                    $('#infoIrisContent').html(contentString);
                });

            }


            var triangleCoords = data.nodes[el].iris.geo;
            var md5 = CryptoJS.MD5(JSON.stringify(triangleCoords)).toString();


            if( shapesCoordinates.indexOf(md5) > -1  ){
                return true
            }

            shapesCoordinates.push(md5);


            var bermudaTriangle = new google.maps.Polygon({
                paths: triangleCoords,
                strokeColor: currentShapeColor,
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillColor: currentShapeColor,
                fillOpacity: currentShapeOpacity
            });


            iris_shapes.push( bermudaTriangle )

            bermudaTriangle.setMap(map);
        })

    }
    function draw(map, data){

        var body = document.body,
            html = document.documentElement;
        var xpName = $('#xp-name').attr("data-name");

        var height = Math.max( body.scrollHeight, body.offsetHeight,
            html.clientHeight, html.scrollHeight, html.offsetHeight );

        var w = document.getElementById("graph_container").offsetWidth;
        var h = height - 20;
        var arrowStartPosition = 5
        var arrowSpacing = 60;
        var verticalStrokeColor = "black";
        var arrowYStartPosition = 5;
        var arrowYEndStartPosition = 150;
        var margin = 150;


        overlay = new google.maps.OverlayView();

        overlay.onAdd = function() {
            // Add the container when the overlay is added to the map.
            layer = d3.select(this.getPanes().overlayLayer)
                .append("div")
                .attr("height", h + "px")
                .attr("width", w + "px")
                .attr("class", "stations")

            overlay.draw = function () {
                radius = 5;
                projection = this.getProjection();
                padding = 10;
                node_coord = {}

                marker = layer.selectAll("svg")
                    .data(data.nodes)
                    .each(transform) // update existing markers
                    .enter().append("svg:svg")
                    .each(transform)
                    .attr("class", 'marker_label');

                marker.append("svg:circle")
                    .attr("r", radius)
                    .attr("cx", padding)
                    .attr("cy", padding);

                // Add a label.
                var labelsName = [];
                marker.append("svg:text")
                    .style("width", '20px')
                    .style("font-size","14px")
                    .attr("x", 15 + 8)
                    .attr("y", 10)
                    .attr("dy", ".10px")
                    .text(function (d) {
                        if ( labelsName.indexOf(d.name) == -1 ){
                            labelsName.push( d.name );
                            return d.name.split('/')[2].split('@')[0];
                        }
                    });


                marker = marker.append("marker")
                    .attr({
                        "id": "arrow",
                        "viewBox": "0 -5 10 10",
                        "refX": 5,
                        "refY": 0,
                        "markerWidth": 3,
                        "markerHeight": 3,
                        "orient": "auto"
                    })
                    .style('fill', currentArrowColor)
                    .style('stroke', currentArrowColor)
                    .append("path")
                    .attr("d", "M0,-5L10,0L0,5")
                    .attr("class", "arrowHead");




                markerLink = layer.selectAll(".links")

                    .data(data.links)
                    .each(pathTransform) // update existing markers
                    .enter().append("svg:svg")
                    .attr("class", "links")
                    .each(pathTransform)



                function pathTransform(d) {
                    var t, b, l, r, w, h, currentSvg;
                    var d1 = new Object();
                    var d2 = new Object();
                    $(this).empty();


                    d1.x = node_coord[d.source + "," + 0]
                    d1.y = node_coord[d.source + "," + 1]
                    d2.x = node_coord[d.target + "," + 0]
                    d2.y = node_coord[d.target + "," + 1]

                    if (d1.y < d2.y) {
                        t = d1.y;
                        b = d2.y;
                    } else {
                        t = d2.y;
                        b = d1.y;
                    }
                    if (d1.x < d2.x) {
                        l = d1.x;
                        r = d2.x;
                    } else {
                        l = d2.x;
                        r = d1.x;
                    }


                    currentSvg = d3.select(this)
                        .style("stroke-width", 10 )
                        .style("left", (l + 2 * radius)  + "px")
                        .style("top", (t + 2 * radius) + "px")
                        .style("width", (r - l + 2 * radius) + "px")
                        .style("height", (b - t + 2 * radius) + "px")

                    var x1 = 0,
                        y1 = 0,
                        x2 = 0,
                        y2 = 0;
                    if ((d1.y < d2.y) && (d1.x < d2.x)) {
                        x2 = r - l;
                        y2 = b - t;
                    } else if ((d1.x > d2.x) && (d1.y > d2.y)) {
                        x2 = r - l;
                        y2 = b - t;
                    } else if ((d1.y < d2.y) && (d1.x > d2.x)) {
                        x1 = r - l;
                        y2 = b - t;
                    } else if ((d1.x < d2.x) && (d1.y > d2.y)) {
                        x1 = r - l;
                        y2 = b - t;
                    }

                    if (res[d.name] == undefined)
                        res[d.name] = [{source: d1, target: d2}];
                    else
                        res[d.name].push({source: d1, target: d2});

                    var currentLineSize = ((parseFloat(d.q.replace(',', '.')) * 100) / max_q) / 15 * edgePower;

                   if( currentLineSize == 0)
                    currentLineSize = 1 * edgePower;

                    line = currentSvg.append("svg:line")
                        .style("stroke-width", currentLineSize )
                        .style("stroke", currentLineColor)
                        .attr("x1", x1)
                        .attr("y1", y1)
                        .attr("x2", x2)
                        .attr("y2", y2)
                        .attr({
                            "class":"arrow",
                            "marker-end":"url(#arrow)",
                        })

                    return currentSvg;
                }

                function transform(d, i) {
                    var c = d.name.split('/ ');
                    d = new google.maps.LatLng(c[0], c[1]);
                    d = projection.fromLatLngToDivPixel(d);

                    node_coord[i + "," + 0] = d.x;
                    node_coord[i + "," + 1] = d.y;

                    return d3.select(this)
                        .style("left", (d.x) + "px")
                        .style("top", (d.y) + "px")
                }

                layer.append("div")
                    .attr("class", "stations.line");


            };
        }

        // Bind our overlay to the map…
        overlay.setMap(map);

        displayMapInfo(data);


    }
    function start(){
        xpName = $('#xp-name').attr("data-name");
        map = new google.maps.Map(d3.select("#graph_container").node(), mapOptions);


        setDefaultAppListeners();

        if ((data == undefined || data.size == 0) && patterns != undefined){
            console.log("Could not load traces because data empty. Loading Ajax.");

            $( "#main table tr:not(:first)" ).on({
                click:function(){patternsTableClickAjax(this, xpName)}
            });

            return true;
        }
        setEventD3Listeners();



        draw(map, data)
    }


    $(document).on('page:load', function() {
        start();
    });

    $(document).ready(function() {
        start();
    });


})(window);
