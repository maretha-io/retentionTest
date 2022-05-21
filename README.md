# retentionTest


To build:

`mvn clean install`


To install on your Nuxeo projects:

`./nuxeoctl mp-install /Users/mariana/nuxeo-workspace/nuxeo-projects/maretha-io/retentionTest/retention-test-package/target/retention-test-package-1.0-SNAPSHOT.zip --nodeps`


This plugin adds the following:

If a new metadata based retention rule for StudentFile docs is defined based on the "ss:RetentionExpirationDate" property:

- automatically puts the document under legal hold (under retention) as soon as a metadata based retention rule is applied
- when the user updates the property "ss:RetentionExpirationDate" on the StudentFile document the retation is updated to so that the "retainUntil" is computed based on the rule and what the value of the "ss:RetentionExpirationDate" is  
