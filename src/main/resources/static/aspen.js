var serverUrl = "http://localhost:8090/api/";
var discoveryUrl = serverUrl + "discovery";
var mvnUrl = serverUrl + "mvn";
var gitUrl = serverUrl + "git";

$(document).ready(function () {

    var MAVEN_SUCCESS = "Maven executed successfully";
    var MAVEN_FAILURE = "Maven execution failed";
    var GIT_PULL_FAILED = "Git pull failed";
    var GIT_PULL_SUCCESS = "Git pull executed successfully";
    // initialize all tooltips -- NOT WORKING
    $("[data-toggle=tooltip]").tooltip();


    /* ------------------- Directory discovery related code ------------------- */

    $("#directory-to-search-submit-button").on("click", function () {
        $.ajax({
            url: discoveryUrl,
            type: "GET",
            data: {
                directoryToSearch: $("#directory-to-search-input").val()
            },

            success: function (result) {
                removeExistingMessage();
                displayRepositories(result.Git_repositories);
                displayMavenModules(result.Maven_modules);
                // trigger git up to date check for repositories?
            },
            error: function (result) {
                removeExistingMessage();
                displayErrorMessage("Error while discovering the directories. Message: " + result.responseText);
                removeDiscoveredDirectories();
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
                    "id=\"maven-module-" + mvnModules[i].name + "\" " +
                    "name=\"" + mvnModules[i].name + "\" " +
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
                "id=\"git-repository-" + gitRepository.name + "\" " +
                "name=\"" + gitRepository.name + "\" " +
                "path=\"" + gitRepository.path + "\" " +
                "data-toggle=\"tooltip\" " +
                "data-placement=\"right\" " +
                "title=\"Click to update!\">" +
                "Out of date" +
                "</button>";
        }

        return "<button type=\"button\" class=\"btn btn-success btn-xs git-update-button\" " +
            "name=\"" + gitRepository.name + "\" " +
            "path=\"" + gitRepository.path + "\" " +
            "data-toggle=\"tooltip\" " +
            "data-placement=\"right\" " +
            "title=\"Click to re-check!\">" +
            "Up to date" +
            "</button>";
    }


    /* ------------------- Maven related code ------------------- */
    $(document).on("click", ".mvn-update-button", function () {
        $mvnButton = $(this);

        // remove existing success/failure glyph
        if ($mvnButton.next('span').length > 0) {
            $mvnButton.next('span').remove();
        }

        $.ajax({
            url: mvnUrl,
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify({
                "path": $mvnButton.attr("path"),
                "name": $mvnButton.attr("name"),
                "status": ""
            }),

            statusCode: {
                200: function (response) {
                    displayMvnBuildStatus(response);
                },
                400: function (response) {
                    //TODO needs refactoring
                    removeExistingMessage();
                    displayErrorMessage(response.responseText + ". Module name: " + $mvnButton.parent().prev().text());
                }
            }
        });
    });

    function displayMvnBuildStatus(response) {
        var buildStatus = response.status;
        var $mavenModuleSelector = $("#maven-module-" + response.name);

        if ($mavenModuleSelector.next('span').length > 0) {
            $mavenModuleSelector.next('span').remove();
        }

        if (buildStatus === MAVEN_SUCCESS) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-ok glyph-success\" aria-hidden=\"true\"></span>");
        } else if (buildStatus === MAVEN_FAILURE) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-remove glyph-failure\" aria-hidden=\"true\"></span>");
        }
    }

    /* ------------------- Git related code ------------------- */
    $(document).on("click", ".git-update-button", function () {
        $gitButton = $(this);

        $.ajax({
            url: gitUrl,
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify({
                "path": $gitButton.attr("path"),
                "name": $gitButton.attr("name"),
                "status": ""
            }),

            statusCode: {
                200: function (response) {
                    displayGitPullStatus(response);
                },
                400: function (response) {
                    //TODO needs refactoring
                    removeExistingMessage();
                    displayErrorMessage(response.responseText + ". Module name: " + $gitButton.parent().prev().text());
                }
            }
        });
    });

    function displayGitPullStatus(response) {
        var pullStatus = response.status;
        var $mavenModuleSelector = $("#git-repository-" + response.name);

        if ($mavenModuleSelector.next('span').length > 0) {
            $mavenModuleSelector.next('span').remove();
        }

        if (pullStatus === GIT_PULL_SUCCESS) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-ok glyph-success\" aria-hidden=\"true\"></span>");
        } else if (pullStatus === GIT_PULL_FAILED) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-remove glyph-failure\" aria-hidden=\"true\"></span>");
        }
    }


    /* ------------------- Common code ------------------- */
    function removeExistingMessage() {
        if ($("#message-holder") !== null) {
            $("#message-holder").html("");
        }
    }

    function displayErrorMessage(message) {
        $("#message-holder").append(
            "<span class=\"error-message\">" + message +
            "</span>"
        );
    }
});
