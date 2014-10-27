function confirmAction(formName, confirmString) {
    var confirmed = confirm(confirmString);

    if (confirmed) {
        document.forms[formName].submit();
    }

    return confirmed;
}

function generateAlertPreviewLink(warningLevel) {

    var warningLevelParameter = "WarningLevel=" + encodeURIComponent(warningLevel);

    var nameParameter = "Name=" + encodeURIComponent(document.getElementById("Name").value.substring(0, 500));
    var descriptionParameter = "Description=" + encodeURIComponent(document.getElementById("Description").value.substring(0, 500));
    var metricGroupNameParameter = "MetricGroupName=" + encodeURIComponent(document.getElementById("MetricGroupName").value.substring(0, 500));

    var cautionAlertTypeParameter;
    if (document.getElementById("CreateAlertCaution_Type_Availability").checked === true) cautionAlertTypeParameter = "CreateAlertCaution_Type=" + encodeURIComponent("Availability");
    else if (document.getElementById("CreateAlertCaution_Type_Threshold").checked === true) cautionAlertTypeParameter = "CreateAlertCaution_Type=" + encodeURIComponent("Threshold");
    else if (document.getElementById("CreateAlertCaution_Type_Disabled").checked === true) cautionAlertTypeParameter = "CreateAlertCaution_Type=" + encodeURIComponent("Disabled");
    else cautionAlertTypeParameter = "CreateAlertCaution_Type=" + encodeURIComponent("undefined");
    
    var cautionWindowDurationParameter = "CautionWindowDuration=" + encodeURIComponent(document.getElementById("CautionWindowDuration").value);
    var cautionMinimumSampleCountParameter = "CautionMinimumSampleCount=" + encodeURIComponent(document.getElementById("CautionMinimumSampleCount").value);
    var cautionOperatorParameter = "CautionOperator=" + encodeURIComponent(document.getElementById("CautionOperator").value);
    var cautionCombinationParameter = "CautionCombination=" + encodeURIComponent(document.getElementById("CautionCombination").value);
    var cautionCombinationCountParameter = "CautionCombinationCount=" + encodeURIComponent(document.getElementById("CautionCombinationCount").value);
    var cautionThresholdParameter = "CautionThreshold=" + encodeURIComponent(document.getElementById("CautionThreshold").value);

    var dangerAlertTypeParameter;
    if (document.getElementById("CreateAlertDanger_Type_Availability").checked === true) dangerAlertTypeParameter = "CreateAlertDanger_Type=" + encodeURIComponent("Availability");
    else if (document.getElementById("CreateAlertDanger_Type_Threshold").checked === true) dangerAlertTypeParameter = "CreateAlertDanger_Type=" + encodeURIComponent("Threshold");
    else if (document.getElementById("CreateAlertDanger_Type_Disabled").checked === true) dangerAlertTypeParameter = "CreateAlertDanger_Type=" + encodeURIComponent("Disabled");
    else dangerAlertTypeParameter = "CreateAlertDanger_Type=" + encodeURIComponent("undefined");

    var dangerWindowDurationParameter = "DangerWindowDuration=" + encodeURIComponent(document.getElementById("DangerWindowDuration").value);
    var dangerMinimumSampleCountParameter = "DangerMinimumSampleCount=" + encodeURIComponent(document.getElementById("DangerMinimumSampleCount").value);
    var dangerOperatorParameter = "DangerOperator=" + encodeURIComponent(document.getElementById("DangerOperator").value);
    var dangerCombinationParameter = "DangerCombination=" + encodeURIComponent(document.getElementById("DangerCombination").value);
    var dangerCombinationCountParameter = "DangerCombinationCount=" + encodeURIComponent(document.getElementById("DangerCombinationCount").value);
    var dangerThresholdParameter = "DangerThreshold=" + encodeURIComponent(document.getElementById("DangerThreshold").value);

    var uriEncodedLink = "AlertPreview?" + warningLevelParameter + "&" +
            nameParameter + "&" + descriptionParameter + "&" + metricGroupNameParameter + "&" +
            cautionAlertTypeParameter + "&" + cautionWindowDurationParameter + "&" + cautionMinimumSampleCountParameter + "&" + cautionOperatorParameter + "&" +
            cautionCombinationParameter + "&" + cautionCombinationCountParameter + "&" + cautionThresholdParameter + "&" +
            dangerAlertTypeParameter + "&" + dangerWindowDurationParameter + "&" + dangerMinimumSampleCountParameter + "&" + dangerOperatorParameter + "&" +
            dangerCombinationParameter + "&" + dangerCombinationCountParameter + "&" + dangerThresholdParameter;

    if (warningLevel === "Caution") {
        document.getElementById("CautionPreview").setAttribute("href", uriEncodedLink);
    }

    if (warningLevel === "Danger") {
        document.getElementById("DangerPreview").setAttribute("href", uriEncodedLink);
    }
}

function generateAlertSuspensionAssociationsPreviewLink() {

    var SuspendByParameter;
    if (document.getElementById("CreateAlertSuspension_SuspendBy_AlertName_Radio").checked === true) SuspendByParameter = "CreateAlertSuspension_SuspendBy=" + encodeURIComponent("AlertName");
    else if (document.getElementById("CreateAlertSuspension_SuspendBy_Tags_Radio").checked === true) SuspendByParameter = "CreateAlertSuspension_SuspendBy=" + encodeURIComponent("Tags");
    else if (document.getElementById("CreateAlertSuspension_SuspendBy_Everything_Radio").checked === true) SuspendByParameter = "CreateAlertSuspension_SuspendBy=" + encodeURIComponent("Everything");
    else SuspendByParameter = "CreateAlertSuspension_SuspendBy=" + encodeURIComponent("undefined");

    var AlertNameParameter = "AlertName=" + encodeURIComponent(document.getElementById("AlertName").value);
    var MetricGroupTagsInclusiveParameter = "MetricGroupTagsInclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsInclusive").value);
    var MetricGroupTagsExclusiveParameter = "MetricGroupTagsExclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsExclusive").value);

    var uriEncodedLink = "AlertSuspensionAlertAssociationsPreview?" + SuspendByParameter + "&" +
            AlertNameParameter + "&" + MetricGroupTagsInclusiveParameter + "&" + MetricGroupTagsExclusiveParameter;

    document.getElementById("AlertSuspensionAlertAssociationsPreview").setAttribute("href", uriEncodedLink);
}

function generateForgetMetricsPreviewLink() {
    var ForgetMetricRegexParameter = "Regex=" + encodeURIComponent(document.getElementById("ForgetMetricRegex").value);
    var uriEncodedLink = "ForgetMetricsPreview?" + ForgetMetricRegexParameter;
    document.getElementById("ForgetMetricsPreview").setAttribute("href", uriEncodedLink);    
}

function generateMergedRegexMetricsPreview() {
    var RegexParameter = "Regexs=" + encodeURIComponent(document.getElementById("Regexs").value);
    var uriEncodedLink = "MergedRegexMetricsPreview?" + RegexParameter;
    document.getElementById("MergedRegexMetricsPreview").setAttribute("href", uriEncodedLink);    
}

$(document).ready(function() {
    $(".iframe").colorbox({iframe: true, width: "80%", height: "80%"});
});

// On page load for 'CreateAlert', show and hide certain UI elements for 'type'
$(document).ready(function() {
    CreateAlert_Type_ShowAndHide();
});

// On changing the alert-type radio buttons on the 'Create Alert' page, show and hide certain UI elements
$('#CreateAlertCaution_Type_Availability').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#CreateAlertCaution_Type_Threshold').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#CreateAlertCaution_Type_Disabled').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#CreateAlertDanger_Type_Availability').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#CreateAlertDanger_Type_Threshold').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#CreateAlertDanger_Type_Disabled').change(function() {
    CreateAlert_Type_ShowAndHide();
});

// On changing the value of the 'Combination' dropdown options on the 'Create Alert' page, show and hide certain UI elements
$('#CautionCombination').change(function() {
    CreateAlert_Type_ShowAndHide();
});
$('#DangerCombination').change(function() {
    CreateAlert_Type_ShowAndHide();
});

function CreateAlert_Type_ShowAndHide() {
    // Caution
    if ($("#CreateAlertCaution_Type_Availability").prop('checked') === true) { 
        $("#CautionNotificationGroupName_Label").show();
        $("#CautionNotificationGroupName").show();
        $("#CautionWindowDuration_Label").show();
        $("#CautionWindowDuration").show();
        $("#CautionMinimumSampleCount_Label").hide();
        $("#CautionMinimumSampleCount").hide();
        $("#CautionOperator_Label").hide();
        $("#CautionOperator").hide();
        $("#CautionCombination_Label").hide();
        $("#CautionCombination").hide();
        $("#CautionCombinationCount_Label").hide();
        $("#CautionCombinationCount").hide();
        $("#CautionThreshold_Label").hide();
        $("#CautionThreshold").hide();
    }
    else if ($("#CreateAlertCaution_Type_Threshold").prop('checked') === true) {
        $("#CautionNotificationGroupName_Label").show();
        $("#CautionNotificationGroupName").show();
        $("#CautionWindowDuration_Label").show();
        $("#CautionWindowDuration").show();
        $("#CautionMinimumSampleCount_Label").show();
        $("#CautionMinimumSampleCount").show();
        $("#CautionOperator_Label").show();
        $("#CautionOperator").show();
        $("#CautionCombination_Label").show();
        $("#CautionCombination").show();

        var CautionCombination_Value = document.getElementById("CautionCombination").selectedIndex;
        if ((CautionCombination_Value === 3) || (CautionCombination_Value === 4)) {
            $("#CautionCombinationCount_Label").show();
            $("#CautionCombinationCount").show();
        }
        else {
            $("#CautionCombinationCount_Label").hide();
            $("#CautionCombinationCount").hide();
        }

        $("#CautionThreshold_Label").show();
        $("#CautionThreshold").show();
    }
    else {
        $("#CautionNotificationGroupName_Label").hide();
        $("#CautionNotificationGroupName").hide();
        $("#CautionWindowDuration_Label").hide();
        $("#CautionWindowDuration").hide();
        $("#CautionMinimumSampleCount_Label").hide();
        $("#CautionMinimumSampleCount").hide();
        $("#CautionOperator_Label").hide();
        $("#CautionOperator").hide();
        $("#CautionCombination_Label").hide();
        $("#CautionCombination").hide();
        $("#CautionCombinationCount_Label").hide();
        $("#CautionCombinationCount").hide();
        $("#CautionThreshold_Label").hide();
        $("#CautionThreshold").hide();
    }

    //Danger
    if ($("#CreateAlertDanger_Type_Availability").prop('checked') === true) {
        $("#DangerNotificationGroupName_Label").show();
        $("#DangerNotificationGroupName").show();
        $("#DangerWindowDuration_Label").show();
        $("#DangerWindowDuration").show();
        $("#DangerMinimumSampleCount_Label").hide();
        $("#DangerMinimumSampleCount").hide();
        $("#DangerOperator_Label").hide();
        $("#DangerOperator").hide();
        $("#DangerCombination_Label").hide();
        $("#DangerCombination").hide();
        $("#DangerCombinationCount_Label").hide();
        $("#DangerCombinationCount").hide();
        $("#DangerThreshold_Label").hide();
        $("#DangerThreshold").hide();
    }
    else if ($("#CreateAlertDanger_Type_Threshold").prop('checked') === true) {
        $("#DangerNotificationGroupName_Label").show();
        $("#DangerNotificationGroupName").show();
        $("#DangerWindowDuration_Label").show();
        $("#DangerWindowDuration").show();
        $("#DangerMinimumSampleCount_Label").show();
        $("#DangerMinimumSampleCount").show();
        $("#DangerOperator_Label").show();
        $("#DangerOperator").show();
        $("#DangerCombination_Label").show();
        $("#DangerCombination").show();

        var DangerCombination_Value = document.getElementById("DangerCombination").selectedIndex;
        if ((DangerCombination_Value === 3) || (DangerCombination_Value === 4)) {
            $("#DangerCombinationCount_Label").show();
            $("#DangerCombinationCount").show();
        }
        else {
            $("#DangerCombinationCount_Label").hide();
            $("#DangerCombinationCount").hide();
        }

        $("#DangerThreshold_Label").show();
        $("#DangerThreshold").show();
    }
    else {
        $("#DangerNotificationGroupName_Label").hide();
        $("#DangerNotificationGroupName").hide();
        $("#DangerWindowDuration_Label").hide();
        $("#DangerWindowDuration").hide();
        $("#DangerMinimumSampleCount_Label").hide();
        $("#DangerMinimumSampleCount").hide();
        $("#DangerOperator_Label").hide();
        $("#DangerOperator").hide();
        $("#DangerCombination_Label").hide();
        $("#DangerCombination").hide();
        $("#DangerCombinationCount_Label").hide();
        $("#DangerCombinationCount").hide();
        $("#DangerThreshold_Label").hide();
        $("#DangerThreshold").hide();
    }
}

$(function() {
    $('#CreateAlertSuspension_DateTimePicker_StartDate_Div').datetimepicker({
        pickDate: true,
        pickTime: false
    });
});

$(function() {
    $('#CreateAlertSuspension_DateTimePicker_StartTime_Div').datetimepicker({
        pickDate: false,
        pickTime: true
    });
});

function CreateAlertSuspension_Type_ShowAndHide() {
    if ($("#CreateAlertSuspension_Type_Recurring").prop('checked') === true) {
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Label_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Label_Div").show();
        $("#CreateAlertSuspension_RecurOnDays_Div").show();
        $("#CreateAlertSuspension_RecurOnDays_Label_Div").show();
        $("#CreateAlertSuspension_Duration_Div").show();
        $("#CreateAlertSuspension_Duration_Label_Div").show();
    }
    else if ($("#CreateAlertSuspension_Type_OneTime").prop('checked') === true) {
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Label_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Div").show();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Label_Div").show();
        $("#CreateAlertSuspension_RecurOnDays_Div").hide();
        $("#CreateAlertSuspension_RecurOnDays_Label_Div").hide();
        $("#CreateAlertSuspension_Duration_Div").show();
        $("#CreateAlertSuspension_Duration_Label_Div").show();
    }
    else {
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Div").hide();
        $("#CreateAlertSuspension_DateTimePicker_StartDate_Label_Div").hide();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Div").hide();
        $("#CreateAlertSuspension_DateTimePicker_StartTime_Label_Div").hide();
        $("#CreateAlertSuspension_RecurOnDays_Div").hide();
        $("#CreateAlertSuspension_RecurOnDays_Label_Div").hide();
        $("#CreateAlertSuspension_Duration_Div").hide();
        $("#CreateAlertSuspension_Duration_Label_Div").hide();
    }
}

// On page load for 'CreateAlertSuspension', show and hide certain UI elements for 'type'
$(document).ready(function() {
    CreateAlertSuspension_Type_ShowAndHide();
});

// On changing the 'type' radio buttons 'CreateAlertSuspension', show and hide certain UI elements
$("input[type=radio]").change(function() {
    CreateAlertSuspension_Type_ShowAndHide();
});


function CreateAlertSuspension_SuspendBy_ShowAndHide() {
    if ($("#CreateAlertSuspension_SuspendBy_AlertName_Radio").prop('checked') === true) {
        $("#CreateAlertSuspension_SuspendBy_AlertName_Div").show();
        $("#CreateAlertSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Everything_Div").hide();
    }
    else if ($("#CreateAlertSuspension_SuspendBy_Tags_Radio").prop('checked') === true) {
        $("#CreateAlertSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Tags_Div").show();
        $("#CreateAlertSuspension_SuspendBy_Everything_Div").hide();
    }
    else if ($("#CreateAlertSuspension_SuspendBy_Everything_Radio").prop('checked') === true) {
        $("#CreateAlertSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Everything_Div").show();
    }
    else {
        $("#CreateAlertSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateAlertSuspension_SuspendBy_Everything_Div").hide();
    }
}

// On page load for 'CreateAlertSuspension', show and hide certain UI elements for 'suspend by'
$(document).ready(function() {
    CreateAlertSuspension_SuspendBy_ShowAndHide();
});

// On changing the 'suspend by' radio buttons 'CreateAlertSuspension', show and hide certain UI elements
$("input[type=radio]").change(function() {
    CreateAlertSuspension_SuspendBy_ShowAndHide();
});

// Setup for the table found on the 'Alerts' page
$(document).ready(function() {
    var doesTableExist = document.getElementById('AlertsTable');

    if (doesTableExist !== null) {
        $('#AlertsTable').dataTable({
            "dom": 'C<"clear">lfrtip',
            "colVis": {"align": "right", "iOverlayFade": 200}, 
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000, // 30 days
            "columnDefs": [
                {
                    "targets": [3],
                    "visible": false
                },
                {
                    "targets": [4],
                    "visible": false
                },
                {
                    "targets": [8],
                    "visible": false
                },
                {
                    "targets": [9],
                    "visible": false
                }
            ]}).columnFilter({
            aoColumns: [
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {type: "select", values: ['true', 'false']},
                {type: "select", values: ['true', 'false']},
                {type: "select", values: ['true', 'false']},
                {type: "select", values: ['true', 'false']},
                {type: "select", values: ['true', 'false']},
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                }
            ]
        });

        $( colvis.button() ).prependTo('#AlertsTable_filter');
    }
});

// Setup for the table found on the 'Alerts' page
$(document).ready(function() {
    var doesTableExist = document.getElementById('AlertSuspensionsTable');
    
    if (doesTableExist !== null) {
        $('#AlertSuspensionsTable').dataTable({
            "dom": 'C<"clear">lfrtip',
            "colVis": {"align": "right", "iOverlayFade": 200}, 
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        }).columnFilter({
            aoColumns: [
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {type: "select", values: ['Alert Name', 'Metric Group Tags', 'Everything']},
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {type: "select", values: ['true', 'false']},
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                }
            ]
        });

        $( colvis.button() ).prependTo('#AlertSuspensionsTable_filter');
    }
});

// Setup for the table found on the 'MetricGroups' page
$(document).ready(function() {
    var doesTableExist = document.getElementById('MetricGroupsTable');
    
    if (doesTableExist !== null) {
        $('#MetricGroupsTable').dataTable({
            "dom": 'C<"clear">lfrtip',
            "colVis": {"align": "right", "iOverlayFade": 200}, 
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        }).columnFilter({
            aoColumns: [
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                }
            ]
        });

        $( colvis.button() ).prependTo('#MetricGroupsTable_filter');
    }
});

// Setup for the table found on the 'NotificationGroups' page
$(document).ready(function() {
    var doesTableExist = document.getElementById('NotificationGroupsTable');
    
    if (doesTableExist !== null) {
        $('#NotificationGroupsTable').dataTable({
            "dom": 'C<"clear">lfrtip',
            "colVis": {"align": "right", "iOverlayFade": 200}, 
            "lengthMenu": [[15, 30, 50, -1], [15, 30, 50, "All"]],
            "order": [[0, "asc"]],
            "autoWidth": false,
            "stateSave": true,
            "iCookieDuration": 2592000 // 30 days
        }).columnFilter({
            aoColumns: [
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                },
                {
                    type: "text",
                    bRegex: true,
                    bSmart: true
                }
            ]
        });

        $( colvis.button() ).prependTo('#NotificationGroupsTable_filter');
    }
});

$(document).ready(function() {
    var doesAlertNameLookupExist = document.getElementById('AlertNameLookup');
    
    if (doesAlertNameLookupExist !== null) {
        var AlertNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=AlertName&Query=%QUERY'
        });

        AlertNameLookup_Bloodhound.initialize();

        $('#AlertNameLookup .typeahead').typeahead({
            source: AlertNameLookup_Bloodhound.ttAdapter(),
            updater: function (item) {
                return $('<div/>').html(item).text();
            }
        });
    }
});

$(document).ready(function() {
    var doesMetricGroupNameLookupExist = document.getElementById('MetricGroupNameLookup');
    
    if (doesMetricGroupNameLookupExist !== null) {
        var MetricGroupNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=MetricGroupName&Query=%QUERY'
        });

        MetricGroupNameLookup_Bloodhound.initialize();

        $('#MetricGroupNameLookup .typeahead').typeahead({
            source: MetricGroupNameLookup_Bloodhound.ttAdapter(),
            updater: function (item) {
                return $('<div/>').html(item).text();
            }
        });
    }
});

$(document).ready(function() {
    var doesCautionNotificationGroupNameLookupExist = document.getElementById('CautionNotificationGroupNameLookup');
    
    if (doesCautionNotificationGroupNameLookupExist !== null) {
        var NotificationGroupNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=NotificationGroupName&Query=%QUERY'
        });

        NotificationGroupNameLookup_Bloodhound.initialize();

        $('#CautionNotificationGroupNameLookup .typeahead').typeahead({
            source: NotificationGroupNameLookup_Bloodhound.ttAdapter(),
            updater: function (item) {
                return $('<div/>').html(item).text();
            }
        });
    }
});

$(document).ready(function() {
    var doesDangerNotificationGroupNameLookupExist = document.getElementById('DangerNotificationGroupNameLookup');
    
    if (doesDangerNotificationGroupNameLookupExist !== null) {
        var NotificationGroupNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=NotificationGroupName&Query=%QUERY'
        });

        NotificationGroupNameLookup_Bloodhound.initialize();

        $('#DangerNotificationGroupNameLookup .typeahead').typeahead({
            source: NotificationGroupNameLookup_Bloodhound.ttAdapter(),
            updater: function (item) {
                return $('<div/>').html(item).text();
            }
        });
    }
});
