$(document).ready(function() {
    var doesExist = document.getElementById('PagerDutyServiceName_Lookup');
    
    if (doesExist !== null) {
        var PagerDutyServiceNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=PagerDutyServiceName&Query=%QUERY'
        });

        PagerDutyServiceNameLookup_Bloodhound.initialize();

       $('#PagerDutyServiceName_Lookup .typeahead').typeahead(
            {
                highlight: false,
                hint: false
            },
            {
                name: 'PagerDutyServiceNameLookup_Bloodhound',
                displayKey: "Value",
                source: PagerDutyServiceNameLookup_Bloodhound.ttAdapter(),
                templates: {
                    suggestion: function (dropdown_display) {
                        return '<p>' + dropdown_display.HtmlValue + '</p>';
                    }
                }
            }
        );
    }
});
