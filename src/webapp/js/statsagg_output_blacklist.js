$(document).ready(function() {
    var doesExist = document.getElementById('MetricGroupName_Lookup');
    
    if (doesExist !== null) {
        var MetricGroupNameLookup_Bloodhound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.whitespace,
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            remote: 'Lookup?Type=MetricGroupName&Query=%QUERY'
        });

        MetricGroupNameLookup_Bloodhound.initialize();

       $('#MetricGroupName_Lookup .typeahead').typeahead(
            {
                highlight: false,
                hint: false
            },
            {
                name: 'MetricGroupNameLookup_Bloodhound',
                displayKey: "Value",
                source: MetricGroupNameLookup_Bloodhound.ttAdapter(),
                templates: {
                    suggestion: function (dropdown_display) {
                        return '<p>' + dropdown_display.HtmlValue + '</p>';
                    }
                }
            }
        );
    }
});
