var serverUrl = "http://localhost:8080/api/";
var discoveryUrl = serverUrl + "discovery";

$(document).ready(function () {
    $("#directory-to-search-submit-button").on("click", function () {
        $.ajax({
            url: discoveryUrl,
            type: "get",
            data: {
                directoryToSearch: "/home/dragos/javadev"
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
        var gitRepositoriesSelector = $("#git-repositories-list");
        if (gitRepositories === null) {
            gitRepositoriesSelector.append("No repositories found");
        } else {
            for (i = 0; i < gitRepositories.length; i++){
                gitRepositoriesSelector.append(
                    "<div class=\"col-sm-12\">"
                    + "<div>" + gitRepositories[i].name + "</div>" +
                    + "<div>" + gitRepositories[i].path + "</div>" +
                    "</div>");

                // gitRepositoriesSelector.append(gitRepositories[i].name + "<br>");
            }

        }
    }

    function displayMavenModules(mvnModules) {
        var mvnModulesSelector = $("#maven-modules-list");
        if (mvnModules === null) {
            mvnModulesSelector.append("No maven modules found");
        } else {
            for (i = 0; i < mvnModules.length; i++){
                mvnModulesSelector.append(mvnModules[i].name + "<br>");
            }
        }

    }
});
