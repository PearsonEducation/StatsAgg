$(document).ready(function () {
    $('[data-toggle="popover"]').popover(); //Initlize all bootstrap-popovers on page.

    $('body').on('click', function (e) {
        $('[data-toggle="popover"]').each(function () {
            // the 'is' for buttons that trigger popups
            // the 'has' for icons within a button that triggers a popup
            if (!$(this).is(e.target)) {
                $(this).popover('hide');
            }
        });
    });
});

$(document).ready(function () {
    $(".iframe").colorbox({iframe: true, width: "93%", height: "93%", speed: 200});
});

function confirmAction(formName, confirmString) {
    var confirmed = confirm(confirmString);
    if (confirmed) document.forms[formName].submit();
    return confirmed;
}

// Code from http://stackoverflow.com/questions/901115/how-can-i-get-query-string-values-in-javascript
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function generateForgetMetricsPreviewLink() {
    var ForgetMetricRegexParameter = "Regex=" + encodeURIComponent(document.getElementById("ForgetMetricRegex").value);
    var uriEncodedLink = "ForgetMetricsPreview?" + ForgetMetricRegexParameter;
    document.getElementById("ForgetMetricsPreview").setAttribute("href", uriEncodedLink);
}

function generateMergedRegexMetricsPreview_Match() {
    var MatchRegexParameter = "MatchRegexes=" + encodeURIComponent(document.getElementById("MatchRegexes").value);
    var uriEncodedLink = "MergedRegexMetricsPreview?" + MatchRegexParameter;
    document.getElementById("MergedRegexMetricsPreview_Match").setAttribute("href", uriEncodedLink);
}

function generateMergedRegexMetricsPreview_Blacklist() {
    var MatchRegexParameter = "MatchRegexes=" + encodeURIComponent(document.getElementById("MatchRegexes").value);
    var BlacklistRegexParameter = "BlacklistRegexes=" + encodeURIComponent(document.getElementById("BlacklistRegexes").value);
    var uriEncodedLink = "MergedRegexMetricsPreview?" + BlacklistRegexParameter + "&" + MatchRegexParameter;
    document.getElementById("MergedRegexMetricsPreview_Blacklist").setAttribute("href", uriEncodedLink);
}

// Setup for the table found on the 'Alerts' page
$(document).ready(function () {
    var table = document.getElementById('AlertsTable');

    if (table !== null) {
        var alertsTable = $('#AlertsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000, // 30 days
            "columnDefs": [
                {
                    "targets": [1],
                    "visible": false
                },
                {
                    "targets": [4],
                    "visible": false
                },
                {
                    "targets": [5],
                    "visible": false
                },
                {
                    "targets": [9],
                    "visible": false
                },
                {
                    "targets": [10],
                    "visible": false
                },
                {
                    "targets": [11],
                    "visible": false
                }
            ]});
        
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) alertsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_AlertsTable(alertsTable);

        var colvis = new $.fn.dataTable.ColVis(alertsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#AlertsTable_filter');
        
        // re-initialize yadcf when a column is unhidden
        alertsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_AlertsTable(alertsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_AlertsTable(alertsTable) {
    yadcf.init(alertsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 3, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 4, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 5, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 6, filter_reset_button_text: false, filter_type: "select", data: ["Yes", "No"], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 7, filter_reset_button_text: false, filter_type: "select", data: ["Yes", "No"], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 8, filter_reset_button_text: false, filter_type: "select", data: ["Yes", "No"], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 9, filter_reset_button_text: false, filter_type: "select", data: ["Yes", "No"], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 10, filter_reset_button_text: false, filter_type: "select", data: ["Yes", "No"], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 11, filter_reset_button_text: false, filter_type: "select", data: ['Yes', 'No', 'Caution Only', 'Danger Only', 'N/A'], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 12, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}
        
// Setup for the table found on the 'Alert Templates' page
$(document).ready(function () {
    var table = document.getElementById('AlertTemplatesTable');

    if (table !== null) {
        var alertsTemplatesTable = $('#AlertTemplatesTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000, // 30 days
            "columnDefs": [
            ]});
        
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) alertsTemplatesTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_AlertTemplatesTable(alertsTemplatesTable);

        var colvis = new $.fn.dataTable.ColVis(alertsTemplatesTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#AlertTemplatesTable_filter');
        
        // re-initialize yadcf when a column is unhidden
        alertsTemplatesTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_AlertTemplatesTable(alertsTemplatesTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_AlertTemplatesTable(alertsTemplatesTable) {
    yadcf.init(alertsTemplatesTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'Suspensions' page
$(document).ready(function () {
    var table = document.getElementById('SuspensionsTable');

    if (table !== null) {
        var suspensionsTable = $('#SuspensionsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });
        
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) suspensionsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_SuspensionsTable(suspensionsTable);

        var colvis = new $.fn.dataTable.ColVis(suspensionsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#SuspensionsTable_filter');

        // re-initialize yadcf when a column is unhidden
        suspensionsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_SuspensionsTable(suspensionsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_SuspensionsTable(suspensionsTable) {
    yadcf.init(suspensionsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "select", data: ['Alert Name', 'Metric Group Tags', 'Everything'], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 3, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 4, filter_reset_button_text: false, filter_type: "select", data: ['Yes', 'No'], sort_as: "none", filter_default_label: "Filter"},
        {column_number: 5, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'MetricGroups' page
$(document).ready(function () {
    var table = document.getElementById('MetricGroupsTable');

    if (table !== null) {
        var metricGroupsTable = $('#MetricGroupsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });

        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) metricGroupsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_MetricGroupsTable(metricGroupsTable);

        var colvis = new $.fn.dataTable.ColVis(metricGroupsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#MetricGroupsTable_filter');

        // re-initialize yadcf when a column is unhidden
        metricGroupsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_MetricGroupsTable(metricGroupsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_MetricGroupsTable(metricGroupsTable) {
    yadcf.init(metricGroupsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 3, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 4, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'MetricGroupTemplates' page
$(document).ready(function () {
    var table = document.getElementById('MetricGroupTemplatesTable');

    if (table !== null) {
        var metricGroupTemplatesTable = $('#MetricGroupTemplatesTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });

        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) metricGroupTemplatesTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_MetricGroupTemplatesTable(metricGroupTemplatesTable);

        var colvis = new $.fn.dataTable.ColVis(metricGroupTemplatesTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#MetricGroupTemplatesTable_filter');

        // re-initialize yadcf when a column is unhidden
        metricGroupTemplatesTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_MetricGroupTemplatesTable(metricGroupTemplatesTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_MetricGroupTemplatesTable(metricGroupTemplatesTable) {
    yadcf.init(metricGroupTemplatesTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'NotificationGroups' page
$(document).ready(function () {
    var table = document.getElementById('NotificationGroupsTable');

    if (table !== null) {
        var notificationGroupsTable = $('#NotificationGroupsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });
       
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) notificationGroupsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_NotificationGroupsTable(notificationGroupsTable);

        var colvis = new $.fn.dataTable.ColVis(notificationGroupsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#NotificationGroupsTable_filter');

        // re-initialize yadcf when a column is unhidden
        notificationGroupsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_NotificationGroupsTable(notificationGroupsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_NotificationGroupsTable(notificationGroupsTable) {
    yadcf.init(notificationGroupsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'VariableSets' page
$(document).ready(function () {
    var table = document.getElementById('VariableSetsTable');

    if (table !== null) {
        var variableSetsTable = $('#VariableSetsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });
       
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) variableSetsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_VariableSetsTable(variableSetsTable);

        var colvis = new $.fn.dataTable.ColVis(variableSetsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#VariableSetsTable_filter');

        // re-initialize yadcf when a column is unhidden
        variableSetsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_VariableSetsTable(variableSetsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_VariableSetsTable(variableSetsTable) {
    yadcf.init(variableSetsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'VariableSetLists' page
$(document).ready(function () {
    var table = document.getElementById('VariableSetListsTable');

    if (table !== null) {
        var variableSetListsTable = $('#VariableSetListsTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });
       
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) variableSetListsTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_VariableSetListsTable(variableSetListsTable);

        var colvis = new $.fn.dataTable.ColVis(variableSetListsTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#VariableSetListsTable_filter');

        // re-initialize yadcf when a column is unhidden
        variableSetListsTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_VariableSetListsTable(variableSetListsTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_VariableSetListsTable(variableSetListsTable) {
    yadcf.init(variableSetListsTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}

// Setup for the table found on the 'Alerts Report' page
$(document).ready(function () {
    var table = document.getElementById('AlertsReportTable');

    if (table !== null) {
        var alertsReportTable = $('#AlertsReportTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000, // 30 days
            "columnDefs": [
                {
                    "targets": [7],
                    "visible": false
                },
                {
                    "targets": [14],
                    "visible": false
                },
                {
                    "targets": [16],
                    "visible": false
                },
                {
                    "targets": [18],
                    "visible": false
                },
                {
                    "targets": [20],
                    "visible": false
                } 
            ]});
        
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) alertsReportTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_AlertsReportTable(alertsReportTable);

        var colvis = new $.fn.dataTable.ColVis(alertsReportTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#AlertsReportTable_filter');
        
        // re-initialize yadcf when a column is unhidden
        alertsReportTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_AlertsTable(alertsReportTable);
            }
        });
        
        table.style.display = null;
    }
});
                
function yadcf_init_AlertsReportTable(alertsReportTable) {
    yadcf.init(alertsReportTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 3, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 4, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 5, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 6, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 7, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 8, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 9, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 10, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 11, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 12, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 13, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 14, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 15, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 16, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 17, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 18, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 19, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 20, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 21, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
    
}

// Setup for the table found on the 'PagerDutyServices' page
$(document).ready(function () {
    var table = document.getElementById('PagerDutyServicesTable');

    if (table !== null) {
        var pagerDutyServicesTable = $('#PagerDutyServicesTable').DataTable({
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        });
       
        var tableSearchParameter = getParameterByName("TableSearch");
        if ((tableSearchParameter !== null) && (tableSearchParameter.trim() !== "")) pagerDutyServicesTable.search(tableSearchParameter.trim()).draw();
        
        yadcf_init_PagerDutyServicesTable(pagerDutyServicesTable);

        var colvis = new $.fn.dataTable.ColVis(pagerDutyServicesTable, {"align": "right", "iOverlayFade": 200});
        $(colvis.button()).prependTo('#PagerDutyServicesTable_filter');

        // re-initialize yadcf when a column is unhidden
        pagerDutyServicesTable.on('column-visibility.dt', function (e, settings, column, state) {
            console.log('Column ' + column + ' has changed to ' + (state ? 'visible' : 'hidden'));
            
            if (state === true) {
                yadcf_init_PagerDutyServicesTable(pagerDutyServicesTable);
            }
        });
        
        table.style.display = null;
    }
});

function yadcf_init_PagerDutyServicesTable(pagerDutyServicesTable) {
    yadcf.init(pagerDutyServicesTable, [
        {column_number: 0, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 1, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"},
        {column_number: 2, filter_reset_button_text: false, filter_type: "text", filter_default_label: "Filter"}
    ], 'footer');
}