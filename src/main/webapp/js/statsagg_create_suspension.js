function generateSuspensionAssociationsPreviewLink() {

    var SuspendByParameter;
    if (document.getElementById("CreateSuspension_SuspendBy_AlertName_Radio").checked === true) SuspendByParameter = "CreateSuspension_SuspendBy=" + encodeURIComponent("AlertName");
    else if (document.getElementById("CreateSuspension_SuspendBy_Tags_Radio").checked === true) SuspendByParameter = "CreateSuspension_SuspendBy=" + encodeURIComponent("Tags");
    else if (document.getElementById("CreateSuspension_SuspendBy_Everything_Radio").checked === true) SuspendByParameter = "CreateSuspension_SuspendBy=" + encodeURIComponent("Everything");
    else if (document.getElementById("CreateSuspension_SuspendBy_Metrics_Radio").checked === true) SuspendByParameter = "CreateSuspension_SuspendBy=" + encodeURIComponent("Metrics");
    else SuspendByParameter = "CreateSuspension_SuspendBy=" + encodeURIComponent("undefined");

    var AlertNameParameter = "AlertName=" + encodeURIComponent(document.getElementById("AlertName").value);
    var MetricGroupTagsInclusiveParameter = "MetricGroupTagsInclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsInclusive").value);
    var MetricGroupTagsExclusiveParameter = "MetricGroupTagsExclusive=" + encodeURIComponent(document.getElementById("MetricGroupTagsExclusive").value);
    var MetricSuspensionRegexesParameter = "MetricSuspensionRegexes=" + encodeURIComponent(document.getElementById("MetricSuspensionRegexes").value);

    var uriEncodedLink = "SuspensionAssociationsPreview?" + SuspendByParameter + "&" + AlertNameParameter + "&" + 
            MetricGroupTagsInclusiveParameter + "&" + MetricGroupTagsExclusiveParameter + "&" + MetricSuspensionRegexesParameter;

    document.getElementById("SuspensionAssociationsPreview").setAttribute("href", uriEncodedLink);
}

$(function () {
    $('#CreateSuspension_DateTimePicker_StartDate_Div').datetimepicker({
        format: 'L'
    });
});

$(function () {
    $('#CreateSuspension_DateTimePicker_StartTime_Div').datetimepicker({
        format: 'LT'
    });
});

// On page load for 'CreateSuspension', show and hide certain UI elements for 'type'
$(document).ready(function () {
    CreateSuspension_Type_ShowAndHide();
});

// On changing the 'type' radio buttons 'CreateSuspension', show and hide certain UI elements
$("input[type=radio]").change(function () {
    CreateSuspension_Type_ShowAndHide();
});

function CreateSuspension_Type_ShowAndHide() {
    if ($("#CreateSuspension_Type_Recurring").prop('checked') === true) {
        $("#CreateSuspension_DateTimePicker_StartDate_Div").show();
        $("#CreateSuspension_DateTimePicker_StartDate_Label_Div").show();
        $("#CreateSuspension_RecurOnDays_Div").show();
        $("#CreateSuspension_RecurOnDays_Label_Div").show();
        $("#CreateSuspension_Type_Spacer1").show();
        $("#CreateSuspension_DateTimePicker_StartTime_Div").show();
        $("#CreateSuspension_DateTimePicker_StartTime_Label_Div").show();
        $("#CreateSuspension_Duration_Div").show();
        $("#CreateSuspension_Duration_Label_Div").show();
    }
    else if ($("#CreateSuspension_Type_OneTime").prop('checked') === true) {
        $("#CreateSuspension_DateTimePicker_StartDate_Div").show();
        $("#CreateSuspension_DateTimePicker_StartDate_Label_Div").show();
        $("#CreateSuspension_RecurOnDays_Div").hide();
        $("#CreateSuspension_RecurOnDays_Label_Div").hide();
        $("#CreateSuspension_Type_Spacer1").show();
        $("#CreateSuspension_DateTimePicker_StartTime_Div").show();
        $("#CreateSuspension_DateTimePicker_StartTime_Label_Div").show();
        $("#CreateSuspension_Duration_Div").show();
        $("#CreateSuspension_Duration_Label_Div").show();
    }
    else {
        $("#CreateSuspension_DateTimePicker_StartDate_Div").hide();
        $("#CreateSuspension_DateTimePicker_StartDate_Label_Div").hide();
        $("#CreateSuspension_RecurOnDays_Div").hide();
        $("#CreateSuspension_RecurOnDays_Label_Div").hide();
        $("#CreateSuspension_Type_Spacer1").hide();
        $("#CreateSuspension_DateTimePicker_StartTime_Div").hide();
        $("#CreateSuspension_DateTimePicker_StartTime_Label_Div").hide();
        $("#CreateSuspension_Duration_Div").hide();
        $("#CreateSuspension_Duration_Label_Div").hide();
    }
}

// On page load for 'CreateSuspension', show and hide certain UI elements for 'suspend by'
$(document).ready(function () {
    CreateSuspension_SuspendBy_ShowAndHide();
});

// On changing the 'suspend by' radio buttons 'CreateSuspension', show and hide certain UI elements
$("input[type=radio]").change(function () {
    CreateSuspension_SuspendBy_ShowAndHide();
});

function CreateSuspension_SuspendBy_ShowAndHide() {
    if ($("#CreateSuspension_SuspendBy_AlertName_Radio").prop('checked') === true) {
        $("#CreateSuspension_SuspendBy_AlertName_Div").show();
        $("#CreateSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateSuspension_SuspendBy_Everything_Div").hide();
        $("#CreateSuspension_SuspendBy_Metrics_Div").hide();
    }
    else if ($("#CreateSuspension_SuspendBy_Tags_Radio").prop('checked') === true) {
        $("#CreateSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateSuspension_SuspendBy_Tags_Div").show();
        $("#CreateSuspension_SuspendBy_Everything_Div").hide();
        $("#CreateSuspension_SuspendBy_Metrics_Div").hide();
    }
    else if ($("#CreateSuspension_SuspendBy_Everything_Radio").prop('checked') === true) {
        $("#CreateSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateSuspension_SuspendBy_Everything_Div").show();
        $("#CreateSuspension_SuspendBy_Metrics_Div").hide();
    }
    else if ($("#CreateSuspension_SuspendBy_Metrics_Radio").prop('checked') === true) {
        $("#CreateSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateSuspension_SuspendBy_Everything_Div").hide();
        $("#CreateSuspension_SuspendBy_Metrics_Div").show();
    }
    else {
        $("#CreateSuspension_SuspendBy_AlertName_Div").hide();
        $("#CreateSuspension_SuspendBy_Tags_Div").hide();
        $("#CreateSuspension_SuspendBy_Everything_Div").hide();
        $("#CreateSuspension_SuspendBy_Metrics_Div").hide();
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
