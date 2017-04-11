var serverUrl = "http://localhost:8090/api/";
var discoveryUrl = serverUrl + "discovery";
var mvnUrl = serverUrl + "mvn";
var gitUrl = serverUrl + "git";

$(document).ready(function () {

    var MAVEN_BUILD_SUCCESS = "Maven build success";
    var MAVEN_BUILD_FAIL = "Maven build fail";
    var MAVEN_INVOKER_FAILURE = "Maven invoker failed";
    var INVALID_MVN_MODULE_PATH = "Invalid maven module path";
    var UNSUPPORTED_OPERATING_SYSTEM = "Unsupported operating system";
    var MAVEN_PATH_NOT_FOUND_IN_PATH_VARIABLE = "Maven not found in path variable";
    var GIT_PULL_FAILED = "Git pull failed";
    var GIT_PULL_SUCCESS = "Git pull executed successfully";
    var GIT_NO_REMOTE_TRACKING_OF_BRANCH = "Returned null, likely no remote tracking of branch";
    var GIT_REPOSITORY_IS_UP_TO_DATE = "Git repository is up to date with origin";
    var GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN = "Git repository is ahead origin";
    var GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN = "Git repository is behind origin";
    var ERROR_WHILE_STASHING_CHANGES = "Error while stashing the changes";
    var ERROR_WHILE_CHECKING_BRANCH_STATUS = "Error while checking the status";
    var NO_MAVEN_MODULES_AND_NO_GIT_REPOSITORIES_FOUND = "No maven modules and no git repositories found";

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

            statusCode: {
                200: function (result) {
                    if ((result.Git_repositories.length===0) || (result.Maven_modules.length===0)) {
                        removeExistingMessage();
                        displayErrorMessage(NO_MAVEN_MODULES_AND_NO_GIT_REPOSITORIES_FOUND);
                        removeDiscoveredDirectories();
                    } else {
                        removeExistingMessage();
                        displayRepositories(result.Git_repositories);
                        displayMavenModules(result.Maven_modules);
                        checkIfRepositoriesAreUpToDate(result.Git_repositories);
                        // trigger git up to date check for repositories
                    }
                },
                202: function (result) {
                    removeExistingMessage();
                    displayErrorMessage(result.responseText);
                    removeDiscoveredDirectories();
                },
                400: function (result) {
                    removeExistingMessage();
                    displayErrorMessage(result.responseText);
                    removeDiscoveredDirectories();
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
                        getDefaultGitRepositoryStatus(gitRepositories[i]) +
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
                    "data-loading-text=\"<i class='fa fa-spinner fa-spin '></i>Building...\""+
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
        switch (gitRepository.status) {
            case GIT_REPOSITORY_IS_AHEAD_OF_ORIGIN:
                //business logic - Up to date - ahead origin
                return "<button type=\"button\" class=\"btn btn-success btn-xs git-update-button\" " +
                    "name=\"" + gitRepository.name + "\" " +
                    "path=\"" + gitRepository.path + "\" " +
                    "data-toggle=\"tooltip\" " +
                    "data-placement=\"right\" " +
                    "data-loading-text=\"<i class='fa fa-spinner fa-spin '></i>Checking...\""+
                    "title=\"Click to re-check!\">" +
                    "Up to date" +
                    "</button>" + " - ahead origin";
            case GIT_REPOSITORY_IS_UP_TO_DATE:
                // up to date
                return "<button type=\"button\" class=\"btn btn-success btn-xs git-update-button\" " +
                    "name=\"" + gitRepository.name + "\" " +
                    "path=\"" + gitRepository.path + "\" " +
                    "data-toggle=\"tooltip\" " +
                    "data-placement=\"right\" " +
                    "data-loading-text=\"<i class='fa fa-spinner fa-spin '></i>Checking...\""+
                    "title=\"Click to re-check!\">" +
                    "Up to date" +
                    "</button>";
            case GIT_REPOSITORY_IS_BEHIND_OF_ORIGIN:
                // business logic - out of date
                return "<button type=\"button\" class=\"btn btn-danger btn-xs git-update-button\" " +
                    "id=\"git-repository-" + gitRepository.name + "\" " +
                    "name=\"" + gitRepository.name + "\" " +
                    "path=\"" + gitRepository.path + "\" " +
                    "data-toggle=\"tooltip\" " +
                    "data-placement=\"right\" " +
                    "data-loading-text=\"<i class='fa fa-spinner fa-spin '></i>Checking...\"" +
                    "title=\"Click to update!\">" +
                    "Out of date" +
                    "</button>" + " - behind origin";
            case GIT_NO_REMOTE_TRACKING_OF_BRANCH:
                // business logic - out of date
                return "<button type=\"button\" class=\"btn btn-danger btn-xs git-update-button\" " +
                    "id=\"git-repository-" + gitRepository.name + "\" " +
                    "name=\"" + gitRepository.name + "\" " +
                    "path=\"" + gitRepository.path + "\" " +
                    "data-toggle=\"tooltip\" " +
                    "data-placement=\"right\" " +
                    "data-loading-text=\"<i class='fa fa-spinner fa-spin '></i>Checking...\"" +
                    "title=\"Click to re-check!\">" +
                    "Unkown" +
                    "</button>" + " - likely no remote tracking branch";
        }
    }

    function getDefaultGitRepositoryStatus(gitRepository) {
        return "<img src=\"./img/ajax-loader-grey.gif\"" +
            "id=\"git-repository-img-" + gitRepository.name + "\" " +
            "name=\"" + gitRepository.name + "\" " +
            "path=\"" + gitRepository.path + "\" " +
            "data-toggle=\"tooltip\" " +
            "data-placement=\"right\" " +
            "title=\"Pending status check!\">";
    }

    function checkIfRepositoriesAreUpToDate(gitRepositories) {
        for (var i = 0; i < gitRepositories.length; i++) {
            $("#git-repository-img-" + gitRepositories[i].name).attr("src", "./img/ajax-loader-red.gif");

            $.ajax({
                url: gitUrl,
                type: "GET",
                data: {
                    repositoryPath: gitRepositories[i].path
                },

                statusCode: {
                    200: function (result) {
                        var $gitRepositorySelector = $("#git-repository-img-" + result.name)
                        $gitRepositorySelector.before(getGitRepositoryStatus(result));
                        $gitRepositorySelector.remove();
                    },
                    202: function (result) {
                        var $gitRepositorySelector = $("#git-repository-img-" + result.name)
                        $gitRepositorySelector.before(getGitRepositoryStatus(result));
                        $gitRepositorySelector.remove();

                    },
                    400: function (result) {
                        //TODO

                    }
                }
            });
        }
    }


    /* ------------------- Maven related code ------------------- */
    $(document).on("click", ".mvn-update-button", function () {
        var $mvnButton = $(this);
        $mvnButton.button('loading');

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
                202: function (response) {
                    var responseMessage = response.responseText;
                    if ((responseMessage === MAVEN_PATH_NOT_FOUND_IN_PATH_VARIABLE) ||
                        (responseMessage === UNSUPPORTED_OPERATING_SYSTEM)) {
                        removeExistingMessage();
                        displayErrorMessage(displayErrorMessage(response.responseText + ". Module name: " + $mvnButton.parent().prev().text()));
                    }

                    if (responseMessage === MAVEN_INVOKER_FAILURE) {
                        //TODO Error message for MAVEN_INVOKER_FAILURE should be displayed per module
                    }
                },
                400: function (response) {
                    //TODO needs refactoring
                    removeExistingMessage();
                    displayErrorMessage(response.responseText + ". Module name: " + $mvnButton.parent().prev().text());
                }
            }
        });
        $mvnButton.button('reset');
    });

    function displayMvnBuildStatus(response) {
        var buildStatus = response.status;
        var $mavenModuleSelector = $("#maven-module-" + response.name);

        if ($mavenModuleSelector.next('span').length > 0) {
            $mavenModuleSelector.next('span').remove();
        }

        if (buildStatus === MAVEN_BUILD_SUCCESS) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-ok glyph-success\" aria-hidden=\"true\"></span>");
        } else if (buildStatus === MAVEN_BUILD_FAIL) {
            $mavenModuleSelector.after("<span class=\"glyphicon glyphicon-remove glyph-failure\" aria-hidden=\"true\"></span>");
        }
    }

    /* ------------------- Git related code ------------------- */
    $(document).on("click", ".git-update-button", function () {
        var $gitButton = $(this);

        $gitButton.button('loading');

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
                202: function (response) {
                    displayGitPullStatus(response);
                    //AspenRestException(RestConstants.GIT_PULL_FAILED, Response.Status.ACCEPTED); - displayed per git repo
                    //AspenRestException(RestConstants.ERROR_BUILDING_GIT_INSTANCE, Response.Status.ACCEPTED) - displayed per git repo and page
                },
                400: function (response) {
                    //TODO needs refactoring
                    removeExistingMessage();
                    displayErrorMessage(response.responseText + ". Module name: " + $gitButton.parent().prev().text());
                }
            }
        });

        $gitButton.button('reset');
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
