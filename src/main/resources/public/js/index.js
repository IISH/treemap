'use strict';

(function ($) {
    var validPids = [];

    $('#dataset').change(function (e) {
        if ($(e.target).is(':checked')) {
            $('#pids').hide();
            $('#pid').val([]).trigger('change');
            $('#datasets').remove();
        }
        else {
            $('#pids').show();
            $('#pid').select2({theme: 'bootstrap', tags: true});
        }
        validate();
    });

    $('#pid').on('select2:select', function (e) {
        var pid = e.params.data.id.trim();
        if (pid.length > 0) {
            var pidSelect = $(e.target).prop('disabled', true);
            var loading = $('#loading').show();
            var datasets = $('#datasets');

            $.getJSON('labour/files', {pid: pid}, function (files) {
                files.forEach(function (file) {
                    var radioBox = '<div class="checkbox"><label>';
                    radioBox += '<input type="checkbox" class="files" data-pid="' + pid + '" ' +
                        'name="file" value="' + file.id + '">';
                    radioBox += file.name;
                    radioBox += '</label></div>';
                    datasets.append(radioBox);
                });

                loading.hide();
                pidSelect.prop('disabled', false);
                validPids.push(pid);
                validate();
            }).fail(function (e) {
                loading.hide();
                pidSelect.prop('disabled', false);
                setErrorMessage(e.responseText);
                validate();
            });
        }
    }).on('select2:unselect', function (e) {
        var pid = e.params.data.id.trim();
        var idx = validPids.indexOf(pid);
        validPids = (idx >= 0) ? validPids.slice(idx) : validPids;
        $('#datasets').find('[data-pid="' + pid + '"]').closest('.checkbox').remove();
        setSelect();
        validate();
    });

    $(document).on('change', '.files', function () {
        setSelect();
    });

    $('#treemap').find('select').on('change', function () {
        validate();
    }).on('select2:select', function (e) {
        var element = $(e.params.data.element);
        element.detach();
        $(this).append(element);
        $(this).trigger('change');
    });

    function setSelect() {
        var files = $('[name=file]').serialize();
        var treemapSelects = $('#treemap').find('select');

        if (files.length > 0) {
            var loading = $('#loading').show();
            treemapSelects.prop('disabled', true);
            $('.alert-danger').hide();

            $.getJSON('labour/columns?' + files, function (columns) {
                var data = columns.sort();

                loading.hide();
                treemapSelects.prop('disabled', false);

                $('#hierarchy')
                    .select2({theme: 'bootstrap', data: data})
                    .val(['txt1.1', 'txt1.2', 'txt1.3.ext', 'txt2.1', 'txt3.1'])
                    .trigger('change');

                $('#filterInfo')
                    .select2({theme: 'bootstrap', data: data})
                    .val(['bmyear', 'continent', 'country'])
                    .trigger('change');

                $('#size')
                    .select2({theme: 'bootstrap', maximumSelectionLength: 1, data: data})
                    .val(['total'])
                    .trigger('change');

                validate();
            }).fail(function (e) {
                loading.hide();
                setErrorMessage(e.responseText);
                treemapSelects.prop('disabled', false).val([]).trigger('change');
                validate();
            });
        }
        else {
            treemapSelects.prop('disabled', false).val([]).trigger('change');
            validate();
        }
    }

    function setErrorMessage(message) {
        var alert = $('.alert-danger');
        if (alert.length === 0)
            alert = $('<div class="alert alert-danger" role="alert"></div>').insertAfter('.page-header');
        alert.text(message).show();
    }

    function validate() {
        var valid = true;

        if (!$('#dataset').is(':checked')) {
            var pid = $('#pid');
            var validPid = (validPids.length === pid.val().length);
            if (!validPid) valid = false;
            setValidate(pid, !valid);

            var datasets = $('#datasets');
            var validDatasets = (datasets.find('.files:checked').length > 0);
            if (!validDatasets) valid = false;
            setValidate(datasets, !validDatasets);
        }

        var hierarchy = $('#hierarchy');
        if (hierarchy.val().length < 2) valid = false;
        setValidate(hierarchy, hierarchy.val().length < 2);

        var size = $('#size');
        if (size.val().length !== 1) valid = false;
        setValidate(size, size.val().length !== 1);

        if (valid) {
            $('#url').text(location.protocol + '//' + location.host + location.pathname +
                'labour/treemap.html?' + $('form').serialize());
            $('button').prop('disabled', false);
        }
        else {
            $('#url').text('');
            $('button').prop('disabled', true);
        }

        function setValidate(elem, valid) {
            if (valid)
                elem.closest('.form-group').addClass('has-error');
            else
                elem.closest('.form-group').removeClass('has-error');
        }
    }

    setSelect();
    validate();
})(jQuery);