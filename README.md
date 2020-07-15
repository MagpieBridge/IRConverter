<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-3-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
# IRConverter [![Build Status](https://travis-ci.com/MagpieBridge/IRConverter.svg?branch=master)](https://travis-ci.com/MagpieBridge/IRConverter)
This IRConverter converts [WALA](https://github.com/wala/WALA) intermediate representation (IR) to [Soot](https://github.com/Sable/soot) IR. This IRConverter enables you to analyze Java source code with Soot.  

## Why is this IRConverter needed? 
Soot comprises a solid Java bytecode front end. However, the bytecode only has the line numbers. This is not sufficient to support features such as hover, fix and codeLens in a code editor. For those features, position information about variable, expressions, calls and parameters are required, which are lost in bytecode. 

## How does this IRConverter work?
This IRConverter takes WALA’s Java source-code front end to generate WALA IR (non-SSA) and convert it to Soot IR. Soot has multiple IRs, the most commonly used IR is called Jimple --- the IR considered by this IRConverter.

The following figure shows the IR statements considered by this IRConverter. 
<img src="https://github.com/MagpieBridge/IRConverter/blob/master/doc/conversion.PNG"  width="500">

This IRConverter was developed for our paper: [MagpieBridge: A General Approach to Integrating Static Analyses into IDEs and Editors](https://drops.dagstuhl.de/opus/volltexte/2019/10813/pdf/LIPIcs-ECOOP-2019-21.pdf) (ECOOP 2019). The parent project MagpieBridge can be found [here](https://github.com/MagpieBridge/MagpieBridge). 

## Limitations
Since this IRConverter uses WALA Java source code frond end, the limitations of WALA are inherited. See discussions [here](https://github.com/MagpieBridge/IRConverter/issues/1). 

## Cite the research paper
For scientific usage, please **cite the paper** [[BibTex](https://drops.dagstuhl.de/opus/volltexte/2019/10813/)].

## How to use IRConverter?
You can either 
1. IRConverter is published on the GitHub Package Registry. You can use the release by adding the following lines to your `pom.xml`  (see all [github package](https://github.com/MagpieBridge/IRConverter/packages/96202)). You can follow [these instructions](https://github.com/MagpieBridge/MagpieBridge/wiki/Tutorial-3.-How-To-Install-a-GitHub-Maven-Package).  
````
<dependencies>
  <dependency>
    <groupId>magpiebridge</groupId>
    <artifactId>irconverter</artifactId>
    <version>0.1.6</version>
  </dependency>
</dependencies>

<repositories>
  <repository>
    <id>github</id>
    <name>GitHub MagpieBridge IRConverter Apache Maven Packages</name>
    <url>https://maven.pkg.github.com/MagpieBridge/IRConverter</url>
  </repository>
</repositories>
````

2. or build IRConverter by yourself 
    -  check out the master branch with `git clone https://github.com/MagpieBridge/IRConverter.git`
    -  run `mvn install` in the project root directory to build the tool and run all tests. To skip tests, run `mvn install -DskipTests`.

## Get Involved
- Pull requests are welcome!
- Submit github issues for any feature enhancements, bugs or documentation problems
- Please format the code with `mvn com.coveo:fmt-maven-plugin:format` before `git push`
 
## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://linghuiluo.github.io/"><img src="https://avatars3.githubusercontent.com/u/18470909?v=4" width="100px;" alt=""/><br /><sub><b>Linghui Luo</b></sub></a><br /><a href="https://github.com/MagpieBridge/IRConverter/commits?author=linghuiluo" title="Code">💻</a> <a href="#design-linghuiluo" title="Design">🎨</a> <a href="https://github.com/MagpieBridge/IRConverter/commits?author=linghuiluo" title="Documentation">📖</a></td>
    <td align="center"><a href="http://joaocpereira.me"><img src="https://avatars0.githubusercontent.com/u/6281876?v=4" width="100px;" alt=""/><br /><sub><b>João Pereira</b></sub></a><br /><a href="https://github.com/MagpieBridge/IRConverter/commits?author=jcp19" title="Code">💻</a> <a href="#ideas-jcp19" title="Ideas, Planning, & Feedback">🤔</a></td>
    <td align="center"><a href="https://github.com/swissiety"><img src="https://avatars0.githubusercontent.com/u/5645864?v=4" width="100px;" alt=""/><br /><sub><b>Markus Schmidt</b></sub></a><br /><a href="https://github.com/MagpieBridge/IRConverter/commits?author=swissiety" title="Tests">⚠️</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## Contact 
&#x2709; linghui[at]outlook.de
