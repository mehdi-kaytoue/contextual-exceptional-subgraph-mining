// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or any plugin's vendor/assets/javascripts directory can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// compiled file.
//
// Read Sprockets README (https://github.com/rails/sprockets#sprockets-directives) for details
// about supported directives.
//

//= require vendor/three.min
//= require vendor/projector
//= require vendor/canvas-renderer
//= require vendor/3d-lines-animation
//= require jquery
//= require vendor/jquery.scrollpanel-0.5.0
//= require jquery_ujs
//= require turbolinks
//= require nprogress
//= require nprogress-turbolinks

//= require vendor/semantic.min








(function(window) {

    $.fn.exists = function () {
        return this.length !== 0;
    }

    function hashcode(s){
        return s.split("").reduce(function(a,b){a=((a<<5)-a)+b.charCodeAt(0);return a&a},0);
    }

    function clone(obj) {
        // Handle the 3 simple types, and null or undefined
        if (null == obj || "object" != typeof obj) return obj;

        // Handle Date
        if (obj instanceof Date) {
            var copy = new Date();
            copy.setTime(obj.getTime());
            return copy;
        }

        // Handle Array
        if (obj instanceof Array) {
            var copy = [];
            for (var i = 0, len = obj.length; i < len; i++) {
                copy[i] = clone(obj[i]);
            }
            return copy;
        }

        // Handle Object
        if (obj instanceof Object) {
            var copy = {};
            for (var attr in obj) {
                if (obj.hasOwnProperty(attr)) copy[attr] = clone(obj[attr]);
            }
            return copy;
        }

        throw new Error("Unable to copy obj! Its type isn't supported.");
    }




    var currentPattern = null;
    var graphPath =  $('#data_urls').data('graph');
    var patternPath = $('#data_urls').data('patterns');
    var linkedByIndex = {};
    var linkedByName = {};
    var targets = {};

    var currentCharge = 400;
    var currentFriction = 0.3;
    var currentSize = 30;
    var currentDistance = 4;

    var body;
    var html;
    var height;
    var w;
    var h;
    var force;
    var link;
    var node;
    var vis;



    function setListeners(){

        //scroll bar Pattern's table container
        $('#patternTableContainer').scrollpanel();


        $('.ui.accordion')
            .accordion()
        ;

        $('.ui.checkbox')
            .checkbox()
        ;

        $('.menu_popup')
            .popup()
        ;


        $( '#xp_display').on('click', function() {
            $('.ui.labeled.icon.sidebar')
                .sidebar('setting', 'transition', 'scale down')
                .sidebar('toggle')
            ;
        });


        //if(typeof(data) == 'undefined') {
        //    // Automatically shows on init if cookie isnt set
        //    $('.cookie.nag')
        //        .nag({
        //            key: 'accepts-cookies',
        //            value: true
        //        })
        //    ;
        //};

        $( "#main table tr:not(:first)" ).on({
            mouseover:function(){patternsTableShowPattern(this, patterns, {force:force, targets: targets, link:link, linkedByName:linkedByName })},
            mouseleave:function(){patternsTableReset(this)},
            click:function(){patternsTableClick(this, patterns, {force:force, targets: targets, link:link, linkedByName:linkedByName })}
        });

        $('#sliderPatternLayout').on({
            change:function(){ setDynamicLayoutForce(this); }
        });

        $('#sliderPatternFriction').on({
            change:function(){ setDynamicLayoutFriction(this); }
        });


        $('#sliderPatternSize').on({
            change:function(){ setDynamicLayoutSize(this); }
        });

        $('#sliderPatternDistance').on({
            change:function(){ setDynamicLayoutDistance(this); }
        });

        $('#xp_reset').on({
            click:function(){ currentPattern = null; $('.ui.accordion').accordion('toggle', 1);  $('#patternContainerContent').empty(); patternsTableReset(this) }
        });


        $('.menu_popup').on("contextmenu", function(e){

            console.log();

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

            console.log('oplop')
            return false;
        });




    }



    function setDynamicLayoutDistance(el){
        var value = $(el).val();
        document.getElementById("edge_distance").innerHTML= value;
        currentDistance = value;
        redraw( data )
    }

    function setDynamicLayoutSize(el){
        var value = $(el).val();
        document.getElementById("node_size").innerHTML= value;
        currentSize = value;
        redraw( data );
    }


    function setDynamicLayoutFriction(el){
        var value = $(el).val();
        document.getElementById("friction").innerHTML= value;
        currentFriction = value;

        redraw( data )
    }
    function setDynamicLayoutForce(el){
        var value = $(el).val();
        document.getElementById("range").innerHTML= value;
        currentCharge = value;

        redraw( data )
    }

    function patternsTableShowPattern(event, patterns, attrs){

        var selector =  $(event);
        var index = selector.data('index');
        var pattern = patterns[index]['links'];

        if (currentPattern != null && index != currentPattern)
            return null;


        selector.addClass('error');

        $('.nodetext').css('display', 'none');
        $('.node .type2').each(function(el){
            $(this).css( 'display', 'none');
            $(this).css('color', '#e3e3e3');
        });

        jQuery.each(attrs.targets, function(element) {

            if(pattern[element] != undefined  ){

                var hashKey = hashcode( element )

                $('.node .' + hashKey).css( 'display', 'block');
                $('.node .' + hashKey).css( 'z-index', 1000000 );

                $('.title_' + hashKey).css( 'display', 'block' );
                $('.title_' + hashKey).css( 'font-size', '15px' );

                $('.' + hashKey).css( 'fill', '#E5B96F' );
            }
        });


        attrs.link.style("display", function (o){
            if(  (pattern[o.source.name] != undefined && jQuery.inArray(o.target.name, pattern[o.source.name]) > -1)  || (pattern[o.target.name] != undefined && jQuery.inArray(o.source.name, pattern[o.target.name]) > -1) )
                if(attrs.linkedByName[o.source.name +","+ o.target.name] == 1 || attrs.linkedByName[o.target.name +","+ o.source.name] == 1)
                    return 'block';
            return 'none';
        });

        return index;
    }
    function patternsTableReset(event, current){
        var selector =  $(event);


        if (currentPattern != null)
            return null;


        $( "#main table tr" ).removeClass('error');


        $('.nodetext').css('display', 'block');
        $('.node .type2').each(function(el){
            $(this).css( 'display', 'block');

            $(this).css('fill', '#F14533');
        });

        $('.link10').each(function(el){
            $(this).css( 'display', 'block');
        });

        $('.title_').css( 'fill', 'white' );
        $('.title_').css( 'font-size', '10px' );
    }

    function patternsTableClick(e, patterns, attrs){
        if (currentPattern == null) {
            //patternTableShowPattern(e, patterns, attrs);

            $('.ui.accordion').accordion('toggle', 2);
            currentPattern = patternsTableShowPattern(e, patterns, attrs);


            var line = '<div class="ui sticky inverted twitter_card  card"><div class="">' +
                        '<div class="content" style="color: #fff;">' +
                        '<div class="header">Pattern</div>' +
                        '</div><div class="content"><h4 class="ui sub header">Attributes</h4><div class="ui small feed"><div class="event"><div class="content" >' +


            jQuery.each(patterns[currentPattern].attrs, function(el) {
                line += '<div class="summary">'+  patterns[currentPattern].attrs[el] +'</div>';

            })

            line += '</div></div></div></div><div class="content"><h4 class="ui sub header">Relations</h4><div class="ui small feed">'

            jQuery.each(patterns[currentPattern].links, function(element) {

                line +=  '<div class="event"><div class="content"><div class="summary">'+  element
                jQuery.each(patterns[currentPattern].links[element], function(attr) {
                    line += " --> " + patterns[currentPattern].links[element][attr];
                })
                line += '</div></div></div>'
            });

            $('#patternContainerContent').html(line + '</div></div></div>');
        }
        else {
            currentPattern = null;
            $('#patternContainerContent').empty();
            patternsTableReset(e);
        }
    }

    function start(){
        body = document.body,
            html = document.documentElement;

        height = Math.max( body.scrollHeight, body.offsetHeight,
            html.clientHeight, html.scrollHeight, html.offsetHeight );

        w = document.getElementById("graph_container").offsetWidth;
        h = height - 20



        setListeners();

        if(typeof(data) != 'undefined'){


            draw( data );
            data.links.forEach(function(d) {
                linkedByIndex[d.source.index + "," + d.target.index] = 1;
                linkedByName[d.source.name + "," + d.target.name] = 1;



                if (targets[d.source.name] != undefined){
                    targets[d.source.name].push( d.target.name);
                }else {
                    targets[d.source.name] = [ d.target.name];
                }


                if (targets[d.target.name] != undefined){
                    targets[d.target.name].push( d.source.name);
                }else {
                    targets[d.target.name] = [ d.source.name];
                }
            });
        }

    }

    function isConnected(a, b) {
        return linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index;
    }

    function fade(opacity, color) {
        return function(d) {
            node.style("stroke-opacity", function(o) {
                var thisOpacity = isConnected(d, o) ? 1 : opacity;
                this.setAttribute('fill-opacity', thisOpacity);
                return thisOpacity;
            });

            link.style("stroke-opacity", function(o) {
                    return o.source === d || o.target === d ? 1 : opacity;
                })

                .style("stroke", function(o) {
                    return o.source === d || o.target === d ? color : "#000";
                });
        };

    }

    function redraw() {

        node.selectAll(".node").attr("r", function(d) {
            return currentSize;
        })

        force.linkDistance(function(d) {
                return (d.distance * currentDistance);
            })
            .friction(currentFriction)
            .charge(currentCharge * -1).start();
    }

    function redrawWindow() {
        var trans = d3.event.translate;
        var scale = d3.event.scale;
        vis.attr("transform",
            "translate(" + trans + ")"
            + " scale(" + scale + ")");
    };


    function draw(json ){
        d3.select("#graph_container").selectAll("*").remove();

        force = self.force = d3.layout.force()
            .nodes(json.nodes)
            .links(json.links)
            .linkDistance(function(d) {
                return (d.distance * currentDistance);
            })
            .friction(currentFriction)
            .charge(currentCharge * -1)
            .size([w, h])
            .start();

        vis = d3.select("#graph_container").append("svg:svg")
            .attr("width", w)
            .attr("height", h)
            .append("svg:svg")
            .call(d3.behavior.zoom().on("zoom", redrawWindow))
            .append('svg:g');


        link = vis.selectAll("line.link")
            .data(json.links)
            .enter().append("svg:line")
            .attr("class", function(d) {

                if (d.name == null)
                    d.name = 'null';
                return "link" + d.value + " " + hashcode( d.name );
            })
            .attr("x1", function(d) {
                return d.source.x;
            })
            .attr("y1", function(d) {
                return d.source.y;
            })
            .attr("x2", function(d) {
                return d.target.x;
            })
            .attr("y2", function(d) {
                return d.target.y;
            })
            .attr("marker-end", function(d) {
                if (d.value == 1) {
                    return "url(#arrowhead)"
                } else {
                    return " "
                };
            });


        node = vis.selectAll("g.node")
            .data(json.nodes)
            .enter().append("svg:g")
            .attr("class", "node")
            .call(force.drag);


        node.append("circle")
            .attr("class", function(d) {

                if (d.name == null)
                    d.name = 'null';

                return "node type" + d.type + " " + hashcode( d.name );
            })
            .attr("r", function(d) {
                if (d.entity == "description") {
                    return 6
                } else {
                    return currentSize
                }
            })


        node.append("text")
            .attr("class", function(d) {

                if (d.name == null)
                    d.name = 'null';
                return "nodetext title_" + hashcode( d.name );
            })
            .attr("dx", 0)
            .attr("dy", ".35em")
            .style("font-size", "13px")
            .attr("text-anchor", "middle")
            .style("fill", "black")
            .style("font-weight", "bold")
            .text(function(d) {
                if (d.entity != "description") {
                    return d.name
                }
            });

        node.on("mouseover", fade(.3, "blue"))
            .on("mouseout", fade(1));

        force.on("tick", function() {
            link.attr("x1", function(d) {
                    return d.source.x;
                })
                .attr("y1", function(d) {
                    return d.source.y;
                })
                .attr("x2", function(d) {
                    return d.target.x;
                })
                .attr("y2", function(d) {
                    return d.target.y;
                });

            node.attr("transform", function(d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
        });
    }



    $(document).on('page:load', function() {
        start();
    });

    $(document).ready(function() {
        start();
    });




})(window);
