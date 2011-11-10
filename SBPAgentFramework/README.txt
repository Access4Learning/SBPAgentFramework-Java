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