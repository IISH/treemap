'use strict';

(function ($, d3) {
    var filters = $('#filters');
    var valuesContainer = $('#values-container');
    var treemap = new Treemap(d3);
    var render = false;

    $('.download.svg').click(function () {
        treemap.downloadSVG('treemap.svg');
    });

    $('.download.png').click(function () {
        treemap.downloadPNG('treemap.png');
    });

    $('.close').click(function () {
        $('#info').slideUp();
    });

    $('#values-toggle').click(function () {
        valuesContainer.slideToggle();
    });

    $('#on-start').click(function () {
        render = true;
        treemap.loadFromUrl(getTreemapUrlWithFilters(), function (treemapInfo) {
            updateValues(treemapInfo.filterInfo);

            $('#on-start').slideUp();
            $('#treemap').slideDown();
        });
    });

    filters.on('change', 'input[type=checkbox], select', reload);

    filters.on('slideStop', 'input.slider', reload);

    filters.on('click', '.unselect-all', function (e) {
        $(e.target).closest('.form-group').find('input').prop('checked', false);
        reload();
    });

    treemap.getTreemapInfo('treemap' + window.location.search, function (treemapInfo) {
        filterForm(treemapInfo.filterInfo);
        updateValues(treemapInfo.filterInfo);
    });

    treemap.onTransition(function (transistioning) {
        filters.find('input,select').prop('disabled', transistioning);
        var slider = filters.find('input.slider');
        transistioning ? slider.slider('disable') : slider.slider('enable');
    });

    function filterForm(filterInfo) {
        var form = filters.html('<form class="form-horizontal"></form>').find('form');

        var html = '<div class="form-group form-group-sm">';
        html += '<label class="col-sm-2 control-label""><strong>Show multiples ';
        html += '<i class="glyphicon glyphicon-info-sign" title="' + getTitle('multiples') + '"></i></strong></label>';
        html += '<div class="col-sm-8"><label class="checkbox-inline">';
        html += '<input type="checkbox" name="multiples" value="show"/> &nbsp;';
        html += '</label></div></div>';
        form.append(html);

        filterInfo.forEach(function (filter) {
            if ((location.search.indexOf('filterInfo=' + filter.column) >= 0) && (!filter.values || (filter.values.length > 1))) {
                var html = '<div class="form-group form-group-sm">';
                html += '<label class="col-sm-2 control-label""><strong>' + filter.column + ' ';
                html += '<i class="glyphicon glyphicon-info-sign" title="' + getTitle(filter.column, filter) + '"></i>';
                html += '</strong></label>';

                if (filter.values) {
                    var values = filter.values.sort();

                    if (values.length < 10) {
                        html += '<div class="col-sm-10">';
                        values.forEach(function (value) {
                            html += '<label class="checkbox-inline">';
                            html += '<input type="checkbox" name="filter:' + filter.column + '" value="' + value + '"/>';
                            html += value;
                            html += '</label>';
                        });
                        html += '<button type="button" class="btn btn-default btn-xs unselect-all">';
                        html += 'Unselect all</button>';
                        html += '</div>';
                    }
                    else {
                        html += '<div class="col-sm-8">';
                        html += '<select class="form-control" multiple name="filter:' + filter.column + '">';

                        values.forEach(function (value) {
                            html += '<option value="' + value + '">';
                            html += value;
                            html += '</option>';
                        });

                        html += '</select>';
                        html += '</div>';
                    }
                }
                else {
                    html += '<div class="col-sm-8">';
                    html += '<span class="min-label control-label">' + filter.min + '</span>';
                    html += '<input type="text" class="slider" data-slider-min="' + filter.min + '" ' +
                        'data-slider-max="' + filter.max + '" data-slider-value="[' + filter.min + ',' + filter.max + ']">';
                    html += '<span class="max-label control-label">' + filter.max + '</span>';
                    html += '<input type="hidden" class="min" name="min:' + filter.column + '" value="' + filter.min + '"/>';
                    html += '<input type="hidden" class="max" name="max:' + filter.column + '" value="' + filter.max + '"/>';
                    html += '</div>';
                }

                html += '</div>';
                form.append(html);
            }
        });

        form.find('select').select2({theme: 'bootstrap', allowClear: true});

        var slider = form.find('input.slider');
        slider.slider({'tooltip': 'hide'});
        slider.on('change', function (e) {
            var values = e.value.newValue;
            var row = $(this).closest('.form-group');

            var min = Math.min(values[0], values[1]);
            row.find('.min').val(min);
            row.find('.min-label').text(min);

            var max = Math.max(values[0], values[1]);
            row.find('.max').val(max);
            row.find('.max-label').text(max);
        });
    }

    function reload() {
        if (render) {
            treemap.loadFromUrl(getTreemapUrlWithFilters(), function (treemapInfo) {
                updateValues(treemapInfo.filterInfo);
            });
        }
    }

    function getTreemapUrlWithFilters() {
        var query = window.location.search;
        var serialized = filters.find('form').serialize();
        if (serialized.length > 0)
            query += ('&' + serialized);
        return 'treemap' + query;
    }

    function updateValues(filterInfo) {
        var html = '';
        filterInfo.forEach(function (info) {            
            html += '<div>';
            html += '<div class="column">All values for ' + info.column + ':</div>';
            if (info.values) {
                html += '<ul class="values list-unstyled">';
                info.values.sort().forEach(function (value) {
                    html += '<li>';
                    html += value;

                    var timePeriods = info.timePeriods[value];
                    if (timePeriods) {
                        html += '<ul class="years list-unstyled">';
                        for (var key in timePeriods) {
                            if (timePeriods.hasOwnProperty(key)) {
                                html += '<li class="year">';
                                html += key + ': ' + timePeriods[key];
                                html += '</li>';
                            }
                        }
                        html += '</ul>';
                    }

                    html += '</li>';
                });
                html += '</ul>';
            }
            else {
                html += '<div class="values">Ranges from ' + info.min + ' to ' + info.max + '</div>';

            }
            html += '</div>';
        });
        valuesContainer.html(html);
    }

    function getTitle(column, filter) {
        switch (column) {
            case 'multiples':
                return 'Show the various combinations as seperate blocks in the treemap, ' +
                    'or just show a single block containing all multiples.';
            case 'bmyear':
                return 'Filter the selection by time period. The year between brackets shows the year ' +
                    'from the dataset that is closest to the time period.';
            default:
                if (filter.values) {
                    if (filter.values.length < 10)
                        return 'Filter the selection by checking one or more values.';

                    return 'Filter the selection by selecting one or more values from the list. ' +
                        'You can add filters by typing in the field.';
                }

                return 'Filter the selection by dragging the handles to the preferred range.';
        }
    }
})(jQuery, d3);