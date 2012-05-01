#########################################################################################################
# Copyright 2010-2011 Systemic Pty Ltd
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied.
# See the License for the specific language governing permissions and limitations under the License.
########################################################################################################

#########################################################################################################
# Summary
#########################################################################################################
The SIS Baseline Profile (SBP) Agent Framework is a basic Java Framework intended to help developing 
SIF Agents in an efficient manner that are based on the Australian SIS Baseline Profile as agreed on by 
many vendors and SIF Agent developers. It is an initiative driven by the National Schools Interoperabelity 
Program (NSIP) and industry vendors of SIS and LMS. 
Systemic Pty Ltd has implemented a first version of a SBP Agent Framework that should help SIF Agent developers 
to write SIF Agents in an efficient manner that are compliant with the business rules as layed out by the SBP.  
The intent of this framework is to provide developers with the infrastructure that underlays the SBP. These 
infrastructure functions include but are not limited to:

  a) Start-up Control of Providers/Subscribers of an agent
  b) A Caching Mechanism of dependent SIF Objects and an automatic request of missing dependent objects

For details please refer to the Developer's Guide in the 'UserGuide' directory of this project.


#########################################################################################################
# Download Instructions
#########################################################################################################

How to download this project:

Option 1 - As a Zip.
====================
Click on the button marked "ZIP" available from the Code tab.


Option 2 - Using a Git client.
==============================
From the command-line type: git clone git://github.com/nsip/SBPAgentFramework-Java.git

Note that if you want to use this option but don't have the client installed, it can be 
downloaded from http://git-scm.com/download.

#########################################################################################################
# Versions: Current version is v1.2-beta
#########################################################################################################

Upgrade instructions from v1.0-beta to v1.1-beta
================================================
Option 1: Run DB Update Script
------------------------------
a) Run the <sbp_rootDir>/DB/scripts/upgrade/SBP_v1_0_to_v1_1.sql in your SQLite DB. If you use another DB (i.e MySQL) then 
   you need to change that script to cater for the syntax of your DB.
b) Copy the <sbp_rootDir>/build/dist/sbpframework-au_1.2-v1.1-beta.jar into the lib directory of your project and remove
   sbpframework-au_1.2-v1.0-beta.jar from your project.
c) Change classpath in your startup scripts to point to this new jar file or use the latest version of the startAgent.bat/sh
   from <sbp_rootDir>/scripts.

Option 2: Only valid if you use provides SQLite DB.
---------------------------------------------------
a) If you do not have any cached object in your Dependent Object Cache or if you don't mind losing what is in there then
   you can simply copy the <sbp_rootDir>/DB/SCF.sqliteDB to the location where you have currently stored the same DB within
   your project.
b) Copy the <sbp_rootDir>/build/dist/sbpframework-au_1.2-v1.1-beta.jar into the lib directory of your project and remove
   sbpframework-au_1.2-v1.0-beta.jar from your project.
c) Change classpath in your startup scripts to point to this new jar file or use the latest version of the startAgent.bat/sh
   from <sbp_rootDir>/scripts.
   
Upgrade instructions from v1.1-beta to v1.2-beta
================================================
This version doesn't require any scripts to be run. Simply use the latest 
<sbp_rootDir>/build/dist/sbpframework-au_1.2-v1.2-beta.jar and add it to your project.

*********************************** IMPORTANT ***************************************************************
PLEASE REFER TO THE DEVELOPER'S GUIDE SECTION 3.3 AS IMPORTANT INSTRUCTIONS HAVE BEEN ADDED IN RELATION TO 
DATABASES USED IN A PRODUCTION ENVIRONMENT.
*************************************************************************************************************


   