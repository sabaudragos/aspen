var serverUrl = "http://localhost:8090/api/";
var discoveryUrl = serverUrl + "discovery";
var mvnUrl = serverUrl + "mvn";

$(document).ready(function () {
    // initialize all tooltips -- NOT WORKING
    $("[data-toggle=tooltip]").tooltip();

    $("#directory-to-search-submit-button").on("click", function () {
        $.ajax({
            url: discoveryUrl,
            type: "GET",
            data: {
                directoryToSearch: $("#directory-to-search-input").val()
            },
            error: function (result) {
                removeMessageHolder();
                $("#message-holder").append(
                    "<span style=\"color:red\">" +
                    "Error while discovering the directories. Status code: "
                    + result.status + " (" + result.responseJSON.error + ")" +
                    "</span>"
                );
                removeDiscoveredDirectories();
            },
            success: function (result) {
                removeMessageHolder();
                displayRepositories(result.Git_repositories);
                displayMavenModules(result.Maven_modules);
                // trigger git up to date check for repositories
            }
        });
    });


    $(document).on("click", ".mvn-update-button", function () {
        $mvnButton = $(this);
        $.ajax({
            url: mvnUrl,
            type: "POST",
            data: {
                mvnModulePath: $(this).attr("path")
            },

            statusCode: {
                200: function (response) {
                    displayMvnSuccess($mvnButton);
                },
                400: function (response) {
                    displayMvnFailure($mvnButton, response);
                }
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
                "<h3>Git repositories</h3>" +
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
            for (var i = 0; i < gitRepositories.length; i++) {
                $("#git-repositories-list").append(
                    "<tr>" +
                    "<td>" + (i + 1) + "</td>" +
                    "<td>" + gitRepositories[i].name + "</td>" +
                    "<td>" +
                    getGitRepositoryStatus(gitRepositories[i]) +
                    "</td>" +
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
                "<h3>Maven modules</h3>" +
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
            for (var i = 0; i < mvnModules.length; i++) {
                $("#maven-modules-list").append(
                    "<tr>" +
                    "<td>" + (i + 1) + "</td>" +
                    "<td>" + mvnModules[i].name + "</td>" +
                    "<td>" +
                    "<button type=\"button\" class=\"btn btn-primary btn-xs mvn-update-button\" " +
                    "path=\"" + mvnModules[i].path + "\" " +
                    "data-toggle=\"tooltip\" " +
                    "data-placement=\"right\" " +
                    "title=\"Click to build. No tests!\">" +
                    "Build" +
                    "</button>" +
                    "</td>" +
                    "</tr>"
                );
            }
        }

    }

    $("#directory-to-search-input").keyup(function (event) {
        if (event.keyCode === 13) {
            $("#directory-to-search-submit-button").click();
        }
    });

    function removeMessageHolder() {
        if ($("#message-holder") !== null) {
            $("#message-holder").html("");
        }
    }

    function removeDiscoveredDirectories() {
        if ($("#git-repositories-holder") !== null) {
            $("#git-repositories").html("");
        }
        if ($("#maven-modules") !== null) {
            $("#maven-modules").html("");
        }
    }

    function getGitRepositoryStatus(gitRepository) {
        if (gitRepository.name === "keystoneOLD") {
            return "<button type=\"button\" class=\"btn btn-danger btn-xs git-update-button\" " +
                "path=\"" + gitRepository.path + "\" " +
                "data-toggle=\"tooltip\" " +
                "data-placement=\"right\" " +
                "title=\"Click to update!\">" +
                "Out of date" +
                "</button>";
        }

        return "<button type=\"button\" class=\"btn btn-success btn-xs git-update-button\" " +
            "path=\"" + gitRepository.path + "\" " +
            "data-toggle=\"tooltip\" " +
            "data-placement=\"right\" " +
            "title=\"Click to re-check!\">" +
            "Up to date" +
            "</button>";
    }

    function displayMvnSuccess($mvnButton){
        // remove animationand  add build button
        // then add success glyphicon
        $mvnButton.after("<span class=\"glyphicon glyphicon-ok label-success\" aria-hidden=\"true\"></span>");
    }

    function displayMvnFailure($mvnButton, response){
        // remove animation add build button
        // then add failure glyphicon

        removeMessageHolder();
        $("#message-holder").append(
            "<span style=\"color:red\">" +
            "Error while building module. Status code: "
            + response.status + " (" + response.responseJSON.error + ")" +
            "</span>"
        );
        $mvnButton.append("<span class=\"glyphicon glyphicon-remove glyphicon-align-left\" aria-hidden=\"true\"></span>");
    }
});
