# The LTH Constituent-to-Dependency Conversion Tool for Penn-style Treebanks #

This repository contains the source code for the conversion tool introduced by
Richard Johansson and Pierre Nugues with some additional minor patches by
Pontus Stenetorp. For further details please see the [tool homepage
][tool_homepage].

[tool_homepage]: http://nlp.cs.lth.se/software/treebank_converter/

## Building ##

As with most Java tools, ant is the build tool of choice. Run:

    ant

Once the command finishes you should have a fully functioning JAR-file in the
repository root directory.

## Citing ##

If you make use of this tool, please cite the following paper in your
publication:

    Richard Johansson and Pierre Nugues. Extended Constituent-to-dependency
    Conversion for English. In Proceedings of NODALIDA 2007. Tartu, Estonia,
    2007.

## License ##

The original tool is made available under the [Three-clause BSD License][bsd]
and the patches by Pontus Stenetorp are available under the BSD-compatible
[ISC License][isc].

[bsd]: http://opensource.org/licenses/BSD-3-Clause
[isc]: http://opensource.org/licenses/ISC
