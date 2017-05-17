Useful when working with many repositories. It helps keep track of the repositories and their status (e.g. up to date, behing origin, out of date). 

It has three components:
- git repository and maven module discovery. Searches for repositories and maven modules in a directory provided by the user
- git operations.
  - checks status of the discovered repositories (if the local copy is up to date) 
  - updates the repositories
- maven operations - currently builds a module (mvn clean install -DskipTests)

Technologies used:
- back-end: Java, Spring Boot, JAX-RS, Jersey, JGit, Maven Invoker, Lombok 
- front-end: HTML, CSS, Javascript, jQuery, Bootstrap
