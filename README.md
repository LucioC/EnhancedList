A fork of https://github.com/timroes/EnhancedListView, since the original author no longer supports it.

EnhancedListView
=============================

An Android ListView with enhanced functionality (e.g. Swipe To Dismiss and Undo)

The documentation can be found in the wiki: https://github.com/timroes/EnhancedListView/wiki

Development Updates
------------------

- A complete refactoring was made from the old timroes project to split classes into different files, and extract some common behaviors and interfaces
- So far, the old ListView version still exist and should work the same
- A first version seems to be working fine

Development TODO list
------------------

- Review RecyclerView use. Is it being used correctly? Using it's full potential?
- Update release process (how should this be released from now?)
- Add automated tests

Update Notice (From Forked project)
-------------

### v0.3.0

* All resources (layouts, colors, etc.) got an `elv_` prefix. So if you have changed 
  some of these in your own app, you must make sure to also add the `elv_` prefix to your
  resources (e.g. to change or internationalize the "Undo" string, you will need to have a
  string resource `elv_undo` instead of `undo`).
