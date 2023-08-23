## Replication package for JRRT in Evaluation

# Table of Contents

- [Table of Contents](#table-of-contents)
- [General Introduction](#general-introduction)
- [Requirements](#requirements)
- [How to Replicate JRRT?](#how-to-replicate-jrrt)
  - [1. Clone replication package to your local file system](#1-clone-replication-package-to-your-local-file-system)
  - [2. Get Subject Applications](#2-get-subject-applications)
  - [3. Move and Overwrite files](#3-move-and-overwrite-files)
  - [4. Configure Path](#4-configure-path)
  - [5. Reproduce JRRT](#5-reproduce-jrrt)
  - [6. Outputs of JRRT](#6-outputs-of-jrrt)


# General Introduction

This is the replication package for JRRT in evaluation.


# Requirements
 - Java6
 - Mac OS or Linux
 - Junit4
 - [JRRT](https://code.google.com/archive/p/jrrt)
 - [Defects4J](https://github.com/rjust/defects4j)
# How to Replicate JRRT?

## 1. Clone replication package to your local file system 

`git clone https://github.com/Anonymous3202/ValExtractor.git`

## 2. Get Subject Applications

`./Implementation/shell/evaluation.sh`

Hint: considering the complexity of defects4j configuration, we provide the zip file of the example project under `evaluation/projects/`.

## 3. Move and Overwrite files

Move and overwrite all files/folders under `Implementation/jrrt` to the directory where JRRT is located in local file system.
  
## 4. Configure Path

Open `Constrants.java`(Locate in `exp.utils` package) and modify `Project_Path`( **Path2Implementation/ValExtractor**),  `EXP2_ROOT`(the root path of the generated result in Evaluation part) 
```java
//----------- need to configure ----------- 
public static final String Project_Path="***/ValExtractor/Implementation/ValExtractor/"; 
public static final String EXP2_ROOT = "***/ValExtractor/Evaluation/"; 
//----------------------------------------- 
```	 
Note: please make sure all the paths end with `/`


## 5. Reproduce JRRT
Run following Commands in Terminal:

`cd PATH2JRRT` 

`java -classpath .:lib/junit.jar:lib/cream106.jar:lib/fastjson.jar:lib/commons-io.jar:lib/org.eclipse.jgit.jar  -Xmx2G junit.awtui.TestRunner tests.eclipse.ExtractTemp.ExtractTempTests`

## 6. Outputs of JRRT 
JRRT generates refactored java file in ``Evaluation/jrrt/**/JRRT`` folders in all tested projects. For example, for project `Lang`, it generated all the refactoring results in `Evaluation/jrrt/Lang/JRRT` folder. 
