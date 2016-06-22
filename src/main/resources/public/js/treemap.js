'use strict';

function Treemap(d3) {
    var legendHeight = 25,
        oneUpHeight = 25,
        informationHeight = 250,
        width = 1120,
        height = 850,
        treemapHeight = height - legendHeight - oneUpHeight - informationHeight,
        format = d3.format("0,000"),
        transitioning, depth, node;

    var x = d3.scale.linear()
        .domain([0, width])
        .range([0, width]);

    var y = d3.scale.linear()
        .domain([0, treemapHeight])
        .range([0, treemapHeight]);

    var treemap, svg, defs, legend, oneUp, information;

    var transitioningCallbacks = [];

    var setUpTreemapLayout = function () {
        treemap = d3.layout.treemap()
            .round(false)
            .children(function (d, depth) {
                return depth ? null : d._children;
            })
            .sort(function (a, b) {
                return a.size - b.size;
            })
            .value(function (d) {
                return d.size;
            })
            .ratio(treemapHeight / width * 0.5 * (1 + Math.sqrt(5)));
    };

    var setUpSVG = function () {
        var container = d3.select("#treemap").append("svg")
            .attr("width", width)
            .attr("height", height)
            .style("font", "14px sans-serif");

        defs = container.append("defs");
        svg = container.append("g").style("shape-rendering", "crispEdges");

        legend = svg.append("g")
            .attr("class", "legend")
            .style("font-weight", "bold");

        legend.append("rect")
            .attr("class", "back")
            .attr("width", width)
            .attr("height", legendHeight)
            .style({
                "stroke": "#fff",
                "fill": "#fff"
            });

        oneUp = svg.append("g")
            .attr("class", "one-up")
            .style("font-weight", "bold")
            .attr("transform", "translate(0," + legendHeight + ")");

        oneUp.append("rect")
            .attr("width", width)
            .attr("height", oneUpHeight)
            .style({
                "stroke": "#fff",
                "fill": "#fff"
            });

        oneUp.append("text")
            .attr("x", 6)
            .attr("y", 6)
            .attr("dy", ".75em");

        oneUp.append("text")
            .attr("x", width - 6)
            .attr("y", 6)
            .attr("dy", ".75em")
            .attr("text-anchor", "end")
            .text("Click to move one level up")
            .style("font-style", "italic");

        information = svg.append("g")
            .attr("class", "information")
            .attr("transform", "translate(0," + (legendHeight + oneUpHeight + treemapHeight) + ")");

        information.append("rect")
            .attr("class", "back")
            .attr("width", width)
            .attr("height", informationHeight)
            .style({
                "stroke": "#fff",
                "fill": "#fff"
            });
    };

    setUpTreemapLayout();
    setUpSVG();

    // -------------------------------------------------------------------------------------------------------- //
    
    this.loadFromUrl = function (url, callback) {
        d3.json(url, function (error, treemapInfo) {
            if (error) throw error;

            setUpLegend(treemapInfo.legend);
            setUpTreemap(treemapInfo.treemap);

            callback(treemapInfo);
        });
    };

    this.getTreemapInfo = function (url, callback) {
        d3.json(url, function (error, treemapInfo) {
            if (error) throw error;            
            callback(treemapInfo);
        });
    };

    this.onTransition = function (callback) {
        transitioningCallbacks.push(callback);
    };

    this.downloadSVG = function (name) {
        downloadData(name, getSvgData());
    };

    this.downloadPNG = function (name) {
        var canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;

        var context = canvas.getContext("2d");
        var image = new Image();

        image.onload = function () {
            context.drawImage(image, 0, 0);
            downloadData(name, canvas.toDataURL('image/png'));
        };
        image.src = getSvgData();
    };

    // -------------------------------------------------------------------------------------------------------- //

    var setUpLegend = function (data) {
        var valueWidth = width / data.length;
        legend.selectAll("*:not(.back)").remove();

        legend.selectAll()
            .data(data)
            .enter()
            .append("rect")
            .attr("x", function (d, i) {
                return (valueWidth * i) + 30;
            })
            .attr("y", ((legendHeight - 10) / 2) - 5)
            .attr("width", 10)
            .attr("height", 10)
            .style("fill", function (d) {
                return d.color;
            });

        legend.selectAll()
            .data(data)
            .enter()
            .append("text")
            .attr("x", function (d, i) {
                return (valueWidth * i) + 50;
            })
            .attr("y", legendHeight / 2)
            .text(function (d) {
                return d.label;
            });
    };

    var setUpTreemap = function (root) {
        initialize(root);
        accumulate(root);
        layout(root);

        var newDepth = false; // TODO: findSameDepth(root);
        transition((newDepth !== false) ? newDepth : root);
    };

    var initialize = function (root) {
        root.x = root.y = 0;
        root.dx = width;
        root.dy = treemapHeight;
        root.depth = 0;
    };

    // Aggregate the values for internal nodes. This is normally done by the
    // treemap layout, but not here because of our custom implementation.
    // We also take a snapshot of the original children (_children) to avoid
    // the children being overwritten when when layout is computed.
    var accumulate = function (d) {
        if (d._children = d.children) {
            d.size = d.children.reduce(function (p, v) {
                return p + accumulate(v);
            }, 0);
        }
        return d.size;
    };

    // Compute the treemap layout recursively such that each group of siblings
    // uses the same size (1×1) rather than the dimensions of the parent cell.
    // This optimizes the layout for the current zoom state. Note that a wrapper
    // object is created for the parent node for each group of siblings so that
    // the parent’s dimensions are not discarded as we recurse. Since each group
    // of sibling was laid out in 1×1, we must rescale to fit using absolute
    // coordinates. This lets us use a viewport to zoom.
    var layout = function (d) {
        if (d._children) {
            treemap.nodes({_children: d._children});
            d._children.forEach(function (c) {
                c.x = d.x + c.x * d.dx;
                c.y = d.y + c.y * d.dy;
                c.dx *= d.dx;
                c.dy *= d.dy;
                c.parent = d;
                layout(c);
            });
        }
    };

    var findSameDepth = function (d) {
        if (d && node) {
            if ((d.column === node.column) && (d.name === node.name))
                return d;
            else if (d._children) {
                for (var i = 0; i < d._children.length; i++) {
                    var c = d._children[i];
                    var result = findSameDepth(c);

                    if (result !== false)
                        return result;
                }
            }
        }
        return false;
    };

    var transition = function (d) {
        if (transitioning || !d) return;
        if (depth) transitionChange(true);

        var isNewDepth = (!node || (d.column !== node.column) || (d.name !== node.name));
        node = d;

        if (depth && isNewDepth) {
            var t1 = depth.transition().duration(750);
            var newDepth = display(d, false);
            var t2 = newDepth.transition().duration(750);

            x.domain([d.x, d.x + d.dx]);
            y.domain([d.y, d.y + d.dy]);

            svg.style("shape-rendering", null)
                .selectAll(".depth")
                .sort(function (a, b) {
                    return a.depth - b.depth;
                });

            newDepth.selectAll("text").call(hide);

            t1.selectAll("text").call(text).call(hide);
            t2.selectAll("text").call(text).call(show);

            t1.selectAll("rect").call(color).call(rect);
            t2.selectAll("rect").call(color).call(rect);

            t1.remove().each("end", function () {
                svg.style("shape-rendering", "crispEdges");
                transitionChange(false);
            });
        }
        else {
            display(d, !isNewDepth);
        }
    };

    var display = function (d, withTransitions) {
        hideInformation();
        setUpOneUp(d);

        var boxes = getBoxes(d, !withTransitions);
        var newBoxes = getOnlyNewBoxes(boxes);
        var childBoxes = getChildBoxes(d, boxes);

        setupBoxes(boxes, newBoxes, childBoxes);
        applyStyling();
        animateBoxes(boxes, newBoxes, childBoxes, withTransitions);

        return boxes;
    };

    var setUpOneUp = function (d) {
        oneUp.datum(d.parent)
            .on("click", transition)
            .select("text").text(name(d));
    };

    var getBoxes = function (d, newDepth) {
        if (newDepth) {
            depth = svg.insert("g", ".legend")
                .datum(d)
                .attr("class", "depth")
                .attr("transform", "translate(0," + (legendHeight + oneUpHeight) + ")");
        }

        return depth.selectAll("g").data(d._children, key);
    };

    var getOnlyNewBoxes = function (boxes) {
        return boxes.enter().append("g").call(hide);
    };

    var getChildBoxes = function (d, boxes) {
        return boxes.selectAll(".child").data(function (d) {
            return d._children || [d];
        }, key);
    };

    var setupBoxes = function (boxes, newBoxes, childBoxes) {
        childBoxes.enter().insert("rect", ".parent")
            .call(color)
            .attr("class", "child");

        newBoxes.insert("rect", "text")
            .call(color)
            .attr("class", "parent")
            .on("mouseover", showInformation);

        newBoxes.append("text")
            .attr("dy", ".75em")
            .text(function (d) {
                return determineName(d);
            });

        boxes.filter(function (d) {
            return d._children;
        })
            .classed("children", true)
            .on("click", transition);

        boxes.exit().remove();
        childBoxes.exit().remove();
    };

    var applyStyling = function () {
        svg.selectAll('rect').style('stroke', '#fff');
        svg.selectAll('.depth text').style({
            'fill': '#fff',
            'stroke': '#000',
            'stroke-width': '2px',
            'paint-order': 'stroke',
            'font-size': '12px'
        });
        svg.selectAll('rect.parent').style('stroke-width', '2px');
        svg.selectAll('.children rect.parent').style('fill', 'none');
    };

    var animateBoxes = function (boxes, newBoxes, childBoxes, withTransitions) {
        if (withTransitions) {
            childBoxes.transition().duration(750).call(rect);
            boxes.select('.parent').transition().duration(750).call(rect);
            var resize = boxes.select('text').transition().duration(750).call(text);

            if (resize.size() > 0) {
                var firstCalledOnce = false;
                resize.each("end", function () {
                    if (!firstCalledOnce) {
                        firstCalledOnce = true;

                        if (newBoxes.size() > 0) {
                            var showing = boxes.transition().duration(750).call(show);
                            showing.each("end", function () {
                                transitionChange(false);
                            });
                        }
                        else {
                            transitionChange(false);
                        }
                    }
                });
            }
            else {
                transitionChange(false);
            }
        }
        else {
            childBoxes.call(rect);
            boxes.select('.parent').call(rect);
            boxes.select('text').call(text);
            boxes.call(show);
        }
    };

    var transitionChange = function (flag) {
        if (transitioning !== flag) {
            transitioning = flag;

            transitioningCallbacks.forEach(function (callback) {
                callback(transitioning);
            });
        }
    };

    var showInformation = function (d) {
        hideInformation();

        var branchesLeft = [], cur = d, popoverWidth = (width - 40) / 2;
        while (cur) {
            branchesLeft.unshift({
                name: cur.name,
                suffix: cur.suffix,
                size: cur.size,
                color: cur.color,
                sizeLast: d.size,
                scale: d3.scale.linear()
                    .domain([0, cur.size])
                    .range([0, popoverWidth])
            });
            cur = cur.parent;
        }

        var total = branchesLeft[0].size;
        var branchesRight = branchesLeft.slice(0, -1);
        var totalScale = d3.scale.linear()
            .domain([0, total])
            .range([0, popoverWidth]);

        information.append('text')
            .attr("x", 15)
            .attr("y", 25)
            .text("Frequency and percentage")
            .style("font-weight", "bold");

        information.selectAll()
            .data(branchesLeft)
            .enter().append("rect")
            .attr("width", popoverWidth)
            .attr("height", 5)
            .attr("x", 10)
            .attr("y", function (d, i) {
                return 60 + (i * 35);
            })
            .attr("fill", "lightgrey");

        information.selectAll()
            .data(branchesLeft)
            .enter().append("rect")
            .attr("width", function (d) {
                return totalScale(d.size);
            })
            .attr("height", 5)
            .attr("x", 10)
            .attr("y", function (d, i) {
                return 60 + (i * 35);
            })
            .attr("fill", function (d) {
                var color = determineColor(d);
                return (color) ? color : '#5580B7';
            });

        information.selectAll()
            .data(branchesLeft)
            .enter().append("text")
            .attr("x", 15)
            .attr("y", function (d, i) {
                return 55 + (i * 35);
            })
            .text(function (d) {
                var percentage = Math.round(d.size * 10000 / total) / 100;
                return determineName(d) + " (" + format(d.size) + " / " + percentage + "%)";
            });

        information.append('text')
            .attr("x", popoverWidth + 30)
            .attr("y", 25)
            .text("Relative percentage")
            .style("font-weight", "bold");

        information.selectAll()
            .data(branchesRight)
            .enter().append("rect")
            .attr("width", popoverWidth)
            .attr("height", 5)
            .attr("x", popoverWidth + 30)
            .attr("y", function (d, i) {
                return 60 + (i * 35);
            })
            .attr("fill", "lightgrey");

        information.selectAll()
            .data(branchesRight)
            .enter().append("rect")
            .attr("width", function (d) {
                return d.scale(d.sizeLast);
            })
            .attr("height", 5)
            .attr("x", popoverWidth + 30)
            .attr("y", function (d, i) {
                return 60 + (i * 35);
            })
            .attr("fill", function (d) {
                var color = determineColor(d);
                return (color) ? color : '#5580B7';
            });

        information.selectAll()
            .data(branchesRight)
            .enter().append("text")
            .attr("x", popoverWidth + 30)
            .attr("y", function (d, i) {
                return 55 + (i * 35);
            })
            .text(function (d2) {
                var percentage = Math.round(d2.sizeLast * 10000 / d2.size) / 100;
                return determineName(d) + " / " + determineName(d2) + " (" + percentage + "%)";
            });
    };

    var hideInformation = function () {
        information.selectAll("*:not(.back)").remove();
    };

    var getSvgData = function () {
        var svg = $('svg')
            .attr("version", 1.1)
            .attr("xmlns", "http://www.w3.org/2000/svg")[0].outerHTML;
        return 'data:image/svg+xml;base64,' + btoa(svg);
    };

    var downloadData = function (filename, data) {
        var a = document.createElement('a');
        a.download = filename;
        a.href = data;
        a.click();
    };

    var key = function (d) {
        return d.column + '_' + d.name;
    };

    var text = function (text) {
        text
            .attr("x", function (d) {
                return x(d.x) + 10;
            })
            .attr("y", function (d) {
                return y(d.y) + 10;
            });
    };

    var color = function (rect) {
        rect.attr("fill", function (d) {
            return determineColor(d);
        });
    };

    var rect = function (rect) {
        rect
            .attr("x", function (d) {
                return x(d.x);
            })
            .attr("y", function (d) {
                return y(d.y);
            })
            .attr("width", function (d) {
                return x(d.x + d.dx) - x(d.x);
            })
            .attr("height", function (d) {
                return y(d.y + d.dy) - y(d.y);
            });
    };

    var name = function (d) {
        return d.parent ? name(d.parent) + ' / ' + determineName(d) : determineName(d);
    };

    var hide = function (d) {
        d.style({
            "fill-opacity": 0,
            "stroke-opacity": 0
        });
    };

    var show = function (d) {
        d.style({
            "fill-opacity": 1,
            "stroke-opacity": 1
        });
    };

    var determineName = function (d) {
        var name = d.name;
        if (d.suffix)
            name += (" " + d.suffix);
        return name;
    };
    
    var determineColor = function (d) {
        if (d.color) {
            if (d.color.indexOf(';') >= 0) {
                return multicolor(d.color.split(';'));
            }
            return d.color;
        }
        return null;
    };

    var multicolor = function (colors) {
        colors = colors.sort();
        var name = 'c' + colors.join('-').replace(/#/g, '');
        if (defs.select('#' + name).size() === 0) {
            defs.append("pattern")
                .attr("id", name)
                .attr("width", colors.length * 5)
                .attr("height", 10)
                .attr("patternUnits", "userSpaceOnUse")
                .attr("patternTransform", "rotate(40)")
                .selectAll("rect")
                .data(colors)
                .enter()
                .append("rect")
                .attr("width", 5)
                .attr("height", 10)
                .attr("x", function (d, i) {
                    return i * 5;
                })
                .attr("y", 0)
                .attr("fill", function (d) {
                    return d;
                });
        }
        return 'url(#' + name + ')';
    };
}