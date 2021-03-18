[![][ButlerImage]][homepage] 

# About

This is the source repository for the **Filesystem List Parameter** plugin for Jenkins.
This plugin provides a build parameter with a value from objects names in filesystem like folders, symlinks or files.

For more information see the [homepage].


# Change Log

##### Version 0.0.7 (March 18, 2021)

-   Merge PullRequest:
    [Added multi select option](https://github.com/jenkinsci/filesystem-list-parameter-plugin/pull/5)

##### Version 0.0.6 (February 16, 2020)

-   Small bugfix - Rebuild issue
    [JENKINS-60497](https://issues.jenkins-ci.org/browse/JENKINS-60497)

##### Version 0.0.5 (March 23, 2019)

-   Add Slave support.  

##### Version 0.0.4 (February 23, 2018)

-   Compatibility changes [JENKINS-49649](https://issues.jenkins-ci.org/browse/JENKINS-49649) -
    Getting issue details... STATUS

##### Version 0.0.3 (September 5, 2014)

-   Small bugfix
    [JENKINS-24586](https://issues.jenkins-ci.org/browse/JENKINS-24586)

##### Version 0.0.2 (June 1, 2014)

-   Add regular expression exclude and include filter for filesystem objects  
    -   **Include** and **exclude pattern** - regular expression to
        filter filesystem objects
        -   Regular expression will be checked with
            "java.util.regex.Pattern.compile(regex)"

##### Version 0.0.1 (March 31, 2014)

-   Initial release

The filesystem-list-parameter-plugin lists file system object names of a
directory. One of the object names can be selected as build parameter.
In addition the objects can be filtered: ALL, DIRECTORY, FILE, SYMLINK.
The order of the list can be reversed.

-   **Name** and **Description** should be clear
-   **Path** to the directory to select filesystem objects
-   **Filesystem object type** - filter for type of the file system
    objects that can be selected.
-   **Sort by last modified date** - If true, the list of the parameter
    values will be sorted by last modified file attribute. Default order
    is alphabetic sort by parameter value.
-   **Sort in reverse order** - If true, the list of the parameter
    values will be sorted in reverse order.


# Purpose

The plugin was designed to support the manual choice of delivery or deployment artefacts. E.g. deploy a defined version of a build package to UAT.. 
 

# Raise Issues

You are welcome if you find any bug or you want to request a change. Please take a look for:
[Report an issue](https://wiki.jenkins.io/display/JENKINS/How+to+report+an+issue).

When creating a ticket in the [Jenkins JIRA](https://issues.jenkins-ci.org/)
system, select the component `filesystem-list-parameter-plugin`.

# Source
The source code can be found on
[GitHub](https://github.com/jenkinsci/filesystem-list-parameter-plugin). Fork us!

# Contributing

Contributions are welcome! Check out the
[open tickets](https://issues.jenkins-ci.org/browse/JENKINS-56125?jql=project%20%3D%20JENKINS%20AND%20status%20in%20(Open%2C%20Reopened)%20AND%20component%20%3D%20filesystem-list-parameter-plugin)
for this plugin in JIRA.


[ButlerImage]: https://jenkins.io/sites/default/files/jenkins_logo.png
[homepage]: https://plugins.jenkins.io/filesystem-list-parameter-plugin

