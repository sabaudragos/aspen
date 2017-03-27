var serverUrl = "http://localhost:8080/api/";
var discoveryUrl = serverUrl + "discovery";

$(document).ready(function () {
    $("#directory-to-search-submit-button").on("click", function () {
        $.ajax({
            url: discoveryUrl,
            type: "get",
            data: {
                directoryToSearch: $("#directory-to-search-input").val()
            },
            error: function (result) {
                $("#git-repositories-list").append("Error while discovering the directories. Status: "
                    + result.status + " Message: " + result.responseJSON.error);
            },
            success: function (result) {
                displayRepositories(result.Git_repositories);
                displayMavenModules(result.Maven_modules);
            }
        });
    });

    function displayRepositories(gitRepositories) {
        var $gitRepositoriesSelector = $("#git-repositories");

        $gitRepositoriesSelector.html(""); //clear the existing repository list

        if (gitRepositories === null) {
            $gitRepositoriesSelector.append("<p>Git repositories</p> No repositories found");
        } else {
            $gitRepositoriesSelector.append(
                "<p>Git repositories</p>" +
                "<table class=\"table table-bordered table-hover\">" +
                "<thead>" +
                "<tr>" +
                "<th>#</th>" +
                "<th>Repository</th>" +
                "<th>Status</th>" +
                "</tr>" +
                "</thead>" +
                "<tbody id=\"git-repositories-list\">" +
                "</tbody>" +
                "</table>");
            for (var i = 0; i < gitRepositories.length; i++){
                $("#git-repositories-list").append(
                    "<tr>" +
                    "<td>" + (i+1) + "</td>" +
                    "<td>" + gitRepositories[i].name + "</td>" +
                    "<td></td>" +
                    "</tr>"
                );
            }
        }
    }

    function displayMavenModules(mvnModules) {
        var $mvnModulesSelector = $("#maven-modules");

        $mvnModulesSelector.html(""); //clear the existing repository list

        if (mvnModules === null) {
            $mvnModulesSelector.append("<p>Maven modules</p> No maven modules found");
        } else {
            $mvnModulesSelector.append(
                "<p>Maven modules</p>" +
                "<table class=\"table table-bordered table-hover\">" +
                    "<thead>" +
                        "<tr>" +
                        "<th>#</th>" +
                        "<th>Maven module</th>" +
                        "<th>Status</th>" +
                        "</tr>" +
                    "</thead>" +
                    "<tbody id=\"maven-modules-list\">" +
                    "</tbody>" +
                "</table>");
            for (var i = 0; i < mvnModules.length; i++){
                $("#maven-modules-list").append(
                    "<tr>" +
                    "<td>" + (i+1) + "</td>" +
                    "<td>" + mvnModules[i].name + "</td>" +
                    "<td></td>" +
                    "</tr>"
                );
            }
        }

    }

    $("#directory-to-search-input").keyup(function(event){
        if(event.keyCode === 13){
            $("#directory-to-search-submit-button").click();
        }
    });
});
