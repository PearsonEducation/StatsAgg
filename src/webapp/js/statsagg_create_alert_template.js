function generateAlertPreviewLink(warningLevel) {

    var warningLevelParameter = "WarningLevel=" + encodeURIComponent(warningLevel);

    var nameParameter = "Name=" + encodeURIComponent(document.getElementById("AlertNameVariable").value.substring(0, 500));
    var descriptionParameter = "Description=" + encodeURIComponent(document.getElementById("Description").value.substring(0, 500));

    var cautionEnabledParameter = "CautionEnabled=" + encodeURIComponent(document.getElementById("CautionEnabled").value);
    var dangerEnabledParameter = "DangerEnabled=" + encodeURIComponent(document.getElementById("DangerEnabled").value);

    var alertTypeParameter;
    if (document.getElementById("Type_Availability").checked === true) alertTypeParameter = "Type=" + encodeURIComponent("Availability");
    else if (document.getElementById("Type_Threshold").checked === true) alertTypeParameter = "Type=" + encodeURIComponent("Threshold");
    else alertTypeParameter = "Type=" + encodeURIComponent("undefined");

    var metricGroupNameParameter = "MetricGroupName=" + encodeURIComponent(document.getElementById("MetricGroupNameVariable").value.substring(0, 500));

    var cautionWindowDurationParameter = "CautionWindowDuration=" + encodeURIComponent(document.getElementById("CautionWindowDuration").value);
    var cautionWindowDurationTimeUnitParameter = "CautionWindowDurationTimeUnit=" + encodeURIComponent(document.getElementById("CautionWindowDurationTimeUnit").value);
    var cautionStopTrackingAfterParameter = "CautionStopTrackingAfter=" + encodeURIComponent(document.getElementById("CautionStopTrackingAfter").value);
    var cautionStopTrackingAfterTimeUnitParameter = "CautionStopTrackingAfterTimeUnit=" + encodeURIComponent(document.getElementById("CautionStopTrackingAfterTimeUnit").value);
    var cautionMinimumSampleCountParameter = "CautionMinimumSampleCount=" + encodeURIComponent(document.getElementById("CautionMinimumSampleCount").value);
    var cautionOperatorParameter = "CautionOperator=" + encodeURIComponent(document.getElementById("CautionOperator").value);
    var cautionCombinationParameter = "CautionCombination=" + encodeURIComponent(document.getElementById("CautionCombination").value);
    var cautionCombinationCountParameter = "CautionCombinationCount=" + encodeURIComponent(document.getElementById("CautionCombinationCount").value);
    var cautionThresholdParameter = "CautionThreshold=" + encodeURIComponent(document.getElementById("CautionThreshold").value);

    var dangerWindowDurationParameter = "DangerWindowDuration=" + encodeURIComponent(document.getElementById("DangerWindowDuration").value);
    var dangerWindowDurationTimeUnitParameter = "DangerWindowDurationTimeUnit=" + encodeURIComponent(document.getElementById("DangerWindowDurationTimeUnit").value);
    var dangerStopTrackingAfterParameter = "DangerStopTrackingAfter=" + encodeURIComponent(document.getElementById("DangerStopTrackingAfter").value);
    var dangerStopTrackingAfterTimeUnitParameter = "DangerStopTrackingAfterTimeUnit=" + encodeURIComponent(document.getElementById("DangerStopTrackingAfterTimeUnit").value);
    var dangerMinimumSampleCountParameter = "DangerMinimumSampleCount=" + encodeURIComponent(document.getElementById("DangerMinimumSampleCount").value);
    var dangerOperatorParameter = "DangerOperator=" + encodeURIComponent(document.getElementById("DangerOperator").value);
    var dangerCombinationParameter = "DangerCombination=" + encodeURIComponent(document.getElementById("DangerCombination").value);
    var dangerCombinationCountParameter = "DangerCombinationCount=" + encodeURIComponent(document.getElementById("DangerCombinationCount").value);
    var dangerThresholdParameter = "DangerThreshold=" + encodeURIComponent(document.getElementById("DangerThreshold").value);

    var uriEncodedLink = "AlertPreview?" + warningLevelParameter + "&" +
            nameParameter + "&" + descriptionParameter + "&" + cautionEnabledParameter + "&" + dangerEnabledParameter + "&" + alertTypeParameter + "&" + metricGroupNameParameter + "&" +
            cautionWindowDurationParameter + "&" + cautionWindowDurationTimeUnitParameter + "&" + 
            cautionStopTrackingAfterParameter + "&" + cautionStopTrackingAfterTimeUnitParameter + "&" + 
            cautionMinimumSampleCountParameter + "&" +
            cautionOperatorParameter + "&" + cautionCombinationParameter + "&" + cautionCombinationCountParameter + "&" + cautionThresholdParameter + "&" +
            dangerWindowDurationParameter + "&" + dangerWindowDurationTimeUnitParameter + "&" +
            dangerStopTrackingAfterParameter + "&" + dangerStopTrackingAfterTimeUnitParameter + "&" + 
            dangerMinimumSampleCountParameter + "&" +
            dangerOperatorParameter + "&" + dangerCombinationParameter + "&" + dangerCombinationCountParameter + "&" + dangerThresholdParameter;

    if (warningLevel === "Caution") document.getElementById("CautionPreview").setAttribute("href", uriEncodedLink);
    else if (warningLevel === "Danger") document.getElementById("DangerPreview").setAttribute("href", uriEncodedLink);
}

// On page load for 'CreateAlertTemplate', show and hide certain UI elements for 'type'
$(document).ready(function () {
    CreateAlertTemplate_Type_ShowAndHide();
    CreateAlertTemplate_CautionCriteria_ShowAndHide();
    CreateAlertTemplate_DangerCriteria_ShowAndHide();
});

// On changing the 'caution alerting enabled' checkbox, show or hide the 'caution criteria' panel
$('#CautionEnabled').change(function () {
    CreateAlertTemplate_CautionCriteria_ShowAndHide();
});

function CreateAlertTemplate_CautionCriteria_ShowAndHide() {
    if ($("#CautionEnabled").prop('checked') === true) $("#CautionCriteria").show();
    else $("#CautionCriteria").hide();
}

// On changing the 'danger alerting enabled' checkbox, show or hide the 'danger criteria' panel
$('#DangerEnabled').change(function () {
    CreateAlertTemplate_DangerCriteria_ShowAndHide();
});

function CreateAlertTemplate_DangerCriteria_ShowAndHide() {
    if ($("#DangerEnabled").prop('checked') === true) $("#DangerCriteria").show();
    else $("#DangerCriteria").hide();
}

// On changing the alert-type radio buttons on the 'Create Alert Template' page, show and hide certain UI elements
$('#Type_Availability').change(function () {
    CreateAlertTemplate_Type_ShowAndHide();
});
$('#Type_Threshold').change(function () {
    CreateAlertTemplate_Type_ShowAndHide();
});

// On changing the 'alert on positive' box the 'Create Alert Template' page, show and hide certain UI elements
$('#AlertOnPositive').change(function () {
    CreateAlertTemplate_Type_ShowAndHide();
});

// On changing the value of the 'Combination' dropdown options on the 'Create Alert Template' page, show and hide certain UI elements
$('#CautionCombination').change(function () {
    CreateAlertTemplate_Type_ShowAndHide();
});
$('#DangerCombination').change(function () {
    CreateAlertTemplate_Type_ShowAndHide();
});

function CreateAlertTemplate_Type_ShowAndHide() {
    // Caution
    if ($("#Type_Availability").prop('checked') === true) {
        $("#CautionNoAlertTypeSelected_Label").hide();
        $("#CautionNotificationGroupNameVariable_Div").show();
        
        if ($("#AlertOnPositive").prop('checked') === true) $("#CautionPositiveNotificationGroupNameVariable_Div").show();
        else $("#CautionPositiveNotificationGroupNameVariable_Div").hide();

        $("#CautionWindowDuration_Div").show();
        $("#CautionStopTrackingAfter_Div").show();
        $("#CautionMinimumSampleCount_Div").hide();
        $("#CautionOperator_Div").hide();
        $("#CautionCombination_Div").hide();
        $("#CautionCombinationCount_Div").hide();
        $("#CautionThreshold_Div").hide();
    }
    else if ($("#Type_Threshold").prop('checked') === true) {
        $("#CautionNoAlertTypeSelected_Label").hide();
        $("#CautionNotificationGroupNameVariable_Div").show();
        
        if ($("#AlertOnPositive").prop('checked') === true) $("#CautionPositiveNotificationGroupNameVariable_Div").show();
        else $("#CautionPositiveNotificationGroupNameVariable_Div").hide();

        $("#CautionWindowDuration_Div").show();
        $("#CautionStopTrackingAfter_Div").hide();
        $("#CautionMinimumSampleCount_Div").show();
        $("#CautionOperator_Div").show();
        $("#CautionCombination_Div").show();

        var CautionCombination_Value = document.getElementById("CautionCombination").selectedIndex;
        if ((CautionCombination_Value === 3) || (CautionCombination_Value === 4)) $("#CautionCombinationCount_Div").show();
        else $("#CautionCombinationCount_Div").hide();

        $("#CautionThreshold_Div").show();
    }
    else {
        $("#CautionNoAlertTypeSelected_Label").show();
        $("#CautionNotificationGroupNameVariable_Div").hide();
        $("#CautionPositiveNotificationGroupNameVariable_Div").hide();
        $("#CautionWindowDuration_Div").hide();
        $("#CautionStopTrackingAfter_Div").hide();
        $("#CautionMinimumSampleCount_Div").hide();
        $("#CautionOperator_Div").hide();
        $("#CautionCombination_Div").hide();
        $("#CautionCombinationCount_Div").hide();
        $("#CautionThreshold_Div").hide();
    }

    //Danger
    if ($("#Type_Availability").prop('checked') === true) {
        $("#DangerNoAlertTypeSelected_Label").hide();
        $("#DangerNotificationGroupNameVariable_Div").show();
        
        if ($("#AlertOnPositive").prop('checked') === true) $("#DangerPositiveNotificationGroupNameVariable_Div").show();
        else $("#DangerPositiveNotificationGroupNameVariable_Div").hide();

        $("#DangerWindowDuration_Div").show();
        $("#DangerStopTrackingAfter_Div").show();
        $("#DangerMinimumSampleCount_Div").hide();
        $("#DangerOperator_Div").hide();
        $("#DangerCombination_Div").hide();
        $("#DangerCombinationCount_Div").hide();
        $("#DangerThreshold_Div").hide();
    }
    else if ($("#Type_Threshold").prop('checked') === true) {
        $("#DangerNoAlertTypeSelected_Label").hide();
        $("#DangerNotificationGroupNameVariable_Div").show();
        
        if ($("#AlertOnPositive").prop('checked') === true) $("#DangerPositiveNotificationGroupNameVariable_Div").show();
        else $("#DangerPositiveNotificationGroupNameVariable_Div").hide();

        $("#DangerWindowDuration_Div").show();
        $("#DangerStopTrackingAfter_Div").hide();
        $("#DangerMinimumSampleCount_Div").show();
        $("#DangerOperator_Div").show();
        $("#DangerCombination_Div").show();

        var DangerCombination_Value = document.getElementById("DangerCombination").selectedIndex;
        if ((DangerCombination_Value === 3) || (DangerCombination_Value === 4)) $("#DangerCombinationCount_Div").show();
        else $("#DangerCombinationCount_Div").hide();

        $("#DangerThreshold_Div").show();
    }
    else {
        $("#DangerNoAlertTypeSelected_Label").show();
        $("#DangerNotificationGroupNameVariable_Div").hide();
        $("#DangerPositiveNotificationGroupNameVariable_Div").hide();
        $("#DangerWindowDuration_Div").hide();
        $("#DangerStopTrackingAfter_Div").hide();
        $("#DangerMinimumSampleCount_Div").hide();
        $("#DangerOperator_Div").hide();
        $("#DangerCombination_Div").hide();
        $("#DangerCombinationCount_Div").hide();
        $("#DangerThreshold_Div").hide();
    }
}

$(document).ready(function() {
    var doesExist = document.getElementById('VariableSetListName_Lookup');
    
    if (doesExist !== null) {
        var VariableSetListNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=VariableSetListName&Query=%QUERY'
        });

        VariableSetListNameLookup_Bloodhound.initialize();

       $('#VariableSetListName_Lookup .typeahead').typeahead(
            {
                highlight: false,
                hint: false
            },
            {
                name: 'VariableSetListNameLookup_Bloodhound',
                displayKey: "Value",
                source: VariableSetListNameLookup_Bloodhound.ttAdapter(),
                templates: {
                    suggestion: function (dropdown_display) {
                        return '<p>' + dropdown_display.HtmlValue + '</p>';
                    }
                }
            }
        );
    }
});