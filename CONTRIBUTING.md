# Contributing

First of all: Thank you for even thinking about working on this repository!

Here are some guidelines and general rules to make sure your contribution is merged and released as fast as possible.

Please also note the [Code Of Conduct](CODE_OF_CONDUCT.md).

## Branches

The main branch is the __master__-branch. This branch is meant only for new releases.

Every change in this branches requires a pull-request.

There are two coupled repositories.

## Workflow

Please make sure all tests pass, you have written additional tests where applicaable and you have a passing build for the whole project.

Whether you are fixing a bug or introducing a new feature, please add the necessary tests. It's fine to create a test named "IssuenumberTest", where number is the number of the GitHub issue your test is reproducing.

Detail the changes you are introducing in the pull request description. If you do not have an issue for that, please also elaborate why and what those changes want to achieve. If in doubt, create an issue first and elaborate the reason and the achievements there.

Remember that this is an opensource project with not one dedicated full-time maintainer for the project. It may take some time until your changes are merged and deployed. Be civil and understanding in this regard :)

## Conventions

 * The code-conventions in this repository are enforced using [ktlin](https://github.com/pinterest/ktlint).
 * The reviewer and the assignee of your pull-request should (if possible) be different.
 * Force-Push is under all circumstances prohibited!
 * 100% test coverage is the goal
 
### Bug-fixes

Only change things, that are relevant to your issue!  
If possible, do not make major changes to the design! If your bug-fix requires a redesign, please explain why in detail in the pull request.  
Modify/create the/a test/s, so that the test shows, that the fixed bug is fixed! Unit-Tests are to be preferred, but integration-tests are also great.

### New features

Only change things, that are relevant to your issue!  
Design the new feature beforehand! If you cannot show any Design, your feature-request might likely be declined!  
If possible, do not make major changes to the design! If you have to, please explain in the issue why you are doing it.  
Create a test, that thoroughly tests your new feature! Unit-Tests are to be preferred.

### Tests

Whenever you read "Test", we mean a Test that can be completed automatically. Tests can be done in 2 ways:

1) Unit-Tests.  
   Create your test in <code>src/test/kotlin/de/thorbenkuck/mockk</code> and then in the same package as the class you are testing  
   Name your Test in the following pattern: _"{Class-Name}Test"_  
   Method names should start with the prefix _"test"_ and are preferred as full descriptive sentences using the `` notation   
   You may freely define the size of your unit. Make it as big as possible.  
   Mock external dependencies.
2) Contract-Tests.  
   Create your test in <code>src/test/kotlin/de/thorbenkuck/mockk</code> and then in the same package as the class you are testing  
   Name your Test in the following pattern: _"{Class-Name}Contract"_  
   Contract tests are extremely isolated tests, that define how one method should behave and are used to automate the validation that no convention has been broken by introduced changes.
3) Integration-Tests.  
   Create your test in <code>src/test/kotlin/de/thorbenkuck/mockk/integration</code> and then in whatever package you feel comfortable with  
   Name your Test in the following pattern: _"{Class-Name}IntegrationTest"_  
   The test should not influence any other test (be isolated) and should be standalone (not require any setup environments)
4) Meta-Integration-Tests.    
   Create your test in <code>src/test/kotlin/de/thorbenkuck/mockk/environment</code> and then in whatever package you feel comfortable with  
   Name your Test in the following pattern: _"{Class-Name}MetaIntegrationTest"_  
   These tests are nearly the same as Integration tests, but will test the behaviour in a real world situation by "using it".
---

### Releases

The repository owner reserves the right to provide a new release to maven-central for now. This might change in the future though to be automated.