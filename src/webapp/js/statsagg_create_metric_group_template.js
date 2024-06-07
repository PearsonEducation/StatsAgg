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