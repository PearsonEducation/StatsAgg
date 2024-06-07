function generateSuspensionAssociationsPreviewLink() {

    var SuspendByParameter;
    if (document.getElementById("SuspendBy_AlertName_Radio").checked === true) SuspendByParameter = "SuspendBy=" + encodeURIComponent("AlertName");
    else if (document.getElementById("SuspendBy_Tags_Radio").checked === true) SuspendByParameter = "SuspendBy=" + encodeURIComponent("Tags");
    else if (document.getElementById("SuspendBy_Everything_Radio").checked === true) SuspendByParameter = "SuspendBy=" + encodeURIComponent("Everything");
    else if (document.getElementById("SuspendBy_Metrics_Radio").checked === true) SuspendByParameter = "SuspendBy=" + encodeURIComponent("Metrics");
    else SuspendByParameter = "SuspendBy=" + encodeURIComponent("undefined");

    var AlertNameParameter = "AlertName=" + encodeURIComponent(document.getElementById("AlertName").value);
    var MetricGroupTagsInclusiveParameter = "MetricGroupTagsInclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsInclusive").value);
    var MetricGroupTagsExclusiveParameter = "MetricGroupTagsExclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsExclusive").value);
    var MetricSuspensionRegexesParameter = "MetricSuspensionRegexes=" + encodeURIComponent(document.getElementById("MetricSuspensionRegexes").value);

    var uriEncodedLink = "SuspensionAssociationsPreview?" + SuspendByParameter + "&" + AlertNameParameter + "&" + 
            MetricGroupTagsInclusiveParameter + "&" + MetricGroupTagsExclusiveParameter + "&" + MetricSuspensionRegexesParameter;

    document.getElementById("SuspensionAssociationsPreview").setAttribute("href", uriEncodedLink);
}

$(function () {
    $('#DateTimePicker_StartDate_Div').datetimepicker({
        format: 'L'
    });
});

$(function () {
    $('#DateTimePicker_StartTime_Div').datetimepicker({
        format: 'LT'
    });
});

// On page load for 'CreateSuspension', show and hide certain UI elements for 'type'
$(document).ready(function () {
    Type_ShowAndHide();
});

// On changing the 'type' radio buttons 'CreateSuspension', show and hide certain UI elements
$("input[type=radio]").change(function () {
    Type_ShowAndHide();
});

function Type_ShowAndHide() {
    if ($("#Type_Recurring").prop('checked') === true) {
        $("#DateTimePicker_StartDate_Div").show();
        $("#DateTimePicker_StartDate_Label_Div").show();
        $("#RecurOnDays_Div").show();
        $("#RecurOnDays_Label_Div").show();
        $("#Type_Spacer1").show();
        $("#DateTimePicker_StartTime_Div").show();
        $("#DateTimePicker_StartTime_Label_Div").show();
        $("#Duration_Div").show();
        $("#Duration_Label_Div").show();
    }
    else if ($("#Type_OneTime").prop('checked') === true) {
        $("#DateTimePicker_StartDate_Div").show();
        $("#DateTimePicker_StartDate_Label_Div").show();
        $("#RecurOnDays_Div").hide();
        $("#RecurOnDays_Label_Div").hide();
        $("#Type_Spacer1").show();
        $("#DateTimePicker_StartTime_Div").show();
        $("#DateTimePicker_StartTime_Label_Div").show();
        $("#Duration_Div").show();
        $("#Duration_Label_Div").show();
    }
    else {
        $("#DateTimePicker_StartDate_Div").hide();
        $("#DateTimePicker_StartDate_Label_Div").hide();
        $("#RecurOnDays_Div").hide();
        $("#RecurOnDays_Label_Div").hide();
        $("#Type_Spacer1").hide();
        $("#DateTimePicker_StartTime_Div").hide();
        $("#DateTimePicker_StartTime_Label_Div").hide();
        $("#Duration_Div").hide();
        $("#Duration_Label_Div").hide();
    }
}

// On page load for 'CreateSuspension', show and hide certain UI elements for 'suspend by'
$(document).ready(function () {
    SuspendBy_ShowAndHide();
});

// On changing the 'suspend by' radio buttons 'CreateSuspension', show and hide certain UI elements
$("input[type=radio]").change(function () {
    SuspendBy_ShowAndHide();
});

function SuspendBy_ShowAndHide() {
    if ($("#SuspendBy_AlertName_Radio").prop('checked') === true) {
        $("#SuspendBy_AlertName_Div").show();
        $("#SuspendBy_Tags_Div").hide();
        $("#SuspendBy_Everything_Div").hide();
        $("#SuspendBy_Metrics_Div").hide();
    }
    else if ($("#SuspendBy_Tags_Radio").prop('checked') === true) {
        $("#SuspendBy_AlertName_Div").hide();
        $("#SuspendBy_Tags_Div").show();
        $("#SuspendBy_Everything_Div").hide();
        $("#SuspendBy_Metrics_Div").hide();
    }
    else if ($("#SuspendBy_Everything_Radio").prop('checked') === true) {
        $("#SuspendBy_AlertName_Div").hide();
        $("#SuspendBy_Tags_Div").hide();
        $("#SuspendBy_Everything_Div").show();
        $("#SuspendBy_Metrics_Div").hide();
    }
    else if ($("#SuspendBy_Metrics_Radio").prop('checked') === true) {
        $("#SuspendBy_AlertName_Div").hide();
        $("#SuspendBy_Tags_Div").hide();
        $("#SuspendBy_Everything_Div").hide();
        $("#SuspendBy_Metrics_Div").show();
    }
    else {
        $("#SuspendBy_AlertName_Div").hide();
        $("#SuspendBy_Tags_Div").hide();
        $("#SuspendBy_Everything_Div").hide();
        $("#SuspendBy_Metrics_Div").hide();
    }
}

$(document).ready(function() {
    var doesExist = document.getElementById('AlertNameLookup');
    
    if (doesExist !== null) {
        var AlertNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=AlertName&Query=%QUERY'
        });

        AlertNameLookup_Bloodhound.initialize();

       $('#AlertNameLookup .typeahead').typeahead(
            {
                highlight: false,
                hint: false
            },
            {
                name: 'AlertNameLookup_Bloodhound',
                displayKey: "Value",
                source: AlertNameLookup_Bloodhound.ttAdapter(),
                templates: {
                    suggestion: function (dropdown_display) {
                        return '<p>' + dropdown_display.HtmlValue + '</p>';
                    }
                }
            }
        );
    }
});
